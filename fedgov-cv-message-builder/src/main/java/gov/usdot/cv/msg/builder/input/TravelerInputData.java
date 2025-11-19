package gov.usdot.cv.msg.builder.input;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import gov.usdot.cv.msg.builder.util.GeoPoint;
import gov.usdot.cv.msg.builder.util.JSONMapper;
import gov.usdot.cv.msg.builder.util.OffsetEncoding.OffsetEncodingType;

public class TravelerInputData {

	private static final List<Long> validMutcds = Arrays.asList(0L, 1L, 2L, 3L, 4L, 5L, 6L);
	private static final List<Long> validInfoTypes = Arrays.asList(0L, 1L, 2L, 3L);
	private static final List<Long> validTTLs = Arrays.asList(0L, 1L, 2L, 3L, 4L, 5L);
	private static final List<Long> validExtents = Arrays.asList(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L,13L, 14L, 15L);
	
	public enum GenerateType {
		ASD("ASD"),
		TIM("TIM"),
		FramePlusTIM("Frame+TIM");

		private String type;

		private GenerateType(String type) {
			this.type = type;
		}

		public static GenerateType fromType(String type) {
			for (GenerateType generateType : values()) {
				if (generateType.type.contentEquals(type)) {
					return generateType;
				}
			}

			throw new IllegalArgumentException(
					"No enum constant " + GenerateType.class.getCanonicalName() + "." + type);
		}
	}

	private static final Logger logger = LogManager.getLogger(TravelerInputData.class);
	private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");

	public Region[] regions;
	public AnchorPoint anchorPoint;
	public VerifiedPoint verifiedPoint;
	public Deposit deposit;
	public ApplicableRegion applicableRegion;
	public String messageType = "TIM";
	public OffsetEncodingType nodeOffsets = OffsetEncodingType.Standard;
	public boolean enableElevation = true;

	// @Override
	// public String toString() {
	// return "TravelerInputData [regions=" + Arrays.toString(regions)
	// + ", anchorPoint=" + anchorPoint + ", verifiedPoint="
	// + verifiedPoint + ", deposit=" + deposit + ", messageType="
	// + messageType + " nodeOffsets=" + nodeOffsets
	// + " enableElevation=" + enableElevation + "]";
	// }

