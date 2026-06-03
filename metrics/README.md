# ISD MAP Analysis Tools

This folder contains tools for parsing and visualizing MAP messages embedded in ISD (Intersection Situation Data) logs.

## Overview

The tools support two primary workflows:

1. **MAP Message Parsing**

   * Extract MAP messages from ISD logs
   * Generate CSV reports for further analysis
   * Aggregate data from multiple log files
   * Remove duplicate records
   * Collect intersection metadata and geometry statistics

2. **MAP Geometry Visualization**

   * Render MAP message geometry on an interactive map
   * Visualize approaches, lanes, nodes, and connections
   * Validate lane geometry and node ordering
   * Assist with troubleshooting and debugging MAP data

---

# Scripts

## 1. isd_map_parser.py

Parses MAP messages from one or more ISD log files and exports selected fields to a CSV file.

### Features

* Supports a single ISD log file
* Supports a folder containing multiple ISD logs
* Recursive search for `isd.log` files
* Aggregates all MAP messages into a single CSV
* Optional duplicate filtering
* Reports duplicate statistics
* Preserves source log file information

---

### Input

The script searches for MAP messages contained within ISD logs.

Example log entry:

```text
2026-04-18 00:05:01 [thread] DEBUG ...
User Input:
{
    "mapData": {...},
    "messageType": "Frame+Map",
    "enableElevation": true
}
```

By default, the script searches:

```text
./metrics/logfiles/**/isd.log
```

Example structure:

```text
metrics/
├── logfiles/
│   ├── collected/
│   │   ├── deployment_1/
│   │   │   └── jetty_logs/
│   │   │       └── isd.log
│   │   ├── deployment_2/
│   │   │   └── jetty_logs/
│   │   │       └── isd.log
```

---

### Output

Default CSV output:

```text
./metrics/reports/map_messages.csv
```

Generated CSV fields:

| Field                      |
| -------------------------- |
| sourceLogFile              |
| timestamp                  |
| minuteOfTheYear            |
| layerType                  |
| descriptiveIntersctionName |
| layerID                    |
| intersectionID             |
| regionID                   |
| msgCount                   |
| masterLaneWidth            |
| referenceLat               |
| referenceLon               |
| referenceElevation         |
| verifiedMapLat             |
| verifiedMapLon             |
| verifiedMapElevation       |
| verifiedSurveyedLat        |
| verifiedSurveyedLon        |
| verifiedSurveyedElevation  |
| numApproaches              |
| numLanes                   |
| numConnections             |
| numNodes                   |
| enableElevation            |
| messageType                |


---

### Duplicate Handling

The parser can optionally remove duplicate MAP records.

Default behavior:

```text
Duplicates Disabled = False
```

Records are considered duplicates when they share the same:

* timestamp
* intersectionID

Statistics are displayed after processing:

```text
Processing Statistics
---------------------
Processed records : 250
Duplicate records : 42
Final CSV rows    : 208
```

---

### Running the Parser

Example:

```bash
python3 isd_map_parser.py
```

Interactive prompts:

```text
Enter input log file/folder:
Enter output CSV file:
Allow duplicate timestamps? (y/N):
```

Example output:

```text
Processing: metrics/logfiles/.../isd.log

Processing Statistics
---------------------
Processed records : 250
Duplicate records : 42
Final CSV rows    : 208

CSV written to:
./metrics/reports/map_messages.csv
```

---

# 2. isd_map_visualizer.py

Creates interactive HTML maps from MAP messages found in ISD logs.

The visualizer reconstructs the MAP geometry and displays:

* Intersection reference point
* Approaches
* Lanes
* Lane nodes
* Lane geometry

using Folium and OpenStreetMap.

---

## Visualization Hierarchy

The visualizer reconstructs the MAP hierarchy:

```text
Intersection
│
├── Reference Point
│
├── Approaches
│   ├── Ingress
│   ├── Egress
│   └── Crosswalk
│
├── Lanes
│
└── Nodes
```

---

## Color Scheme

| Approach Type | Color  |
| ------------- | ------ |
| Ingress       | Blue   |
| Egress        | Green  |
| Crosswalk     | Orange |
| Unknown       | Gray   |

---

## Input

Default input:

```text
./metrics/logfiles/.../isd.log
```

The visualizer processes MAP messages found in the log and generates one map per intersection.

---

## Output

Default output folder:

```text
./metrics/reports/intersection_maps/
```

Generated files:

```text
intersection_1002.html
intersection_1005.html
intersection_2001.html
```

---

## Viewing Maps

Open the generated HTML file in a web browser:

```text
metrics/reports/intersection_maps/intersection_1002.html
```

The map supports:

* Zooming
* Panning
* Node inspection
* Lane inspection
* Approach identification

---

## Debugging and Troubleshooting

The visualizer is useful when validating MAP data.

### Verify Node Ordering

Nodes should form a smooth lane geometry.

Potential issues:

* Reversed node order
* Missing nodes
* Duplicate nodes
* Geometry jumps

---

### Verify Lane Geometry

Inspect lane shapes and alignment.

Potential issues:

* Incorrect lane width
* Disconnected lanes
* Unexpected turns
* Misaligned geometry

---

### Verify Approach Classification

Check that:

* Ingress lanes are entering the intersection
* Egress lanes are leaving the intersection
* Crosswalks are represented correctly

---

### Validate Reference Point

Ensure:

* Reference latitude is correct
* Reference longitude is correct
* All lanes are positioned relative to the expected intersection

---

### Interactive Debugging Workflow

A useful workflow is:

1. Run the visualizer.
2. Set breakpoints in VSCode.
3. Refresh the generated HTML file after each save.
4. Observe geometry as lanes and nodes are added.

This allows developers to visually validate MAP message geometry while stepping through the parser.

---

# Dependencies

Install Folium:

```bash
pip install folium
```

Verify installation:

```bash
python3 -c "import folium; print(folium.__version__)"
```

---

# Typical Workflow

1. Collect ISD logs.
2. Run `isd_map_parser.py`.
3. Review generated CSV statistics.
4. Identify intersections of interest.
5. Run `isd_map_visualizer.py`.
6. Inspect generated HTML maps.
7. Validate MAP geometry and troubleshoot issues.

This workflow provides both quantitative analysis (CSV reports) and visual validation (interactive maps).
