import re
import json
import csv
from datetime import datetime
import os
import glob
from pathlib import Path

# ------------------------------------------------------------------
# Default paths
# ------------------------------------------------------------------

DEFAULT_INPUT_DIR = "./metrics/logfiles"
DEFAULT_OUTPUT_DIR = "./metrics/reports"


# ------------------------------------------------------------------
# Resolve input path
# ------------------------------------------------------------------

user_input = input(
    f"Enter input log file/folder "
    f"(Press Enter for default search: {DEFAULT_INPUT_DIR}): "
).strip()


def discover_log_files(path_input=None):
    """
    Discover all supported log files.

    Supports:
      - *isd.log (MAP)
      - *tim.log (TIM)

    Parameters
    ----------
    path_input : str or None
        Directory, file, or None.

    Returns
    -------
    list[pathlib.Path]
        Sorted list of log files.
    """

    if not path_input:
        search_root = Path(DEFAULT_INPUT_DIR)

    else:
        search_root = Path(path_input)

    if search_root.is_file():

        matches = [search_root]

    elif search_root.is_dir():

        matches = (
            list(search_root.rglob("*isd.log")) +
            list(search_root.rglob("*tim.log"))
        )

    else:
        raise FileNotFoundError(
            f"Invalid input path: {path_input}"
        )

    if not matches:
        raise FileNotFoundError(
            f"No supported log files found under '{search_root}'."
        )

    return sorted(matches)


INPUT_LOG_FILES = discover_log_files(user_input)


map_logs = [f for f in INPUT_LOG_FILES if f.name.endswith("isd.log")]
tim_logs = [f for f in INPUT_LOG_FILES if f.name.endswith("tim.log")]

print("Discovered log files")
print("--------------------")
print(f"MAP log files : {len(map_logs)}")
print(f"TIM log files : {len(tim_logs)}")
print(f"Total          : {len(INPUT_LOG_FILES)}")

# ------------------------------------------------------------------
# Resolve output directory
# ------------------------------------------------------------------

user_output_dir = input(
    f"Enter output folder "
    f"(Press Enter for default: {DEFAULT_OUTPUT_DIR}): "
).strip()

OUTPUT_DIR = (
    user_output_dir
    if user_output_dir
    else DEFAULT_OUTPUT_DIR
)

# Create it if it doesn't exist
os.makedirs(OUTPUT_DIR, exist_ok=True)

MAP_OUTPUT_CSV = os.path.join(
    OUTPUT_DIR,
    "map_messages.csv"
)

TIM_OUTPUT_CSV = os.path.join(
    OUTPUT_DIR,
    "tim_messages.csv"
)

# ------------------------------------------------------------------
# Duplicate handling
# ------------------------------------------------------------------

allow_duplicates_input = input(
    "Allow duplicate timestamps? (y/N): "
).strip().lower()

ALLOW_DUPLICATES = (
    allow_duplicates_input == "y"
)

def extract_json_messages(file_path):
    """
    Reads a log file and yields complete JSON messages.

    Supports both

        User Input: {...}

    and

        Building TIM/ADV with input data : {
            ...
        }

    Returns:
        (timestamp, json_object)
    """

    timestamp = None

    collecting = False
    brace_depth = 0
    json_lines = []

    with open(file_path, "r", encoding="utf-8") as f:

        for line in f:

            #
            # Capture timestamp whenever one exists
            #

            ts_match = re.match(
                            r"^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2})",
                            line,
                        )

            if ts_match:
                timestamp = ts_match.group(1)

            #
            # Single-line MAP JSON
            #

            if "User Input:" in line:

                json_text = line.split("User Input:", 1)[1].strip()

                try:
                    yield timestamp, json.loads(json_text)
                except Exception as ex:
                    print(f"Failed to parse MAP JSON: {ex}")

                continue

            #
            # Beginning of TIM JSON
            #

            TIM_MARKER = "Building TIM/ADV with input data"

            if TIM_MARKER in line:

                collecting = True
                json_lines = []

                brace_start = line.find("{")

                if brace_start == -1:
                    continue

                json_text = line[brace_start:]

                json_lines.append(json_text)

                brace_depth = 1

                continue

            #
            # Continue collecting TIM JSON
            #

            if collecting:

                json_lines.append(line)

                brace_depth += line.count("{")
                brace_depth -= line.count("}")

                #
                # Finished JSON
                #

                if brace_depth == 0:

                    collecting = False

                    json_text = "".join(json_lines)

                    try:

                        yield timestamp, json.loads(json_text)

                    except Exception as ex:

                        print("Failed parsing TIM JSON")
                        print(ex)


def safe_get(dct, path, default=None):
    """
    Safely extract nested dictionary values.
    path: list of keys
    """
    for key in path:
        if isinstance(dct, dict) and key in dct:
            dct = dct[key]
        else:
            return default
    return dct


