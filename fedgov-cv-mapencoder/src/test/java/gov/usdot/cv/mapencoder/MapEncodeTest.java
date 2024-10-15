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
package gov.usdot.cv.mapencoder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MapEncodeTest {
    private static final Logger logger = LogManager.getLogger(MapEncodeTest.class);
    Encoder encoder;
    MapData mockMap;
    DataParameters mockDataParameters;
    RoadSegmentList mockRoadSegmentList;
    RoadSegment mockRoadSegment;
    IntersectionGeometry mockIntersectionGeometry;
    IntersectionReferenceID mockIntersectionReferenceID;
    IntersectionGeometry mockIntersectionGeometry2;
    RestrictionClassAssignment mockRestrictionClass;
    RestrictionClassList mockRestrictionClassList;
    Position3D mockIntersectionRefPoint;
    SpeedLimitType mockSpeedLimitType;
    RegulatorySpeedLimit mockRegulatorySpeedLimit;
    SpeedLimitList mockSpeedLimitList;
    LaneList mockLaneList;
    GenericLane mockGenericLane;
    AllowedManeuvers mockAllowedManeuvers;
    LaneAttributes mockLaneAttributes;
    LaneDirection mockLaneDirection;
    LaneSharing mockLaneSharing;
    LaneTypeAttributes mockLaneTypeAttributes;
    NodeListXY mockNodeListXY;
    NodeSetXY mockNodeSetXY;
    NodeXY mockNodeXY1;
    NodeOffsetPointXY mockNodeOffsetPointXY1;
    NodeXY20b mockNodeXY20b;
    NodeXY mockNodeXY2;  
    NodeOffsetPointXY mockNodeOffsetPointXY2;
    NodeXY22b mockNodeXY22b;
    NodeXY mockNodeXY3;  
    NodeOffsetPointXY mockNodeOffsetPointXY3;
    NodeXY24b mockNodeXY24b;
    NodeXY mockNodeXY4;  
    NodeOffsetPointXY mockNodeOffsetPointXY4;
    NodeXY26b mockNodeXY26b;
    NodeXY mockNodeXY5;  
    NodeOffsetPointXY mockNodeOffsetPointXY5;
    NodeXY28b mockNodeXY28b;
    NodeXY mockNodeXY6;  
    NodeOffsetPointXY mockNodeOffsetPointXY6;
    NodeXY32b mockNodeXY32b;
    NodeXY mockNodeXY7;  
    NodeOffsetPointXY mockNodeOffsetPointXY7;
    NodeLLmD64b mockNodeLLmD64b;
    ComputedLane mockComputedLane;
    OffsetXaxis mockOffsetXaxis;
    OffsetYaxis mockOffsetYaxis;
    LaneAttributesVehicle mockLaneAttributesVehicle;
    LaneAttributesCrosswalk mockLaneAttributesCrosswalk;
    LaneAttributesBike mockLaneAttributesBike;
    LaneAttributesSidewalk mockLaneAttributesSidewalk;
    LaneAttributesBarrier mockLaneAttributesBarrier;
    LaneAttributesStriping mockLaneAttributesStriping;
    LaneAttributesTrackedVehicle mockLaneAttributesTrackedVehicle;
    LaneAttributesParking mockLaneAttributesParking;
    LaneDataAttribute mockLaneDataAttribute;
    LaneDataAttributeList mockLaneDataAttributeList;
    NodeAttributeSetXY mockNodeAttributeSetXY;
    Connection mockConnection;
    ConnectingLane mockConnectingLane;

    @Before
    public void setup() {

        // MapData(boolean time_stamp_exists, byte msg_issue_revision, int layer_type,
        //            int layer_id, boolean layer_id_exists, boolean intersections_exists, DataParameters data_parameters, RoadSegmentList road_segments,
        //            IntersectionGeometry[] intersections, RestrictionClassList restriction_list)


        mockMap = mock(MapData.class);
        mockDataParameters = mock(DataParameters.class);
        mockRoadSegment = mock(RoadSegment.class);
        mockRoadSegmentList = mock(RoadSegmentList.class);
        mockIntersectionGeometry = mock(IntersectionGeometry.class);
        mockIntersectionReferenceID = mock(IntersectionReferenceID.class);
        mockIntersectionGeometry2 = mock(IntersectionGeometry.class);
        mockRestrictionClassList = mock(RestrictionClassList.class);
        mockIntersectionRefPoint = mock(Position3D.class);
        mockSpeedLimitType = mock(SpeedLimitType.class);
        mockRegulatorySpeedLimit = mock(RegulatorySpeedLimit.class);
        mockSpeedLimitList = mock(SpeedLimitList.class);
        mockLaneList = mock(LaneList.class);
        mockGenericLane = mock(GenericLane.class);
        mockAllowedManeuvers = mock(AllowedManeuvers.class);
        mockLaneAttributes = mock(LaneAttributes.class);
        mockLaneDirection = mock(LaneDirection.class);
        mockLaneSharing = mock(LaneSharing.class);
        mockLaneTypeAttributes = mock(LaneTypeAttributes.class);
        mockNodeListXY = mock(NodeListXY.class);
        mockNodeSetXY = mock(NodeSetXY.class);
        mockNodeXY1 = mock(NodeXY.class);
        mockNodeOffsetPointXY1 = mock(NodeOffsetPointXY.class);
        mockNodeXY20b= mock(NodeXY20b.class);
        mockNodeXY2 = mock(NodeXY.class);    
        mockNodeOffsetPointXY2 = mock(NodeOffsetPointXY.class);
        mockNodeXY22b= mock(NodeXY22b.class);
        mockNodeXY3 = mock(NodeXY.class);    
        mockNodeOffsetPointXY3 = mock(NodeOffsetPointXY.class);
        mockNodeXY24b= mock(NodeXY24b.class);
        mockNodeXY4 = mock(NodeXY.class);    
        mockNodeOffsetPointXY4 = mock(NodeOffsetPointXY.class);
        mockNodeXY26b= mock(NodeXY26b.class);
        mockNodeXY5 = mock(NodeXY.class);    
        mockNodeOffsetPointXY5 = mock(NodeOffsetPointXY.class);
        mockNodeXY28b= mock(NodeXY28b.class);
        mockNodeXY6 = mock(NodeXY.class);    
        mockNodeOffsetPointXY6 = mock(NodeOffsetPointXY.class);
        mockNodeXY32b= mock(NodeXY32b.class);
        mockNodeXY7 = mock(NodeXY.class);    
        mockNodeOffsetPointXY7 = mock(NodeOffsetPointXY.class);
        mockNodeLLmD64b= mock(NodeLLmD64b.class);
        mockComputedLane = mock(ComputedLane.class);
        mockOffsetXaxis = mock(OffsetXaxis.class);
        mockOffsetYaxis = mock(OffsetYaxis.class);
        mockLaneAttributesVehicle = mock(LaneAttributesVehicle.class);
        mockLaneAttributesCrosswalk = mock(LaneAttributesCrosswalk.class);
        mockLaneAttributesBike = mock(LaneAttributesBike.class);
        mockLaneAttributesSidewalk = mock(LaneAttributesSidewalk.class);
        mockLaneAttributesBarrier = mock(LaneAttributesBarrier.class);
        mockLaneAttributesStriping= mock(LaneAttributesStriping.class);
        mockLaneAttributesTrackedVehicle = mock(LaneAttributesTrackedVehicle.class);
        mockLaneAttributesParking = mock(LaneAttributesParking.class);
        mockLaneDataAttribute = mock(LaneDataAttribute.class);
        mockLaneDataAttributeList = mock(LaneDataAttributeList.class);
        mockNodeAttributeSetXY = mock(NodeAttributeSetXY.class);
        mockConnection = mock(Connection.class);
        mockConnectingLane = mock(ConnectingLane.class);

        encoder = new Encoder();

        when(mockMap.getLayerId()).thenReturn(1);
        when(mockMap.getTimeStamp()).thenReturn(12L);
        when(mockMap.getMsgCount()).thenReturn(1);
        when(mockMap.getLayerType()).thenReturn(1);
        when(mockDataParameters.getProcessMethod()).thenReturn("a");
        when(mockDataParameters.getProcessAgency()).thenReturn("b");
        when(mockDataParameters.getLastCheckedDate()).thenReturn("a");
        when(mockDataParameters.getGeoidUsed()).thenReturn("a");
        when(mockMap.getDataParameters()).thenReturn(mockDataParameters);
        when(mockRoadSegment.getName()).thenReturn("segment");
        when(mockRoadSegmentList.getRoadSegmentList()).thenReturn(new RoadSegment[]{mockRoadSegment});
        when(mockMap.getRoadSegmentList()).thenReturn(mockRoadSegmentList);
        when(mockRestrictionClassList.getRestrictionClassList()).thenReturn(new RestrictionClassAssignment[0]);
        when(mockMap.getRestrictionClassList()).thenReturn(mockRestrictionClassList);
        
        // Intersection Data
        when(mockIntersectionGeometry.isNameExists()).thenReturn(true);
        when(mockIntersectionGeometry.getName()).thenReturn("Intersection 1");

        // Intersection Id
        when(mockIntersectionReferenceID.isRegionExists()).thenReturn(true);
        when(mockIntersectionReferenceID.getRegion()).thenReturn(12);
        when(mockIntersectionReferenceID.getId()).thenReturn(11);
        when(mockIntersectionGeometry.getId()).thenReturn(mockIntersectionReferenceID);
        
        when(mockIntersectionGeometry.getRevision()).thenReturn(22);

        // RAID
        when(mockIntersectionGeometry.isFullRdAuthIDExists()).thenReturn(false);
        when(mockIntersectionGeometry.getFullRdAuthID()).thenReturn(new int[]{1, 2, 3});
        when(mockIntersectionGeometry.isRelRdAuthIDExists()).thenReturn(true);
        when(mockIntersectionGeometry.getRelRdAuthID()).thenReturn(new int[]{1, 2, 3});

        // Intersection Ref Point
        when(mockIntersectionRefPoint.getLatitude()).thenReturn((double)7.2);
        when(mockIntersectionRefPoint.getLongitude()).thenReturn((double)11.1);
        when(mockIntersectionRefPoint.isElevationExists()).thenReturn(true);
        when(mockIntersectionRefPoint.getElevation()).thenReturn((float)13.12);
        when(mockIntersectionGeometry.getRefPoint()).thenReturn(mockIntersectionRefPoint);

        when(mockIntersectionGeometry.isLaneWidthExists()).thenReturn(true);
        when(mockIntersectionGeometry.getLaneWidth()).thenReturn(99);
        when(mockSpeedLimitType.getSpeedLimitType()).thenReturn((byte)4);
        when(mockRegulatorySpeedLimit.getSpeed()).thenReturn((double)9);
        when(mockRegulatorySpeedLimit.getType()).thenReturn(mockSpeedLimitType);
        when(mockSpeedLimitList.getSpeedLimits()).thenReturn(new RegulatorySpeedLimit[]{mockRegulatorySpeedLimit});
        when(mockIntersectionGeometry.isSpeedLimitsExists()).thenReturn(true);
        when(mockIntersectionGeometry.getSpeedLimits()).thenReturn(mockSpeedLimitList);

        // Intersection Laneset
        when(mockGenericLane.getLaneID()).thenReturn(222);
        when(mockGenericLane.isNameExists()).thenReturn(true);
        when(mockGenericLane.getName()).thenReturn("Lane 1");
        when(mockGenericLane.isIngressApproachExists()).thenReturn(false);
        when(mockGenericLane.getIngressApproach()).thenReturn((byte)14);
        when(mockGenericLane.isEgressApproachExists()).thenReturn(true);
        when(mockGenericLane.getEgressApproach()).thenReturn((byte)15);
        when(mockAllowedManeuvers.getAllowedManeuvers()).thenReturn(0b1101000000000000);
        when(mockGenericLane.isManeuversExists()).thenReturn(true);
        when(mockGenericLane.getManeuvers()).thenReturn(mockAllowedManeuvers);

        when(mockLaneDirection.getLaneDirection()).thenReturn((byte)0b01000000);
        when(mockLaneAttributes.getLaneDirectionAttribute()).thenReturn(mockLaneDirection);

        when(mockLaneSharing.getLaneSharing()).thenReturn((short)0b0101010101000000);
        when(mockLaneAttributes.getLaneSharingAttribute()).thenReturn(mockLaneSharing);

        int laneTypeChoice = LaneTypeAttributes.SIDEWALK;
        when(mockLaneTypeAttributes.getChoice()).thenReturn((byte)laneTypeChoice);
        if (laneTypeChoice == LaneTypeAttributes.VEHICLE) {
            when(mockLaneAttributesVehicle.getLaneAttributesVehicle()).thenReturn((byte)0b11111111);
            when(mockLaneTypeAttributes.getVehicle()).thenReturn(mockLaneAttributesVehicle);
        } else if (laneTypeChoice == LaneTypeAttributes.CROSSWALK) {
            when(mockLaneAttributesCrosswalk.getLaneAttributesCrosswalk()).thenReturn((short)0b1111111110000000);
            when(mockLaneTypeAttributes.getCrosswalk()).thenReturn(mockLaneAttributesCrosswalk);
        } else if (laneTypeChoice == LaneTypeAttributes.BIKE_LANE) {
            when(mockLaneAttributesBike.getLaneAttributesBike()).thenReturn((short)0b11111110);
            when(mockLaneTypeAttributes.getBikeLane()).thenReturn(mockLaneAttributesBike);
        } else if (laneTypeChoice == LaneTypeAttributes.SIDEWALK) {
            when(mockLaneAttributesSidewalk.getLaneAttributesSidewalk()).thenReturn((short)0b11110000);
            when(mockLaneTypeAttributes.getSidewalk()).thenReturn(mockLaneAttributesSidewalk);
        } else if (laneTypeChoice == LaneTypeAttributes.MEDIAN) {
            when(mockLaneAttributesBarrier.getLaneAttributesBarrier()).thenReturn((short)0b1010100000000000);
            when(mockLaneTypeAttributes.getMedian()).thenReturn(mockLaneAttributesBarrier);
        } else if (laneTypeChoice == LaneTypeAttributes.STRIPING) {
            when(mockLaneAttributesStriping.getLaneAttributesStriping()).thenReturn((short)0b11111100);
            when(mockLaneTypeAttributes.getStriping()).thenReturn(mockLaneAttributesStriping);
        } else if (laneTypeChoice == LaneTypeAttributes.TRACKED_VEHICLE) {
            when(mockLaneAttributesTrackedVehicle.getLaneAttributesTrackedVehicle()).thenReturn((short)0b11111000);
            when(mockLaneTypeAttributes.getTrackedVehicle()).thenReturn(mockLaneAttributesTrackedVehicle);
        } else if (laneTypeChoice == LaneTypeAttributes.PARKING) {
            when(mockLaneAttributesParking.getLaneAttributesParking()).thenReturn((short)0b11111000);
            when(mockLaneTypeAttributes.getParking()).thenReturn(mockLaneAttributesParking);
        }
       
        when(mockLaneAttributes.getLaneTypeAttribute()).thenReturn(mockLaneTypeAttributes);

        when(mockNodeXY20b.getX()).thenReturn(2F);
        when(mockNodeXY20b.getY()).thenReturn(3F);
        when(mockNodeOffsetPointXY1.getChoice()).thenReturn((byte)1);
        when(mockNodeOffsetPointXY1.getNodeXY1()).thenReturn(mockNodeXY20b);
        when(mockNodeXY1.getDelta()).thenReturn(mockNodeOffsetPointXY1);

        when(mockNodeXY22b.getX()).thenReturn(4F);
        when(mockNodeXY22b.getY()).thenReturn(5F);
        when(mockNodeOffsetPointXY2.getChoice()).thenReturn((byte)2);
        when(mockNodeOffsetPointXY2.getNodeXY2()).thenReturn(mockNodeXY22b);
        when(mockNodeXY2.getDelta()).thenReturn(mockNodeOffsetPointXY2);

        when(mockNodeXY24b.getX()).thenReturn((short)6);
        when(mockNodeXY24b.getY()).thenReturn((short)7);
        when(mockNodeOffsetPointXY3.getChoice()).thenReturn((byte)3);
        when(mockNodeOffsetPointXY3.getNodeXY3()).thenReturn(mockNodeXY24b);
        when(mockNodeXY3.getDelta()).thenReturn(mockNodeOffsetPointXY3);

        when(mockNodeXY26b.getX()).thenReturn(8F);
        when(mockNodeXY26b.getY()).thenReturn(9F);
        when(mockNodeOffsetPointXY4.getChoice()).thenReturn((byte)4);
        when(mockNodeOffsetPointXY4.getNodeXY4()).thenReturn(mockNodeXY26b);
        when(mockNodeXY4.getDelta()).thenReturn(mockNodeOffsetPointXY4);

        when(mockNodeXY28b.getX()).thenReturn(10F);
        when(mockNodeXY28b.getY()).thenReturn(11F);
        when(mockNodeOffsetPointXY5.getChoice()).thenReturn((byte)5);
        when(mockNodeOffsetPointXY5.getNodeXY5()).thenReturn(mockNodeXY28b);
        when(mockNodeXY5.getDelta()).thenReturn(mockNodeOffsetPointXY5);

        when(mockNodeXY32b.getX()).thenReturn(12F);
        when(mockNodeXY32b.getY()).thenReturn(13F);
        when(mockNodeOffsetPointXY6.getChoice()).thenReturn((byte)6);
        when(mockNodeOffsetPointXY6.getNodeXY6()).thenReturn(mockNodeXY32b);
        when(mockNodeXY6.getDelta()).thenReturn(mockNodeOffsetPointXY6);

        when(mockNodeLLmD64b.getLatitude()).thenReturn(14);
        when(mockNodeLLmD64b.getLongitude()).thenReturn(15);
        when(mockNodeOffsetPointXY7.getChoice()).thenReturn((byte)7);
        when(mockNodeOffsetPointXY7.getNodeLatLon()).thenReturn(mockNodeLLmD64b);

        when(mockLaneDataAttribute.getChoice()).thenReturn(6);
        when(mockLaneDataAttribute.getSpeedLimits()).thenReturn(mockSpeedLimitList);
        when(mockLaneDataAttributeList.getLaneAttributeList()).thenReturn(new LaneDataAttribute[]{mockLaneDataAttribute});
        when(mockNodeAttributeSetXY.isDataExists()).thenReturn(true);
        when(mockNodeAttributeSetXY.getData()).thenReturn(mockLaneDataAttributeList);
        when(mockNodeAttributeSetXY.isDWidthExists()).thenReturn(true);
        when(mockNodeAttributeSetXY.getDWidth()).thenReturn((float)32);
        when(mockNodeAttributeSetXY.isDElevationExists()).thenReturn(true);
        when(mockNodeAttributeSetXY.getDElevation()).thenReturn((float)33);
        when(mockNodeXY7.getDelta()).thenReturn(mockNodeOffsetPointXY7);
        when(mockNodeXY1.isAttributesExists()).thenReturn(false);
        when(mockNodeXY2.isAttributesExists()).thenReturn(false);
        when(mockNodeXY3.isAttributesExists()).thenReturn(false);
        when(mockNodeXY4.isAttributesExists()).thenReturn(false);
        when(mockNodeXY5.isAttributesExists()).thenReturn(false);
        when(mockNodeXY6.isAttributesExists()).thenReturn(false);
        when(mockNodeXY7.isAttributesExists()).thenReturn(true);
        when(mockNodeXY7.getAttributes()).thenReturn(mockNodeAttributeSetXY);

        when(mockNodeSetXY.getNodeSetXY()).thenReturn(new NodeXY[]{mockNodeXY1, mockNodeXY2, mockNodeXY3, mockNodeXY4, mockNodeXY5, mockNodeXY6, mockNodeXY7});
        when(mockNodeListXY.getNodes()).thenReturn(mockNodeSetXY);

        when(mockComputedLane.getReferenceLaneId()).thenReturn(9);
        when(mockOffsetXaxis.getChoice()).thenReturn((byte)0);
        when(mockOffsetYaxis.getChoice()).thenReturn((byte)1);
        when(mockOffsetXaxis.getSmall()).thenReturn((short)22);
        when(mockOffsetYaxis.getLarge()).thenReturn((short)33);
        when(mockComputedLane.getOffsetXaxis()).thenReturn(mockOffsetXaxis);
        when(mockComputedLane.getOffsetYaxis()).thenReturn(mockOffsetYaxis);
        when(mockNodeListXY.getComputed()).thenReturn(mockComputedLane);
        when(mockNodeListXY.getChoice()).thenReturn((byte)0);

        when(mockConnectingLane.getLaneId()).thenReturn(5);
        when(mockConnectingLane.getManeuverExists()).thenReturn(true);
        when(mockConnectingLane.getManeuver()).thenReturn(mockAllowedManeuvers);
        when(mockConnection.getConnectingLane()).thenReturn(mockConnectingLane);
        when(mockConnection.getRemoteIntersectionExists()).thenReturn(true);
        when(mockConnection.getRemoteIntersection()).thenReturn(mockIntersectionReferenceID);
        when(mockConnection.getSignalGroupExists()).thenReturn(true);
        when(mockConnection.getSignalGroup()).thenReturn((long)223);
        when(mockConnection.getUserClassExists()).thenReturn(true);
        when(mockConnection.getUserClass()).thenReturn((long)224);
        when(mockConnection.getConnectionIDExists()).thenReturn(true);
        when(mockConnection.getConnectionID()).thenReturn((long)225);

        when(mockGenericLane.getLaneAttributes()).thenReturn(mockLaneAttributes);
        when(mockGenericLane.getNodeList()).thenReturn(mockNodeListXY);
        when(mockGenericLane.isConnectsToExists()).thenReturn(true);
        when(mockGenericLane.getConnections()).thenReturn(new Connection[]{mockConnection});
        when(mockLaneList.getLaneList()).thenReturn(new GenericLane[]{mockGenericLane});
        when(mockIntersectionGeometry.getLaneSet()).thenReturn(mockLaneList);
        when(mockMap.getIntersections()).thenReturn(new IntersectionGeometry[]{mockIntersectionGeometry});

        // when(mockMap.intersectionExists()).thenReturn(false);
    }


    @Test
    public void MAPEncodeTest() {
        long start = System.currentTimeMillis();
        logger.debug("mockMap timestamp: " + mockMap.getTimeStamp());
        ByteArrayObject res = encoder.encode(mockMap);
        long end = System.currentTimeMillis();
        try {
            for (byte b : res.getMessage()) {
                logger.debug(b + " ");
            }
        }
        catch(Exception e) {
            logger.error("Exception while printing res");
        }

        byte[] expected = { 0, 18, 110, 56 , 1, 16, 32, -16, -39, 59, -70, 101, -27, -49, 46, 62, -102, 119, -18, 64, -58, 0, 24, 0, 22, 89, 53, -92, -23, 7, 107, 73, -46, 10, 16, 13, 0, -58, 4, 0, 72, 2, -26, -16, -77, 48, -18, -54, -127, -113, -107, 84, -4, 0, 52, 0 , 20, 16, 20, 6, 24, 9, 1, 69, 0, -48, 14, 56, 4, 64, 36, -112, 5, 64, 22, 88, 0, -56, 0, -41, 53, -92, -23, 7, 53, -92, -23, 14, 14, 10, 4, 0, 76, 65, 16, -121, -63, 116, 2, 0, 24, 0, 23, -65, -63, -62, 2, 10, -127, -128, -127, 1, -128 };

        Assert.assertArrayEquals(expected, res.getMessage());
    }
}