/*
 * Copyright (C) 2024 LEIDOS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package gov.usdot.cv.msg.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.DecoderException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.usdot.cv.msg.builder.exception.MessageBuildException;
import gov.usdot.cv.msg.builder.exception.MessageEncodeException;
import gov.usdot.cv.msg.builder.input.IntersectionInputData;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.Approach;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.ContentDateTime;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.LaneConnection;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.CrosswalkLane;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.DrivingLane;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.GenerateType;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.LaneNode;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.ReferencePoint;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.ReferencePointChild;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.SpatData;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.State;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.TimeOfCalculation;
import gov.usdot.cv.msg.builder.message.IntersectionMessage;
import gov.usdot.cv.msg.builder.util.BitStringHelper;
import gov.usdot.cv.msg.builder.util.GeoPoint;
import gov.usdot.cv.msg.builder.util.J2735Helper;
import gov.usdot.cv.msg.builder.util.J2945Helper;
import gov.usdot.cv.msg.builder.util.JSONMapper;
import gov.usdot.cv.msg.builder.util.OffsetEncoding;
import gov.usdot.cv.msg.builder.util.OffsetEncoding.OffsetEncodingSize;
import gov.usdot.cv.msg.builder.util.OffsetEncoding.OffsetEncodingType;
import gov.usdot.cv.rgaencoder.BaseLayer;
import gov.usdot.cv.rgaencoder.DDate;
import gov.usdot.cv.rgaencoder.DDateTime;
import gov.usdot.cv.rgaencoder.RGAData;
import gov.usdot.cv.mapencoder.AllowedManeuvers;
import gov.usdot.cv.mapencoder.ComputedLane;
import gov.usdot.cv.mapencoder.ConnectingLane;
import gov.usdot.cv.mapencoder.Connection;
import gov.usdot.cv.mapencoder.Encoder;
import gov.usdot.cv.mapencoder.GenericLane;
import gov.usdot.cv.mapencoder.MapData;
import gov.usdot.cv.mapencoder.NodeAttributeSetXY;
import gov.usdot.cv.mapencoder.NodeListXY;
import gov.usdot.cv.mapencoder.NodeOffsetPointXY;
import gov.usdot.cv.mapencoder.NodeSetXY;
import gov.usdot.cv.mapencoder.NodeXY;
import gov.usdot.cv.mapencoder.NodeXY20b;
import gov.usdot.cv.mapencoder.NodeXY22b;
import gov.usdot.cv.mapencoder.OffsetXaxis;
import gov.usdot.cv.mapencoder.OffsetYaxis;
import gov.usdot.cv.mapencoder.IntersectionGeometry;
import gov.usdot.cv.mapencoder.IntersectionReferenceID;
import gov.usdot.cv.mapencoder.LaneAttributes;
import gov.usdot.cv.mapencoder.LaneAttributesBarrier;
import gov.usdot.cv.mapencoder.LaneAttributesBike;
import gov.usdot.cv.mapencoder.LaneAttributesCrosswalk;
import gov.usdot.cv.mapencoder.LaneAttributesParking;
import gov.usdot.cv.mapencoder.LaneAttributesSidewalk;
import gov.usdot.cv.mapencoder.LaneAttributesStriping;
import gov.usdot.cv.mapencoder.LaneAttributesTrackedVehicle;
import gov.usdot.cv.mapencoder.LaneAttributesVehicle;
import gov.usdot.cv.mapencoder.LaneDataAttribute;
import gov.usdot.cv.mapencoder.LaneDataAttributeList;
import gov.usdot.cv.mapencoder.LaneDirection;
import gov.usdot.cv.mapencoder.LaneList;
import gov.usdot.cv.mapencoder.LaneSharing;
import gov.usdot.cv.mapencoder.LaneTypeAttributes;
import gov.usdot.cv.mapencoder.Position3D;
import gov.usdot.cv.mapencoder.RegulatorySpeedLimit;
import gov.usdot.cv.mapencoder.SpeedLimitList;
import gov.usdot.cv.mapencoder.SpeedLimitType;

@Path("/messages/intersection")
public class IntersectionSituationDataBuilder {

	public static int requestId = 0;

	private static final Logger logger = LogManager.getLogger(IntersectionSituationDataBuilder.class);

	// Common bit string variables
	private static final int SMALL_BIT_STRING = 0b00000000; // An 8-bit binary string
	private static final int SMALL_BIT_STRING_LENGTH = 8; // Length of the 8-bit binary string
	private static final int LONG_BIT_STRING = 0b0000000000000000; // A 16-bit binary string
	private static final int LONG_BIT_STRING_LENGTH = 16; // Length of the 16-bit binary string

	// TODO: temporarily commented out
	// @GET
	// @Produces(MediaType.APPLICATION_JSON)
	// public IntersectionMessage build() {
	// IntersectionMessage im = new IntersectionMessage();
	// IntersectionSituationData isd = new IntersectionSituationData();
	// try {
	// // im.setHexString(J2735Helper.getHexString(isd));
	// im.setReadableString(isd.toString());
	// } catch (Exception e) {
	// logger.error("Error encoding MapData ", e);
	// throw new MessageEncodeException(e.toString());
	// }
	// return im;
	// }

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	public IntersectionMessage build(String intersectionData) {
		IntersectionMessage im = new IntersectionMessage();
		logger.debug("User Input: " + intersectionData);
		MapData md = null;
		RGAData rd = null;

		// TODO: temporarily commented out
		// SpatRecord sr = null;
		// SPAT spat = null;
		// IntersectionSituationData isd = null;
		// MessageFrame mf = null;
		// GenerateType generateType = GenerateType.ISD;
		GenerateType generateType = GenerateType.FramePlusMap;
		IntersectionInputData isdInputData = new IntersectionInputData();
		try {
			isdInputData = JSONMapper.jsonStringToPojo(intersectionData,
					IntersectionInputData.class);
			generateType = isdInputData.getGenerateType();
			logger.debug("generateType: " + generateType);
			
			
			// TODO: temporarily commented out
			// sr = buildSpatRecord(isdInputData);
			// isd = buildISD(isdInputData, md, sr);
			// spat = buildSPAT(sr);
			// if(generateType.equals(GenerateType.FramePlusMap)) {
			// mf = buildMessageFramePlusMap(md);
			// }
			// else if(generateType.equals(GenerateType.FramePlusSPaT)) {
			// mf = buildMessageFramePlusSPaT(spat);
			// }
		} catch (Exception e) {
			logger.error("Error parsing MapData ", e);
			throw new MessageBuildException(e.toString());
		}

		try {
			String hexString = "00";
			isdInputData.applyLatLonOffset();
			String readableString = "Unexpected type: " + generateType;
			switch (generateType) {
				case ISD:
					break;
				case Map:
					isdInputData.validate();
					md = buildMapData(isdInputData);
					logger.debug("in MAP: " );
					// Removing the first 8 characters from the MessageFrame provides the MAP message
					// This was tested manually by removing the characters from MessageFrame and testing using the decoder
					hexString = (J2735Helper.getHexString(md)).substring(8);
					readableString = md.toString();
					break;
				case RGA: 
					isdInputData.validatePoints();
					rd = buildRGAData(isdInputData);
				 	logger.debug("in RGA: " );
					hexString = J2945Helper.getHexString(rd).substring(8);
					readableString = rd.toString();
					break;
				case SPaT:
					break;
				case FramePlusMap:
					isdInputData.validate();
					md = buildMapData(isdInputData);
					logger.debug("in FramePlusMap: ");
					hexString = J2735Helper.getHexString(md);
					readableString = md.toString();
					break;
				case FramePlusRGA:
					isdInputData.validatePoints();
					rd = buildRGAData(isdInputData);
					logger.debug("in RGA: " );
					hexString = J2945Helper.getHexString(rd);
					readableString = rd.toString();
					break;
				case FramePlusSPaT :
					break;
				case SpatRecord:
					break; 
			}
			im.setHexString(hexString);
			im.setReadableString(readableString);
			logger.debug("readableString output: " + readableString);
			logger.debug("Encoded hexString output: " + hexString);
		} catch (Exception e) {
			logger.error("Error encoding MapData ", e);
			throw new MessageEncodeException(e.toString());
		}
		return im;
	}

	private int getRequestId() {
		if (requestId >= Integer.MAX_VALUE) {
			requestId = 0;
		}
		return ++requestId;
	}

	private MapData buildMapData(IntersectionInputData isdInputData) {
		MapData mapData = new MapData();
		mapData.setTimeStamp((long) isdInputData.mapData.minuteOfTheYear);
		mapData.setMsgIssueRevision((byte) isdInputData.mapData.intersectionGeometry.referencePoint.msgCount);
		mapData.setLayerType(getLayerType(isdInputData.mapData.layerType));
		mapData.setLayerID(isdInputData.mapData.intersectionGeometry.referencePoint.layerID);
		mapData.setIntersections(buildIntersections(isdInputData));
		return mapData;
	}

	
	private RGAData buildRGAData(IntersectionInputData isdInputData) {
		RGAData rgaData = new RGAData();
		rgaData.setBaseLayer(buildBaseLayer(isdInputData));
		return rgaData;
	}

	public BaseLayer buildBaseLayer(IntersectionInputData isdInputData) {
		BaseLayer baseLayer = new BaseLayer();
		ReferencePoint referencePoint = isdInputData.mapData.intersectionGeometry.referencePoint;
		TimeOfCalculation timeOfCalculation = isdInputData.mapData.timeOfCalculation;
		ContentDateTime contentDateTime = isdInputData.mapData.contentDateTime;

		//DataSetFormatVersionInfo
		baseLayer.setMajorVer(isdInputData.mapData.majorVer);
		baseLayer.setMinorVer(isdInputData.mapData.minorVer);

		//ReferencePointInfo
		Position3D position3d = new Position3D();
		position3d.setLongitude(J2735Helper.convertGeoCoordinateToInt(referencePoint.referenceLon));
		position3d.setLatitude(J2735Helper.convertGeoCoordinateToInt(referencePoint.referenceLat));
		if (referencePoint.referenceElevation != 0.00) {
			position3d.setElevationExists(true);
			position3d.setElevation((float) referencePoint.getReferenceElevation());
		} else {
			position3d.setElevationExists(false);
		}
		baseLayer.setLocation(position3d);

		DDate dDate = new DDate();
		dDate.setDay(timeOfCalculation.day);
		dDate.setMonth(timeOfCalculation.month);
		dDate.setYear(timeOfCalculation.year);
		baseLayer.setTimeOfCalculation(dDate);

		//RoadGeometryRefIDInfo
		baseLayer.setRelativeToRdAuthID(isdInputData.mapData.relativeToRdAuthID);

		if(referencePoint.roadAuthorityIdType != null) {
			if (referencePoint.roadAuthorityIdType.replaceAll("\\s","").toLowerCase().equals("full")) {
				baseLayer.setRelRdAuthIDExists(false);
				baseLayer.setFullRdAuthIDExists(true);
				baseLayer.setFullRdAuthID(referencePoint.roadAuthorityId);
			} else if (referencePoint.roadAuthorityIdType.replaceAll("\\s","").toLowerCase().equals("relative")) {
				baseLayer.setFullRdAuthIDExists(false);
				baseLayer.setRelRdAuthIDExists(true);
				baseLayer.setRelRdAuthID(referencePoint.roadAuthorityId);
			} else {
				baseLayer.setFullRdAuthIDExists(false);
				baseLayer.setRelRdAuthIDExists(false);
			}
		}	

		//DataSetContentIdentification
		baseLayer.setContentVer(isdInputData.mapData.contentVer);

		DDateTime dDateTime = new DDateTime();
		dDateTime.setHour(contentDateTime.hour);
		dDateTime.setMinute(contentDateTime.minute);
		dDateTime.setSecond(contentDateTime.second);
		dDateTime.setDay(timeOfCalculation.day);
		dDateTime.setMonth(timeOfCalculation.month);
		dDateTime.setYear(timeOfCalculation.year);
		baseLayer.setContentDateTime(dDateTime);
		
		return baseLayer;
	}

	public IntersectionGeometry[] buildIntersections(IntersectionInputData isdInputData) {
		/*
		 * Currently this is hardcoded to 1 since the IntersectionInputData sent from
		 * the UI contains
		 * a IntersectionGeometry as an object and not an array. This needs to be
		 * addressed in the future.
		 */
		IntersectionGeometry[] intersections = new IntersectionGeometry[1];
		IntersectionGeometry intersection = new IntersectionGeometry();
		ReferencePoint referencePoint = isdInputData.mapData.intersectionGeometry.referencePoint;
		ReferencePointChild referencePointChild = isdInputData.mapData.intersectionGeometry.referencePointChild;
		
		// Set Intersection Name
		intersection.setName(referencePoint.descriptiveIntersctionName);
		
		// Set Intersection ID
		IntersectionReferenceID intersectionReferenceID = new IntersectionReferenceID();
		intersectionReferenceID.setId(referencePoint.intersectionID);	
		if (referencePoint.regionID != 0) {
			intersectionReferenceID.setRegionExists(true);
			intersectionReferenceID.setRegion(referencePoint.regionID);
		} else {
			intersectionReferenceID.setRegionExists(false);
		}
	
		intersection.setId(intersectionReferenceID);

		if(referencePoint.roadAuthorityIdType != null) {
			if (referencePoint.roadAuthorityIdType.replaceAll("\\s","").toLowerCase().equals("full")) {
				intersection.setRelRdAuthIDExists(false);
				intersection.setFullRdAuthIDExists(true);
				intersection.setFullRdAuthID(referencePoint.roadAuthorityId);
			} else if (referencePoint.roadAuthorityIdType.replaceAll("\\s","").toLowerCase().equals("relative")) {
				intersection.setFullRdAuthIDExists(false);
				intersection.setRelRdAuthIDExists(true);
				intersection.setRelRdAuthID(referencePoint.roadAuthorityId);
			} else {
				intersection.setFullRdAuthIDExists(false);
				intersection.setRelRdAuthIDExists(false);
			}
		}	
		
		// Set Intersection Revision
		intersection.setRevision(referencePoint.msgCount);

		// Set Position RefPoint
		Position3D position3d = new Position3D();
		position3d.setLongitude(J2735Helper.convertGeoCoordinateToInt(referencePoint.referenceLon));
		position3d.setLatitude(J2735Helper.convertGeoCoordinateToInt(referencePoint.referenceLat));
		if (referencePoint.referenceElevation != 0.00) {
			position3d.setElevationExists(true);
			position3d.setElevation((float) referencePoint.getReferenceElevation());
		} else {
			position3d.setElevationExists(false);
		}
		intersection.setRefPoint(position3d);

		// Set LaneWidth
		intersection.setLaneWidthExists(true);
		intersection.setLaneWidth(referencePoint.masterLaneWidth);

		// Set Speed Limits
		if(referencePointChild != null && referencePointChild.speedLimitType != null && referencePointChild.speedLimitType.length > 0) {
			SpeedLimitList speedLimitList = new SpeedLimitList();
			int speedLimitListLength = referencePointChild.speedLimitType.length;
			RegulatorySpeedLimit[] regulatorySpeedLimits = new RegulatorySpeedLimit[speedLimitListLength];
			for(int regIndex = 0; regIndex < speedLimitListLength; regIndex++) {
				RegulatorySpeedLimit regulatorySpeedLimit = new RegulatorySpeedLimit();
				short currentVelocity = referencePointChild.speedLimitType[regIndex].getVelocity();
				regulatorySpeedLimit.setType(getSpeedLimitType(referencePointChild.speedLimitType[regIndex].speedLimitType));
				regulatorySpeedLimit.setSpeed(currentVelocity);
				regulatorySpeedLimits[regIndex] = regulatorySpeedLimit;
			}
			intersection.setSpeedLimitsExists(true);
			speedLimitList.setSpeedLimits(regulatorySpeedLimits);
			intersection.setSpeedLimits(speedLimitList);
		} else {
			intersection.setSpeedLimitsExists(false);
		}

		Approach[] approaches = isdInputData.mapData.intersectionGeometry.laneList.approach;
		OffsetEncoding offsetEncoding = new OffsetEncoding(isdInputData.nodeOffsets);
		// Set Laneset
		intersection.setLaneSet(buildLaneList(isdInputData, approaches, referencePoint, offsetEncoding));

		intersections[0] = intersection;
		return intersections;
	}

	// This function builds and returns the LaneList required for the LaneSet
	private LaneList buildLaneList(IntersectionInputData isdInputData, Approach[] approaches, ReferencePoint referencePoint, OffsetEncoding offsetEncoding) {
		LaneList lanes = new LaneList();

		if(offsetEncoding.type != OffsetEncodingType.Tight) {
			offsetEncoding.size = getOffsetEncodingSize(offsetEncoding.type, approaches, referencePoint);
		}
		
		int laneCount = 0;
		int laneCounter = 0;

		// Count number of lanes
		for (int k = 0; k < approaches.length; k++) {
			Approach approach = approaches[k];
			if (approach.drivingLanes != null) {
				laneCount += approach.drivingLanes.length;
			}

			if (approach.crosswalkLanes != null) {
				laneCount += approach.crosswalkLanes.length;
			}
		}

		GenericLane[] genericLanes = new GenericLane[laneCount];
		
		// Loop through all approaches
		for (int i = 0; i < approaches.length; i++) {
			Approach approach = approaches[i];

			// Check if an approach is not a crosswalk and there exists at least one driving lane
			if (approach.approachID != IntersectionInputData.CrosswalkLane.CROSSWALK_APPROACH_ID && approach.drivingLanes != null && approach.drivingLanes.length > 0) {
				// Loop through all the driving lanes for each approach
				for (int j = 0; j < approach.drivingLanes.length; j++) {
					DrivingLane drivingLane = approach.drivingLanes[j];
					GenericLane lane = new GenericLane();
					int laneDirectionBitString = SMALL_BIT_STRING;

					// Set LaneID
					lane.setLaneID(Integer.valueOf(drivingLane.laneID));

					// Set Lane Name
					if(drivingLane.descriptiveName != null && !drivingLane.descriptiveName.isEmpty()) {
						lane.setNameExists(true);
						lane.setName(drivingLane.descriptiveName);
					}

					if (approach.approachType == null) {
						approach.approachType = "None";
					}

					switch (approach.approachType.toLowerCase()) {
						case "ingress":
							laneDirectionBitString = 0b10000000;
							lane.setIngressApproach((byte) approach.approachID);
							lane.setIngressApproachExists(true);
							break;
						case "egress":
							laneDirectionBitString = 0b01000000;
							lane.setEgressApproach((byte) approach.approachID);
							lane.setEgressApproachExists(true);
							break;
						case "both":
							laneDirectionBitString = 0b11000000;
							lane.setIngressApproach((byte) approach.approachID);
							lane.setEgressApproach((byte) approach.approachID);
							lane.setIngressApproachExists(true);
							lane.setEgressApproachExists(true);
							break;
						case "none":
							lane.setIngressApproachExists(false);
							lane.setEgressApproachExists(false);
							break;
						default:
							break;
					}

					// Set LaneAttributes to Lane
					lane.setLaneAttributes(buildLaneAttributes(drivingLane, laneDirectionBitString));

					// Set Maneuvers
					if(drivingLane.laneManeuvers != null && drivingLane.laneManeuvers.length > 0) {
						lane.setManeuversExists(true);
						lane.setManeuvers(buildAllowedManeuvers(drivingLane.laneManeuvers));
					}

					// Set NodeList Choice to Lane;
					lane.setNodeList(buildNodeList(isdInputData, drivingLane, referencePoint, offsetEncoding));

					// Set Connections
 					if(drivingLane.connections != null && drivingLane.connections.length > 0) {
						Connection[] allConnections = buildConnectsTo(drivingLane.connections);
						if(allConnections != null) {
							lane.setConnectsToExists(true);
							lane.setConnections(allConnections);
						}
					}

					// Assign lane to jth Generic Lane
					genericLanes[laneCounter] = lane;

					laneCounter++;
				}
			} else {
				// Pedestrian Crosswalk Lanes
				for (int j = 0; j < approach.crosswalkLanes.length; j++) {
					CrosswalkLane crosswalkLane = approach.crosswalkLanes[j];
					GenericLane lane = new GenericLane();
					lane.setLaneID(Integer.valueOf(crosswalkLane.laneID));

					// Set Crosswalk Lane Name
					if(crosswalkLane.descriptiveName != null && !crosswalkLane.descriptiveName.isEmpty()) {
						lane.setNameExists(true);
						lane.setName(crosswalkLane.descriptiveName);
					}

					int laneDirectionBitString = SMALL_BIT_STRING;

					// Set LaneAttributes to Lane
					lane.setLaneAttributes(buildLaneAttributes(crosswalkLane, laneDirectionBitString));

					// Set NodeList Choice to Lane;
					lane.setNodeList(buildNodeList(isdInputData, crosswalkLane, referencePoint, offsetEncoding));

					// Set Connections
					if(crosswalkLane.connections != null && crosswalkLane.connections.length > 0) {
						Connection[] allConnections = buildConnectsTo(crosswalkLane.connections);
						if(allConnections != null) {
							lane.setConnectsToExists(true);
							lane.setConnections(allConnections);
						}	
					}

					// Assign lane to jth Generic Lane
					genericLanes[laneCounter] = lane;

					laneCounter++;
				}
			}
			// Set LaneList to GenericLanes[]
			lanes.setLaneList(genericLanes);
		}
		return lanes;
	}

	private LaneAttributes buildLaneAttributes(DrivingLane drivingLane, int laneDirectionBitString) {
		// Initialize Lane Attributes
		LaneAttributes laneAttributes = new LaneAttributes();

		// Set LaneDirection in Lane Attributes
		laneAttributes.setLaneDirectionAttribute(getLaneDirection(laneDirectionBitString));

		// Set LaneSharing in Lane Attributes
		laneAttributes.setLaneSharingAttribute(getLaneSharing(drivingLane));

		// Set LaneType in Lane Attributes
		laneAttributes.setLaneTypeAttribute(getLaneTypeAttributes(drivingLane));

		// Return Lane Attributes
		return laneAttributes;
	}

	private LaneDirection getLaneDirection(int direction) {
		LaneDirection laneDirection = new LaneDirection();
		laneDirection.setLaneDirection((byte) direction);
		return laneDirection;
	}

	private LaneSharing getLaneSharing(DrivingLane drivingLane) {
		LaneSharing laneSharing = new LaneSharing();
		int laneSharingBitString = LONG_BIT_STRING;
		if (drivingLane.sharedWith != null && drivingLane.sharedWith.length > 0) {
			laneSharingBitString = BitStringHelper.getBitString(laneSharingBitString, LONG_BIT_STRING_LENGTH, drivingLane.sharedWith);
		}
		laneSharing.setLaneSharing((short) laneSharingBitString);

		return laneSharing;
	}

	private LaneTypeAttributes getLaneTypeAttributes(DrivingLane drivingLane) {
		LaneTypeAttributes laneTypeAttributes = new LaneTypeAttributes();
		int[] laneTypeAttrArray = new int[] {};
		if (drivingLane.typeAttributes != null && drivingLane.typeAttributes.length > 0) {
			laneTypeAttrArray = drivingLane.typeAttributes;
		}

		laneTypeAttributes = toLaneTypeAttributes(drivingLane.laneType, laneTypeAttrArray);
		return laneTypeAttributes;
	}

	private LaneTypeAttributes toLaneTypeAttributes(String type, int[] typeAttributes) {
		LaneTypeAttributes laneTypeAttributes = new LaneTypeAttributes();
		type = type.toLowerCase();
		if (type.equals("vehicle")) {
			LaneAttributesVehicle laneAttributesVehicle = new LaneAttributesVehicle();
			int vehicleBitString = BitStringHelper.getBitString(SMALL_BIT_STRING, SMALL_BIT_STRING_LENGTH, typeAttributes);
			laneAttributesVehicle.setLaneAttributesVehicle((byte) vehicleBitString);
			laneTypeAttributes.setChoice((byte) LaneTypeAttributes.VEHICLE);
			laneTypeAttributes.setVehicle(laneAttributesVehicle);
		} else if (type.equals("crosswalk")) {
			LaneAttributesCrosswalk laneAttributesCrosswalk = new LaneAttributesCrosswalk();
			int crosswalkBitString = BitStringHelper.getBitString(LONG_BIT_STRING, LONG_BIT_STRING_LENGTH, typeAttributes);
			laneAttributesCrosswalk.setLaneAttributesCrosswalk((short) crosswalkBitString);
			laneTypeAttributes.setChoice((byte) LaneTypeAttributes.CROSSWALK);
			laneTypeAttributes.setCrosswalk(laneAttributesCrosswalk);
		} else if (type.equals("bike")) {
			LaneAttributesBike laneAttributesBike = new LaneAttributesBike();
			int bikeBitString = BitStringHelper.getBitString(SMALL_BIT_STRING, SMALL_BIT_STRING_LENGTH, typeAttributes);
			laneAttributesBike.setLaneAttributesBike((short) bikeBitString);
			laneTypeAttributes.setChoice((byte) LaneTypeAttributes.BIKE_LANE);
			laneTypeAttributes.setBikeLane(laneAttributesBike);
		} else if (type.equals("sidewalk")) {
			LaneAttributesSidewalk laneAttributesSidewalk = new LaneAttributesSidewalk();
			int sidewalkBitString = BitStringHelper.getBitString(SMALL_BIT_STRING, SMALL_BIT_STRING_LENGTH, typeAttributes);
			laneAttributesSidewalk.setLaneAttributesSidewalk((short) sidewalkBitString);
			laneTypeAttributes.setChoice((byte) LaneTypeAttributes.SIDEWALK);
			laneTypeAttributes.setSidewalk(laneAttributesSidewalk);
		} else if (type.equals("median")) {
			LaneAttributesBarrier laneAttributesBarrier = new LaneAttributesBarrier();
			int medianBitString = BitStringHelper.getBitString(LONG_BIT_STRING, LONG_BIT_STRING_LENGTH, typeAttributes);
			laneAttributesBarrier.setLaneAttributesBarrier((short) medianBitString);
			laneTypeAttributes.setChoice((byte) LaneTypeAttributes.MEDIAN);
			laneTypeAttributes.setMedian(laneAttributesBarrier);
		} else if (type.equals("striping")) {
			LaneAttributesStriping laneAttributesStriping = new LaneAttributesStriping();
			int stripingBitString = BitStringHelper.getBitString(SMALL_BIT_STRING, SMALL_BIT_STRING_LENGTH, typeAttributes);
			laneAttributesStriping.setLaneAttributesStriping((short) stripingBitString);
			laneTypeAttributes.setChoice((byte) LaneTypeAttributes.STRIPING);
			laneTypeAttributes.setStriping(laneAttributesStriping);
		} else if (type.equals("trackedVehicle")) {
			LaneAttributesTrackedVehicle laneAttributesTrackedVehicle = new LaneAttributesTrackedVehicle();
			int trackedVehicleBitString = BitStringHelper.getBitString(SMALL_BIT_STRING, SMALL_BIT_STRING_LENGTH, typeAttributes);
			laneAttributesTrackedVehicle.setLaneAttributesTrackedVehicle((short) trackedVehicleBitString);
			laneTypeAttributes.setChoice((byte) LaneTypeAttributes.TRACKED_VEHICLE);
			laneTypeAttributes.setTrackedVehicle(laneAttributesTrackedVehicle);
		} else if (type.equals("parking")) {
			LaneAttributesParking laneAttributesParking = new LaneAttributesParking();
			int parkingBitString = BitStringHelper.getBitString(SMALL_BIT_STRING, SMALL_BIT_STRING_LENGTH, typeAttributes);
			laneAttributesParking.setLaneAttributesParking((short) parkingBitString);
			laneTypeAttributes.setChoice((byte) LaneTypeAttributes.PARKING);
			laneTypeAttributes.setParking(laneAttributesParking);
		}
		return laneTypeAttributes;
	}

	// This function builds maneuvers bit string, then sets and returns AllowedManeuvers
	private AllowedManeuvers buildAllowedManeuvers(int[] attributes) {
		AllowedManeuvers maneuvers = new AllowedManeuvers();
		int maneuversBitString = BitStringHelper.getBitString(LONG_BIT_STRING, LONG_BIT_STRING_LENGTH, attributes);
		maneuvers.setAllowedManeuvers(maneuversBitString);
		return maneuvers;
	}

	// This functions builds and returns the NodeList
	private NodeListXY buildNodeList(IntersectionInputData isdInputData, DrivingLane lane,
			ReferencePoint referencePoint, OffsetEncoding offsetEncoding) {
		NodeListXY nodeList = new NodeListXY();
		if (!lane.isComputed) {
			nodeList.setChoice(NodeListXY.NODE_SET_XY);

			NodeSetXY nodeSetXY = new NodeSetXY();
			double curElevation = referencePoint.referenceElevation;
			GeoPoint refPoint = new GeoPoint(referencePoint.referenceLat, referencePoint.referenceLon);

			// Intializing a NodeXY array to store the lane nodes data
			NodeXY[] nodeXyArray = new NodeXY[lane.laneNodes.length];
			int nodeIndex = 0;


			// Loop through the lane nodes
			for (LaneNode laneNode : lane.laneNodes) {
				GeoPoint nextPoint = new GeoPoint(laneNode.nodeLat, laneNode.nodeLong);

				// Get Encoding Size based on given points
				if (offsetEncoding.type == OffsetEncodingType.Tight) {
					offsetEncoding.size = offsetEncoding.getOffsetEncodingSize(refPoint, nextPoint);
				}

				// Get the Node Offset
				NodeOffsetPointXY delta = offsetEncoding.encodeOffset(refPoint, nextPoint);
				NodeXY nodeXy = new NodeXY();
				nodeXy.setDelta(delta);

				// Set Node Attributes
				NodeAttributeSetXY attributes = new NodeAttributeSetXY();
				boolean hasAttributes = false;

				// Set dWidth
				if (laneNode.laneWidthDelta != 0) {
					attributes.setDWidthExists(true);
					attributes.setDWidth(laneNode.laneWidthDelta);
					hasAttributes = true;
				}

				// Set dElevation
				if (laneNode.nodeElev != 0 && isdInputData.enableElevation) {
					short elevDelta = getElevationDelta(laneNode.nodeElev, curElevation);
					if (elevDelta != 0) {
						curElevation = laneNode.nodeElev;
						attributes.setDElevationExists(true);
						attributes.setDElevation(elevDelta);
						hasAttributes = true;
					}
				}

				if (laneNode.speedLimitType != null && laneNode.speedLimitType.length > 0) {
					LaneDataAttributeList laneDataAttributeList = new LaneDataAttributeList();
					LaneDataAttribute[] laneDataAttribute = new LaneDataAttribute[1];
					LaneDataAttribute currentLaneDataAttribute = new LaneDataAttribute();
					SpeedLimitList speedLimitList = new SpeedLimitList();
					int speedLimitListLength = laneNode.speedLimitType.length;
					RegulatorySpeedLimit[] regulatorySpeedLimits = new RegulatorySpeedLimit[speedLimitListLength];
					for (int regIndex = 0; regIndex < speedLimitListLength; regIndex++) {
						RegulatorySpeedLimit regulatorySpeedLimit = new RegulatorySpeedLimit();
						short currentVelocity = laneNode.speedLimitType[regIndex].getVelocity();
						regulatorySpeedLimit.setType(getSpeedLimitType(laneNode.speedLimitType[regIndex].speedLimitType));
						regulatorySpeedLimit.setSpeed(currentVelocity);
						regulatorySpeedLimits[regIndex] = regulatorySpeedLimit;
					}
					currentLaneDataAttribute.setChoice(LaneDataAttribute.SPEED_LIMITS);
					speedLimitList.setSpeedLimits(regulatorySpeedLimits);

					currentLaneDataAttribute.setSpeedLimits(speedLimitList);
					laneDataAttribute[0] = currentLaneDataAttribute;
					laneDataAttributeList.setLaneAttributeList(laneDataAttribute);
					attributes.setDataExists(true);
					attributes.setData(laneDataAttributeList);
					hasAttributes = true;
				}

				if (hasAttributes) {
					nodeXy.setAttributesExists(true);
					nodeXy.setAttributes(attributes);
				}

				nodeXyArray[nodeIndex] = nodeXy;
				nodeIndex++;
				refPoint = nextPoint;
			}

			nodeSetXY.setNodeSetXY(nodeXyArray);
			nodeList.setNodes(nodeSetXY);

		} else {
			nodeList.setChoice(NodeListXY.COMPUTED_LANE);

			ComputedLane computedLane = new ComputedLane();
			computedLane.setReferenceLaneId(Integer.valueOf(lane.computedLane.referenceLaneID));

			OffsetXaxis offsetXaxis = new OffsetXaxis();
			OffsetYaxis offsetYaxis = new OffsetYaxis();

			offsetXaxis.setChoice(OffsetXaxis.SMALL);
			offsetXaxis.setSmall((short)lane.computedLane.offsetX);

			offsetYaxis.setChoice(OffsetYaxis.SMALL);
			offsetYaxis.setSmall((short) lane.computedLane.offsetY);

			computedLane.setOffsetXaxis(offsetXaxis);
			computedLane.setOffsetYaxis(offsetYaxis);

			nodeList.setComputed(computedLane);
		}
		return nodeList;
	}

	// This function builds and returns Connection array
	private Connection[] buildConnectsTo(LaneConnection[] laneConnections) {
		List<Connection> connectionsList = new ArrayList<Connection>();
		for (int connIndex = 0; connIndex < laneConnections.length; connIndex++) {
			LaneConnection currentLaneConnection = laneConnections[connIndex];
			if (currentLaneConnection.toLane <= 0)
				continue;

			ConnectingLane connectingLane = new ConnectingLane();

			// Set Connection Lane ID
			connectingLane.setLaneId(currentLaneConnection.toLane);

			// Set Connecting Lane Maneuvers
			if (currentLaneConnection.maneuvers != null && currentLaneConnection.maneuvers.length > 0) {
				connectingLane.setManeuverExists(true);
				connectingLane.setManeuver(buildAllowedManeuvers(currentLaneConnection.maneuvers));
			}

			Connection connection = new Connection();
			connection.setConnectingLane(connectingLane);

			// Set RemoteIntersection ID
			if (currentLaneConnection.remoteID > 0) {
				connection.setRemoteIntersectionExists(true);
				IntersectionReferenceID remoteIntrReferenceID = new IntersectionReferenceID();
				remoteIntrReferenceID.setId(currentLaneConnection.remoteID);
				remoteIntrReferenceID.setRegionExists(false);
				connection.setRemoteIntersection(remoteIntrReferenceID);
			}

			// Set SignalGroup ID
			if (currentLaneConnection.signal_id > 0) {
				connection.setSignalGroupExists(true);
				connection.setSignalGroup(currentLaneConnection.signal_id);
			} 

			// Set Connection ID
			if(currentLaneConnection.connectionId > 0) {
				connection.setConnectionIDExists(true);
				connection.setConnectionID(currentLaneConnection.connectionId);
			}
			
			connectionsList.add(connection);
		}

		// return connectionsArray;
		Connection[] connectionsArray = connectionsList.toArray(new Connection[connectionsList.size()]);
		return connectionsList.size() > 0 ? connectionsArray : null;
	}

	private Integer getLayerType(String layerTypeName) {
		if (layerTypeName.equals("mixedContent")) {
			return 1;
		} else if (layerTypeName.equals("generalMapData")) {
			return 2;
		} else if (layerTypeName.equals("intersectionData")) {
			return 3;
		} else if (layerTypeName.equals("curveData")) {
			return 4;
		} else if (layerTypeName.equals("roadwaySectionData")) {
			return 5;
		} else if (layerTypeName.equals("parkingAreaData")) {
			return 6;
		} else if (layerTypeName.equals("sharedLaneData")) {
			return 7;
		} else if (layerTypeName.equals("none")) {
			return 0;
		} else {
			logger.error("Unknown LayerType: " + layerTypeName);
			return 0;
		}
	}

	// This function returns the SpeedLimit Type
	private SpeedLimitType getSpeedLimitType(String speedLimitTypeString) {
		SpeedLimitType byteSpeedLimitType = new SpeedLimitType();
		switch (speedLimitTypeString) {
			case "Unknown":
				byteSpeedLimitType.setSpeedLimitType(SpeedLimitType.UNKNOWN);
				break;
			case "Max Speed in School Zone":
				byteSpeedLimitType.setSpeedLimitType(SpeedLimitType.MAXSPEEDINSCHOOLZONE);
				break;
			case "Max Speed in School Zone w/ Children":
				byteSpeedLimitType.setSpeedLimitType(SpeedLimitType.MAXSPEEDINSCHOOLZONEWHENCHILDRENAREPRESENT);
				break;
			case "Max Speed in Construction Zone":
				byteSpeedLimitType.setSpeedLimitType(SpeedLimitType.MAXSPEEDINCONSTRUCTIONZONE);
				break;
			case "Vehicle Min Speed":
				byteSpeedLimitType.setSpeedLimitType(SpeedLimitType.VEHICLEMINSPEED);
				break;
			case "Vehicle Max Speed":
				byteSpeedLimitType.setSpeedLimitType(SpeedLimitType.VEHICLEMAXSPEED);
				break;
			case "Vehicle Night Max Speed":
				byteSpeedLimitType.setSpeedLimitType(SpeedLimitType.VEHICLENIGHTMAXSPEED);
				break;
			case "Truck Min Speed":
				byteSpeedLimitType.setSpeedLimitType(SpeedLimitType.TRUCKMINSPEED);
				break;
			case "Truck Max Speed":
				byteSpeedLimitType.setSpeedLimitType(SpeedLimitType.TRUCKMAXSPEED);
				break;
			case "Truck Night Max Speed":
				byteSpeedLimitType.setSpeedLimitType(SpeedLimitType.TRUCKNIGHTMAXSPEED);
				break;
			case "Vehicles w/ Trailers Min Speed":
				byteSpeedLimitType.setSpeedLimitType(SpeedLimitType.VEHICLESWITHTRAILERSMINSPEED);
				break;
			case "Vehicles w/ Trailers Max Speed":
				byteSpeedLimitType.setSpeedLimitType(SpeedLimitType.VEHICLESWITHTRAILERSMAXSPEED);
				break;
			case "Vehicles w/ Trailers Night Max Speed":
				byteSpeedLimitType.setSpeedLimitType(SpeedLimitType.VEHICLESWITHTRAILERSNIGHTMAXSPEED);
				break;
			default:
				logger.warn("Unexpected speed limit type: " + speedLimitTypeString);
				break;
		}
		return byteSpeedLimitType;
	}

	// This function computes and returns the offset encoding size 
	private OffsetEncodingSize getOffsetEncodingSize(OffsetEncodingType offsetEncodingType, Approach[] approaches,
			ReferencePoint referencePoint) {
		OffsetEncodingSize offsetEncodingSize;

		switch (offsetEncodingType) {
			case Compact:
				int longestApproachOffsetInCm = 0;
				for (Approach approach : approaches) {
					if (approach.approachType.equalsIgnoreCase(CrosswalkLane.CROSSWALK_APPROACH_TYPE)) {
						for (CrosswalkLane crosswalkLane : approach.crosswalkLanes) {
							int longestLaneOffsetInCm = Math
									.abs(getLongestOffsetDistanceInCm(referencePoint, crosswalkLane.laneNodes));
							if (longestLaneOffsetInCm > longestApproachOffsetInCm) {
								longestApproachOffsetInCm = longestLaneOffsetInCm;
							}
						}
					} else {
						for (DrivingLane drivingLane : approach.drivingLanes) {
							int longestLaneOffsetInCm = Math
									.abs(getLongestOffsetDistanceInCm(referencePoint, drivingLane.laneNodes));
							if (longestLaneOffsetInCm > longestApproachOffsetInCm) {
								longestApproachOffsetInCm = longestLaneOffsetInCm;
							}
						}
					}
				}

				offsetEncodingSize = OffsetEncodingSize.getOffsetEncodingSize(longestApproachOffsetInCm);
				break;
			case Explicit:
				offsetEncodingSize = OffsetEncodingSize.Explicit64Bit;
				break;
			case Standard:
			default:
				offsetEncodingSize = OffsetEncodingSize.Offset32Bit;
				break;
		}

		return offsetEncodingSize;
	}

	private int getLongestOffsetDistanceInCm(IntersectionInputData.ReferencePoint referencePoint,
			LaneNode[] laneNodes) {
		int longestDistanceInCm = 0;

		try {
			// Check the offset from the reference point first
			GeoPoint gpReference = new GeoPoint(referencePoint.referenceLat, referencePoint.referenceLon);
			GeoPoint gpFirstNode = new GeoPoint(laneNodes[0].nodeLat, laneNodes[0].nodeLong);

			int longerOffset = OffsetEncoding.getLongerOffset(gpReference, gpFirstNode);
			if (longerOffset > longestDistanceInCm) {
				longestDistanceInCm = longerOffset;
			}

			// Work through the rest of the points in the lane
			for (int i = 0; i < laneNodes.length - 1; i++) {
				GeoPoint gp1 = new GeoPoint(laneNodes[i].nodeLat, laneNodes[i].nodeLong);
				GeoPoint gp2 = new GeoPoint(laneNodes[i + 1].nodeLat, laneNodes[i + 1].nodeLong);

				longerOffset = OffsetEncoding.getLongerOffset(gp1, gp2);
				if (longerOffset > longestDistanceInCm) {
					longestDistanceInCm = longerOffset;
				}
			}
		} catch (IllegalArgumentException e) {
			// If we catch this, the length is CM is larger than fits in a short.
			// Currently the standard only supports up to what fits in a short for
			// offset distances. An explicit Lat/Lon value will have to be used.
			// Return a value greater than the short max value
			longestDistanceInCm = Short.MAX_VALUE + 1;
		}

		return longestDistanceInCm;
	}

	// This function calculates the elevation offset for a given node
	static short getElevationDelta(double nodeElevation, double curElevation) {
		int celev = IntersectionInputData.convertElevation(curElevation);
		int nelev = IntersectionInputData.convertElevation(nodeElevation);
		return celev != IntersectionInputData.INVALID_ELEVATION &&
				nelev != IntersectionInputData.INVALID_ELEVATION ? (short) (nelev - celev) : 0;
	}
}