def count_map_fields(map_data):
    """
    Compute derived metrics:
    - numApproaches
    - numLanes
    - numConnections
    - numNodes
    """
    geometry = safe_get(map_data, ["intersectionGeometry"], {})

    lane_list = safe_get(geometry, ["laneList", "approach"], [])

    num_approaches = len(lane_list)

    num_lanes = 0
    num_connections = 0
    num_nodes = 0

    for ap in lane_list:
        lanes = ap.get("drivingLanes", []) or ap.get("crosswalkLanes", [])
        num_lanes += len(lanes)

        for lane in lanes:
            connections = lane.get("connections", [])
            num_connections += len(connections)

            nodes = lane.get("laneNodes", [])
            num_nodes += len(nodes)

    return num_approaches, num_lanes, num_connections, num_nodes


def process_log_files():

    map_rows = []
    tim_rows = []

    seen_map_records = set()
    seen_tim_records = set()

    map_found_count = 0
    tim_found_count = 0

    processed_map_count = 0
    processed_tim_count = 0

    map_duplicate_count = 0
    tim_duplicate_count = 0

    tim_skipped_count = 0

    for log_file in INPUT_LOG_FILES:

        print(f"\nProcessing: {log_file}")

        # with open(log_file, "r", encoding="utf-8") as f:

        #     for line in f:

        # # # --------------------------------------------------
        # # # Extract timestamp
        # # # --------------------------------------------------

        # timestamp = None

        # ts_match = re.match(
        #     r"^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2})",
        #     line,
        # )

        # if ts_match:
        #     timestamp = ts_match.group(1)

        # --------------------------------------------------
        # Extract JSON
        # --------------------------------------------------

        for timestamp, data in extract_json_messages(log_file):

            try:

                if not data:
                    continue
                message_type = (
                    data.get("messageType", "")
                        .upper()
                )

                if "MAP" in message_type:

                    map_found_count += 1

                    map_data = data.get("mapData", {})
                    geometry = safe_get(
                        map_data,
                        ["intersectionGeometry"],
                        {}
                    )

                    ref = geometry.get(
                        "referencePoint",
                        {}
                    )

                    verified = geometry.get(
                        "verifiedPoint",
                        {}
                    )

                    # --------------------------------------------------
                    # Derived metrics
                    # --------------------------------------------------

                    (
                        num_approaches,
                        num_lanes,
                        num_connections,
                        num_nodes
                    ) = count_map_fields(map_data)

                    # --------------------------------------------------
                    # Duplicate key
                    # --------------------------------------------------

                    duplicate_key = (
                        timestamp
                    )

                    if (
                        not ALLOW_DUPLICATES
                        and duplicate_key in seen_map_records
                    ):
                        map_duplicate_count += 1
                        continue

                    seen_map_records.add(duplicate_key)

                    # --------------------------------------------------
                    # Build row
                    # --------------------------------------------------

                    row = {

                        "sourceLogFile": log_file,

                        "timestamp": timestamp,

                        "minuteOfTheYear":
                            map_data.get("minuteOfTheYear"),

                        "layerType":
                            map_data.get("layerType"),

                        "descriptiveIntersctionName":
                            ref.get("descriptiveIntersctionName"),

                        "layerID":
                            ref.get("layerID"),

                        "intersectionID":
                            ref.get("intersectionID"),

                        "regionID":
                            ref.get("regionID"),

                        "msgCount":
                            ref.get("msgCount"),

                        "masterLaneWidth":
                            ref.get("masterLaneWidth"),

                        "referenceLat":
                            ref.get("referenceLat"),

                        "referenceLon":
                            ref.get("referenceLon"),

                        "referenceElevation":
                            ref.get("referenceElevation"),

                        "verifiedMapLat":
                            verified.get("verifiedMapLat"),

                        "verifiedMapLon":
                            verified.get("verifiedMapLon"),

                        "verifiedMapElevation":
                            verified.get("verifiedMapElevation"),

                        "verifiedSurveyedLat":
                            verified.get("verifiedSurveyedLat"),

                        "verifiedSurveyedLon":
                            verified.get("verifiedSurveyedLon"),

                        "verifiedSurveyedElevation":
                            verified.get("verifiedSurveyedElevation"),

                        "numApproaches":
                            num_approaches,

                        "numLanes":
                            num_lanes,

                        "numConnections":
                            num_connections,

                        "numNodes":
                            num_nodes,

                        "enableElevation":
                            data.get("enableElevation"),

                        "messageType":
                            data.get("messageType"),
                    }

                    map_rows.append(row)

                    processed_map_count += 1


                elif "TIM" in message_type:

                    tim_found_count += 1

                    missing = []

                    if data.get("anchorPoint") is None:
                        missing.append("anchorPoint")

                    if data.get("verifiedPoint") is None:
                        missing.append("verifiedPoint")

                    if not data.get("regions"):
                        missing.append("regions")

                    if missing:
                        print(
                            f"Warning: Skipping incomplete TIM message "
                            f"in {log_file} at {timestamp}. "
                            f"Missing: {', '.join(missing)}"
                        )
                        tim_skipped_count += 1
                        continue

                    verified = data.get("verifiedPoint", {})

                    #
                    # Duplicate detection
                    #

                    anchor = data.get("anchorPoint") or {}

                    can_check_duplicates = all([
                        timestamp
                    ])

                    if can_check_duplicates:

                        duplicate_key = (
                            timestamp
                        )

                        if (
                            not ALLOW_DUPLICATES
                            and duplicate_key in seen_tim_records
                        ):
                            tim_duplicate_count += 1
                            continue

                        seen_tim_records.add(duplicate_key)

                    else:
                        print(
                            f"Warning: Incomplete duplicate key at {timestamp}. "
                            "Keeping the message."
                        )
                    #
                    # Collect all lane nodes
                    #

                    all_points = []

                    for region in data.get("regions", []):

                        for node in region.get("laneNodes", []):

                            all_points.append({
                                "nodeNumber": node.get("nodeNumber"),
                                "lat": node.get("nodeLat"),
                                "lon": node.get("nodeLong"),
                                "elevation": node.get("nodeElevation"),
                                "laneWidth": node.get("laneWidth")
                            })

                    #
                    # Collect ITIS
                    #

                    itis_codes = []
                    itis_text = []

                    for item in anchor.get("content", []):

                        itis_codes.extend(item.get("codes", []))

                        text = item.get("text")

                        if text:
                            itis_text.append(text)

                    #
                    # Build TIM row
                    #

                    row = {

                        "sourceLogFile": log_file,

                        "timestamp": timestamp,

                        "direction":
                            anchor.get("direction"),

                        "mutcd":
                            anchor.get("mutcd"),

                        "infoType":
                            anchor.get("infoType"),

                        "priority":
                            anchor.get("priority"),

                        "startTime":
                            anchor.get("startTime"),

                        "endTime":
                            anchor.get("endTime"),

                        "applicableRegion":
                            json.dumps(data.get("applicableRegion", {})),

                        "allPoints":
                            json.dumps(all_points),

                        "anchorPointName":
                            anchor.get("name"),

                        "masterLaneWidth":
                            anchor.get("masterLaneWidth"),

                        "itisCodes":
                            ",".join(itis_codes),

                        "itisText":
                            " | ".join(itis_text),

                        "referenceLat":
                            anchor.get("referenceLat"),

                        "referenceLon":
                            anchor.get("referenceLon"),

                        "referenceElevation":
                            anchor.get("referenceElevation"),

                        "verifiedMapLat":
                            verified.get("verifiedMapLat"),

                        "verifiedMapLon":
                            verified.get("verifiedMapLon"),

                        "verifiedMapElevation":
                            verified.get("verifiedMapElevation"),

                        "verifiedSurveyedLat":
                            verified.get("verifiedSurveyedLat"),

                        "verifiedSurveyedLon":
                            verified.get("verifiedSurveyedLon"),

                        "verifiedSurveyedElevation":
                            verified.get("verifiedSurveyedElevation"),

                        "enableElevation":
                            data.get("enableElevation"),

                        "messageType":
                            data.get("messageType")
                    }

                    tim_rows.append(row)

                    processed_tim_count += 1

            except Exception as e:

                print(
                    f"\nWarning: Could not process message"
                    f"\nFile: {log_file}"
                    f"\nTimestamp: {timestamp}"
                    f"\nReason: {e}\n"
                )

                continue                
    # --------------------------------------------------------------
    # Statistics
    # --------------------------------------------------------------

    print("\nProcessing Statistics")
    print("=" * 50)

    print("\nMAP Messages")
    print("-" * 12)
    print(f"Messages found    : {map_found_count}")
    print(f"Processed records : {processed_map_count}")
    print(f"Duplicate records : {map_duplicate_count}")
    print(f"Final CSV rows    : {len(map_rows)}")

    print("\nTIM Messages")
    print("-" * 12)
    print(f"Messages found    : {tim_found_count}")
    print(f"Processed records : {processed_tim_count}")
    print(f"Skipped records   : {tim_skipped_count}")
    print(f"Duplicate records : {tim_duplicate_count}")
    print(f"Final CSV rows    : {len(tim_rows)}")

    return map_rows, tim_rows



def write_csv(map_rows, tim_rows):
    if map_rows:
        with open(MAP_OUTPUT_CSV, "w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=map_rows[0].keys())
            writer.writeheader()
            writer.writerows(map_rows)

        print(f"MAP CSV written to: {MAP_OUTPUT_CSV}")

    if tim_rows:
        with open(TIM_OUTPUT_CSV, "w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=tim_rows[0].keys())
            writer.writeheader()
            writer.writerows(tim_rows)

        print(f"TIM CSV written to: {TIM_OUTPUT_CSV}")


if __name__ == "__main__":
    map_rows, tim_rows = process_log_files()
    write_csv(map_rows, tim_rows)