	public GenerateType getGenerateType() {
		return GenerateType.fromType(messageType);
	}

/*
 * Validates TIM message inputs (region types, coordinates,
 * MUTCD codes, start/end times, infoType, TTL, etc.).
 * Throws IllegalArgumentException if any value is invalid.
 */
	public void validate() {
		if (regions == null || regions.length == 0) {
			throw new IllegalArgumentException("regions cannot be null/empty");
		} else {
			for (Region region : regions) {
				if (region.regionType == null) {
					throw new IllegalArgumentException("regionType is required");
				} else {
					if (!region.regionType.equals("lane") && !region.regionType.equals("region")
							&& !region.regionType.equals("circle")) {
						throw new IllegalArgumentException("regionType must be either 'lane','region', or 'circle'");
					}
				}
				if (region.extent != -1) {
					if (!validExtents.contains((long) region.extent)) {
						throw new IllegalArgumentException("Invalid extent value: " +
								region.extent + " valid values are: " + validExtents);
					}
				}

				if (region.laneNodes != null) {
					for (LaneNode laneNode : region.laneNodes) {
						validateLat("nodeNumber " + laneNode.nodeNumber + " nodeLat",
								laneNode.nodeLat);
						validateLon("nodeNumber " + laneNode.nodeNumber + " nodeLong",
								laneNode.nodeLong);
					}
				}
			}
		}
		if (anchorPoint == null) {
			throw new IllegalArgumentException("anchorPoint cannot be null");
		} else {
			validateLat("anchorPoint.referenceLat", anchorPoint.referenceLat);
			validateLon("anchorPoint.referenceLon", anchorPoint.referenceLon);

			if (anchorPoint.content == null || anchorPoint.content.length == 0)
				throw new IllegalArgumentException("content cannot be null/empty");

			if (!validMutcds.contains((long) anchorPoint.mutcd)) {
				throw new IllegalArgumentException("Invalid mutcd value: " +
						anchorPoint.mutcd + " valid values are: " + validMutcds);
			}

			Date startDate = null;
			if (anchorPoint.startTime == null || anchorPoint.startTime.isEmpty()) {
				throw new IllegalArgumentException("startTime cannot be null/empty");
			} else {
				try {
					startDate = sdf.parse(anchorPoint.startTime);
				} catch (ParseException e) {
					throw new IllegalArgumentException(
							"startTime must use the format \"MM/dd hh:mm a yyyy\" error: " + e);
				}
			}

			Date endDate = null;
			if (anchorPoint.endTime == null || anchorPoint.endTime.isEmpty()) {
				throw new IllegalArgumentException("endTime cannot be null/empty");
			} else {
				try {
					endDate = sdf.parse(anchorPoint.endTime);
				} catch (ParseException e) {
					throw new IllegalArgumentException(
							"endTime must use the format \"MM/dd hh:mm a yyyy\" error: " + e);
				}
			}

			if (startDate != null && endDate != null) {
				if (startDate.after(endDate)) {
					throw new IllegalArgumentException("startTime must be before endTime");
				}
			}

			if (!validInfoTypes.contains((long) anchorPoint.infoType)) {
				throw new IllegalArgumentException("Invalid infoType value: " +
						anchorPoint.infoType + " valid values are: " + validInfoTypes);
			}
		}
		if (verifiedPoint == null) {
			throw new IllegalArgumentException("verifiedPoint cannot be null");
		} else {
			validateLat("verifiedPoint.verifiedMapLat", verifiedPoint.verifiedMapLat);
			validateLon("verifiedPoint.verifiedMapLon", verifiedPoint.verifiedMapLon);
			validateLat("verifiedPoint.verifiedSurveyedLat",
					verifiedPoint.verifiedSurveyedLat);
			validateLon("verifiedPoint.verifiedSurveyedLon",
					verifiedPoint.verifiedSurveyedLon);
		}

		if (deposit != null) {

			if (deposit.timeToLive != -1) {
				if (!validTTLs.contains((long) deposit.timeToLive)) {
					throw new IllegalArgumentException("Invalid timeToLive value: " +
							deposit.timeToLive + " valid values are: " + validTTLs);
				}
			}
		}
	}

	private void validateLat(String name, double lat) {
		// 0.0 is invalid for our purposes, catches uninitialized values
		if (lat == 0.0)
			throw new IllegalArgumentException(name + " is required");
		if (lat < -90.0 | lat > 90.0)
			throw new IllegalArgumentException(name + " " + lat + " is not a valid Latitude value");
	}

	private void validateLon(String name, double lon) {
		// 0.0 is invalid for our purposes, catches uninitialized values
		if (lon == 0.0)
			throw new IllegalArgumentException(name + " is required");
		if (lon < -180.0 | lon > 180.0)
			throw new IllegalArgumentException(name + " " + lon + " is not a valid Longitude value");
	}

	public void applyLatLonOffset() {
		double latOffset = verifiedPoint.verifiedSurveyedLat - verifiedPoint.verifiedMapLat;
		double lonOffset = verifiedPoint.verifiedSurveyedLon - verifiedPoint.verifiedMapLon;
		double elevOffset = verifiedPoint.verifiedSurveyedElevation - verifiedPoint.verifiedMapElevation;
		logger.debug("latOffset: " + latOffset);
		logger.debug("lonOffset: " + lonOffset);
		logger.debug("elevOffset: " + elevOffset);

		anchorPoint.referenceLat += latOffset;
		anchorPoint.referenceLon += lonOffset;
		anchorPoint.referenceElevation += elevOffset;
		for (Region region : regions) {
			if (region.laneNodes != null) {
				for (LaneNode laneNode : region.laneNodes) {
					laneNode.nodeLat += latOffset;
					laneNode.nodeLong += lonOffset;
					laneNode.nodeElevation += elevOffset;
				}
			}
		}
	}

