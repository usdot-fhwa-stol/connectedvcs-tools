/**
 * Created by lewisstet on 3/20/2015.
 * Updated 3/2017 by martzth
 */

/**
 * @defines: intersection features, help notes, itis codes
 */

var intersection_features = [
    {
        id: 0,
        name: "Verified Point Marker",
        type: "VER",
        description: "Used to mark a known, verified location near an intersection",
        img_src: "./img/intersection-builder/markers/Map-Marker-Ball-Azure-icon.png"
    },
    {
        id: 1,
        name: "Speed Limit",
        type: "TIM",
        description: "Used to show where a sign is located",
        img_src: "./img/intersection-builder/markers/speedlimit.png",
        limit: 16
    },
    {
        id: 2,
        name: "Advisory",
        type: "TIM",
        description: "Used to show where a sign is located",
        img_src: "./img/intersection-builder/markers/advisory.png",
        limit: 500
    },
    {
        id: 3,
        name: "Work Zone",
        type: "TIM",
        description: "Used to show where a sign is located",
        img_src: "./img/intersection-builder/markers/workzone.png",
        limit: 16
    },
    {
        id: 4,
        name: "Exit Service",
        type: "TIM",
        description: "Used to show where a sign is located",
        img_src: "./img/intersection-builder/markers/exit.png",
        limit: 16
    },
    {
        id: 5,
        name: "Generic",
        type: "TIM",
        description: "Used to show where a sign is located",
        img_src: "./img/intersection-builder/markers/generic.png",
        limit: 16
    }
];


var help_notes = [
    {
        value: "start_time",
        title: "Start Time",
        max: "12/31/2100",
        min: "1/1/1900",
        units: "mm/dd/yyyy",
        description: "Denotes when the message should start to be active."
    },
    {
        value: "end_time",
        title: "End Time",
        max: "12/31/2100",
        min: "1/1/1900",
        units: "mm/dd/yyyy",
        description: "Denotes when the message should end being active."
    },
    {
        value: "nwlat",
        title: "Northwest Latitude",
        max: "90.0",
        min: "-90.0",
        units: "Decimal Degrees",
        description: "The geographic latitude of the northwest corner of the bounding box object."
    },
    {
        value: "nwlong",
        title: "Northwest Longitude",
        max: "180.0",
        min: "-180.0",
        units: "Decimal Degrees",
        description: "The geographic longitude of the northwest corner of the bounding box object."
    },
    {
        value: "selat",
        title: "Southeast Latitude",
        max: "90.0",
        min: "-90.0",
        units: "Decimal Degrees",
        description: "The geographic latitude of the southeast corner of the bounding box object."
    },
    {
        value: "selong",
        title: "Southeast Longitude",
        max: "180.0",
        min: "-180.0",
        units: "Decimal Degrees",
        description: "The geographic longitude of the southeast corner of the bounding box object."
    },
    {
        value: "extent",
        title: "Extent",
        max: "N/A",
        min: "N/A",
        units: "N/A",
        description: "The spatial distance over which this message applies and should be presented to the driver."
    },
    {
        value: "lat",
        title: "Latitude",
        max: "90.0",
        min: "-90.0",
        units: "Decimal Degrees",
        description: "The geographic latitude of an object."
    },
    {
        value: "long",
        title: "Longitude",
        max: "180.0",
        min: "-180.0",
        units: "Decimal Degrees",
        description: "The geographic longitude of an object."
    },
    {
        value: "elev",
        title: "Elevation",
        max: "6143.9",
        min: "-409.6",
        units: "Meters",
        description: "The geographic elevation of an object."
    },
    {
        value: "lane_width",
        title: "Lane Width Delta",
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
        value: "ssp_tim_rights",
        title: "SSP TIM Rights",
        max: "31",
        min: "0",
        units: "N/A",
        description: "Declares what content is allowed in TIM."
    },
    {
        value: "ssp_loc_rights",
        title: "SSP Location Rights",
        max: "31",
        min: "0",
        units: "N/A",
        description: "Declares what content is allowed in location."
    },
    {
        value: "ssp_type_rights",
        title: "SSP Type Rights",
        max: "31",
        min: "0",
        units: "N/A",
        description: "Declares what content is allowed in type."
    },
    {
        value: "ssp_content_rights",
        title: "SSP Content Rights",
        max: "31",
        min: "0",
        units: "N/A",
        description: "Declares what content is allowed in content"
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
        value: "packet_id",
        title: "Packet ID",
        max: "N/A",
        min: "N/A",
        units: "N/A",
        description: "Provides a unique value which can be used to connet to other supporting messages"
    },
    {
        value: "content",
        title: "Content",
        max: "N/A",
        min: "N/A",
        units: "N/A",
        description: "Conveys an assigned index that corresponds to road and location descriptions."
    },
    {
        value: "speed_limit",
        title: "Speed Limit",
        max: "366",
        min: "0",
        units: "MPH",
        description: "Relates the type of speed limit to which a given speed refers."
    },
    {
        value: "mutcd",
        title: "MUTCD",
        max: "N/A",
        min: "N/A",
        units: "N/A",
        description: "Defines what basic MUTCD type a sign expression falls into."
    },
    {
        value: "info-type",
        title: "Information Type",
        max: "N/A",
        min: "N/A",
        units: "N/A",
        description: "Describes the type of meessage to follow in the rest of the message frame structure."
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
        value: "priority",
        title: "Priority",
        max: "7",
        min: "0",
        units: "N/A",
        description: "The relative importance of a sign on a scale of 0 (least) to 7 (most)"
    },
    {
        value: "direction",
        title: "Direction",
        max: "N/A",
        min: "N/A",
        units: "N/A",
        description: "Denotes the direction of sign in relation to a lane object."
    },
    {
        value: "radius",
        title: "Radius",
        max: "10,000",
        min: "0",
        units: "Meters",
        description: "Denotes the size of a circle region."
    },
    {
        value: "road_surface",
        title: "Description Of Road Surface",
        max: "N/A",
        min: "N/A",
        units: "N/A",
        description: "Indicates the composition of the surface of the roadway for use in estimation of friction."
    },
    {
        value: "portland_cement_type",
        title: "Portland Cement Type",
        max: "N/A",
        min: "N/A",
        units: "N/A",
        description: "Indicates the type of portland cement."
    },
    {   
        value: "asphalt_or_tar_type",
        title: "Asphalt or Tar Type",
        max: "N/A",
        min: "N/A",
        units: "N/A",
        description: "Indicates the type of asphalt or tar."
    },
     {   
        value: "gravel_type",
        title: "Gravel Type",
        max: "N/A",
        min: "N/A",
        units: "N/A",
        description: "Indicates the type of gravel."
    },
    {   
        value: "snow_type",
        title: "Snow Type",
        max: "N/A",
        min: "N/A",
        units: "N/A",
        description: "Indicates the type of snow."
    },
    {
        value: "road_condition",
        title: "Road Surface Condition",
        max: "N/A",
        min: "N/A",
        units: "N/A",
        description: "Indicates the surface of the road surface condition for use in friction estimation."
    },
    {
        value: "meanVerticalVariation",
        title: "Mean Vertical Variation",
        max: "25000",
        min: "0",
        units: "Microns",
        description: "The mean variation in measured vertical road roughness."
    },
    {
        value: "verticalVariationStdDev",
        title: "Vertical Variation Standard Deviation",
        max: "2500",
        min: "0",
        units: "Microns",
        description: "The standard deviation of variation of measured mean of the vertical road roughness."
    },
    {
        value: "meanHorizontalVariation",
        title: "Mean Horizontal Variation",
        max: "25000",
        min: "0",
        units: "Microns",
        description: "The mean variation in measured horizontal road roughness."
    },
    {
        value: "horizontalVariationStdDev",
        title: "Horizontal Variation Standard Deviation",
        max: "2500",
        min: "0",
        units: "Microns",
        description: "The standard deviation of variation of measured mean of the horizontal road roughness."
    },
];

