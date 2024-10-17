/**
 * Created by lewisstet on 3/20/2015.
 * Updated 3/2017 by martzth
 */

/**
 * @defines: intersection features, help notes, lane attributes
 */

var intersection_features = [
    {
        id: 0,
        name: "Reference Point Marker",
        description: "Used to mark the center of the intersection",
        img_src: "./img/intersection-builder/markers/Map-Marker-Marker-Outside-Azure-icon.png"
    },
    {
        id: 1,
        name: "Verified Point Marker",
        description: "Used to mark a known, verified location near an intersection",
        img_src: "./img/intersection-builder/markers/Map-Marker-Ball-Azure-icon.png"
    }
];

var lane_attributes = [
	{
	  id: 0,
	  name: "Straight",
	  description: "Maneuver Straight Allowed",
	  img_src: "./img/intersection-builder/lane/straight.png"
	},
	{
	  id: 1,
	  name: "Left",
	  description: "Maneuver Left Allowed",
	  img_src: "./img/intersection-builder/lane/left.png"
	},
	{
	  id: 2,
	  name: "Right",
	  description: "Maneuver Right Allowed",
	  img_src: "./img/intersection-builder/lane/right.png"
	},
	{
	  id: 3,
	  name: "U-Turn",
	  description: "Maneuver U-Turn Allowed",
	  img_src: "./img/intersection-builder/lane/u-turn.png"
	},
	{
	  id: 4,
	  name: "Left Turn on Red",
	  description: "Maneuver Left Turn on Red Allowed",
	  img_src: "./img/intersection-builder/lane/left-turn-on-red.jpg"
	},
	{
	  id: 5,
	  name: "Right Turn on Red",
	  description: "Maneuver Right Turn on Red Allowed",
	  img_src: "./img/intersection-builder/lane/right-turn-on-red.jpg"
	},
	{
	  id: 6,
	  name: "Lange Change",
	  description: "Lane Change Allowed",
	  img_src: "./img/intersection-builder/lane/lane-change.png"
	},
	{
	  id: 7,
	  name: "No Stopping",
	  description: "No Stopping Allowed",
	  img_src: "./img/intersection-builder/lane/no-stopping.gif"
	},
	{
	  id: 8,
	  name: "Yield Always Required",
	  description: "Yield Always Required",
	  img_src: "./img/intersection-builder/lane/yield.png"
	},
	{
	  id: 9,
	  name: "Go With Halt",
	  description: "Go With Halt",
	  img_src: "./img/intersection-builder/lane/stop-sign.png"
	},
	{
	  id: 10,
	  name: "Caution",
	  description: "Caution",
	  img_src: "./img/intersection-builder/lane/caution.png"
	}
];