	public void initialzeReferencePoints() {
		GeoPoint point = new GeoPoint(anchorPoint.referenceLat, anchorPoint.referenceLon);
		for (Region region : regions) {
			if (region.laneNodes != null)
				region.refPoint = point;
		}
	}

	public static class LaneNode {
		public final static int circleCenter = 0;
		public final static int circleEdge = 1;

		public int nodeNumber;
		public double nodeLat;
		public double nodeLong;
		public double nodeElevation;
		public short laneWidth; // lane width delta -512 to 511

		@Override
		public String toString() {
			return "LaneNode [nodeNumber=" + nodeNumber
					+ ", nodeLat=" + nodeLat
					+ ", nodeLong=" + nodeLong
					+ ", nodeElevation=" + nodeElevation
					+ ", laneWidth=" + laneWidth
					+ "]";
		}
	}

	public static class Region {
		public String regionType;
		public double radius;
		public LaneNode[] laneNodes;
		public int extent = -1;
		public GeoPoint refPoint;

		@Override
		public String toString() {
			return "Region [regionType=" + regionType
					+ ", radius=" + radius
					+ ", laneNodes=" + Arrays.toString(laneNodes)
					+ ", extent=" + extent
					+ ", refPoint=" + refPoint
					+ "]";
		}
	}

	public static class ItisContent {
		public String[] codes;
		public String text;

		public boolean hasCodes() {
			return codes != null && codes.length > 0;
		}

		public boolean hasText() {
			return text != null && text.trim().length() > 0;
		}

		public String getText(int maxLength) {
			if (!hasText())
				return "";
			if (text.length() <= maxLength)
				return text;
			return text.substring(0, maxLength - 1);
		}

		@Override
		public String toString() {
			return "ItisContent [codes=" + Arrays.toString(codes)
					+ ", text=" + text
					+ "]";
		}
	}

	public static class AnchorPoint {
		public String name;
		public double referenceLat;
		public double referenceLon;
		public double referenceElevation;
		public short masterLaneWidth;
		public short sspTimRights;
		public long packetID;
		public ItisContent[] content;
		public short sspTypeRights;
		public short sspContentRights;
		public short sspLocationRights;
		public int direction;
		public int mutcd;
		public int priority;
		public String startTime;
		public String endTime;
		public int[] heading;
		public int infoType;

		/* DescriptionOfRoadSurface */
		public String road_surface; // material of the road surface
		public int road_surface_type;
		/* RoadSurfaceCondition */
		public int road_condition; // dry or wet
		/* RoadRoughness Values */
		public long meanVerticalVariation;
		public long verticalVariationStdDev;
		public long meanHorizontalVariation;
		public long horizontalVariationStdDev;

		public short getReferenceElevation() {
			return IntersectionInputData.convertElevation(referenceElevation);
		}

		@Override
		public String toString() {
			return "AnchorPoint [" +
					"name=" + name +
					", referenceLat=" + referenceLat +
					", referenceLon=" + referenceLon +
					", referenceElevation=" + referenceElevation +
					", masterLaneWidth=" + masterLaneWidth +
					", sspTimRights=" + sspTimRights +
					", sspTypeRights=" + sspTypeRights +
					", sspContentRights=" + sspContentRights +
					", sspLocationRights=" + sspLocationRights +
					", packetID=" + packetID +
					", content=" + Arrays.toString(content) +
					", direction=" + direction +
					", mutcd=" + mutcd +
					", priority=" + priority +
					", startTime=" + startTime +
					", endTime=" + endTime +
					", heading=" + Arrays.toString(heading) +
					", infoType=" + infoType +
					", road_surface=" + road_surface +
					", road_surface_type=" + road_surface_type +
					", road_condition=" + road_condition +
					", meanVerticalVariation=" + meanVerticalVariation +
					", verticalVariationStdDev=" + verticalVariationStdDev +
					", meanHorizontalVariation=" + meanHorizontalVariation +
					", horizontalVariationStdDev=" + horizontalVariationStdDev +
					"]";
		}

	}

