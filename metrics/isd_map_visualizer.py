import re
import json
import os
import folium

INPUT_LOG_FILE = "./metrics/logfiles/collected/2025-12-15_11-47-57_ET_ecstatic_robinson_c2622c68953b/jetty_logs/isd.log"
OUTPUT_DIR = "./metrics/reports/intersection_maps"

os.makedirs(OUTPUT_DIR, exist_ok=True)


def extract_json_from_line(line):
    """
    Extract JSON payload from log line.
    """

    if "User Input:" not in line:
        return None

    try:
        json_part = line.split("User Input:", 1)[1].strip()

        start = json_part.find("{")
        end = json_part.rfind("}")

        if start == -1 or end == -1:
            return None

        return json.loads(json_part[start:end + 1])

    except Exception as e:
        print(f"JSON parse error: {e}")
        return None


def get_lane_color(approach_type):
    """
    Simple color scheme by approach type.
    """

    colors = {
        "Ingress": "blue",
        "Egress": "green",
        "Crosswalk": "orange"
    }

    return colors.get(approach_type, "gray")


def create_intersection_map(data, timestamp):

    map_data = data.get("mapData", {})
    geometry = map_data.get("intersectionGeometry", {})

    reference = geometry.get("referencePoint", {})
    lane_list = geometry.get("laneList", {}).get("approach", [])

    intersection_id = reference.get("intersectionID", "unknown")
    intersection_name = reference.get("descriptiveIntersctionName", "unknown")

    ref_lat = reference.get("referenceLat")
    ref_lon = reference.get("referenceLon")

    if ref_lat is None or ref_lon is None:
        print(f"Skipping intersection {intersection_id} (missing reference point)")
        return

    print(f"\nProcessing intersection {intersection_id}")

    # ------------------------------------------------------------------
    # Create folium map centered on intersection
    # ------------------------------------------------------------------

    m = folium.Map(
        location=[ref_lat, ref_lon],
        zoom_start=20
    )

    # ------------------------------------------------------------------
    # Add reference point marker
    # ------------------------------------------------------------------

    popup_text = f"""
    <b>Intersection:</b> {intersection_name}<br>
    <b>ID:</b> {intersection_id}<br>
    <b>Timestamp:</b> {timestamp}
    """
    filename = f"{OUTPUT_DIR}/intersection_{intersection_id}.html"

    folium.Marker(
        [ref_lat, ref_lon],
        popup=popup_text,
        tooltip=f"Intersection {intersection_id}",
        icon=folium.Icon(color="red", icon="info-sign")
    ).add_to(m)

    # ------------------------------------------------------------------
    # Process approaches
    # ------------------------------------------------------------------

    for approach_index, approach in enumerate(lane_list):

        approach_type = approach.get("approachType", "Unknown")

        print(f"  Approach {approach_index}: {approach_type}")

        lanes = (
            approach.get("drivingLanes", [])
            or approach.get("crosswalkLanes", [])
        )

        for lane_index, lane in enumerate(lanes):

            lane_id = lane.get("laneID", f"lane_{lane_index}")

            print(f"    Lane {lane_id}")

            lane_nodes = lane.get("laneNodes", [])

            coordinates = []

            for node in lane_nodes:

                lat = node.get("nodeLat")
                lon = node.get("nodeLong")

                if lat is None or lon is None:
                    continue

                coordinates.append([lat, lon])

            print(f"      Nodes found: {len(coordinates)}")

            if not coordinates:
                continue

            # ----------------------------------------------------------
            # Draw lane polyline
            # ----------------------------------------------------------

            folium.PolyLine(
                coordinates,
                color=get_lane_color(approach_type),
                weight=5,
                opacity=0.8,
                tooltip=f"{approach_type}({approach_index}:{approach.get('approachID', 'Unkown')}) | Lane {lane_id}"
            ).add_to(m)
            m.save(filename)
            # ----------------------------------------------------------
            # Add node markers
            # ----------------------------------------------------------

            for idx, coord in enumerate(coordinates):

                folium.CircleMarker(
                    location=coord,
                    radius=4,
                    popup=f"""
                    <b>Lane:</b> {lane_id}<br>
                    <b>Node:</b> {idx}<br>
                    <b>Lat:</b> {coord[0]}<br>
                    <b>Lon:</b> {coord[1]}
                    """,
                    tooltip=f"{lane_id} Node {idx}",
                    color=get_lane_color(approach_type),
                    fill=True
                ).add_to(m)
                m.save(filename)
    # ------------------------------------------------------------------
    # Save HTML map
    # ------------------------------------------------------------------

    filename = f"{OUTPUT_DIR}/intersection_{intersection_id}.html"

    m.save(filename)

    print(f"Saved map: {filename}")


def process_log_file():

    with open(INPUT_LOG_FILE, "r", encoding="utf-8") as f:

        for line_number, line in enumerate(f):

            ts_match = re.match(r"^(.*?)\s+\[", line)
            timestamp = ts_match.group(1) if ts_match else "unknown"

            data = extract_json_from_line(line)

            if not data:
                continue

            try:
                create_intersection_map(data, timestamp)

            except Exception as e:
                print(f"Error processing line {line_number}: {e}")


if __name__ == "__main__":

    process_log_file()