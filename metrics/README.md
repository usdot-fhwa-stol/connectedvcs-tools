# V2X Log Analysis Tools

This folder contains tools for parsing and visualizing V2X messages embedded in ISD and TIM logs.

## Overview

The tools support two primary workflows:

1. **V2X Message Parsing**

   * Extract MAP and TIM messages from log files
   * Generate CSV reports for further analysis
   * Aggregate data from multiple log files
   * Remove duplicate records
   * Collect MAP intersection metadata and geometry statistics
   * Collect TIM location, advisory, and region information

2. **MAP Geometry Visualization**

   * Render MAP message geometry on an interactive map
   * Visualize approaches, lanes, nodes, and connections
   * Validate lane geometry and node ordering
   * Assist with troubleshooting and debugging MAP data

---

# Scripts

## 1. v2x_log_parser.py

Parses MAP and TIM messages from one or more log files and exports the extracted information into CSV reports.

### Features

* Supports a single log file or a folder of logs
* Recursively searches for both `isd.log` and `tim.log`
* Processes MAP and TIM messages in a single execution
* Aggregates messages from multiple log files
* Generates separate CSV reports for MAP and TIM messages
* Optional duplicate filtering
* Reports processing and duplicate statistics
* Preserves source log file information
* Skips incomplete or malformed messages while continuing processing

---

### Input

The parser searches recursively for both:

```text
isd.log
tim.log
```

By default:

```text
./metrics/logfiles/
```

The parser may also be pointed at:

* a single log file
* a directory containing logs
* a directory tree containing multiple deployments

Example:

```text
metrics/
├── logfiles/
│   ├── deployment_1/
│   │   ├── isd.log
│   │   └── tim.log
│   ├── deployment_2/
│   │   ├── isd.log
│   │   └── tim.log
```

---

### Supported Messages

The parser extracts two message types:

#### MAP Messages

Example:

```text
User Input:
{
    "mapData": {...},
    "messageType": "Frame+Map",
    "enableElevation": true
}
```

#### TIM Messages

Example:

```text
Building TIM/ADV with input data :
{
    "anchorPoint": {...},
    "regions": [...],
    "verifiedPoint": {...},
    "messageType": "Frame+TIM"
}
```

---

### Output

Two CSV reports are generated.

#### MAP Report

Default:

```text
./metrics/reports/map_messages.csv
```

Columns:

| Field |
|------|
| sourceLogFile |
| timestamp |
| minuteOfTheYear |
| layerType |
| descriptiveIntersctionName |
| layerID |
| intersectionID |
| regionID |
| msgCount |
| masterLaneWidth |
| referenceLat |
| referenceLon |
| referenceElevation |
| verifiedMapLat |
| verifiedMapLon |
| verifiedMapElevation |
| verifiedSurveyedLat |
| verifiedSurveyedLon |
| verifiedSurveyedElevation |
| numApproaches |
| numLanes |
| numConnections |
| numNodes |
| enableElevation |
| messageType |

---

#### TIM Report

Default:

```text
./metrics/reports/tim_messages.csv
```

Columns:

| Field |
|------|
| sourceLogFile |
| timestamp |
| direction |
| mutcd |
| infoType |
| priority |
| startTime |
| endTime |
| applicableRegion |
| allPoints |
| anchorPointName |
| masterLaneWidth |
| itisCodes |
| itisText |
| referenceLat |
| referenceLon |
| referenceElevation |
| verifiedMapLat |
| verifiedMapLon |
| verifiedMapElevation |
| verifiedSurveyedLat |
| verifiedSurveyedLon |
| verifiedSurveyedElevation |
| enableElevation |
| messageType |

---

### Duplicate Handling

Duplicate filtering is optional.

The parser performs duplicate detection independently for MAP and TIM messages.

Typical duplicate keys:

#### MAP

* timestamp

#### TIM

* timestamp

Processing statistics are displayed for both message types.

Example:

```text
Processing: metrics/logfiles/.../isd.log

Processing: metrics/logfiles/.../tim.log

Processing Statistics
==================================================

MAP Messages
------------
Messages found    : 1706
Processed records : 195
Duplicate records : 1511
Final CSV rows    : 195

TIM Messages
------------
Messages found    : 434
Processed records : 61
Skipped records   : 19
Duplicate records : 354
Final CSV rows    : 61


MAP CSV written to:
./metrics/reports/map_messages.csv

TIM CSV written to:
./metrics/reports/tim_messages.csv
```

---

### Invalid or Incomplete Messages

Some logs contain incomplete TIM messages, for example:

```json
{
  "anchorPoint": null,
  "verifiedPoint": null,
  "regions": [],
  "messageType": "TIM"
}
```

These messages do not contain sufficient information for analysis.

The parser reports a warning and skips them while continuing to process the remaining log entries.

---

### Running the Parser

Example:

```bash
python3 v2x_log_parser.py
```

Interactive prompts:

```text
Enter input log file/folder:
Enter output folder:
Allow duplicate messages? (y/N):
```


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