	public static class VerifiedPoint {
		public double verifiedMapLat;
		public double verifiedMapLon;
		public double verifiedMapElevation;
		public double verifiedSurveyedLat;
		public double verifiedSurveyedLon;
		public double verifiedSurveyedElevation;

		public short getVerifiedMapElevation() {
			return IntersectionInputData.convertElevation(verifiedMapElevation);
		}

		public short getVerifiedSurveyedElevation() {
			return IntersectionInputData.convertElevation(verifiedSurveyedElevation);
		}

		@Override
		public String toString() {
			return "VerifiedPoint [verifiedMapLat=" + verifiedMapLat
					+ ", verifiedMapLon=" + verifiedMapLon
					+ ", verifiedMapElevation=" + getVerifiedMapElevation()
					+ ", verifiedSurveyedLat=" + verifiedSurveyedLat
					+ ", verifiedSurveyedLon=" + verifiedSurveyedLon
					+ ", verifiedSurveyedElevation=" + getVerifiedSurveyedElevation()
					+ "]";
		}
	}

	public static class Deposit {
		public String systemName;
		public short timeToLive = -1;

		@Override
		public String toString() {
			return "Deposit [systemName=" + systemName + ", timeToLive=" + timeToLive + "]";
		}
	}

	public static class ApplicableRegion {
		public double nwLat;
		public double nwLon;
		public double seLat;
		public double seLon;

		@Override
		public String toString() {
			return "Deposit [nwLat=" + nwLat
					+ ", nwLon=" + nwLon + ", seLat=" + seLat + ", seLon="
					+ seLon + "]";
		}
	}

	public static void main(String args[]) throws JsonParseException, JsonMappingException, IOException {
		String travJson = "{\r\n    \"regions\": [\r\n        {\r\n            \"laneWidth\": \"366\",\r\n            \"laneNodes\": [\r\n                {\r\n                    \"nodeNumber\": 0,\r\n                    \"nodeLat\": 42.33757684267676,\r\n                    \"nodeLong\": -83.05125328295235\r\n                },\r\n                {\r\n                    \"nodeNumber\": 1,\r\n                    \"nodeLat\": 42.33688687290945,\r\n                    \"nodeLong\": -83.05078657858425\r\n                }\r\n            ],\r\n            \"extent\": \"5\"\r\n        }\r\n    ],\r\n    \"anchorPoint\": {\r\n        \"name\": \"Work Zone\",\r\n        \"referenceLat\": 42.337656942112716,\r\n        \"referenceLon\": -83.05142065277923,\r\n        \"referenceElevation\": \"184\",\r\n        \"content\": \"testing\",\r\n        \"direction\": \"0\",\r\n        \"mutcd\": \"2\",\r\n        \"priority\": \"5\",\r\n        \"startTime\": \"05/26/2015 10:56 AM\",\r\n        \"endTime\": \"05/30/2015 10:56 AM\",\r\n        \"heading\": [\r\n            \"0001\",\r\n            \"0080\",\r\n            \"0100\",\r\n            \"8000\"\r\n        ],\r\n        \"infoType\": \"2\"\r\n    },\r\n    \"verifiedPoint\": {\r\n        \"verifiedMapLat\": 42.33791859880715,\r\n        \"verifiedMapLon\": -83.05089362151372,\r\n        \"verifiedMapElevation\": 180,\r\n        \"verifiedSurveyedLat\": \"42.13791859880715\",\r\n        \"verifiedSurveyedLon\": -83.01089362151372,\r\n        \"verifiedSurveyedElevation\": \"184\"\r\n    }\r\n}";
		TravelerInputData trav = JSONMapper.jsonStringToPojo(travJson, TravelerInputData.class);
		System.out.println(trav);
	}

}