var help_notes = [
    {
    	value: "approach_type",
    	title: "Approach Type",
    	max: "N/A",
    	min: "N/A",
    	units: "N/A",
    	description: "Specifies the type of the approach."
    },
    {
    	value: "direction",
    	title: "Direction",
    	max: "N/A",
    	min: "N/A",
    	units: "N/A",
    	description: "Denotes the allowed direction of travel over a lane object."
    },
    {
    	value: "descriptive_name",
    	title: "Descrpitive Name",
    	max: "N/A",
    	min: "N/A",
    	units: "N/A",
    	description: "Specifies a short descriptive name for the lane. Max length is 63 characters."
    },
    {
    	value: "lane_type",
    	title: "Lane Type",
    	max: "N/A",
    	min: "N/A",
    	units: "N/A",
    	description: "Specifies the type of the lane."
    },
    {
    	value: "lane_type_attributes",
    	title: "Lane Type Attributes",
    	max: "N/A",
    	min: "N/A",
    	units: "N/A",
    	description: "Attributes information specific to a given lane type."
    },
    {
    	value: "shared_with",
    	title: "Shared With",
    	max: "N/A",
    	min: "N/A",
    	units: "N/A",
    	description: "Describes co-users of the lane path."
    },
    {
    	value: "lane_number",
    	title: "Lane Number",
    	max: "255",
    	min: "0",
    	units: "N/A",
    	description: "Conveys an assigned index that is unique within an intersection. <br> --The value 0 shall be used" +
    			"when the lane ID is not available or not known. <br> --The value 255 is reserved for future use. "
    },
    {
    	value: "approach_id",
    	title: "Approach ID",
    	max: "15",
    	min: "0",
    	units: "N/A",
    	description: "Used to relate the index of an approach, either ingress or egress within the subject lane. <br>" +
    			"--Zero to be used when valid value is unknown."
    },
    {
    	value: "intersection_name",
    	title: "Intersection Name",
    	max: "N/A",
    	min: "N/A",
    	units: "N/A",
    	description: "Specifies the name of the intersection."
    },
    {
    	value: "layer_id",
    	title: "Layer ID",
    	max: "100",
    	min: "0",
    	units: "N/A",
    	description: "Used to uniquely identify the layers of a geographic map fragment such as an intersection."
    },
    {
    	value: "revision",
    	title: "Revision Number",
    	max: "N/A",
    	min: "N/A",
    	units: "N/A",
    	description: "Specifies revision number."
    },
    {
    	value: "latitude",
    	title: "Latitude",
    	max: "90.0",
    	min: "-90.0",
    	units: "Decimal Degrees",
    	description: "The geographic latitude of an object."
    },
    {
    	value: "longitude",
    	title: "Longitude",
    	max: "180.0",
    	min: "-180.0",
    	units: "Decimal Degrees",
    	description: "The geographic longitude of an object."
    },
	{
    	value: "region_id",
    	title: "Region ID",
    	max: "65535",
    	min: "0",
    	units: "N/A",
    	description: 'A globally unique regional assignment value that is typically assigned to a regional DOT authority. Also known as "Road Regulator ID" and is a portion of the "Intersection Reference ID".'
    },
    {
    	value: "intersection_id",
    	title: "Intersection ID",
    	max: "65535",
    	min: "0",
    	units: "N/A",
    	description: "Used within a region to uniquely define an intersection within that country or region."
    },
    {
    	value: "elevation",
    	title: "Elevation",
    	max: "6143.9",
    	min: "-409.6",
    	units: "Meters",
    	description: "The geographic elevation of an object."
    },
    {
    	value: "lane_width",
    	title: "Lane Width",
    	max: "511",
    	min: "-512",
    	units: "Centimeters",
    	description: "Used to convey the delta width of a lane in LSB units of 1cm."
    },
    {
    	value: "master_lane_width",
    	title: "Master Lane Width",
    	max: "32767",
    	min: "0",
    	units: "Centimeters",
    	description: "An overriding value to set the width of all described lanes."
    },
    {
    	value: "verified_lat",
    	title: "Verified Latitude",
    	max: "90.0",
    	min: "-90.0",
    	units: "Decimal Degrees",
    	description: "The verified geographic latitude of an object."
    },
    {
    	value: "verified_long",
    	title: "Verified Longitude",
    	max: "180.0",
    	min: "-180.0",
    	units: "Decimal Degrees",
    	description: "The verified geographic longitude of an object."
    },
    {
    	value: "verified_elev",
    	title: "Verified Elevation",
    	max: "6143.9",
    	min: "-409.6",
    	units: "Meters",
    	description: "The verified geographic elevation of an object."
    },
    {
    	value: "spat_revision",
    	title: "SPaT Revision",
    	max: "N/A",
    	min: "N/A",
    	units: "N/A",
    	description: "Specifies SPaT revision."
    },
    {
    	value: "spat_label",
    	title: "Signal Group",
    	max: "N/A",
    	min: "N/A",
    	units: "N/A",
    	description: "Specifies Signal Group ID."
    },
    {
    	value: "phase",
    	title: "Phase",
    	max: "N/A",
    	min: "N/A",
    	units: "N/A",
    	description: "Provides the overall current state of the movement."
    },
    {
    	value: "start_time",
    	title: "Start Time",
    	max: "65535",
    	min: "1",
    	units: "LSB units of 10 mSec",
    	description: "Used to relate when the phase itself started or is expected to start."
    },
    {
    	value: "min_end_time",
    	title: "Minimum End Time",
    	max: "65535",
    	min: "1",
    	units: "LSB units of 10 mSec",
    	description: "Used to convey the earliest time possible at which a phase could change, except when unpredictable events relating to a preemption or priority call disrupt a currently active timing plan."
    },
    {
    	value: "max_end_time",
    	title: "Maximum End Time",
    	max: "65535",
    	min: "1",
    	units: "LSB units of 10 mSec",
    	description: "Used to convey the longest expected end time."
    },
    {
    	value: "likely_time",
    	title: "Likely Time",
    	max: "65535",
    	min: "1",
    	units: "LSB units of 10 mSec",
    	description: "Used to convey the most likely time the phase changes."
    },
    {
    	value: "confidence",
    	title: "Confidence",
    	max: "100%",
    	min: "21%",
    	units: "Percentage",
    	description: "Used to convey basic confidnece data about likely time."
    },
    {
    	value: "next_time",
    	title: "Next Time",
    	max: "65535",
    	min: "1",
    	units: "LSB units of 10 mSec",
    	description: "Used to express a general (and presumably less precise) value regarding when this phase will next occur."
    },
    {
    	value: "speed_limit_type",
    	title: "Speed Limit Type",
    	max: "N/A",
    	min: "N/A",
    	units: "N/A",
    	description: "Relates the type of speed limit to which a given speed refers."
    },
    {
    	value: "velocity",
    	title: "Velocity",
    	max: "366",
    	min: "0",
    	units: "Miles Per Hour",
    	description: "Represents the vehicle speed."
    },
	{
		value: "referenceLaneID",
		title: "Reference Lane ID",
		max: "63",
		min: "1",
		units: "N/A",
		description: "Represents a computed lanes reference lane."
	},
	{
		value: "offsetX",
		title: "Offset X",
		max: "2047",
		min: "-2047",
		units: "Centimeters",
		description: "A path X offset for translations."
	},
	{
		value: "offsetY",
		title: "Offset Y",
		max: "2047",
		min: "-2047",
		units: "Centimeters",
		description: "A path Y offset for translations."
	},
	{
		value: "rotation",
		title: "Rotation",
		max: "359.9875",
		min: "0",
		units: "Degrees",
		description: "A rotation value for the entire lane."
	},
	{
		value: "scaleX",
		title: "Scale X",
		max: "102.35",
		min: "-99.95",
		units: "Steps of 0.05 percent",
		description: "Expand or contract lane along the X axis."
	},
	{
		value: "scaleY",
		title: "Scale Y",
		max: "102.35",
		min: "-99.95",
		units: "Steps of 0.05 percent",
		description: "Expand or contract lane along the Y axis."
	},
	{
		value: "major_version",
		title: "Major Version (RGA Only)",
		max: "15",
		min: "0",
		units: "N/A",
		description: "It indicates that changes have been made to the content regarding the user of the contents of the dataset such that the new dataset is incompatble with prior versions of the dataset. Integer (0...15)."
	},
	{
		value: "minor_version",
		title: "Minor Version (RGA Only)",
		max: "64",
		min: "0",
		units: "N/A",
		description: "It indicates that the dataset has changed but still compatible with prior versions of the dataset."
	},
        {
		value: "road_authority_id_type",
		title: "Road Authority Identifier Type",
		max: "NA",
		min: "NA",
		units: "N/A",
		description: "This Road Authority Identifier Type indicates whether the Road Authority ID is full or relative. If no type specified and region id is not zero, Road Authority ID is optional."
	},
	{
		value: "road_authority_id",
		title: "Road Authority Identifier",
		max: "NA",
		min: "NA",
		units: "N/A",
		description: "Road Authority ID, contained in the Base Layer of the RGA has the same value as that of the organization responsible for providing the DS content. If IEEE 1609.2 security is used (see 6.8), this will be contained in the ‘Operating Organization ID’ field of the security certificate used to sign the RGA. How this is obtained if other security mechanisms are used is outside the scope of this report."
	},
	{
		value: "mapped_geometry_id",
		title: "Mapped Geometry Identifier (RGA Only)",
		max: "NA",
		min: "NA",
		units: "N/A",
		description: "Uniquely identify a mapped location managed by an authority."
	},
	{
		value: "content_version",
		title: "Content Version (RGA Only)",
		max: "32",
		min: "0",
		units: "N/A",
		description: "Provides a version number which pertains to a combination of the dataset contents and the content values. INTEGER (0..32)"
	},
	{
		value: "content_date_time",
		title: "Content Date Time (RGA Only)",
		max: "NA",
		min: "NA",
		units: "Datetime",
		description: "Timestamp corresponding to the contents in format yyyy, mm, dd, hh, mm, ss (sss+)"
	}
];
