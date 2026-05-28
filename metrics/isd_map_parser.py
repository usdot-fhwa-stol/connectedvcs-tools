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
# Resolve input log file
# ------------------------------------------------------------------

user_input_file = input(
    f"Enter input log file/folder "
    f"(Press Enter for default search: {DEFAULT_INPUT_PATTERN}): "
).strip()

if user_input_file:

    # --------------------------------------------------------------
    # User entered something
    # --------------------------------------------------------------

    if os.path.isdir(user_input_file):

        # Search recursively for isd.log inside folder
        matches = glob.glob(
            os.path.join(user_input_file, "**", "isd.log"),
            recursive=True
        )

        if not matches:
            raise FileNotFoundError(
                f"No isd.log files found in {user_input_file}"
            )

        INPUT_LOG_FILE = matches[0]

    else:

        # Assume user entered direct file path
        INPUT_LOG_FILE = user_input_file

else:

    # --------------------------------------------------------------
    # Use default recursive search
    # --------------------------------------------------------------

    matches = glob.glob(DEFAULT_INPUT_PATTERN, recursive=True)

    if not matches:
        raise FileNotFoundError(
            f"No isd.log files found using pattern: "
            f"{DEFAULT_INPUT_PATTERN}"
        )

    INPUT_LOG_FILE = matches[0]


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
# Ensure output folder exists
# ------------------------------------------------------------------

output_dir = os.path.dirname(OUTPUT_CSV_FILE)

if output_dir:
    os.makedirs(output_dir, exist_ok=True)


# ------------------------------------------------------------------
# Display final paths
# ------------------------------------------------------------------

print("\nUsing paths:")
print(f"  INPUT_LOG_FILE  = {INPUT_LOG_FILE}")
print(f"  OUTPUT_CSV_FILE = {OUTPUT_CSV_FILE}")


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


def process_log_file():
    rows = []

    with open(INPUT_LOG_FILE, "r", encoding="utf-8") as f:
        for line in f:

            # 1. Extract timestamp from log prefix
            ts_match = re.match(r"^(.*?)\s+\[", line)
            timestamp = ts_match.group(1) if ts_match else None

            # 2. Extract JSON payload
            data = extract_json_from_line(line)
            if not data:
                continue

            map_data = data.get("mapData", {})
            geometry = safe_get(map_data, ["intersectionGeometry"], {})

            ref = geometry.get("referencePoint", {})
            verified = geometry.get("verifiedPoint", {})

            # 3. Derived metrics
            num_approaches, num_lanes, num_connections, num_nodes = count_map_fields(map_data)

            row = {
                "timestamp": timestamp,
                "minuteOfTheYear": map_data.get("minuteOfTheYear"),
                "layerType": map_data.get("layerType"),
                "descriptiveIntersctionName": ref.get("descriptiveIntersctionName"),
                "layerID": ref.get("layerID"),
                "intersectionID": ref.get("intersectionID"),
                "regionID": ref.get("regionID"),
                "msgCount": ref.get("msgCount"),
                "masterLaneWidth": ref.get("masterLaneWidth"),

                "referenceLat": ref.get("referenceLat"),
                "referenceLon": ref.get("referenceLon"),
                "referenceElevation": ref.get("referenceElevation"),

                "verifiedMapLat": verified.get("verifiedMapLat"),
                "verifiedMapLon": verified.get("verifiedMapLon"),
                "verifiedMapElevation": verified.get("verifiedMapElevation"),

                "verifiedSurveyedLat": verified.get("verifiedSurveyedLat"),
                "verifiedSurveyedLon": verified.get("verifiedSurveyedLon"),
                "verifiedSurveyedElevation": verified.get("verifiedSurveyedElevation"),

                "numApproaches": num_approaches,
                "numLanes": num_lanes,
                "numConnections": num_connections,
                "numNodes": num_nodes,

                "enableElevation": data.get("enableElevation"),
                "messageType": data.get("messageType"),
            }

            rows.append(row)

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
    rows = process_log_file()
    write_csv(rows)