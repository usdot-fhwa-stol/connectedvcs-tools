/*
 * Copyright (C) 2025 LEIDOS.
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.usdot.cv.mapencoder.AllowedManeuvers;
import gov.usdot.cv.mapencoder.ComputedLane;
import gov.usdot.cv.mapencoder.ConnectingLane;
import gov.usdot.cv.mapencoder.Connection;
import gov.usdot.cv.mapencoder.GenericLane;
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
import gov.usdot.cv.mapencoder.MapData;
import gov.usdot.cv.mapencoder.NodeAttributeSetXY;
import gov.usdot.cv.mapencoder.NodeListXY;
import gov.usdot.cv.mapencoder.NodeOffsetPointXY;
import gov.usdot.cv.mapencoder.NodeSetXY;
import gov.usdot.cv.mapencoder.NodeXY;
import gov.usdot.cv.mapencoder.OffsetXaxis;
import gov.usdot.cv.mapencoder.OffsetYaxis;
import gov.usdot.cv.mapencoder.Position3D;
import gov.usdot.cv.mapencoder.RegulatorySpeedLimit;
import gov.usdot.cv.mapencoder.SpeedLimitList;
import gov.usdot.cv.mapencoder.SpeedLimitType;
import gov.usdot.cv.msg.builder.exception.MessageBuildException;
import gov.usdot.cv.msg.builder.exception.MessageEncodeException;
import gov.usdot.cv.msg.builder.input.IntersectionInputData;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.Approach;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.ApproachTypeRow;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.ContentDateTime;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.CrosswalkLane;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.DrivingLane;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.GenerateType;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.ISDRegulatorySpeedLimit;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.LaneConnection;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.LaneNode;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.ReferencePoint;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.ReferencePointChild;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.TimeOfCalculation;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.TimePeriodRange;
import gov.usdot.cv.msg.builder.input.IntersectionInputData.TimeRestrictions;
import gov.usdot.cv.msg.builder.message.IntersectionMessage;
import gov.usdot.cv.msg.builder.util.BitStringHelper;
import gov.usdot.cv.msg.builder.util.GeoPoint;
import gov.usdot.cv.msg.builder.util.J2735Helper;
import gov.usdot.cv.msg.builder.util.J2945Helper;
import gov.usdot.cv.msg.builder.util.JSONMapper;
import gov.usdot.cv.msg.builder.util.ObjectPrinter;
import gov.usdot.cv.msg.builder.util.OffsetEncoding;
import gov.usdot.cv.msg.builder.util.OffsetEncoding.OffsetEncodingSize;
import gov.usdot.cv.msg.builder.util.OffsetEncoding.OffsetEncodingType;
import gov.usdot.cv.rgaencoder.ApproachGeometryLayer;
import gov.usdot.cv.rgaencoder.ApproachWayTypeIDSet;
import gov.usdot.cv.rgaencoder.BaseLayer;
import gov.usdot.cv.rgaencoder.BicycleLaneConnectionsLayer;
import gov.usdot.cv.rgaencoder.BicycleLaneGeometryLayer;
import gov.usdot.cv.rgaencoder.CnxnManeuverInfo;
import gov.usdot.cv.rgaencoder.ComputedXYZNodeInfo;
import gov.usdot.cv.rgaencoder.CrosswalkLaneGeometryLayer;
import gov.usdot.cv.rgaencoder.DDate;
import gov.usdot.cv.rgaencoder.DDateTime;
import gov.usdot.cv.rgaencoder.DaysOfTheWeek;
import gov.usdot.cv.rgaencoder.GeneralPeriod;
import gov.usdot.cv.rgaencoder.GeometryContainer;
import gov.usdot.cv.rgaencoder.IndividualApproachGeometryInfo;
import gov.usdot.cv.rgaencoder.IndividualSpeedLimitSettings;
import gov.usdot.cv.rgaencoder.IndividualWayCnxnsManeuvers;
import gov.usdot.cv.rgaencoder.IndividualWayConnections;
import gov.usdot.cv.rgaencoder.IndividualWayDirectionsOfTravel;
import gov.usdot.cv.rgaencoder.IndividualWaySpeedLimits;
import gov.usdot.cv.rgaencoder.IndividualXYZNodeGeometryInfo;
import gov.usdot.cv.rgaencoder.IndvBikeLaneGeometryInfo;
import gov.usdot.cv.rgaencoder.IndvCrosswalkLaneGeometryInfo;
import gov.usdot.cv.rgaencoder.IndvMtrVehLaneGeometryInfo;
import gov.usdot.cv.rgaencoder.LaneConnectionFromInfo;
import gov.usdot.cv.rgaencoder.LaneConnectionToInfo;
import gov.usdot.cv.rgaencoder.LaneConstructorType;
import gov.usdot.cv.rgaencoder.LocationSpeedLimits;
import gov.usdot.cv.rgaencoder.MotorVehicleLaneGeometryLayer;
import gov.usdot.cv.rgaencoder.MovementsContainer;
import gov.usdot.cv.rgaencoder.MtrVehLaneConnectionsLayer;
import gov.usdot.cv.rgaencoder.MtrVehLaneConnectionsManeuversLayer;
import gov.usdot.cv.rgaencoder.MtrVehLaneDirectionOfTravelLayer;
import gov.usdot.cv.rgaencoder.MtrVehLaneSpeedLimitsLayer;
import gov.usdot.cv.rgaencoder.NodeIndexOffset;
import gov.usdot.cv.rgaencoder.NodeXYZOffsetInfo;
import gov.usdot.cv.rgaencoder.NodeXYZOffsetValue;
import gov.usdot.cv.rgaencoder.PhysicalXYZNodeInfo;
import gov.usdot.cv.rgaencoder.RGAData;
import gov.usdot.cv.rgaencoder.RGATimeRestrictions;
import gov.usdot.cv.rgaencoder.SpeedLimitInfo;
import gov.usdot.cv.rgaencoder.SpeedLimitTypeRGA;
import gov.usdot.cv.rgaencoder.SpeedLimitVehicleType;
import gov.usdot.cv.rgaencoder.TimeWindowInformation;
import gov.usdot.cv.rgaencoder.TimeWindowItemControlInfo;
import gov.usdot.cv.rgaencoder.UnsignalizedMovementStates;
import gov.usdot.cv.rgaencoder.WayCnxnManeuverControlType;
import gov.usdot.cv.rgaencoder.WayCnxnManeuverInfo;
import gov.usdot.cv.rgaencoder.WayCnxnManeuvers;
import gov.usdot.cv.rgaencoder.WayDirectionOfTravelInfo;
import gov.usdot.cv.rgaencoder.WayPlanarGeometryInfo;
import gov.usdot.cv.rgaencoder.WayToWayConnectionInfo;
import gov.usdot.cv.rgaencoder.WayType;
import gov.usdot.cv.rgaencoder.WayUseContainer;
import gov.usdot.cv.rgaencoder.WayWidth;

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
					logger.debug("in MAP: ");
					// Removing the first 8 characters from the MessageFrame provides the MAP message
					// This was tested manually by removing the characters from MessageFrame and testing using the decoder
					hexString = (J2735Helper.getHexString(md)).substring(8);
					readableString = md.toString();
					break;
				case RGA:
					isdInputData.validatePoints();
					rd = buildRGAData(isdInputData);
					logger.debug("in RGA: ");
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
					logger.debug("in RGA: ");
					hexString = J2945Helper.getHexString(rd);
					readableString = rd.toString();
					break;
				case FramePlusSPaT:
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
		
		// Build and set Geometry Containers
		List<GeometryContainer> geometryContainers = buildGeometryContainers(isdInputData);
		if (geometryContainers.size() > 0) {
			rgaData.setGeometryContainers(geometryContainers);
		}

		// Build and set Movements Containers
		List<MovementsContainer> movementsContainers = buildMovementsContainers(isdInputData);
		if (movementsContainers.size() > 0) {
			rgaData.setMovementsContainers(movementsContainers);
		}

		// Build and set WayUse Containers
		List<WayUseContainer> wayUseContainers = buildWayUseContainers(isdInputData);
		if (wayUseContainers.size() > 0) {
			rgaData.setWayUseContainers(wayUseContainers);
		}

		return rgaData;
	}

	public BaseLayer buildBaseLayer(IntersectionInputData isdInputData) {
		BaseLayer baseLayer = new BaseLayer();
		ReferencePoint referencePoint = isdInputData.mapData.intersectionGeometry.referencePoint;
		TimeOfCalculation timeOfCalculation = isdInputData.mapData.timeOfCalculation;
		ContentDateTime contentDateTime = isdInputData.mapData.contentDateTime;

		//DataSetFormatVersionInfo
		baseLayer.setMajorVer(1); // TODO: Pull value from ASN1
		baseLayer.setMinorVer(1); // TODO: Pull value from ASN1

		// ReferencePointInfo
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

		// RoadGeometryRefIDInfo
		baseLayer.setRelativeToRdAuthID(referencePoint.mappedGeomID);

		if (referencePoint.roadAuthorityIdType != null) {
			if (referencePoint.roadAuthorityIdType.replaceAll("\\s", "").toLowerCase().equals("full")) {
				baseLayer.setRelRdAuthIDExists(false);
				baseLayer.setFullRdAuthIDExists(true);
				baseLayer.setFullRdAuthID(referencePoint.roadAuthorityId);
			} else if (referencePoint.roadAuthorityIdType.replaceAll("\\s", "").toLowerCase().equals("relative")) {
				baseLayer.setFullRdAuthIDExists(false);
				baseLayer.setRelRdAuthIDExists(true);
				baseLayer.setRelRdAuthID(referencePoint.roadAuthorityId);
			} else {
				baseLayer.setFullRdAuthIDExists(false);
				baseLayer.setRelRdAuthIDExists(false);
			}
		}

		// DataSetContentIdentification
		baseLayer.setContentVer(isdInputData.mapData.contentVersion);

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

	/**
	 * This function builds and returns list of wayUse containers for RGA Data
	 * @param isdInputData
	 * @return wayUseContainers
	 */
	public List<WayUseContainer> buildWayUseContainers(IntersectionInputData isdInputData) {
		List<WayUseContainer> wayUseContainers = new ArrayList<>();
		// Checking if approaches are null
		if (isdInputData.mapData.intersectionGeometry.laneList == null
				|| isdInputData.mapData.intersectionGeometry.laneList.approach == null) {
			return wayUseContainers;
		}

		Approach[] approaches = isdInputData.mapData.intersectionGeometry.laneList.approach;
		WayUseContainer mtrVehLaneSpeedLimits = new WayUseContainer();
		MtrVehLaneSpeedLimitsLayer mtrVehLaneSpeedLimitsLayer = new MtrVehLaneSpeedLimitsLayer();

		for (int approachIndex = 0; approachIndex < approaches.length; approachIndex++) {
			Approach approach = approaches[approachIndex];
			// Excluding crosswalk lanes as currently crosswalks do not have an approach id and it is default to -1
			if (approach.approachID != IntersectionInputData.CrosswalkLane.CROSSWALK_APPROACH_ID) {
				// Loop through the driving lanes
				for (int drivingLaneIndex = 0; drivingLaneIndex < approach.drivingLanes.length; drivingLaneIndex++) {
					DrivingLane drivingLane = approach.drivingLanes[drivingLaneIndex];
					IndividualWaySpeedLimits individualWaySpeedLimits = new IndividualWaySpeedLimits();
					individualWaySpeedLimits.setWayID(Integer.valueOf(drivingLane.laneID));
					if ((drivingLane.laneType.toLowerCase()).equals("vehicle")) {
						LaneNode[] laneNodes = drivingLane.laneNodes;
						if (laneNodes != null && laneNodes.length > 0) {
							for (int nodeIndex = 0; nodeIndex < 1; nodeIndex++) {
								LaneNode laneNode = laneNodes[nodeIndex];
								LocationSpeedLimits locationSpeedLimits = new LocationSpeedLimits();
								NodeIndexOffset location = new NodeIndexOffset();
								SpeedLimitInfo speedLimitInfo = new SpeedLimitInfo();

								int speedLimitListLength = laneNode.speedLimitType.length;
								for (int speedIndex = 0; speedIndex < speedLimitListLength; speedIndex++) {
									ISDRegulatorySpeedLimit currentRegulatorySpeedLimit = laneNode.speedLimitType[speedIndex];
									String currentSpeedLimitString = laneNode.speedLimitType[speedIndex].speedLimitType;

									if (currentSpeedLimitString != null) {
										if (currentRegulatorySpeedLimit == null
												|| currentRegulatorySpeedLimit.speedLimitType == null) {
											continue; // skip nulls
										}
	
										switch (currentSpeedLimitString) {
											case "Max Speed in School Zone":
												speedLimitInfo.addMaxSpeedLimitSettingsSet(buildIndividualSpeedLimitSettings(currentRegulatorySpeedLimit, SpeedLimitVehicleType.ALL_VEHICLES));
												break;
											case "Max Speed in School Zone w/ Children":
												speedLimitInfo.addMaxSpeedLimitSettingsSet(buildIndividualSpeedLimitSettings(currentRegulatorySpeedLimit, SpeedLimitVehicleType.ALL_VEHICLES));
												break;
											case "Max Speed in Construction Zone":
												speedLimitInfo.addMaxSpeedLimitSettingsSet(buildIndividualSpeedLimitSettings(currentRegulatorySpeedLimit, SpeedLimitVehicleType.ALL_VEHICLES));
												break;
											case "Vehicle Min Speed":
												speedLimitInfo.addMinSpeedLimitSettingsSet(buildIndividualSpeedLimitSettings(currentRegulatorySpeedLimit, SpeedLimitVehicleType.ALL_VEHICLES));
												break;
											case "Vehicle Max Speed":
												speedLimitInfo.addMaxSpeedLimitSettingsSet(buildIndividualSpeedLimitSettings(currentRegulatorySpeedLimit, SpeedLimitVehicleType.ALL_VEHICLES));
												break;
											case "Vehicle Night Max Speed":
												currentRegulatorySpeedLimit.timeRestrictions = setNightTimeRestrictions(currentRegulatorySpeedLimit.timeRestrictions);
												speedLimitInfo.addMaxSpeedLimitSettingsSet(buildIndividualSpeedLimitSettings(currentRegulatorySpeedLimit, SpeedLimitVehicleType.ALL_VEHICLES));
												break;
											case "Truck Min Speed":
												speedLimitInfo.addMinSpeedLimitSettingsSet(buildIndividualSpeedLimitSettings(currentRegulatorySpeedLimit, SpeedLimitVehicleType.TRUCK));
												break;
											case "Truck Max Speed":
												speedLimitInfo.addMaxSpeedLimitSettingsSet(buildIndividualSpeedLimitSettings(currentRegulatorySpeedLimit, SpeedLimitVehicleType.TRUCK));
												break;
											case "Truck Night Max Speed":
												currentRegulatorySpeedLimit.timeRestrictions = setNightTimeRestrictions(currentRegulatorySpeedLimit.timeRestrictions);
												speedLimitInfo.addMaxSpeedLimitSettingsSet(buildIndividualSpeedLimitSettings(currentRegulatorySpeedLimit, SpeedLimitVehicleType.TRUCK));
												break;
											case "Passenger Vehicles Min Speed":
												speedLimitInfo.addMinSpeedLimitSettingsSet(buildIndividualSpeedLimitSettings(currentRegulatorySpeedLimit, SpeedLimitVehicleType.PASSENGER_VEHICLES));
												break;
											case "Passenger Vehicles Max Speed":
												speedLimitInfo.addMaxSpeedLimitSettingsSet(buildIndividualSpeedLimitSettings(currentRegulatorySpeedLimit, SpeedLimitVehicleType.PASSENGER_VEHICLES));
												break;
											default:
												logger.warn("Unexpected speed limit type: " + currentSpeedLimitString);
												break;
										}
									}
								}
								
								if (speedLimitInfo.getMaxSpeedLimitSettingsSet().size() > 0
										|| speedLimitInfo.getMinSpeedLimitSettingsSet().size() > 0) {
									// Note: The value of 1 is the index for the first node in the set of nodes that
									// are being indexed. The value of 0 is for unknown/unavailable or when a node
									// index may not apply if allowed by the application requirements.
									location.setNodeIndex(nodeIndex + 1);
									locationSpeedLimits.setLocation(location);
									locationSpeedLimits.setSpeedLimitInfo(speedLimitInfo);
									individualWaySpeedLimits.addLocationSpeedLimits(locationSpeedLimits);
								}
							}

							if (!individualWaySpeedLimits.getLocationSpeedLimitSet().isEmpty()) {
								mtrVehLaneSpeedLimitsLayer.addIndividualWaySpeedLimits(individualWaySpeedLimits);
	
							}
						}
					}
				}
			}
		}

		if (!mtrVehLaneSpeedLimitsLayer.getLaneSpeedLimitLaneSet().isEmpty()) {
			mtrVehLaneSpeedLimits.setWaUseContainerId(WayUseContainer.MTR_VEH_LANE_SPEED_LIMITS_LAYER_ID);
			mtrVehLaneSpeedLimits.setMtrVehLaneSpeedLimitsLayer(mtrVehLaneSpeedLimitsLayer);
			wayUseContainers.add(mtrVehLaneSpeedLimits);
		}

		return wayUseContainers;
	}

	public TimeRestrictions setNightTimeRestrictions(TimeRestrictions timeRestrictions) {
		if (timeRestrictions == null) {
			timeRestrictions = new TimeRestrictions();
		}

		boolean isDaysEmpty = (timeRestrictions.daysOfTheWeek == null || timeRestrictions.daysOfTheWeek.length == 0);
		boolean isTimePeriodTypeEmpty = (timeRestrictions.timePeriodType == null
				|| timeRestrictions.timePeriodType.isEmpty());
		boolean isTimePeriodValueEmpty = (timeRestrictions.timePeriodValue == null
				|| timeRestrictions.timePeriodValue.isEmpty());
		boolean isRangeEmpty = (timeRestrictions.timePeriodRange == null
				|| (timeRestrictions.timePeriodRange.startDatetime == null
						&& timeRestrictions.timePeriodRange.endDatetime == null
						&& timeRestrictions.timePeriodRange.startOffset == 0
						&& timeRestrictions.timePeriodRange.endOffset == 0));

		if (isDaysEmpty && isTimePeriodTypeEmpty && isTimePeriodValueEmpty && isRangeEmpty) {
			timeRestrictions.daysOfTheWeek = new int[] { DaysOfTheWeek.ALLDAYS }; // All Days
			timeRestrictions.timePeriodType = "general";
			timeRestrictions.timePeriodValue = "night";
			timeRestrictions.timePeriodRange = new TimePeriodRange();
		}
		
		return timeRestrictions;
	}

	public IndividualSpeedLimitSettings buildIndividualSpeedLimitSettings(ISDRegulatorySpeedLimit currentISDRegulatorySpeedLimit, int vehicleType) {
		short currentVelocity = currentISDRegulatorySpeedLimit.getVelocity();
		String currentSpeedLimitChoice = currentISDRegulatorySpeedLimit.speedLimitChoice;

		IndividualSpeedLimitSettings indSpeedLimitSettingsSet = new IndividualSpeedLimitSettings();

		indSpeedLimitSettingsSet.setSpeedLimit(currentVelocity);
		indSpeedLimitSettingsSet.setSpeedLimitType(buiSpeedLimitTypeRGA(currentSpeedLimitChoice));
		indSpeedLimitSettingsSet.setVehicleTypes(buildSpeedLimitVehicleType(vehicleType));
		indSpeedLimitSettingsSet.setTimeRestrictions(buildLaneTimeRestriction(currentISDRegulatorySpeedLimit.timeRestrictions));
		return indSpeedLimitSettingsSet;
	}

	public SpeedLimitTypeRGA buiSpeedLimitTypeRGA(String currentSpeedLimitChoice) {
		SpeedLimitTypeRGA speedLimitTypeRGA = new SpeedLimitTypeRGA();
		if (currentSpeedLimitChoice != null) {
			if ((currentSpeedLimitChoice.toLowerCase()).equals("regulatory")) {
				speedLimitTypeRGA.setSpeedLimitTypeValue(SpeedLimitTypeRGA.REGULATORY);
			} else if ((currentSpeedLimitChoice.toLowerCase()).equals("advisory")) {
				speedLimitTypeRGA.setSpeedLimitTypeValue(SpeedLimitTypeRGA.ADVISORY);
			}
		}
		return speedLimitTypeRGA;
	}

	public SpeedLimitVehicleType buildSpeedLimitVehicleType(int vehicleType) {
		SpeedLimitVehicleType vehicleTypes = new SpeedLimitVehicleType();
		int[] speedVehicleTypes = { vehicleType };
		int allVehiclesBitString = BitStringHelper.getBitString(SMALL_BIT_STRING,
				SMALL_BIT_STRING_LENGTH, speedVehicleTypes);
		vehicleTypes.setSpeedLimitVehicleTypeValue((short) allVehiclesBitString);
		return vehicleTypes;
	}

	/**
	 * This function builds and returns list of movements containers for RGA Data
	 * @param isdInputData
	 * @return movementsContainers
	 */
	public List<MovementsContainer> buildMovementsContainers(IntersectionInputData isdInputData) {
		List<MovementsContainer> movementsContainers = new ArrayList<>();
		// Checking if approaches are null
		if (isdInputData.mapData.intersectionGeometry.laneList == null
				|| isdInputData.mapData.intersectionGeometry.laneList.approach == null) {
			return movementsContainers;
		}

		Approach[] approaches = isdInputData.mapData.intersectionGeometry.laneList.approach;
		MovementsContainer mtrVehDirectionOfTravel = new MovementsContainer();
		MtrVehLaneDirectionOfTravelLayer mtrVehLaneDirectionOfTravelLayer = new MtrVehLaneDirectionOfTravelLayer();

		MovementsContainer mtrVehLaneConnections = new MovementsContainer();
		MtrVehLaneConnectionsLayer mtrVehLnCnxnsLayer = new MtrVehLaneConnectionsLayer();

		MovementsContainer bicycleLaneConnections = new MovementsContainer();
		BicycleLaneConnectionsLayer bikeLnCnxnsLayer = new BicycleLaneConnectionsLayer();

		MovementsContainer mtrVehLaneCnxnManeuvers = new MovementsContainer();
		MtrVehLaneConnectionsManeuversLayer mtrVehLaneConnectionsManeuversLayer = new MtrVehLaneConnectionsManeuversLayer();

		for (int approachIndex = 0; approachIndex < approaches.length; approachIndex++) {
			Approach approach = approaches[approachIndex];
			List<WayDirectionOfTravelInfo> wayDirectionOfTravelInfoList = new ArrayList<>();

			// Excluding crosswalk lanes as currently crosswalks do not have an approach id and it is default to -1
			if (approach.approachID != IntersectionInputData.CrosswalkLane.CROSSWALK_APPROACH_ID) {
				for(int approachTypeIndex = 0; approachTypeIndex < approach.approachTypes.length; approachTypeIndex++) {
					WayDirectionOfTravelInfo wayDirectionOfTravelInfo = new WayDirectionOfTravelInfo();
					ApproachTypeRow currentApproachTypeRow =  approach.approachTypes[approachTypeIndex];

					switch (currentApproachTypeRow.approachType.toLowerCase()) {
						case "ingress":
							int[] ingressDirection = { WayDirectionOfTravelInfo.LAST_TO_FIRST_NODE };
							int ingressBitString = BitStringHelper.getBitString(SMALL_BIT_STRING, SMALL_BIT_STRING_LENGTH, ingressDirection);
							wayDirectionOfTravelInfo.setWayNodeDirectionOfTravel((short) ingressBitString);
							break;
						case "egress":
							int[] egressDirection = { WayDirectionOfTravelInfo.FIRST_TO_LAST_NODE };
							int egressBitString = BitStringHelper.getBitString(SMALL_BIT_STRING, SMALL_BIT_STRING_LENGTH, egressDirection);
							wayDirectionOfTravelInfo.setWayNodeDirectionOfTravel((short) egressBitString);
							break;
						case "both":
							int[] bothDirection = { WayDirectionOfTravelInfo.FIRST_TO_LAST_NODE, WayDirectionOfTravelInfo.LAST_TO_FIRST_NODE };
							int bothBitString = BitStringHelper.getBitString(SMALL_BIT_STRING, SMALL_BIT_STRING_LENGTH, bothDirection);
							wayDirectionOfTravelInfo.setWayNodeDirectionOfTravel((short) bothBitString);
							break;
						default:
							break;
					}

					wayDirectionOfTravelInfo.setTimeRestrictions(buildLaneTimeRestriction(currentApproachTypeRow.timeRestrictions));
					// Add to wayDirectionOfTravelInfoList
					wayDirectionOfTravelInfoList.add(wayDirectionOfTravelInfo);
				}

				// Loop through the driving lanes
				for (int drivingLaneIndex = 0; drivingLaneIndex < approach.drivingLanes.length; drivingLaneIndex++) {
					IndividualWayDirectionsOfTravel individualWayDirectionsOfTravel = new IndividualWayDirectionsOfTravel();
					DrivingLane drivingLane = approach.drivingLanes[drivingLaneIndex];

					if ((drivingLane.laneType.toLowerCase()).equals("vehicle")) {
						individualWayDirectionsOfTravel.setWayID(Integer.valueOf(drivingLane.laneID));
						individualWayDirectionsOfTravel.setDirectionsOfTravelSet(wayDirectionOfTravelInfoList);

						// Currently Movements Container supports only MtrVehLaneDirectionOfTravelLayer
						mtrVehLaneDirectionOfTravelLayer.addIndividualWayDirectionsOfTravel(individualWayDirectionsOfTravel);

						if (drivingLane.connections != null && drivingLane.connections.length > 0) {
							IndividualWayConnections individualWayConnections = buildIndividualWayConnections(drivingLane.laneID, drivingLane.connections, isdInputData);
							List<WayToWayConnectionInfo> currentWayToWayConnectionInfoList = individualWayConnections.getConnectionsSet();
							if (currentWayToWayConnectionInfoList != null && !currentWayToWayConnectionInfoList.isEmpty()) {
								mtrVehLnCnxnsLayer.addIndividualWayConnections(individualWayConnections);
							}

							String currentManeuverControlType = "Signalized";
							if(approach.maneuverControlType != null) {
								currentManeuverControlType = approach.maneuverControlType;
							}

							IndividualWayCnxnsManeuvers individualWayCnxnsManeuvers = buildIndividualWayCnxnsManeuvers(drivingLane.laneID, drivingLane.connections, currentManeuverControlType);
							List<WayCnxnManeuverInfo> cnxnManeuversSet = individualWayCnxnsManeuvers.getCnxnManeuversSet();
							if (cnxnManeuversSet != null && !cnxnManeuversSet.isEmpty()) {
								mtrVehLaneConnectionsManeuversLayer.addMtrVehLaneConnectionsManeuversLayer(individualWayCnxnsManeuvers);
							}
							
						}
					}

					if ((drivingLane.laneType.toLowerCase()).equals("bike")) {
						if (drivingLane.connections != null && drivingLane.connections.length > 0) {
							IndividualWayConnections bikeIndividualWayConnections = buildIndividualWayConnections(drivingLane.laneID, drivingLane.connections, isdInputData);
							List<WayToWayConnectionInfo> currentBikeWayToWayConnectionInfoList = bikeIndividualWayConnections.getConnectionsSet();
							if (currentBikeWayToWayConnectionInfoList != null && !currentBikeWayToWayConnectionInfoList.isEmpty()) {
								bikeLnCnxnsLayer.addIndividualWayConnections(bikeIndividualWayConnections);
							}	
						}
					}
				}
			}
		}

		if (!mtrVehLaneDirectionOfTravelLayer.getLaneDirOfTravelLaneSet().isEmpty()) {
			mtrVehDirectionOfTravel.setMovementsContainerId(MovementsContainer.MTR_VEH_LANE_DIRECTION_OF_TRAVEL_LAYER_ID);
			mtrVehDirectionOfTravel.setMtrVehLaneDirectionOfTravelLayer(mtrVehLaneDirectionOfTravelLayer);
			movementsContainers.add(mtrVehDirectionOfTravel);
		}
	
		if (!mtrVehLnCnxnsLayer.getMtrVehLaneCnxnsLaneSet().isEmpty()) {
			mtrVehLaneConnections.setMovementsContainerId(MovementsContainer.MTR_VEH_LANE_CONNECTIONS_LAYER_ID);
			mtrVehLaneConnections.setMtrVehLnCnxnsLayer(mtrVehLnCnxnsLayer);
			movementsContainers.add(mtrVehLaneConnections);
		}

		if (!mtrVehLaneConnectionsManeuversLayer.getMtrVehLaneConnectionsManeuversLayer().isEmpty()) {
			mtrVehLaneCnxnManeuvers.setMovementsContainerId(MovementsContainer.MTR_VEH_LANE_CONNECTIONS_MANEUVERS_LAYER_ID);
			mtrVehLaneCnxnManeuvers.setMtrVehLnCnxnxMnvrLayer(mtrVehLaneConnectionsManeuversLayer);
			movementsContainers.add(mtrVehLaneCnxnManeuvers);
		}

		if (!bikeLnCnxnsLayer.getBicycleLaneCnxnsLaneSet().isEmpty()) {
			bicycleLaneConnections.setMovementsContainerId(MovementsContainer.BIKE_LANE_CONNECTIONS_LAYER_ID);
			bicycleLaneConnections.setBikeLnCnxnsLayer(bikeLnCnxnsLayer);
			movementsContainers.add(bicycleLaneConnections);
		}

		return movementsContainers;
	}

	public IndividualWayCnxnsManeuvers buildIndividualWayCnxnsManeuvers(String laneId, LaneConnection[] laneConnections,
			String maneuverControlType) {
		IndividualWayCnxnsManeuvers individualWayCnxnsManeuvers = new IndividualWayCnxnsManeuvers();
		individualWayCnxnsManeuvers.setWayID(Integer.valueOf(laneId));
		for (int conIndex = 0; conIndex < laneConnections.length; conIndex++) {
			WayCnxnManeuverInfo wayCnxnManeuverInfo = new WayCnxnManeuverInfo();
			LaneConnection currentLaneConnection = laneConnections[conIndex];

			if (currentLaneConnection.connectionId > 0) {
				wayCnxnManeuverInfo.setConnectionID(currentLaneConnection.connectionId);

				if (currentLaneConnection.maneuvers.length > 0) {
					for (int mIndex = 0; mIndex < currentLaneConnection.maneuvers.length; mIndex++) {
						int currentManeuver = currentLaneConnection.maneuvers[mIndex];

						if (currentManeuver != 6 && currentManeuver != 7 && currentManeuver != 8 && currentManeuver != 9 && currentManeuver != 10) {
							CnxnManeuverInfo cnxnManeuverInfo = new CnxnManeuverInfo();
							WayCnxnManeuvers wayCnxnManeuvers = buildWayCnxnManeuvers(currentManeuver, maneuverControlType);
							cnxnManeuverInfo.setAllowedManeuver(wayCnxnManeuvers);
							WayCnxnManeuverControlType wayCnxnManeuverControlType = new WayCnxnManeuverControlType();

							switch (maneuverControlType) {
								case "Signalized":
									wayCnxnManeuverControlType.setChoice(WayCnxnManeuverControlType.SIGNALIZED_CONTROL);
									break;
								case "Unsignalized":
									UnsignalizedMovementStates unsignalizedMovementStates = new UnsignalizedMovementStates();
									wayCnxnManeuverControlType.setChoice(WayCnxnManeuverControlType.UNSIGNALIZED_CONTROL);
									unsignalizedMovementStates.setUnsignalizedMovementStatesValue(UnsignalizedMovementStates.PROTECTED_MOVEMENT_ALLOWED);
									for (int unsignalizedManeuver : currentLaneConnection.maneuvers) {
										if ((unsignalizedManeuver == 8) || (unsignalizedManeuver == 10)) {
											unsignalizedMovementStates.setUnsignalizedMovementStatesValue(UnsignalizedMovementStates.PERMISSIVE_MOVEMENT_ALLOWED);
											break;
										} else if (unsignalizedManeuver == 9) {
											unsignalizedMovementStates.setUnsignalizedMovementStatesValue(UnsignalizedMovementStates.STOP_THEN_PROCEED);
											break;
										}
									}
									wayCnxnManeuverControlType.setUnsignalizedMovementStates(unsignalizedMovementStates);
									break;
								case "Uncontrolled":
									wayCnxnManeuverControlType.setChoice(WayCnxnManeuverControlType.UNCONTROLLED);
									break;
								default:
									break;
							}
							cnxnManeuverInfo.setManeuverControlType(wayCnxnManeuverControlType);
							wayCnxnManeuverInfo.addManeuverInfo(cnxnManeuverInfo);

						}
					}
				}
				individualWayCnxnsManeuvers.addWayCnxnManeuverInfo(wayCnxnManeuverInfo);
			}
		}
		return individualWayCnxnsManeuvers;
	}

	public WayCnxnManeuvers buildWayCnxnManeuvers(int currentManeuver, String maneuverControlType) {
		WayCnxnManeuvers wayCnxnManeuvers = new WayCnxnManeuvers();

		if (currentManeuver == 0) {
			wayCnxnManeuvers.setWayCnxnManeuvers(WayCnxnManeuvers.STRAIGHT);
		} else if (currentManeuver == 1) {
			wayCnxnManeuvers.setWayCnxnManeuvers(WayCnxnManeuvers.LEFT_TURN);
		} else if (currentManeuver == 2) {
			wayCnxnManeuvers.setWayCnxnManeuvers(WayCnxnManeuvers.RIGHT_TURN);
		} else if (currentManeuver == 3) {
			wayCnxnManeuvers.setWayCnxnManeuvers(WayCnxnManeuvers.LEFT_U_TURN);
		} else if (currentManeuver == 12) {
			wayCnxnManeuvers.setWayCnxnManeuvers(WayCnxnManeuvers.RIGHT_U_TURN);
		}

		if (maneuverControlType.equals("Signalized")) {
			if (currentManeuver == 4) {
				wayCnxnManeuvers.setWayCnxnManeuvers(WayCnxnManeuvers.LEFT_TURN);
			} else if (currentManeuver == 5) {
				wayCnxnManeuvers.setWayCnxnManeuvers(WayCnxnManeuvers.RIGHT_TURN);
			}
		} 
		return wayCnxnManeuvers;
	}

	public IndividualWayConnections buildIndividualWayConnections(String laneId, LaneConnection[] laneConnections,
			IntersectionInputData isdInputData) {
		IndividualWayConnections individualWayConnections = new IndividualWayConnections();
		individualWayConnections.setWayID(Integer.valueOf(laneId));

		for (int conIndex = 0; conIndex < laneConnections.length; conIndex++) {
			WayToWayConnectionInfo wayToWayConnectionInfo = new WayToWayConnectionInfo();
			LaneConnectionFromInfo connectionFromInfo = new LaneConnectionFromInfo();
			LaneConnectionToInfo connectionToInfo = new LaneConnectionToInfo();

			LaneConnection currentLaneConnection = laneConnections[conIndex];

			if (currentLaneConnection.toLane <= 0)
				continue;

			if (currentLaneConnection.connectionId > 0) {
				wayToWayConnectionInfo.setLaneConnectionID(currentLaneConnection.connectionId);

				// Setting connectionFromInfo
				connectionFromInfo.setNodeFromPosition(LaneConnectionFromInfo.FIRST_NODE);
				wayToWayConnectionInfo.setConnectionFromInfo(connectionFromInfo);

				if (currentLaneConnection.toLane > 0) {
					// Setting connectionToInfo
					connectionToInfo.setWayID(currentLaneConnection.toLane);
					connectionToInfo.setNodeToPosition(LaneConnectionToInfo.FIRST_NODE);
					WayType remoteWayType = new WayType();

					// This logic is used to find the remote lane id's vehicle type
					Approach[] allApproaches = isdInputData.mapData.intersectionGeometry.laneList.approach;
					for (Approach approach : allApproaches) {
						if (approach.drivingLanes != null) {
							for (DrivingLane lane : approach.drivingLanes) {
								if (lane.laneID != null && Integer.parseInt(lane.laneID) == currentLaneConnection.toLane
										&& lane.laneType != null && !lane.laneType.trim().isEmpty()) {
									String remoteVehicleType = lane.laneType;
									if ((remoteVehicleType.toLowerCase()).equals("vehicle")) {
										remoteWayType.setWayTypeValue(WayType.MOTOR_VEHICLE_LANE);
										connectionToInfo.setWayType(remoteWayType);
									} else if ((remoteVehicleType.toLowerCase()).equals("bike")) {
										remoteWayType.setWayTypeValue(WayType.BICYCLE_LANE);
										connectionToInfo.setWayType(remoteWayType);
									}
									break;
								}
							}
						}
					}

					wayToWayConnectionInfo.setConnectionToInfo(connectionToInfo);

					if (currentLaneConnection.timeRestrictions != null) {
						wayToWayConnectionInfo
								.setTimeRestrictions(buildLaneTimeRestriction(currentLaneConnection.timeRestrictions));
					}
				}
			}

			if (wayToWayConnectionInfo.getLaneConnectionID() > 0 &&
					wayToWayConnectionInfo.getConnectionFromInfo() != null &&
					wayToWayConnectionInfo.getConnectionToInfo() != null) {
				individualWayConnections.addWayToWayConnectionInfo(wayToWayConnectionInfo);
			}
		}

		return individualWayConnections;
	}

	/**
	 * This function builds and returns list of geometry containers for RGA Data
	 * @param isdInputData
	 * @return geometryContainers
	 */
	public List<GeometryContainer> buildGeometryContainers(IntersectionInputData isdInputData) {
		List<GeometryContainer> geometryContainers = new ArrayList<>();

		// Checking if approaches are null
		if (isdInputData.mapData.intersectionGeometry.laneList == null
				|| isdInputData.mapData.intersectionGeometry.laneList.approach == null) {
			return geometryContainers;
		}

		Approach[] approaches = isdInputData.mapData.intersectionGeometry.laneList.approach;

		ReferencePoint referencePoint = isdInputData.mapData.intersectionGeometry.referencePoint;
		OffsetEncoding offsetEncoding = new OffsetEncoding(isdInputData.nodeOffsets);
		if (offsetEncoding.type != OffsetEncodingType.Tight) {
			offsetEncoding.size = getOffsetEncodingSize(offsetEncoding.type, approaches, referencePoint);
		}

		GeometryContainer approachGeometryContainer = new GeometryContainer();
		ApproachGeometryLayer approachGeometryLayer = new ApproachGeometryLayer();

		GeometryContainer motorVehicleGeometryContainer = new GeometryContainer();
		MotorVehicleLaneGeometryLayer motorVehicleLaneGeometryLayer = new MotorVehicleLaneGeometryLayer();

		GeometryContainer bicycleGeometryContainer = new GeometryContainer();
		BicycleLaneGeometryLayer bicycleLaneGeometryLayer = new BicycleLaneGeometryLayer();

		GeometryContainer crosswalkGeometryContainer = new GeometryContainer();
		CrosswalkLaneGeometryLayer crosswalkLaneGeometryLayer = new CrosswalkLaneGeometryLayer();

		for (int approachIndex = 0; approachIndex < approaches.length; approachIndex++) {
			Approach approach = approaches[approachIndex];
			IndividualApproachGeometryInfo individualApproachGeometryInfo = new IndividualApproachGeometryInfo();
			ApproachWayTypeIDSet mtrVehicleApproachWayTypeIDSet = new ApproachWayTypeIDSet();
			ApproachWayTypeIDSet bicycleApproachWayTypeIDSet = new ApproachWayTypeIDSet();

			// Excluding crosswalk lanes as currently crosswalks do not have an approach id and it is default to -1
			if (approach.approachID != IntersectionInputData.CrosswalkLane.CROSSWALK_APPROACH_ID) {
				List<Long> mtrVehicleWayIDSet =  new ArrayList<>();
				List<Long> bicycleWayIDSet =  new ArrayList<>();

				// Setting approach ID
				individualApproachGeometryInfo.setApproachID(approach.approachID);

				// Loop through the driving lanes
				for (int drivingLaneIndex = 0; drivingLaneIndex < approach.drivingLanes.length; drivingLaneIndex++) {
					DrivingLane drivingLane = approach.drivingLanes[drivingLaneIndex];

					if ((drivingLane.laneType.toLowerCase()).equals("vehicle")) {
						// Setting the MotorVehicleLaneGeometryLayer
						motorVehicleLaneGeometryLayer.addIndvMtrVehLaneGeometryInfo(buildIndvMtrVehLaneGeometryInfo(drivingLane, referencePoint, offsetEncoding));
						mtrVehicleWayIDSet.add(Long.valueOf(drivingLane.laneID));
					} else if ((drivingLane.laneType.toLowerCase()).equals("bike")) {
						// Setting the BicycleLaneGeometryLayer
						bicycleLaneGeometryLayer.addIndvBikeLaneGeometryInfo(buildIndvBikeLaneGeometryInfo(drivingLane, referencePoint, offsetEncoding));
						bicycleWayIDSet.add(Long.valueOf(drivingLane.laneID));
					}
				}

				
				if (!mtrVehicleWayIDSet.isEmpty()) {
					WayType currentWayType1 = new WayType();
					currentWayType1.setWayTypeValue(WayType.MOTOR_VEHICLE_LANE);
					mtrVehicleApproachWayTypeIDSet.setWayType(currentWayType1);
					mtrVehicleApproachWayTypeIDSet.setWayIDSet(mtrVehicleWayIDSet);

					// Populate mtrVehicleApproachWayTypeIDSet into the individualApproachGeometryInfo
					individualApproachGeometryInfo.addIndividualWayTypesSet(mtrVehicleApproachWayTypeIDSet);
				}

				if (!bicycleWayIDSet.isEmpty()) {
					WayType currentWayType2 = new WayType();
					currentWayType2.setWayTypeValue(WayType.BICYCLE_LANE);
					bicycleApproachWayTypeIDSet.setWayType(currentWayType2);
					bicycleApproachWayTypeIDSet.setWayIDSet(bicycleWayIDSet);

					// Populate bicycleApproachWayTypeIDSet into the individualApproachGeometryInfo
					individualApproachGeometryInfo.addIndividualWayTypesSet(bicycleApproachWayTypeIDSet);
				}

				approachGeometryLayer.addIndividualApproachGeometryInfo(individualApproachGeometryInfo);
			} else {
				// Loop through the crosswalk lanes
				for (int crosswalkLaneIndex = 0; crosswalkLaneIndex < approach.crosswalkLanes.length; crosswalkLaneIndex++) {
					CrosswalkLane crosswalkLane = approach.crosswalkLanes[crosswalkLaneIndex];
					if ((crosswalkLane.laneType.toLowerCase()).equals("crosswalk")) {
						// Setting the CrosswalkLaneGeometryLayer
						crosswalkLaneGeometryLayer.addIndvCrosswalkLaneGeometryInfo(buildIndvCrosswalkLaneGeometryInfo(crosswalkLane, referencePoint, offsetEncoding));
					}
				}
			}

			
		}

		// Check if approachGeometryLayer approachGeomApproachSet is not empty
		if (!approachGeometryLayer.getApproachGeomApproachSet().isEmpty()) {
			// Setting the Approach Geometry Layer
			approachGeometryContainer.setGeometryContainerID(GeometryContainer.APPROACH_GEOMETRY_LAYER_ID);
			approachGeometryContainer.setApproachGeometryLayer(approachGeometryLayer);

			// Adding approachGeometryContainer to the list of containers
			geometryContainers.add(approachGeometryContainer);
		}

		// Check if motorVehicleLaneGeometryLayer laneGeomLaneSet is not empty
		if (!motorVehicleLaneGeometryLayer.getLaneGeomLaneSet().isEmpty()) {
			// Setting the Motor Vehicle Lane Geometry Layer
			motorVehicleGeometryContainer
					.setGeometryContainerID(GeometryContainer.MOTOR_VEHICLE_LANE_GEOMETRY_LAYER_ID);
			motorVehicleGeometryContainer.setMotorVehicleLaneGeometryLayer(motorVehicleLaneGeometryLayer);

			// Adding motorVehicleGeometryContainer to the list of containers
			geometryContainers.add(motorVehicleGeometryContainer);
		}

		// Check if bicycleLaneGeometryLayer laneGeomLaneSet is not empty
		if (!bicycleLaneGeometryLayer.getLaneGeomLaneSet().isEmpty()) {
			// Setting the Bicycle Lane Geometry Layer
			bicycleGeometryContainer.setGeometryContainerID(GeometryContainer.BICYCLE_LANE_GEOMETRY_LAYER_ID);
			bicycleGeometryContainer.setBicycleLaneGeometryLayer(bicycleLaneGeometryLayer);

			// Adding bicycleGeometryContainer to the list of containers
			geometryContainers.add(bicycleGeometryContainer);
		}

		// Check if crosswalkLaneGeometryLayer laneGeomLaneSet is not empty 
		if (!crosswalkLaneGeometryLayer.getLaneGeomLaneSet().isEmpty()) {
			// Setting the Crosswalk Lane Geometry Layer
			crosswalkGeometryContainer.setGeometryContainerID(GeometryContainer.CROSSWALK_LANE_GEOMETRY_LAYER_ID);
			crosswalkGeometryContainer.setCrosswalkLaneGeometryLayer(crosswalkLaneGeometryLayer);

			// Adding crosswalkGeometryContainer to the list of containers
			geometryContainers.add(crosswalkGeometryContainer);
		}

		return geometryContainers;
	}

	/**
	 * This method takes time restriction from Intersection Input and returns RGA Time Restrictions
	 * @param timeRestrictions
	 * @return
	 */
	public RGATimeRestrictions buildLaneTimeRestriction(TimeRestrictions timeRestrictions) {
		RGATimeRestrictions rgaTimeRestrictions = new RGATimeRestrictions();
		TimeWindowItemControlInfo timeWindowItemControlInfo = new TimeWindowItemControlInfo();
		TimeWindowInformation timeWindowInformation = new TimeWindowInformation();

		if(timeRestrictions.daysOfTheWeek != null && timeRestrictions.daysOfTheWeek.length > 0) {
			DaysOfTheWeek daysOfTheWeek = new DaysOfTheWeek();
			int daysOfTheWeekBitString = BitStringHelper.getBitString(SMALL_BIT_STRING, SMALL_BIT_STRING_LENGTH, timeRestrictions.daysOfTheWeek);
			daysOfTheWeek.setDaysOfTheWeekValue((short)daysOfTheWeekBitString);
			timeWindowInformation.setDaysOfTheWeek(daysOfTheWeek);
		} 

		if(timeRestrictions.timePeriodType != null && !timeRestrictions.timePeriodType.equals("none")) {
			if(timeRestrictions.timePeriodType.equals("general") && timeRestrictions.timePeriodValue != null) {
				GeneralPeriod generalPeriod = new GeneralPeriod();
				if(timeRestrictions.timePeriodValue.equals("day")) {
					generalPeriod.setGeneralPeriodValue(GeneralPeriod.DAY);
				}

				if(timeRestrictions.timePeriodValue.equals("night")) {
					generalPeriod.setGeneralPeriodValue(GeneralPeriod.NIGHT);
				}

				timeWindowInformation.setGeneralPeriod(generalPeriod);
			} 

			if(timeRestrictions.timePeriodType.equals("range") && timeRestrictions.timePeriodRange != null ) {
				if(timeRestrictions.timePeriodRange.startDatetime != null && !(timeRestrictions.timePeriodRange.startDatetime).isEmpty()) {
					String startDDateTimeString = timeRestrictions.timePeriodRange.startDatetime;
					DDateTime startDDateTime = buildDDateTime(startDDateTimeString, timeRestrictions.timePeriodRange.startOffset);
					timeWindowInformation.setStartPeriod(startDDateTime);
				}

				if(timeRestrictions.timePeriodRange.endDatetime != null && !(timeRestrictions.timePeriodRange.endDatetime).isEmpty()) {
					String endDDateTimeString = timeRestrictions.timePeriodRange.endDatetime;
					DDateTime endDDateTime = buildDDateTime(endDDateTimeString, timeRestrictions.timePeriodRange.endOffset);
					timeWindowInformation.setEndPeriod(endDDateTime);
				}
			}		
		}
		
		timeWindowItemControlInfo.addTimeWindowSet(timeWindowInformation);
		rgaTimeRestrictions.setChoice(RGATimeRestrictions.TIME_WINDOW_ITEM_CONTROL);
		rgaTimeRestrictions.setFixedTimeWindowCtrl(timeWindowItemControlInfo);

		return rgaTimeRestrictions; 
	}

	/**
	 * This method takes dateTimeString and offset as inputs and returns RGA DDateTIme
	 * @param dateTimeString
	 * @param offset
	 * @return
	 */
	public DDateTime buildDDateTime(String dateTimeString, int offset) {
		DDateTime currentDDateTimeValue = new DDateTime();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);

		currentDDateTimeValue.setYear(dateTime.getYear());
		currentDDateTimeValue.setMonth(dateTime.getMonthValue());
		currentDDateTimeValue.setDay(dateTime.getDayOfMonth());
		currentDDateTimeValue.setHour(dateTime.getHour());
		currentDDateTimeValue.setMinute(dateTime.getMinute());
		currentDDateTimeValue.setSecond(dateTime.getSecond());
		currentDDateTimeValue.setOffset(offset);

		return currentDDateTimeValue;
	}

	/**
	 * This method creates and returns RGA IndvMtrVehLaneGeometryInfo object for each of the motor vehicle lanes
	 * @param drivingLane
	 * @param referencePoint
	 * @param offsetEncoding
	 * @return indvMtrVehLaneGeometryInfo
	 */
	public IndvMtrVehLaneGeometryInfo buildIndvMtrVehLaneGeometryInfo(DrivingLane drivingLane, ReferencePoint referencePoint, OffsetEncoding offsetEncoding) {
		IndvMtrVehLaneGeometryInfo indvMtrVehLaneGeometryInfo = new IndvMtrVehLaneGeometryInfo();
		indvMtrVehLaneGeometryInfo.setLaneID(Integer.valueOf(drivingLane.laneID));
		indvMtrVehLaneGeometryInfo.setLaneConstructorType(buildLaneConstructorType(drivingLane, referencePoint, offsetEncoding));
		if(drivingLane.timeRestrictions != null && checkTimeRestrictionsObject(drivingLane.timeRestrictions)) {
			indvMtrVehLaneGeometryInfo.setTimeRestrictions(buildLaneTimeRestriction(drivingLane.timeRestrictions));
		}
		return indvMtrVehLaneGeometryInfo;
	}

	/**
	 * This method creates and returns RGA IndvBikeLaneGeometryInfo object for each of the bike lanes
	 * @param drivingLane
	 * @param referencePoint
	 * @param offsetEncoding
	 * @return indvBikeLaneGeometryInfo
	 */
	public IndvBikeLaneGeometryInfo buildIndvBikeLaneGeometryInfo(DrivingLane drivingLane, ReferencePoint referencePoint, OffsetEncoding offsetEncoding) {
		IndvBikeLaneGeometryInfo indvBikeLaneGeometryInfo = new IndvBikeLaneGeometryInfo();
		indvBikeLaneGeometryInfo.setLaneID(Integer.valueOf(drivingLane.laneID));
		indvBikeLaneGeometryInfo.setLaneConstructorType(buildLaneConstructorType(drivingLane, referencePoint, offsetEncoding));
		if (drivingLane.timeRestrictions != null && checkTimeRestrictionsObject(drivingLane.timeRestrictions)) {
			indvBikeLaneGeometryInfo.setTimeRestrictions(buildLaneTimeRestriction(drivingLane.timeRestrictions));
		}
		return indvBikeLaneGeometryInfo;
	}

	/**
	 * This method creates and returns RGA IndvCrosswalkLaneGeometryInfo object for each of the crosswalk lanes
	 * @param crosswalkLane
	 * @param referencePoint
	 * @param offsetEncoding
	 * @return indvCrosswalkLaneGeometryInfo
	 */
	public IndvCrosswalkLaneGeometryInfo buildIndvCrosswalkLaneGeometryInfo(CrosswalkLane crosswalkLane, ReferencePoint referencePoint, OffsetEncoding offsetEncoding) {
		IndvCrosswalkLaneGeometryInfo indvCrosswalkLaneGeometryInfo = new IndvCrosswalkLaneGeometryInfo();
		indvCrosswalkLaneGeometryInfo.setLaneID(Integer.valueOf(crosswalkLane.laneID));
		indvCrosswalkLaneGeometryInfo.setLaneConstructorType(buildLaneConstructorType(crosswalkLane, referencePoint, offsetEncoding));
		if(crosswalkLane.timeRestrictions != null && checkTimeRestrictionsObject(crosswalkLane.timeRestrictions)) {
			indvCrosswalkLaneGeometryInfo.setTimeRestrictions(buildLaneTimeRestriction(crosswalkLane.timeRestrictions));
		}
		return indvCrosswalkLaneGeometryInfo;
	}

	/**
	 * Checks if time restrictions object is not empty
	 * @param timeRestrictions
	 * @return
	 */
	public boolean checkTimeRestrictionsObject(TimeRestrictions timeRestrictions) {
		if (timeRestrictions == null) return false;
	
		boolean hasDays = timeRestrictions.daysOfTheWeek != null && timeRestrictions.daysOfTheWeek.length > 0;
		boolean hasType = timeRestrictions.timePeriodType != null && !timeRestrictions.timePeriodType.trim().isEmpty();
	
		return hasDays || hasType;
	}
	
	/**
	 * This method creates and returns RGA LaneConstructorType object for each of motor vehicle, bike and crosswalk lanes
	 * Both Physical and Computed Lane Node Offsets are set in this method
	 * @param lane
	 * @param referencePoint
	 * @param offsetEncoding
	 * @return laneConstructorType
	 */ 
	public LaneConstructorType buildLaneConstructorType(DrivingLane lane, ReferencePoint referencePoint, OffsetEncoding offsetEncoding) {
		LaneConstructorType laneConstructorType = new LaneConstructorType();
		if (!lane.isComputed) {
			laneConstructorType.setChoice(LaneConstructorType.PHYSICAL_NODE);
			PhysicalXYZNodeInfo physicalXYZNodeInfo = new PhysicalXYZNodeInfo();

			GeoPoint refPoint = new GeoPoint(referencePoint.referenceLat, referencePoint.referenceLon, referencePoint.referenceElevation);

			// Loop through the lane nodes
			for (LaneNode laneNode : lane.laneNodes) {
				IndividualXYZNodeGeometryInfo individualXYZNodeGeometryInfo = new IndividualXYZNodeGeometryInfo();
				GeoPoint nextPoint = new GeoPoint(laneNode.nodeLat, laneNode.nodeLong, laneNode.nodeElev);

				// Get Encoding Size based on given points if type is Tight
				if (offsetEncoding.type == OffsetEncodingType.Tight) {
					// Here, both refPoint and nextPoint are passed to the getOffsetEncodingSize method in OffsetEncoding java class
					// getOffsetEncodingSize method calculates difference between both latitudes and longitudes using the GeoPoint Java class
					// After obtaining the lat and lon offsets, it compares which one is larger
					// Now, larger offset is compared with the maxSize of defined enum to obtainer the right size
					offsetEncoding.size = offsetEncoding.getOffsetEncodingSize(refPoint, nextPoint);
				}

				NodeXYZOffsetInfo nodeXYZOffsetInfo = offsetEncoding.encodeRGAOffset(refPoint, nextPoint);
				individualXYZNodeGeometryInfo.setNodeXYZOffsetInfo(nodeXYZOffsetInfo);

				WayWidth wayWidth = new WayWidth();
				// Here, primary node is set to full width while rest of the nodes are set to delta width
				if(laneNode.nodeNumber == 0) {
					wayWidth.setChoice(WayWidth.FULL_WIDTH);
					wayWidth.setFullWidth(referencePoint.masterLaneWidth + laneNode.laneWidthDelta);
				} else {
					wayWidth.setChoice(WayWidth.DELTA_WIDTH);
					wayWidth.setDeltaWidth(laneNode.laneWidthDelta);
				}

				WayPlanarGeometryInfo nodeLocPlanarGeomInfo = new WayPlanarGeometryInfo();
				nodeLocPlanarGeomInfo.setWayWidth(wayWidth);
				individualXYZNodeGeometryInfo.setNodeLocPlanarGeomInfo(nodeLocPlanarGeomInfo);
				physicalXYZNodeInfo.addIndividualXYZNodeGeometryInfo(individualXYZNodeGeometryInfo);

				// refPoint is updated to nextPoint at the end of for loop
				refPoint = nextPoint;
			}
			laneConstructorType.setPhysicalXYZNodeInfo(physicalXYZNodeInfo);
		} else {
			laneConstructorType.setChoice(LaneConstructorType.COMPUTED_NODE);
			ComputedXYZNodeInfo computedXYZNodeInfo = new ComputedXYZNodeInfo();
			NodeXYZOffsetInfo laneCenterLineXYZOffset = new NodeXYZOffsetInfo();

			// Currently setting computed lane node offsets only to OFFSET_B12 to match MAP message computed lane encoding
			NodeXYZOffsetValue nodeXOffsetValue = new NodeXYZOffsetValue();
			nodeXOffsetValue.setChoice(NodeXYZOffsetValue.OFFSET_B12);
			nodeXOffsetValue.setOffsetB12((long)lane.computedLane.offsetX);
			laneCenterLineXYZOffset.setNodeXOffsetValue(nodeXOffsetValue);

			NodeXYZOffsetValue nodeYOffsetValue = new NodeXYZOffsetValue();
			nodeYOffsetValue.setChoice(NodeXYZOffsetValue.OFFSET_B12);
			nodeYOffsetValue.setOffsetB12((long)lane.computedLane.offsetY);
			laneCenterLineXYZOffset.setNodeYOffsetValue(nodeYOffsetValue);

			NodeXYZOffsetValue nodeZOffsetValue = new NodeXYZOffsetValue();
			nodeZOffsetValue.setChoice(NodeXYZOffsetValue.OFFSET_B12);
			nodeZOffsetValue.setOffsetB12((long)lane.computedLane.offsetZ);
			laneCenterLineXYZOffset.setNodeZOffsetValue(nodeZOffsetValue);

			WayWidth wayWidth = new WayWidth();
			wayWidth.setChoice(WayWidth.FULL_WIDTH);
			wayWidth.setFullWidth(referencePoint.masterLaneWidth);

			WayPlanarGeometryInfo lanePlanarGeomInfo = new WayPlanarGeometryInfo(); 
			lanePlanarGeomInfo.setWayWidth(wayWidth);
			
			computedXYZNodeInfo.setRefLaneID(Integer.valueOf(lane.computedLane.referenceLaneID));
			computedXYZNodeInfo.setLaneCenterLineXYZOffset(laneCenterLineXYZOffset);
			computedXYZNodeInfo.setLanePlanarGeomInfo(lanePlanarGeomInfo);
			laneConstructorType.setComputedXYZNodeInfo(computedXYZNodeInfo);
		}
		return laneConstructorType;
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

		if (referencePoint.roadAuthorityIdType != null) {
			if (referencePoint.roadAuthorityIdType.replaceAll("\\s", "").toLowerCase().equals("full")) {
				intersection.setRelRdAuthIDExists(false);
				intersection.setFullRdAuthIDExists(true);
				intersection.setFullRdAuthID(referencePoint.roadAuthorityId);
			} else if (referencePoint.roadAuthorityIdType.replaceAll("\\s", "").toLowerCase().equals("relative")) {
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
		if (referencePointChild != null && referencePointChild.speedLimitType != null
				&& referencePointChild.speedLimitType.length > 0) {
			SpeedLimitList speedLimitList = new SpeedLimitList();
			int speedLimitListLength = referencePointChild.speedLimitType.length;
			RegulatorySpeedLimit[] regulatorySpeedLimits = new RegulatorySpeedLimit[speedLimitListLength];
			for (int regIndex = 0; regIndex < speedLimitListLength; regIndex++) {
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

		if (offsetEncoding.type != OffsetEncodingType.Tight) {
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
			if (approach.approachID != IntersectionInputData.CrosswalkLane.CROSSWALK_APPROACH_ID
					&& approach.drivingLanes != null && approach.drivingLanes.length > 0) {
				// Loop through all the driving lanes for each approach
				for (int j = 0; j < approach.drivingLanes.length; j++) {
					DrivingLane drivingLane = approach.drivingLanes[j];
					GenericLane lane = new GenericLane();
					int laneDirectionBitString = SMALL_BIT_STRING;

					// Set LaneID
					lane.setLaneID(Integer.valueOf(drivingLane.laneID));

					// Set Lane Name
					if (drivingLane.descriptiveName != null && !drivingLane.descriptiveName.isEmpty()) {
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
					if (drivingLane.laneManeuvers != null && drivingLane.laneManeuvers.length > 0) {
						lane.setManeuversExists(true);
						lane.setManeuvers(buildAllowedManeuvers(drivingLane.laneManeuvers));
					}

					// Set NodeList Choice to Lane;
					lane.setNodeList(buildNodeList(isdInputData, drivingLane, referencePoint, offsetEncoding));

					// Set Connections
					if (drivingLane.connections != null && drivingLane.connections.length > 0) {
						Connection[] allConnections = buildConnectsTo(drivingLane.connections);
						if (allConnections != null) {
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
					if (crosswalkLane.descriptiveName != null && !crosswalkLane.descriptiveName.isEmpty()) {
						lane.setNameExists(true);
						lane.setName(crosswalkLane.descriptiveName);
					}

					int laneDirectionBitString = SMALL_BIT_STRING;

					// Set LaneAttributes to Lane
					lane.setLaneAttributes(buildLaneAttributes(crosswalkLane, laneDirectionBitString));

					// Set NodeList Choice to Lane;
					lane.setNodeList(buildNodeList(isdInputData, crosswalkLane, referencePoint, offsetEncoding));

					// Set Connections
					if (crosswalkLane.connections != null && crosswalkLane.connections.length > 0) {
						Connection[] allConnections = buildConnectsTo(crosswalkLane.connections);
						if (allConnections != null) {
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
	private NodeListXY buildNodeList(IntersectionInputData isdInputData, DrivingLane lane, ReferencePoint referencePoint, OffsetEncoding offsetEncoding) {
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
			offsetXaxis.setSmall((short) lane.computedLane.offsetX);

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
			if (currentLaneConnection.connectionId > 0) {
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
	private OffsetEncodingSize getOffsetEncodingSize(OffsetEncodingType offsetEncodingType, Approach[] approaches, ReferencePoint referencePoint) {
		OffsetEncodingSize offsetEncodingSize;

		switch (offsetEncodingType) {
			case Compact:
				int longestApproachOffsetInCm = 0;
				for (Approach approach : approaches) {
					if (approach.approachType != null && approach.approachType.equalsIgnoreCase(CrosswalkLane.CROSSWALK_APPROACH_TYPE)) {
						for (CrosswalkLane crosswalkLane : approach.crosswalkLanes) {
							int longestLaneOffsetInCm = Math
									.abs(getLongestOffsetDistanceInCm(referencePoint, crosswalkLane.laneNodes));
							if (longestLaneOffsetInCm > longestApproachOffsetInCm) {
								longestApproachOffsetInCm = longestLaneOffsetInCm;
							}
						}
					} else {
						for (DrivingLane drivingLane : approach.drivingLanes) {
							if (!drivingLane.isComputed) {
								int longestLaneOffsetInCm = Math
										.abs(getLongestOffsetDistanceInCm(referencePoint, drivingLane.laneNodes));
								if (longestLaneOffsetInCm > longestApproachOffsetInCm) {
									longestApproachOffsetInCm = longestLaneOffsetInCm;
								}
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

	private int getLongestOffsetDistanceInCm(IntersectionInputData.ReferencePoint referencePoint, LaneNode[] laneNodes) {
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