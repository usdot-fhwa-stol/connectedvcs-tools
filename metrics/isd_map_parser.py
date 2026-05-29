import re
import json
import csv
from datetime import datetime
import os
import glob

# ------------------------------------------------------------------
# Default paths
# ------------------------------------------------------------------

DEFAULT_INPUT_PATTERN = "./metrics/logfiles/**/isd.log"
DEFAULT_OUTPUT_CSV = "./metrics/reports/map_messages.csv"


# ------------------------------------------------------------------
# Resolve input path
# ------------------------------------------------------------------

user_input = input(
    f"Enter input log file/folder "
    f"(Press Enter for default search: {DEFAULT_INPUT_PATTERN}): "
).strip()


def discover_log_files(path_input):
    """
    Return list of all matching isd.log files.
    """

    if not path_input:

        matches = glob.glob(
            DEFAULT_INPUT_PATTERN,
            recursive=True
        )

    elif os.path.isdir(path_input):

        matches = glob.glob(
            os.path.join(path_input, "**", "isd.log"),
            recursive=True
        )

    elif os.path.isfile(path_input):

        matches = [path_input]

    else:
        raise FileNotFoundError(
            f"Invalid input path: {path_input}"
        )

    if not matches:
        raise FileNotFoundError(
            "No isd.log files found."
        )

    return sorted(matches)


INPUT_LOG_FILES = discover_log_files(user_input)


print("\nDiscovered log files:")
for file in INPUT_LOG_FILES:
    print(f"  - {file}")

# ------------------------------------------------------------------
# Resolve output CSV
# ------------------------------------------------------------------

user_output_file = input(
    f"Enter output CSV file "
    f"(Press Enter for default: {DEFAULT_OUTPUT_CSV}): "
).strip()

OUTPUT_CSV_FILE = (
    user_output_file
    if user_output_file
    else DEFAULT_OUTPUT_CSV
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


def extract_json_from_line(line: str):
    """
    Extract JSON starting from 'User Input:' safely.
    """
    if "User Input:" not in line:
        return None

    try:
        json_part = line.split("User Input:", 1)[1].strip()

        # Some logs may have trailing garbage, keep only JSON
        start = json_part.find("{")
        end = json_part.rfind("}")

        if start == -1 or end == -1:
            return None

        return json.loads(json_part[start:end + 1])

    except Exception:
        return None


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

    rows = []

    seen_records = set()

    duplicate_count = 0
    processed_count = 0

    for log_file in INPUT_LOG_FILES:

        print(f"\nProcessing: {log_file}")

        with open(log_file, "r", encoding="utf-8") as f:

            for line in f:

                # --------------------------------------------------
                # Extract timestamp
                # --------------------------------------------------

                ts_match = re.match(r"^(.*?)\s+\[", line)

                timestamp = (
                    ts_match.group(1)
                    if ts_match
                    else None
                )

                # --------------------------------------------------
                # Extract JSON
                # --------------------------------------------------

                data = extract_json_from_line(line)

                if not data:
                    continue

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
                    timestamp,
                    ref.get("intersectionID")
                )

                if (
                    not ALLOW_DUPLICATES
                    and duplicate_key in seen_records
                ):
                    duplicate_count += 1
                    continue

                seen_records.add(duplicate_key)

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

                rows.append(row)

                processed_count += 1

    # --------------------------------------------------------------
    # Statistics
    # --------------------------------------------------------------

    print("\nProcessing Statistics")
    print("---------------------")
    print(f"Processed records : {processed_count}")
    print(f"Duplicate records : {duplicate_count}")
    print(f"Final CSV rows    : {len(rows)}")

    return rows


def write_csv(rows):
    if not rows:
        print("No MAP messages found.")
        return

    fieldnames = rows[0].keys()

    with open(OUTPUT_CSV_FILE, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)

    print(f"CSV written to: {OUTPUT_CSV_FILE} ({len(rows)} records)")


if __name__ == "__main__":
    rows = process_log_files()
    write_csv(rows)