var itis_list = [
    {
        value: 268,
        text: "(268) Speed Limit Follows"
    },
    {
        value: 2574,
        text: "(2574) Height Follows"
    },
    {
        value: 7989,
        text: "(7989) On the Right"
    },
    {
        value: 7990,
        text: "(7990) On the Left"
    },
    {
        value: 7991,
        text: "(7991) In the Center"
    },
    {
        value: 7992,
        text: "(7992) In the Opposite Direction"
    },
    {
        value: 7993,
        text: "(7993) Cross Traffic"
    },
    {
        value: 7994,
        text: "(7994) Northbound Traffic"
    },
    {
        value: 7995,
        text: "(7995) Eastbound Traffic"
    },
    {
        value: 7996,
        text: "(7996) Southbound Traffic"
    },
    {
        value: 7997,
        text: "(7997) Westbound Traffic"
    },
    {
        value: 7998,
        text: "(7998) North"
    },
    {
        value: 7999,
        text: "(7999) South"
    },
    {
        value: 8000,
        text: "(8000) East"
    },
    {
        value: 8001,
        text: "(8001) West"
    },
    {
        value: 8002,
        text: "(8002) Northeast"
    },
    {
        value: 8003,
        text: "(8003) Northwest"
    },
    {
        value: 8004,
        text: "(8004) Southeast"
    },
    {
        value: 8005,
        text: "(8005) Southwest"
    },
    {
        value: 8229,
        text: "(8229) Bridge"
    },
    {
        value: 8230,
        text: "(8230) Overpass"
    },
    {
        value: 8231,
        text: "(8231) Elevated Lanes"
    },
    {
        value: 8232,
        text: "(8232) Underpass"
    },
    {
        value: 8233,
        text: "(8233) Tunnel"
    },
    {
        value: 8721,
        text: "(8721) Units of KPH"
    },
    {
        value: 13609,
        text: "(13609) Right Hand Curve"
    },
    {
        value: 13610,
        text: "(13610) Left Hand Curve"
    }
];