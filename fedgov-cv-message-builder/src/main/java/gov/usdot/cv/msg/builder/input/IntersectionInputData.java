package gov.usdot.cv.msg.builder.input;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import gov.usdot.cv.msg.builder.util.OffsetEncoding.OffsetEncodingType;
import gov.usdot.cv.rgaencoder.DDate;
import gov.usdot.cv.rgaencoder.DDateTime;
import gov.usdot.cv.rgaencoder.RGAData;

// import gov.usdot.cv.msg.builder.util.OffsetEncoding.OffsetEncodingType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IntersectionInputData {

	public enum GenerateType {
		ISD("ISD"),
		Map("Map"),
		RGA("RGA"), //NEW
		FramePlusMap("Frame+Map"),
		FramePlusRGA("Frame+RGA"), //NEW
		SPaT("SPaT"),
		FramePlusSPaT("Frame+SPaT"),
		SpatRecord("SpatRecord");
		
		private String type;
		
		private GenerateType(String type) {
			this.type = type;
		}
		
		public static GenerateType fromType(String type) {
			for(GenerateType generateType : values()) {
				if(generateType.type.contentEquals(type)) {
					return generateType;
				}
			}
			
			throw new IllegalArgumentException("No enum constant " + GenerateType.class.getCanonicalName() + "." + type);
		}
	}

	private static final Logger logger = LogManager.getLogger(IntersectionInputData.class);
	
	public static final short INVALID_ELEVATION = -4096;
	
	public long minuteOfTheYear;
	public MapData mapData;
	public String messageType = "Frame+Map";
	public OffsetEncodingType nodeOffsets = OffsetEncodingType.Standard;
	public boolean enableElevation = true;
	
	
	@Override
	public String toString() {
		return "MapDetails";
		//  [mapData=" + mapData +  ", messageType=" + messageType + " nodeOffsets=" + nodeOffsets 
		// 		+ " enableElevation=" + enableElevation + "]";
	}
	
	public GenerateType getGenerateType() {
		return GenerateType.fromType(messageType);
	}

	public void validatePoints() {
		ReferencePoint referencePoint = mapData.intersectionGeometry.referencePoint;
		VerifiedPoint verifiedPoint = mapData.intersectionGeometry.verifiedPoint;
		if (referencePoint == null) {
			throw new IllegalArgumentException("referencePoint cannot be null");
		} else {
			validateLat("referencePoint.referenceLat", referencePoint.referenceLat);
			validateLon("referencePoint.referenceLon", referencePoint.referenceLon);
		}
		if (verifiedPoint == null) {
			throw new IllegalArgumentException("verifiedPoint cannot be null");
		} else {
			validateLat("verifiedPoint.verifiedMapLat", verifiedPoint.verifiedMapLat);
			validateLon("verifiedPoint.verifiedMapLon", verifiedPoint.verifiedMapLon);
			validateLat("verifiedPoint.verifiedSurveyedLat", verifiedPoint.verifiedSurveyedLat);
			validateLon("verifiedPoint.verifiedSurveyedLon", verifiedPoint.verifiedSurveyedLon);
		}
	}
	
	public void validate() {
		validatePoints(); 
		
		Approach[] approaches = mapData.intersectionGeometry.laneList.approach;
		if (approaches == null) {
			throw new IllegalArgumentException("approaches cannot be null");
		}
		if (approaches.length == 0) {
			throw new IllegalArgumentException("approaches.approach contains no approaches");
		}
		for (int i=0; i<approaches.length; i++) {
			Approach approach = approaches[i];

			if(approach.approachType == null) {
				throw new IllegalArgumentException("Approach Type missing for Approach ID: " + approach.approachID);
			}

			if(approach.approachID != CrosswalkLane.CROSSWALK_APPROACH_ID) {
				if (approach.drivingLanes == null || approach.drivingLanes.length == 0) {
					throw new IllegalArgumentException("approach " + approach.approachID + " contains no drivingLanes");
				}
				for (int j=0; j<approach.drivingLanes.length; j++) {
					DrivingLane drivingLane = approach.drivingLanes[j];
					if(!drivingLane.isComputed) {
						if (drivingLane.laneNodes == null || drivingLane.laneNodes.length == 0) {
							throw new IllegalArgumentException("approach " + approach.approachID + 
									" lane " + drivingLane.laneID + " contains no laneNodes");
						}
						
						for (int k=0; k<drivingLane.laneNodes.length; k++) {
							LaneNode laneNode = drivingLane.laneNodes[k];
							validateLat("approach " + approach.approachID + " drivingLane " + 
									drivingLane.laneID + " laneNode " + laneNode.nodeNumber + " nodeLat", laneNode.nodeLat);
							validateLon("approach " + approach.approachID + " drivingLane " + 
									drivingLane.laneID + " laneNode " + laneNode.nodeNumber + " nodeLong", laneNode.nodeLong);
						}
					}
					else {
						// Computed Lane validation
					}
				}
			}
			else {
				// Pedestrian Crosswalk approach
				if(approach.crosswalkLanes != null || approach.crosswalkLanes.length != 0) {
					for(int j = 0; j < approach.crosswalkLanes.length; j++) {
						CrosswalkLane crosswalkLane = approach.crosswalkLanes[j];
						if(!crosswalkLane.isComputed) {
							if(crosswalkLane.laneNodes == null || crosswalkLane.laneNodes.length == 0) {
								throw new IllegalArgumentException("approach " + approach.approachID + 
										" crosswalkLane " + crosswalkLane.laneID + " contains no laneNodes");
							}
							
							for(int k = 0; k < crosswalkLane.laneNodes.length; k++) {
								LaneNode laneNode = crosswalkLane.laneNodes[k];
								validateLat("approach " + approach.approachID + " crosswalkLane " + 
										crosswalkLane.laneID + " laneNode " + laneNode.nodeNumber + " nodeLat", laneNode.nodeLat);
								validateLon("approach " + approach.approachID + " crosswalkLane " + 
										crosswalkLane.laneID + " laneNode " + laneNode.nodeNumber + " nodeLong", laneNode.nodeLong);
							}
						}
						else {
							// Computed Lane validation
						}
					}
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
		ReferencePoint referencePoint = mapData.intersectionGeometry.referencePoint;
		VerifiedPoint verifiedPoint = mapData.intersectionGeometry.verifiedPoint;
		double latOffset = verifiedPoint.verifiedSurveyedLat - verifiedPoint.verifiedMapLat;
		double lonOffset = verifiedPoint.verifiedSurveyedLon - verifiedPoint.verifiedMapLon;
		double elvOffset = verifiedPoint.verifiedSurveyedElevation - verifiedPoint.verifiedMapElevation;
		referencePoint.referenceLat += latOffset;
		referencePoint.referenceLon += lonOffset;
		referencePoint.referenceElevation += elvOffset;
		
		if(mapData.intersectionGeometry.laneList != null) {
			Approach[] intersectionApproaches = mapData.intersectionGeometry.laneList.approach; 
			for(int i = 0; i < intersectionApproaches.length; i++) {
				if(intersectionApproaches[i].approachID != CrosswalkLane.CROSSWALK_APPROACH_ID) {
					DrivingLane[] drivingLanes = intersectionApproaches[i].drivingLanes;
					for(int j = 0; j < drivingLanes.length; j++) {
						if(!drivingLanes[j].isComputed) {
							LaneNode[] laneNodes = drivingLanes[j].laneNodes;
							for(int k = 0; k < laneNodes.length; k++) {
								laneNodes[k].nodeLat += latOffset;
								laneNodes[k].nodeLong += lonOffset;
								laneNodes[k].nodeElev += elvOffset;				
							}
						}
					}
				}
				else {
					// Pedestrian Crosswalk approach
					CrosswalkLane[] crosswalkLanes = intersectionApproaches[i].crosswalkLanes;
					for(int j = 0; j < crosswalkLanes.length; j++) {
						if(!crosswalkLanes[j].isComputed) {
							LaneNode[] laneNodes = crosswalkLanes[j].laneNodes;
							for(int k = 0; k < laneNodes.length; k++) {
								laneNodes[k].nodeLat += latOffset;
								laneNodes[k].nodeLong += lonOffset;
								laneNodes[k].nodeElev += elvOffset;				
							}
						}
					}
				}
			}
		}
	}
	
	public static short convertElevation(double elevation) {
		if ( elevation < -409.5 || elevation > 6143.9 )
			return INVALID_ELEVATION; // unknown
		return (short)Math.round(elevation*10); // In units of 10 cm steps
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ReferencePoint {
		public String descriptiveIntersctionName;
		public int intersectionID;
		public String roadAuthorityIdType;
		public int[] roadAuthorityId;
		public int regionID;
		public int layerID;
		public int msgCount;
		public short masterLaneWidth;
		public double referenceLat;
		public double referenceLon;
		public double referenceElevation;
		
		public short getReferenceElevation() {
			return convertElevation(referenceElevation);
		}

		@Override
		public String toString() {
			return String.format("ReferencePoint [%s=%s,%s=%d,%s=%d,%s=%d,%s=%d,%s=%g,%s=%g,%s=%d,%s=%s,", 
					"descriptiveIntersctionName", descriptiveIntersctionName,
					"intersectionID", intersectionID,
					"roadAuthorityIdType", roadAuthorityIdType, 
					"roadAuthorityId", roadAuthorityId,
					"regionID", regionID,					
					"layerID", layerID,
					"msgCount", msgCount,
					"masterLaneWidth", masterLaneWidth,
					"referenceLat", referenceLat,
					"referenceLon", referenceLon,
					"referenceElevation", getReferenceElevation());
		}
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class VerifiedPoint {
		public double verifiedMapLat;
		public double verifiedMapLon;
		public double verifiedMapElevation;
		public double verifiedSurveyedLat;
		public double verifiedSurveyedLon;
		public double verifiedSurveyedElevation;
			
		public short getVerifiedMapElevation() {
			return convertElevation(verifiedMapElevation);
		}
		
		public short getVerifiedSurveyedElevation() {
			return convertElevation(verifiedSurveyedElevation);
		}

		@Override
		public String toString() {
			return "VerifiedPoint [verifiedMapLat=" + verifiedMapLat
					+ ", verifiedMapLon=" + verifiedMapLon
					+ ", verifiedMapElevation=" +  getVerifiedMapElevation()
					+ ", verifiedSurveyedLat=" + verifiedSurveyedLat
					+ ", verifiedSurveyedLaon=" + verifiedSurveyedLon
					+ ", verifiedSurveyedElevation=" + getVerifiedSurveyedElevation() 
					+ "]";
		}
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ReferencePointChild {
		public RegulatorySpeedLimit[] speedLimitType;	
	}
	
	// @JsonTypeName("speedLimitType") 
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class RegulatorySpeedLimit {
		public String speedLimitType;
		public double velocity;
		
		public short getVelocity() {
			// editor value is 0 - 366 mph, target value is 0 � 8191 0.02 m/s, with 8191 means unknown
			if ( velocity < 0 || velocity > 366.41017)
				return 8191; // unknown
			return (short)Math.round(22.352 * velocity);
		}

		@Override
		public String toString() {
			return String.format("RegulatorySpeedLimit [%s=%s,%s=%d,", 
					"speedLimitType", speedLimitType,					
					"velocity", getVelocity());
		}
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Approach {
		public String approachType;
		public short approachID;

		public String laneDirection;
		public DrivingLane[] drivingLanes;
		public CrosswalkLane[] crosswalkLanes;
		
		public Approach() {
			super();
		}


	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class DrivingLane {
		public String laneID;
		public String laneType;
		public String descriptiveName;
		public int[] typeAttributes;		
		public int[] sharedWith;
		public int[] laneManeuvers;
		public LaneConnection[] connections;
		public LaneNode[] laneNodes;
		public boolean isComputed;
		public ComputedLane computedLane;

		@Override
		public String toString() {
			String nodeListType = (isComputed) ? 
										("laneNodes=" + Arrays.toString(laneNodes)) :
										("copmutedLane=" + computedLane);
									
			return "DrivingLane [laneID=" + laneID 
					+ ", laneType=" + laneType 
					+ ", descriptiveName=" + descriptiveName
					+ ", typeAttributes=" + typeAttributes					
					+ ", sharedWith=" + sharedWith 
					+ ", laneManeuvers=" + Arrays.toString(laneManeuvers) 
					+ ", connections=" + Arrays.toString(connections)
					+ ", isComputed=" + isComputed
					+ ", " + nodeListType + "]";
		}
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class CrosswalkLane extends DrivingLane {
		public static final String CROSSWALK_APPROACH_TYPE = "Crosswalk";
		public static final short CROSSWALK_APPROACH_ID = -1;

		@Override
		public String toString() {
			String nodeListType = (isComputed) ? 
										("laneNodes=" + Arrays.toString(laneNodes)) :
										("copmutedLane=" + computedLane);
					
			return "CrosswalkLane [laneID=" + laneID 
					+ ", laneType=" + laneType
					+ ", descriptiveName=" + descriptiveName
					+ ", typeAttributes=" + typeAttributes
					+ ", sharedWith=" + sharedWith
					+ ", laneManeuvers=" + Arrays.toString(laneManeuvers)
					+ ", connections=" + Arrays.toString(connections)
					+ ", isComputed=" + isComputed
					+ ", " + nodeListType + "]";
		}
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class LaneConnection {
		public int toLane;
		public int signal_id;
		public int[] maneuvers;
		public int remoteID;
		public int connectionId;

		@Override
		public String toString() {
			return "DrivingLane [connectionId=" + connectionId
					+ ", toLane=" + toLane
					+ ", signal_id=" + signal_id
					+ ", remoteID=" + remoteID
					+ ", laneManeuvers=" + Arrays.toString(maneuvers)
					+ "]";
		}
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class LaneNode {
		public int nodeNumber;
		public double nodeLat;
		public double nodeLong;
		public double nodeElev;
		public short laneWidthDelta;
		public RegulatorySpeedLimit[] speedLimitType;
		
		@Override
		public String toString() {
			return "LaneNode [nodeNumber=" + nodeNumber + 
					", nodeLat=" + nodeLat + 
					", nodeLong=" + nodeLong + 
					", nodeElev=" + nodeElev +	
					", laneWidthDelta=" + laneWidthDelta + "]";
		}
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ComputedLane {
        public String referenceLaneID;
        public int offsetX;
        public int offsetY;
        public double rotation;
        public double scaleX;
        public double scaleY;
		
		@Override
		public String toString() {
			return "CopmutedLane [referenceLaneID=" + referenceLaneID +
					", offsetX=" + offsetX +
					", offsetY=" + offsetY +
					", rotation=" + rotation +
					", scaleX=" + scaleX +
					", scaleY=" + scaleY + "]";
		}
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class MapData {
		public int minuteOfTheYear;
		public TimeOfCalculation timeOfCalculation;
		public ContentDateTime contentDateTime;
		public int majorVer;
		public int minorVer;
		public int[] relativeToRdAuthID;
		public int contentVer;
		public String layerType;
		public IntersectionGeometry intersectionGeometry;
		public SpatData spatData;

		@Override
		public String toString() {
			return "MapData [minuteOfTheYear=" + minuteOfTheYear 
					+ ", timeOfCalculation=" + timeOfCalculation
					+ ", contentDateTime=" + contentDateTime
					+ ", majorVer=" + majorVer
					+ ", minorVer=" + minorVer
					+ ", relativeToRdAuthID" + relativeToRdAuthID
					+ ", contentVer=" + contentVer
					+ ", layerType=" + layerType 
					+ ",  intersectionGeometry=" + intersectionGeometry 
					+ ", spatData=" + spatData 	
					+ "]";
		}
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class TimeOfCalculation {
		public int day;
		public int month;
		public int year; 

		@Override
		public String toString() {
			return "TimeOfCalculation [day=" + day
					+ ", month=" + month
					+ ", year=" + year
					+ "]";
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ContentDateTime extends TimeOfCalculation {
		public int hour;
		public int minute;
		public int second; 

		@Override
		public String toString() {
			return "TimeOfCalculation [day=" + day
					+ ", month=" + month
					+ ", year=" + year
					+ ", hour=" + hour
					+ ", minute=" + minute
					+ ", second=" + second
					+ "]";
		}
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class IntersectionGeometry {
		public ReferencePoint referencePoint;
		public VerifiedPoint verifiedPoint;
		public ReferencePointChild referencePointChild;
		public LaneList laneList;
		
		@Override
		public String toString() {
			return "IntersectionGeometry [referencePoint=" + referencePoint 
					+ ", verifiedPoint=" + verifiedPoint 
					+ ", laneList=" + laneList 
					+ ", referencePointChild=" + referencePointChild 					
					+ "]";
		}		
	}
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class LaneList {
		public Approach[] approach;
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ComputedLaneList {
		public ComputedLane[] computedLanes;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class SpatData {
		public Intersection intersections;

		@Override
		public String toString() {
			return "SpatData [intersections=" + intersections + "]";
		}
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Intersection {
		public int id;
		public byte status;
		public State[] states;
		
		@Override
		public String toString() {
			return "Intersection [id=" + id + ", status=" + status
					+ ", states=" + Arrays.toString(states) + "]";
		}
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class State {
		public String laneSet;
		public int spatRevision;
		public int signalGroupID;
		public String signalPhase;
		public int startTime;
		public int minEndTime;
		public int maxEndTime;
		public int likelyTime;
		public int stateConfidence;
		public int nextTime;
		
		public int getSignalPhaseIndex() {
			if ( signalPhase != null && signalPhase.length() > 1 ) {
				char indexChar = signalPhase.charAt(1);
				if ( indexChar >= '0' && indexChar <= '9')
					return indexChar - '0';
			}
			return 0;
		}
		
		@Override
		public String toString() {
			return "State [laneSet=" + laneSet 
					+ ", signalGroupID=" + signalGroupID 
					+ ", signalPhase=" + signalPhase 
					+ ", startTime=" + startTime 
					+ ", minEndTime=" + minEndTime 
					+ ", maxEndTime=" + maxEndTime 
					+ ", likelyTime=" + likelyTime 					
					+ ", stateConfidence=" + stateConfidence
					+ ", nextTime=" + nextTime + "]";
		}
	}
}
