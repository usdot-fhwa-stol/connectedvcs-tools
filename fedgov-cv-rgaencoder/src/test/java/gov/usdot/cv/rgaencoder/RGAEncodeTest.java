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
package gov.usdot.cv.rgaencoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import gov.usdot.cv.mapencoder.ByteArrayObject;
import gov.usdot.cv.mapencoder.Position3D;

public class RGAEncodeTest {
    private static final Logger logger = LogManager.getLogger(RGAEncodeTest.class);
    Encoder encoder;
    RGAData mockRGA;
    BaseLayer mockBaseLayer;
    Position3D mockLocation;
    DDate mockTimeOfCalculation;
    DDateTime mockContentDateTime;

    List<GeometryContainer> mockGeometryContainerList;
    List<MovementsContainer> mockMovementsContainerList;

    GeometryContainer mockGeometryContainer1;
    ApproachGeometryLayer mockApproachGeometryLayer1;
    List<IndividualApproachGeometryInfo> mockIndividualApproachGeometryInfoList1;
    IndividualApproachGeometryInfo mockIndividualApproachGeometryInfo1;
    IndividualApproachGeometryInfo mockIndividualApproachGeometryInfo2;
    List<ApproachWayTypeIDSet> mockApproachWayTypeIDSet1;
    ApproachWayTypeIDSet mockIndividualApproachWayTypeID1;
    ApproachWayTypeIDSet mockIndividualApproachWayTypeID2;
    List<Long> mockLaneIDSet1;
    WayType mockWayTypeID1;
    Long mockLaneID1;
    List<Long> mockLaneIDSet2;
    WayType mockWayTypeID2;
    Long mockLaneID2;

    GeometryContainer mockGeometryContainer2;
    ApproachGeometryLayer mockApproachGeometryLayer2;
    IndividualApproachGeometryInfo mockIndividualApproachGeometryInfo3;
    List<IndividualApproachGeometryInfo> mockIndividualApproachGeometryInfoList2;

    GeometryContainer mockGeometryContainer3;
    MotorVehicleLaneGeometryLayer mockMotorVehicleLaneGeometryLayer1;
    IndvMtrVehLaneGeometryInfo mockIndvMtrVehLaneGeometryInfo1;
    List<IndvMtrVehLaneGeometryInfo> mockIndvMtrVehLaneGeometryInfoList1;
    DuplicateXYZNodeInfo mockDuplicateXYZNodeInfo1;
    LaneConstructorType mockLaneConstructorType1;

    GeometryContainer mockGeometryContainer4;
    MotorVehicleLaneGeometryLayer mockMotorVehicleLaneGeometryLayer2;
    IndvMtrVehLaneGeometryInfo mockIndvMtrVehLaneGeometryInfo2;
    List<IndvMtrVehLaneGeometryInfo> mockIndvMtrVehLaneGeometryInfoList2;
    LaneConstructorType mockLaneConstructorType2;
    ComputedXYZNodeInfo mockComputedXYZNodeInfo1;
    NodeXYZOffsetInfo mockNodeXYZOffsetInfo1;
    NodeXYZOffsetValue mockNodeXYZOffsetValue1;
    NodeXYZOffsetValue mockNodeXYZOffsetValue2;
    NodeXYZOffsetValue mockNodeXYZOffsetValue3;
    WayWidth mockWayWidth1;
    WayPlanarGeometryInfo mockWayPlanarGeometryInfo1;
    DaysOfTheWeek mockDaysOfTheWeek1;
    DDateTime mockStartPeriod1;
    DDateTime mockEndPeriod1;
    GeneralPeriod mockGeneralPeriod1;
    TimeWindowInformation mockTimeWindowInformation1;
    List<TimeWindowInformation> mockTimeWindowSet1;
    TimeWindowItemControlInfo mockFixedTimeWindowCtrl1;
    RGATimeRestrictions mockTimeRestrictions1;

    GeometryContainer mockGeometryContainer5;
    MotorVehicleLaneGeometryLayer mockMotorVehicleLaneGeometryLayer3;
    IndvMtrVehLaneGeometryInfo mockIndvMtrVehLaneGeometryInfo3;
    List<IndvMtrVehLaneGeometryInfo> mockIndvMtrVehLaneGeometryInfoList3;
    LaneConstructorType mockLaneConstructorType3;
    PhysicalXYZNodeInfo mockPhysicalXYZNodeInfo1;
    IndividualXYZNodeGeometryInfo mockIndividualXYZNodeGeometryInfo1;
    List<IndividualXYZNodeGeometryInfo> mockIndividualXYZNodeGeometryInfoList1;
    NodeXYZOffsetInfo mockNodeXYZOffsetInfo2;
    NodeXYZOffsetValue mockNodeXYZOffsetValue4;
    NodeXYZOffsetValue mockNodeXYZOffsetValue5;
    NodeXYZOffsetValue mockNodeXYZOffsetValue6;
    WayWidth mockWayWidth2;
    WayPlanarGeometryInfo mockWayPlanarGeometryInfo4;
    ReferencePointInfo mockReferencePointInfo1;
    Position3D mockLocation1;
    DDate mockTimeOfCalculation1;
    OtherDSItemControlInfo mockOtherDataSetItemCtrl1;
    RGATimeRestrictions mockTimeRestrictions2;

    GeometryContainer mockGeometryContainer6;
    BicycleLaneGeometryLayer mockBicycleLaneGeometryLayer1;
    IndvBikeLaneGeometryInfo mockIndvBikeLaneGeometryInfo1;
    List<IndvBikeLaneGeometryInfo> mockIndvBikeLaneGeometryInfoList1;
    LaneConstructorType mockLaneConstructorType4;
    DuplicateXYZNodeInfo mockDuplicateXYZNodeInfo2;

    GeometryContainer mockGeometryContainer7;
    BicycleLaneGeometryLayer mockBicycleLaneGeometryLayer2;
    IndvBikeLaneGeometryInfo mockIndvBikeLaneGeometryInfo2;
    List<IndvBikeLaneGeometryInfo> mockIndvBikeLaneGeometryInfoList2;
    LaneConstructorType mockLaneConstructorType5;
    ComputedXYZNodeInfo mockComputedXYZNodeInfo2;
    NodeXYZOffsetInfo mockNodeXYZOffsetInfo3;
    NodeXYZOffsetValue mockNodeXYZOffsetValue7;
    NodeXYZOffsetValue mockNodeXYZOffsetValue8;
    NodeXYZOffsetValue mockNodeXYZOffsetValue9;
    WayWidth mockWayWidth3;
    WayPlanarGeometryInfo mockWayPlanarGeometryInfo2;
    DaysOfTheWeek mockDaysOfTheWeek2;
    DDateTime mockStartPeriod2;
    DDateTime mockEndPeriod2;
    GeneralPeriod mockGeneralPeriod2;
    TimeWindowInformation mockTimeWindowInformation2;
    List<TimeWindowInformation> mockTimeWindowSet2;
    TimeWindowItemControlInfo mockFixedTimeWindowCtrl2;
    RGATimeRestrictions mockTimeRestrictions3;

    GeometryContainer mockGeometryContainer8;
    BicycleLaneGeometryLayer mockBicycleLaneGeometryLayer3;
    IndvBikeLaneGeometryInfo mockIndvBikeLaneGeometryInfo3;
    List<IndvBikeLaneGeometryInfo> mockIndvBikeLaneGeometryInfoList3;
    LaneConstructorType mockLaneConstructorType6;
    PhysicalXYZNodeInfo mockPhysicalXYZNodeInfo2;
    IndividualXYZNodeGeometryInfo mockIndividualXYZNodeGeometryInfo2;
    List<IndividualXYZNodeGeometryInfo> mockIndividualXYZNodeGeometryInfoList2;
    NodeXYZOffsetInfo mockNodeXYZOffsetInfo4;
    NodeXYZOffsetValue mockNodeXYZOffsetValue10;
    NodeXYZOffsetValue mockNodeXYZOffsetValue11;
    NodeXYZOffsetValue mockNodeXYZOffsetValue12;
    WayWidth mockWayWidth4;
    WayPlanarGeometryInfo mockWayPlanarGeometryInfo5;

    GeometryContainer mockGeometryContainer9;
    CrosswalkLaneGeometryLayer mockCrosswalkLaneGeometryLayer1;
    IndvCrosswalkLaneGeometryInfo mockIndvCrosswalkLaneGeometryInfo1;
    List<IndvCrosswalkLaneGeometryInfo> mockIndvCrosswalkLaneGeometryInfoList1;
    LaneConstructorType mockLaneConstructorType7;
    DuplicateXYZNodeInfo mockDuplicateXYZNodeInfo3;

    GeometryContainer mockGeometryContainer10;
    CrosswalkLaneGeometryLayer mockCrosswalkLaneGeometryLayer2;
    IndvCrosswalkLaneGeometryInfo mockIndvCrosswalkLaneGeometryInfo2;
    List<IndvCrosswalkLaneGeometryInfo> mockIndvCrosswalkLaneGeometryInfoList2;
    LaneConstructorType mockLaneConstructorType8;
    ComputedXYZNodeInfo mockComputedXYZNodeInfo3;
    NodeXYZOffsetInfo mockNodeXYZOffsetInfo5;
    NodeXYZOffsetValue mockNodeXYZOffsetValue13;
    NodeXYZOffsetValue mockNodeXYZOffsetValue14;
    NodeXYZOffsetValue mockNodeXYZOffsetValue15;
    WayPlanarGeometryInfo mockWayPlanarGeometryInfo3;

    GeometryContainer mockGeometryContainer11;
    CrosswalkLaneGeometryLayer mockCrosswalkLaneGeometryLayer3;
    IndvCrosswalkLaneGeometryInfo mockIndvCrosswalkLaneGeometryInfo3;
    List<IndvCrosswalkLaneGeometryInfo> mockIndvCrosswalkLaneGeometryInfoList3;
    LaneConstructorType mockLaneConstructorType9;
    PhysicalXYZNodeInfo mockPhysicalXYZNodeInfo3;
    IndividualXYZNodeGeometryInfo mockIndividualXYZNodeGeometryInfo3;
    List<IndividualXYZNodeGeometryInfo> mockIndividualXYZNodeGeometryInfoList3;
    NodeXYZOffsetInfo mockNodeXYZOffsetInfo6;
    NodeXYZOffsetValue mockNodeXYZOffsetValue16;
    NodeXYZOffsetValue mockNodeXYZOffsetValue17;
    NodeXYZOffsetValue mockNodeXYZOffsetValue18;
    DaysOfTheWeek mockDaysOfTheWeek3;
    DDateTime mockStartPeriod3;
    DDateTime mockEndPeriod3;
    GeneralPeriod mockGeneralPeriod3;
    TimeWindowInformation mockTimeWindowInformation3;
    List<TimeWindowInformation> mockTimeWindowSet3;
    TimeWindowItemControlInfo mockFixedTimeWindowCtrl3;
    RGATimeRestrictions mockTimeRestrictions4;

    MovementsContainer mockMovementsContainer1;
    MtrVehLaneDirectionOfTravelLayer mockMtrVehLaneDirectionOfTravelLayer1;
    IndividualWayDirectionsOfTravel mockIndividualWayDirectionsOfTravel1;
    List<IndividualWayDirectionsOfTravel> mockIndividualWayDirectionsOfTravelList1;
    WayDirectionOfTravelInfo mockWayDirectionOfTravelInfo1;
    List<WayDirectionOfTravelInfo> mockWayDirectionOfTravelInfoList1;
    DaysOfTheWeek mockDaysOfTheWeek5;
    DDateTime mockStartPeriod5;
    DDateTime mockEndPeriod5;
    GeneralPeriod mockGeneralPeriod5;
    TimeWindowInformation mockTimeWindowInformation5;
    List<TimeWindowInformation> mockTimeWindowSet5;
    TimeWindowItemControlInfo mockFixedTimeWindowCtrl5;
    RGATimeRestrictions mockTimeRestrictions5;

    MovementsContainer mockMovementsContainer2;
    MtrVehLaneDirectionOfTravelLayer mockMtrVehLaneDirectionOfTravelLayer2;
    IndividualWayDirectionsOfTravel mockIndividualWayDirectionsOfTravel2;
    List<IndividualWayDirectionsOfTravel> mockIndividualWayDirectionsOfTravelList2;
    WayDirectionOfTravelInfo mockWayDirectionOfTravelInfo2;
    List<WayDirectionOfTravelInfo> mockWayDirectionOfTravelInfoList2;
    GeneralPeriod mockGeneralPeriod6;
    TimeWindowInformation mockTimeWindowInformation6;
    List<TimeWindowInformation> mockTimeWindowSet6;
    TimeWindowItemControlInfo mockFixedTimeWindowCtrl6;
    RGATimeRestrictions mockTimeRestrictions6;

    MovementsContainer mockMovementsContainer3;
    MtrVehLaneConnectionsLayer mockMtrVehLaneConnectionsLayer1;
    IndividualWayConnections mockIndividualWayConnections1;
    List<IndividualWayConnections> mockIndividualWayConnectionsList1;
    WayToWayConnectionInfo mockWayToWayConnectionInfo1;
    List<WayToWayConnectionInfo> mockWayToWayConnectionInfoList1;
    LaneConnectionFromInfo mockLaneConnectionFromInfo1;
    LaneConnectionToInfo mockLaneConnectionToInfo1;
    WayType mockWayType3;
    GeneralPeriod mockGeneralPeriod7;
    TimeWindowInformation mockTimeWindowInformation7;
    List<TimeWindowInformation> mockTimeWindowSet7;
    TimeWindowItemControlInfo mockFixedTimeWindowCtrl7;
    RGATimeRestrictions mockTimeRestrictions7;

    MovementsContainer mockMovementsContainer4;
    BicycleLaneConnectionsLayer mockBicycleLaneConnectionsLayer1;
    IndividualWayConnections mockIndividualWayConnections2;
    List<IndividualWayConnections> mockIndividualWayConnectionsList2;
    WayToWayConnectionInfo mockWayToWayConnectionInfo2;
    List<WayToWayConnectionInfo> mockWayToWayConnectionInfoList2;
    LaneConnectionFromInfo mockLaneConnectionFromInfo2;
    LaneConnectionToInfo mockLaneConnectionToInfo2;
    WayType mockWayType4;
    GeneralPeriod mockGeneralPeriod8;
    TimeWindowInformation mockTimeWindowInformation8;
    List<TimeWindowInformation> mockTimeWindowSet8;
    TimeWindowItemControlInfo mockFixedTimeWindowCtrl8;
    RGATimeRestrictions mockTimeRestrictions8;

    MovementsContainer mockMovementsContainer5;
    MtrVehLaneConnectionsManeuversLayer mockMtrVehLaneCnxnMnvrLayer1;
    List<IndividualWayCnxnsManeuvers> mockIndividualLaneCnxnsManeuversLaneSet1;
    List<WayCnxnManeuverInfo> mockCnxnManeuversSet1;
    WayCnxnManeuverInfo mockWayCnxnManeuverInfo1;
    List<CnxnManeuverInfo> mockManeuversSet1;
    CnxnManeuverInfo mockCnxnManeuverInfo1;
    WayCnxnManeuvers mockAllowedManeuvers1;
    WayCnxnManeuverControlType mockManeuverControlType1;
    IndividualWayCnxnsManeuvers mockIndidvidualWayCnxnsManeuvers1;
    MtrVehLaneConnectionsManeuversLayer mockMtrVehLaneConnectionsManeuversLayer1;

    @Before
    public void setup() {
        mockRGA = mock(RGAData.class);
        mockBaseLayer = mock(BaseLayer.class);
        mockLocation = mock(Position3D.class);
        mockTimeOfCalculation = mock(DDate.class);
        mockContentDateTime = mock(DDateTime.class);

        mockGeometryContainer1 = mock(GeometryContainer.class);
        mockApproachGeometryLayer1 = mock(ApproachGeometryLayer.class);
        mockIndividualApproachGeometryInfo1 = mock(IndividualApproachGeometryInfo.class);
        mockIndividualApproachGeometryInfo2 = mock(IndividualApproachGeometryInfo.class);
        mockIndividualApproachWayTypeID1 = mock(ApproachWayTypeIDSet.class);
        mockIndividualApproachWayTypeID2 = mock(ApproachWayTypeIDSet.class);
        mockWayTypeID1 = mock(WayType.class);
        mockWayTypeID2 = mock(WayType.class);

        mockGeometryContainer2 = mock(GeometryContainer.class);
        mockApproachGeometryLayer2 = mock(ApproachGeometryLayer.class);
        mockIndividualApproachGeometryInfo3 = mock(IndividualApproachGeometryInfo.class);

        mockGeometryContainer3 = mock(GeometryContainer.class);
        mockMotorVehicleLaneGeometryLayer1 = mock(MotorVehicleLaneGeometryLayer.class);
        mockIndvMtrVehLaneGeometryInfo1 = mock(IndvMtrVehLaneGeometryInfo.class);
        mockDuplicateXYZNodeInfo1 = mock(DuplicateXYZNodeInfo.class);
        mockLaneConstructorType1 = mock(LaneConstructorType.class);

        mockGeometryContainer4 = mock(GeometryContainer.class);
        mockMotorVehicleLaneGeometryLayer2 = mock(MotorVehicleLaneGeometryLayer.class);
        mockIndvMtrVehLaneGeometryInfo2 = mock(IndvMtrVehLaneGeometryInfo.class);
        mockLaneConstructorType2 = mock(LaneConstructorType.class);
        mockComputedXYZNodeInfo1 = mock(ComputedXYZNodeInfo.class);
        mockNodeXYZOffsetInfo1 = mock(NodeXYZOffsetInfo.class);
        mockNodeXYZOffsetValue1 = mock(NodeXYZOffsetValue.class);
        mockNodeXYZOffsetValue2 = mock(NodeXYZOffsetValue.class);
        mockNodeXYZOffsetValue3 = mock(NodeXYZOffsetValue.class);
        mockWayWidth1 = mock(WayWidth.class);
        mockWayPlanarGeometryInfo1 = mock(WayPlanarGeometryInfo.class);
        mockTimeRestrictions1 = mock(RGATimeRestrictions.class);
        mockFixedTimeWindowCtrl1 = mock(TimeWindowItemControlInfo.class);
        mockDaysOfTheWeek1 = mock(DaysOfTheWeek.class);
        mockStartPeriod1 = mock(DDateTime.class);
        mockEndPeriod1 = mock(DDateTime.class);
        mockGeneralPeriod1 = mock(GeneralPeriod.class);
        mockTimeWindowInformation1 = mock(TimeWindowInformation.class);

        mockGeometryContainer5 = mock(GeometryContainer.class);
        mockMotorVehicleLaneGeometryLayer3 = mock(MotorVehicleLaneGeometryLayer.class);
        mockIndvMtrVehLaneGeometryInfo3 = mock(IndvMtrVehLaneGeometryInfo.class);
        mockLaneConstructorType3 = mock(LaneConstructorType.class);
        mockPhysicalXYZNodeInfo1 = mock(PhysicalXYZNodeInfo.class);
        mockIndividualXYZNodeGeometryInfo1 = mock(IndividualXYZNodeGeometryInfo.class);
        mockNodeXYZOffsetInfo2 = mock(NodeXYZOffsetInfo.class);
        mockNodeXYZOffsetValue4 = mock(NodeXYZOffsetValue.class);
        mockNodeXYZOffsetValue5 = mock(NodeXYZOffsetValue.class);
        mockNodeXYZOffsetValue6 = mock(NodeXYZOffsetValue.class);
        mockWayWidth2 = mock(WayWidth.class);
        mockWayPlanarGeometryInfo4 = mock(WayPlanarGeometryInfo.class);
        mockGeometryContainer6 = mock(GeometryContainer.class);
        mockBicycleLaneGeometryLayer1 = mock(BicycleLaneGeometryLayer.class);
        mockIndvBikeLaneGeometryInfo1 = mock(IndvBikeLaneGeometryInfo.class);
        mockLaneConstructorType4 = mock(LaneConstructorType.class);
        mockDuplicateXYZNodeInfo2 = mock(DuplicateXYZNodeInfo.class);
        mockReferencePointInfo1 = mock(ReferencePointInfo.class);
        mockLocation1 = mock(Position3D.class);
        mockTimeOfCalculation1 = mock(DDate.class);
        mockTimeRestrictions2 = mock(RGATimeRestrictions.class);
        mockOtherDataSetItemCtrl1 = mock(OtherDSItemControlInfo.class);

        mockGeometryContainer7 = mock(GeometryContainer.class);
        mockBicycleLaneGeometryLayer2 = mock(BicycleLaneGeometryLayer.class);
        mockIndvBikeLaneGeometryInfo2 = mock(IndvBikeLaneGeometryInfo.class);
        mockLaneConstructorType5 = mock(LaneConstructorType.class);
        mockComputedXYZNodeInfo2 = mock(ComputedXYZNodeInfo.class);
        mockNodeXYZOffsetInfo3 = mock(NodeXYZOffsetInfo.class);
        mockNodeXYZOffsetValue7 = mock(NodeXYZOffsetValue.class);
        mockNodeXYZOffsetValue8 = mock(NodeXYZOffsetValue.class);
        mockNodeXYZOffsetValue9 = mock(NodeXYZOffsetValue.class);
        mockWayWidth3 = mock(WayWidth.class);
        mockWayPlanarGeometryInfo2 = mock(WayPlanarGeometryInfo.class);
        mockTimeRestrictions3 = mock(RGATimeRestrictions.class);
        mockFixedTimeWindowCtrl2 = mock(TimeWindowItemControlInfo.class);
        mockDaysOfTheWeek2 = mock(DaysOfTheWeek.class);
        mockStartPeriod2 = mock(DDateTime.class);
        mockEndPeriod2 = mock(DDateTime.class);
        mockGeneralPeriod2 = mock(GeneralPeriod.class);
        mockTimeWindowInformation2 = mock(TimeWindowInformation.class);

        mockGeometryContainer8 = mock(GeometryContainer.class);
        mockBicycleLaneGeometryLayer3 = mock(BicycleLaneGeometryLayer.class);
        mockIndvBikeLaneGeometryInfo3 = mock(IndvBikeLaneGeometryInfo.class);
        mockLaneConstructorType6 = mock(LaneConstructorType.class);
        mockPhysicalXYZNodeInfo2 = mock(PhysicalXYZNodeInfo.class);
        mockIndividualXYZNodeGeometryInfo2 = mock(IndividualXYZNodeGeometryInfo.class);
        mockNodeXYZOffsetInfo4 = mock(NodeXYZOffsetInfo.class);
        mockNodeXYZOffsetValue10 = mock(NodeXYZOffsetValue.class);
        mockNodeXYZOffsetValue11 = mock(NodeXYZOffsetValue.class);
        mockNodeXYZOffsetValue12 = mock(NodeXYZOffsetValue.class);
        mockWayWidth4 = mock(WayWidth.class);
        mockWayPlanarGeometryInfo5 = mock(WayPlanarGeometryInfo.class);
        mockGeometryContainer9 = mock(GeometryContainer.class);
        mockCrosswalkLaneGeometryLayer1 = mock(CrosswalkLaneGeometryLayer.class);
        mockIndvCrosswalkLaneGeometryInfo1 = mock(IndvCrosswalkLaneGeometryInfo.class);
        mockLaneConstructorType7 = mock(LaneConstructorType.class);
        mockDuplicateXYZNodeInfo3 = mock(DuplicateXYZNodeInfo.class);

        mockGeometryContainer10 = mock(GeometryContainer.class);
        mockCrosswalkLaneGeometryLayer2 = mock(CrosswalkLaneGeometryLayer.class);
        mockIndvCrosswalkLaneGeometryInfo2 = mock(IndvCrosswalkLaneGeometryInfo.class);
        mockLaneConstructorType8 = mock(LaneConstructorType.class);
        mockComputedXYZNodeInfo3 = mock(ComputedXYZNodeInfo.class);
        mockNodeXYZOffsetInfo5 = mock(NodeXYZOffsetInfo.class);
        mockNodeXYZOffsetValue13 = mock(NodeXYZOffsetValue.class);
        mockNodeXYZOffsetValue14 = mock(NodeXYZOffsetValue.class);
        mockNodeXYZOffsetValue15 = mock(NodeXYZOffsetValue.class);
        mockWayPlanarGeometryInfo3 = mock(WayPlanarGeometryInfo.class);

        mockGeometryContainer11 = mock(GeometryContainer.class);
        mockCrosswalkLaneGeometryLayer3 = mock(CrosswalkLaneGeometryLayer.class);
        mockIndvCrosswalkLaneGeometryInfo3 = mock(IndvCrosswalkLaneGeometryInfo.class);
        mockLaneConstructorType9 = mock(LaneConstructorType.class);
        mockPhysicalXYZNodeInfo3 = mock(PhysicalXYZNodeInfo.class);
        mockIndividualXYZNodeGeometryInfo3 = mock(IndividualXYZNodeGeometryInfo.class);
        mockNodeXYZOffsetInfo6 = mock(NodeXYZOffsetInfo.class);
        mockNodeXYZOffsetValue16 = mock(NodeXYZOffsetValue.class);
        mockNodeXYZOffsetValue17 = mock(NodeXYZOffsetValue.class);
        mockNodeXYZOffsetValue18 = mock(NodeXYZOffsetValue.class);
        mockTimeRestrictions4 = mock(RGATimeRestrictions.class);
        mockFixedTimeWindowCtrl3 = mock(TimeWindowItemControlInfo.class);
        mockDaysOfTheWeek3 = mock(DaysOfTheWeek.class);
        mockStartPeriod3 = mock(DDateTime.class);
        mockEndPeriod3 = mock(DDateTime.class);
        mockGeneralPeriod3 = mock(GeneralPeriod.class);
        mockTimeWindowInformation3 = mock(TimeWindowInformation.class);

        mockMovementsContainer1 = mock(MovementsContainer.class);
        mockMtrVehLaneDirectionOfTravelLayer1 =  mock(MtrVehLaneDirectionOfTravelLayer.class);
        mockIndividualWayDirectionsOfTravel1 = mock(IndividualWayDirectionsOfTravel.class);
        mockWayDirectionOfTravelInfo1 = mock(WayDirectionOfTravelInfo.class);
        mockTimeRestrictions5 = mock(RGATimeRestrictions.class);
        mockFixedTimeWindowCtrl5 = mock(TimeWindowItemControlInfo.class);
        mockDaysOfTheWeek5 = mock(DaysOfTheWeek.class);
        mockStartPeriod5 = mock(DDateTime.class);
        mockEndPeriod5 = mock(DDateTime.class);
        mockGeneralPeriod5 = mock(GeneralPeriod.class);
        mockTimeWindowInformation5 = mock(TimeWindowInformation.class);

        mockMovementsContainer2 = mock(MovementsContainer.class);
        mockMtrVehLaneDirectionOfTravelLayer2 =  mock(MtrVehLaneDirectionOfTravelLayer.class);
        mockIndividualWayDirectionsOfTravel2 = mock(IndividualWayDirectionsOfTravel.class);
        mockWayDirectionOfTravelInfo2 = mock(WayDirectionOfTravelInfo.class);
        mockTimeRestrictions6 = mock(RGATimeRestrictions.class);
        mockFixedTimeWindowCtrl6 = mock(TimeWindowItemControlInfo.class);
        mockGeneralPeriod6 = mock(GeneralPeriod.class);
        mockTimeWindowInformation6 = mock(TimeWindowInformation.class);

        mockMovementsContainer3 = mock(MovementsContainer.class);
        mockMtrVehLaneConnectionsLayer1 = mock(MtrVehLaneConnectionsLayer.class);
        mockIndividualWayConnections1 = mock(IndividualWayConnections.class);
        mockWayToWayConnectionInfo1 = mock(WayToWayConnectionInfo.class);
        mockLaneConnectionFromInfo1 = mock(LaneConnectionFromInfo.class);
        mockLaneConnectionToInfo1 = mock(LaneConnectionToInfo.class);
        mockWayType3 = mock(WayType.class);
        mockTimeRestrictions7 = mock(RGATimeRestrictions.class);
        mockFixedTimeWindowCtrl7 = mock(TimeWindowItemControlInfo.class);
        mockGeneralPeriod7 = mock(GeneralPeriod.class);
        mockTimeWindowInformation7 = mock(TimeWindowInformation.class);

        mockMovementsContainer4 = mock(MovementsContainer.class);
        mockBicycleLaneConnectionsLayer1 = mock(BicycleLaneConnectionsLayer.class);
        mockIndividualWayConnections2 = mock(IndividualWayConnections.class);
        mockWayToWayConnectionInfo2 = mock(WayToWayConnectionInfo.class);
        mockLaneConnectionFromInfo2 = mock(LaneConnectionFromInfo.class);
        mockLaneConnectionToInfo2 = mock(LaneConnectionToInfo.class);
        mockWayType4 = mock(WayType.class);
        mockTimeRestrictions8 = mock(RGATimeRestrictions.class);
        mockFixedTimeWindowCtrl8 = mock(TimeWindowItemControlInfo.class);
        mockGeneralPeriod8 = mock(GeneralPeriod.class);
        mockTimeWindowInformation8 = mock(TimeWindowInformation.class);

        mockMovementsContainer5 = mock(MovementsContainer.class);
        mockMtrVehLaneCnxnMnvrLayer1 = mock(MtrVehLaneConnectionsManeuversLayer.class);
        mockAllowedManeuvers1 = mock(WayCnxnManeuvers.class);
        mockManeuverControlType1 = mock(WayCnxnManeuverControlType.class);
        mockCnxnManeuverInfo1 = mock(CnxnManeuverInfo.class);
        mockWayCnxnManeuverInfo1 = mock(WayCnxnManeuverInfo.class);
        mockIndidvidualWayCnxnsManeuvers1 = mock(IndividualWayCnxnsManeuvers.class);
        mockMtrVehLaneConnectionsManeuversLayer1 = mock(MtrVehLaneConnectionsManeuversLayer.class);

        encoder = new Encoder();

        when(mockBaseLayer.getMajorVer()).thenReturn(1);
        when(mockBaseLayer.getMinorVer()).thenReturn(1);

        // RAID
        when(mockBaseLayer.isFullRdAuthIDExists()).thenReturn(false);
        when(mockBaseLayer.getFullRdAuthID()).thenReturn(new int[] { 1, 2, 83493 });
        when(mockBaseLayer.isRelRdAuthIDExists()).thenReturn(true);
        when(mockBaseLayer.getRelRdAuthID()).thenReturn(new int[] { 8, 4, 8571 });

        when(mockBaseLayer.getRelativeToRdAuthID()).thenReturn(new int[] { 1, 3, 6, 1, 4, 1, 311, 21, 20 });

        // location
        when(mockLocation.getLatitude()).thenReturn((double) 7.2);
        when(mockLocation.getLongitude()).thenReturn((double) 11.1);
        when(mockLocation.isElevationExists()).thenReturn(true);
        when(mockLocation.getElevation()).thenReturn((float) 13.12);

        // TimeOfCalculation
        when(mockTimeOfCalculation.getMonth()).thenReturn(8);
        when(mockTimeOfCalculation.getDay()).thenReturn(21);
        when(mockTimeOfCalculation.getYear()).thenReturn(2024);

        when(mockBaseLayer.getContentVer()).thenReturn(13);

        // ContentDateTime
        when(mockContentDateTime.getHour()).thenReturn(13);
        when(mockContentDateTime.getMinute()).thenReturn(51);
        when(mockContentDateTime.getSecond()).thenReturn(20);

        when(mockBaseLayer.getLocation()).thenReturn(mockLocation);
        when(mockBaseLayer.getTimeOfCalculation()).thenReturn(mockTimeOfCalculation);
        when(mockBaseLayer.getContentDateTime()).thenReturn(mockContentDateTime);

        // CONTAINER 1
        when(mockGeometryContainer1.getGeometryContainerID()).thenReturn(GeometryContainer.APPROACH_GEOMETRY_LAYER_ID);
        when(mockWayTypeID1.getWayTypeValue()).thenReturn(WayType.BICYCLE_LANE);
        mockLaneID1 = (long)3;
        mockLaneIDSet1 = Arrays.asList(mockLaneID1);
        when(mockIndividualApproachWayTypeID1.getWayType()).thenReturn(mockWayTypeID1);
        when(mockIndividualApproachWayTypeID1.getWayIDSet()).thenReturn(mockLaneIDSet1);

        when(mockWayTypeID2.getWayTypeValue()).thenReturn(WayType.CROSSWALK_LANE);
        mockLaneID2 = (long)5;

        mockLaneIDSet2 = Arrays.asList(mockLaneID2);
        when(mockIndividualApproachWayTypeID2.getWayType()).thenReturn(mockWayTypeID2);
        when(mockIndividualApproachWayTypeID2.getWayIDSet()).thenReturn(mockLaneIDSet2);

        mockApproachWayTypeIDSet1 = Arrays.asList(mockIndividualApproachWayTypeID1, mockIndividualApproachWayTypeID2);
        when(mockIndividualApproachGeometryInfo1.getApproachWayTypeIDSet()).thenReturn(mockApproachWayTypeIDSet1);
        when(mockIndividualApproachGeometryInfo1.getApproachID()).thenReturn(2);
        when(mockIndividualApproachGeometryInfo2.getApproachID()).thenReturn(3);
        mockIndividualApproachGeometryInfoList1 = Arrays.asList(mockIndividualApproachGeometryInfo1,
                        mockIndividualApproachGeometryInfo2);
        when(mockApproachGeometryLayer1.getApproachGeomApproachSet())
                        .thenReturn(mockIndividualApproachGeometryInfoList1);
        when(mockGeometryContainer1.getApproachGeometryLayer()).thenReturn(mockApproachGeometryLayer1);

        // CONTAINER 2
        when(mockGeometryContainer2.getGeometryContainerID()).thenReturn(GeometryContainer.APPROACH_GEOMETRY_LAYER_ID);
        when(mockIndividualApproachGeometryInfo3.getApproachID()).thenReturn(4);
        mockIndividualApproachGeometryInfoList2 = Arrays.asList(mockIndividualApproachGeometryInfo3);
        when(mockApproachGeometryLayer2.getApproachGeomApproachSet())
                .thenReturn(mockIndividualApproachGeometryInfoList2);
        when(mockGeometryContainer2.getApproachGeometryLayer()).thenReturn(mockApproachGeometryLayer2);

        // CONTAINER 3
        when(mockDuplicateXYZNodeInfo1.getRefLaneID()).thenReturn(23);
        when(mockLaneConstructorType1.getChoice()).thenReturn(LaneConstructorType.DUPLICATE_NODE);
        when(mockLaneConstructorType1.getDuplicateXYZNodeInfo()).thenReturn(mockDuplicateXYZNodeInfo1);
        when(mockIndvMtrVehLaneGeometryInfo1.getLaneID()).thenReturn(11);
        when(mockIndvMtrVehLaneGeometryInfo1.getLaneConstructorType()).thenReturn(mockLaneConstructorType1);
        mockIndvMtrVehLaneGeometryInfoList1 = Arrays.asList(mockIndvMtrVehLaneGeometryInfo1);
        when(mockMotorVehicleLaneGeometryLayer1.getLaneGeomLaneSet()).thenReturn(mockIndvMtrVehLaneGeometryInfoList1);
        when(mockGeometryContainer3.getGeometryContainerID())
                .thenReturn(GeometryContainer.MOTOR_VEHICLE_LANE_GEOMETRY_LAYER_ID);
        when(mockGeometryContainer3.getMotorVehicleLaneGeometryLayer()).thenReturn(mockMotorVehicleLaneGeometryLayer1);

        // CONTAINER 4
        when(mockNodeXYZOffsetValue1.getChoice()).thenReturn(NodeXYZOffsetValue.OFFSET_B10);
        when(mockNodeXYZOffsetValue1.getOffsetB10()).thenReturn((long) 2);
        when(mockNodeXYZOffsetValue2.getChoice()).thenReturn(NodeXYZOffsetValue.OFFSET_B11);
        when(mockNodeXYZOffsetValue2.getOffsetB11()).thenReturn((long) 3);
        when(mockNodeXYZOffsetValue3.getChoice()).thenReturn(NodeXYZOffsetValue.OFFSET_B12);
        when(mockNodeXYZOffsetValue3.getOffsetB12()).thenReturn((long) 4);
        when(mockNodeXYZOffsetInfo1.getNodeXOffsetValue()).thenReturn(mockNodeXYZOffsetValue1);
        when(mockNodeXYZOffsetInfo1.getNodeYOffsetValue()).thenReturn(mockNodeXYZOffsetValue2);
        when(mockNodeXYZOffsetInfo1.getNodeZOffsetValue()).thenReturn(mockNodeXYZOffsetValue3);
        when(mockComputedXYZNodeInfo1.getRefLaneID()).thenReturn(24);
        when(mockComputedXYZNodeInfo1.getLaneCenterLineXYZOffset()).thenReturn(mockNodeXYZOffsetInfo1);
        when(mockWayWidth1.getChoice()).thenReturn((byte) WayWidth.FULL_WIDTH);
        when(mockWayWidth1.getFullWidth()).thenReturn((int) 9);
        when(mockWayPlanarGeometryInfo1.getWayWidth()).thenReturn(mockWayWidth1);
        when(mockComputedXYZNodeInfo1.getLanePlanarGeomInfo()).thenReturn(mockWayPlanarGeometryInfo1);
        when(mockLaneConstructorType2.getChoice()).thenReturn(LaneConstructorType.COMPUTED_NODE);
        when(mockLaneConstructorType2.getComputedXYZNodeInfo()).thenReturn(mockComputedXYZNodeInfo1);
        when(mockIndvMtrVehLaneGeometryInfo2.getLaneID()).thenReturn(12);
        when(mockIndvMtrVehLaneGeometryInfo2.getLaneConstructorType()).thenReturn(mockLaneConstructorType2);
        // TimeRestrictions
        when(mockDaysOfTheWeek1.getDaysOfTheWeekValue()).thenReturn((short) 7);
        when(mockTimeWindowInformation1.getDaysOfTheWeek()).thenReturn(mockDaysOfTheWeek1);
        when(mockStartPeriod1.getYear()).thenReturn(2024);
        when(mockStartPeriod1.getMonth()).thenReturn(8);
        when(mockStartPeriod1.getDay()).thenReturn(21);
        when(mockStartPeriod1.getHour()).thenReturn(13);
        when(mockStartPeriod1.getMinute()).thenReturn(51);
        when(mockStartPeriod1.getSecond()).thenReturn(20);
        when(mockStartPeriod1.getOffset()).thenReturn(180);
        when(mockTimeWindowInformation1.getStartPeriod()).thenReturn(mockStartPeriod1);
        when(mockEndPeriod1.getYear()).thenReturn(2024);
        when(mockEndPeriod1.getMonth()).thenReturn(8);
        when(mockEndPeriod1.getDay()).thenReturn(21);
        when(mockEndPeriod1.getHour()).thenReturn(19);
        when(mockEndPeriod1.getMinute()).thenReturn(51);
        when(mockEndPeriod1.getSecond()).thenReturn(20);
        when(mockTimeWindowInformation1.getEndPeriod()).thenReturn(mockEndPeriod1);
        when(mockGeneralPeriod1.getGeneralPeriodValue()).thenReturn(GeneralPeriod.NIGHT);
        when(mockTimeWindowInformation1.getGeneralPeriod()).thenReturn(mockGeneralPeriod1);
        mockTimeWindowSet1 = Arrays.asList(mockTimeWindowInformation1);
        when(mockFixedTimeWindowCtrl1.getTimeWindowSet()).thenReturn(mockTimeWindowSet1);
        when(mockTimeRestrictions1.getChoice()).thenReturn((int) RGATimeRestrictions.TIME_WINDOW_ITEM_CONTROL);
        when(mockTimeRestrictions1.getFixedTimeWindowCtrl()).thenReturn(mockFixedTimeWindowCtrl1);
        when(mockIndvMtrVehLaneGeometryInfo2.getTimeRestrictions()).thenReturn(mockTimeRestrictions1);
        mockIndvMtrVehLaneGeometryInfoList2 = Arrays.asList(mockIndvMtrVehLaneGeometryInfo2);
        when(mockMotorVehicleLaneGeometryLayer2.getLaneGeomLaneSet()).thenReturn(mockIndvMtrVehLaneGeometryInfoList2);
        when(mockGeometryContainer4.getGeometryContainerID())
                .thenReturn(GeometryContainer.MOTOR_VEHICLE_LANE_GEOMETRY_LAYER_ID);
        when(mockGeometryContainer4.getMotorVehicleLaneGeometryLayer()).thenReturn(mockMotorVehicleLaneGeometryLayer2);

        // CONTAINER 5
        when(mockNodeXYZOffsetValue4.getChoice()).thenReturn(NodeXYZOffsetValue.OFFSET_B13);
        when(mockNodeXYZOffsetValue4.getOffsetB13()).thenReturn((long) 100);
        when(mockNodeXYZOffsetValue5.getChoice()).thenReturn(NodeXYZOffsetValue.OFFSET_B14);
        when(mockNodeXYZOffsetValue5.getOffsetB14()).thenReturn((long) 150);
        when(mockNodeXYZOffsetValue6.getChoice()).thenReturn(NodeXYZOffsetValue.OFFSET_B16);
        when(mockNodeXYZOffsetValue6.getOffsetB16()).thenReturn((long) 200);
        when(mockNodeXYZOffsetInfo2.getNodeXOffsetValue()).thenReturn(mockNodeXYZOffsetValue4);
        when(mockNodeXYZOffsetInfo2.getNodeYOffsetValue()).thenReturn(mockNodeXYZOffsetValue5);
        when(mockNodeXYZOffsetInfo2.getNodeZOffsetValue()).thenReturn(mockNodeXYZOffsetValue6);
        when(mockIndividualXYZNodeGeometryInfo1.getNodeXYZOffsetInfo()).thenReturn(mockNodeXYZOffsetInfo2);
        when(mockWayWidth2.getChoice()).thenReturn((byte) WayWidth.FULL_WIDTH);
        when(mockWayWidth2.getFullWidth()).thenReturn((int) 10);
        when(mockWayPlanarGeometryInfo4.getWayWidth()).thenReturn(mockWayWidth2);
        when(mockIndividualXYZNodeGeometryInfo1.getNodeLocPlanarGeomInfo()).thenReturn(mockWayPlanarGeometryInfo4);
        mockIndividualXYZNodeGeometryInfoList1 = Arrays.asList(mockIndividualXYZNodeGeometryInfo1);
        when(mockPhysicalXYZNodeInfo1.getNodeXYZGeometryNodeSet()).thenReturn(mockIndividualXYZNodeGeometryInfoList1);
        // MockReferencePoint1
        when(mockLocation1.getLatitude()).thenReturn((double) 7.2);
        when(mockLocation1.getLongitude()).thenReturn((double) 11.1);
        when(mockLocation1.isElevationExists()).thenReturn(true);
        when(mockLocation1.getElevation()).thenReturn((float) 13.12);
        when(mockTimeOfCalculation1.getMonth()).thenReturn(8);
        when(mockTimeOfCalculation1.getDay()).thenReturn(21);
        when(mockTimeOfCalculation1.getYear()).thenReturn(2024);
        when(mockReferencePointInfo1.getLocation()).thenReturn(mockLocation1);
        when(mockReferencePointInfo1.getTimeOfCalculation()).thenReturn(mockTimeOfCalculation1);
        when(mockPhysicalXYZNodeInfo1.getReferencePointInfo()).thenReturn(mockReferencePointInfo1);
        when(mockLaneConstructorType3.getChoice()).thenReturn(LaneConstructorType.PHYSICAL_NODE);
        when(mockLaneConstructorType3.getPhysicalXYZNodeInfo()).thenReturn(mockPhysicalXYZNodeInfo1);
        when(mockIndvMtrVehLaneGeometryInfo3.getLaneID()).thenReturn(13);
        when(mockIndvMtrVehLaneGeometryInfo3.getLaneConstructorType()).thenReturn(mockLaneConstructorType3);
        when(mockTimeRestrictions2.getChoice()).thenReturn((int) RGATimeRestrictions.OTHER_DATA_SET_ITEM_CONTROL);
        when(mockOtherDataSetItemCtrl1.getMessageID()).thenReturn(34L);
        when(mockOtherDataSetItemCtrl1.getEnaAttributeID()).thenReturn(35L);
        when(mockTimeRestrictions2.getOtherDataSetItemCtrl()).thenReturn(mockOtherDataSetItemCtrl1);
        when(mockIndvMtrVehLaneGeometryInfo3.getTimeRestrictions()).thenReturn(mockTimeRestrictions2);
        mockIndvMtrVehLaneGeometryInfoList3 = Arrays.asList(mockIndvMtrVehLaneGeometryInfo3);
        when(mockMotorVehicleLaneGeometryLayer3.getLaneGeomLaneSet()).thenReturn(mockIndvMtrVehLaneGeometryInfoList3);
        when(mockGeometryContainer5.getGeometryContainerID())
                .thenReturn(GeometryContainer.MOTOR_VEHICLE_LANE_GEOMETRY_LAYER_ID);
        when(mockGeometryContainer5.getMotorVehicleLaneGeometryLayer()).thenReturn(mockMotorVehicleLaneGeometryLayer3);

        // CONTAINER 6
        when(mockDuplicateXYZNodeInfo2.getRefLaneID()).thenReturn(33);
        when(mockLaneConstructorType4.getChoice()).thenReturn(LaneConstructorType.DUPLICATE_NODE);
        when(mockLaneConstructorType4.getDuplicateXYZNodeInfo()).thenReturn(mockDuplicateXYZNodeInfo2);
        when(mockIndvBikeLaneGeometryInfo1.getLaneID()).thenReturn(14);
        when(mockIndvBikeLaneGeometryInfo1.getLaneConstructorType()).thenReturn(mockLaneConstructorType4);
        mockIndvBikeLaneGeometryInfoList1 = Arrays.asList(mockIndvBikeLaneGeometryInfo1);
        when(mockBicycleLaneGeometryLayer1.getLaneGeomLaneSet()).thenReturn(mockIndvBikeLaneGeometryInfoList1);
        when(mockGeometryContainer6.getGeometryContainerID())
                .thenReturn(GeometryContainer.BICYCLE_LANE_GEOMETRY_LAYER_ID);
        when(mockGeometryContainer6.getBicycleLaneGeometryLayer()).thenReturn(mockBicycleLaneGeometryLayer1);

        // CONTAINER 7
        when(mockNodeXYZOffsetValue7.getChoice()).thenReturn(NodeXYZOffsetValue.OFFSET_B10);
        when(mockNodeXYZOffsetValue7.getOffsetB10()).thenReturn((long) 2);
        when(mockNodeXYZOffsetValue8.getChoice()).thenReturn(NodeXYZOffsetValue.OFFSET_B11);
        when(mockNodeXYZOffsetValue8.getOffsetB11()).thenReturn((long) 3);
        when(mockNodeXYZOffsetValue9.getChoice()).thenReturn(NodeXYZOffsetValue.OFFSET_B12);
        when(mockNodeXYZOffsetValue9.getOffsetB12()).thenReturn((long) 4);
        when(mockNodeXYZOffsetInfo3.getNodeXOffsetValue()).thenReturn(mockNodeXYZOffsetValue7);
        when(mockNodeXYZOffsetInfo3.getNodeYOffsetValue()).thenReturn(mockNodeXYZOffsetValue8);
        when(mockNodeXYZOffsetInfo3.getNodeZOffsetValue()).thenReturn(mockNodeXYZOffsetValue9);
        when(mockComputedXYZNodeInfo2.getRefLaneID()).thenReturn(34);
        when(mockComputedXYZNodeInfo2.getLaneCenterLineXYZOffset()).thenReturn(mockNodeXYZOffsetInfo3);
        when(mockWayWidth3.getChoice()).thenReturn((byte) WayWidth.DELTA_WIDTH);
        when(mockWayWidth3.getDeltaWidth()).thenReturn((int) 2);
        when(mockWayPlanarGeometryInfo2.getWayWidth()).thenReturn(mockWayWidth3);
        when(mockComputedXYZNodeInfo2.getLanePlanarGeomInfo()).thenReturn(mockWayPlanarGeometryInfo2);
        when(mockLaneConstructorType5.getChoice()).thenReturn(LaneConstructorType.COMPUTED_NODE);
        when(mockLaneConstructorType5.getComputedXYZNodeInfo()).thenReturn(mockComputedXYZNodeInfo2);
        when(mockIndvBikeLaneGeometryInfo2.getLaneID()).thenReturn(15);
        when(mockIndvBikeLaneGeometryInfo2.getLaneConstructorType()).thenReturn(mockLaneConstructorType5);
        // TimeRestrictions
        when(mockDaysOfTheWeek2.getDaysOfTheWeekValue()).thenReturn((short) 5);
        when(mockTimeWindowInformation2.getDaysOfTheWeek()).thenReturn(mockDaysOfTheWeek2);
        when(mockStartPeriod2.getYear()).thenReturn(2023);
        when(mockStartPeriod2.getMonth()).thenReturn(10);
        when(mockStartPeriod2.getDay()).thenReturn(30);
        when(mockStartPeriod2.getHour()).thenReturn(12);
        when(mockStartPeriod2.getMinute()).thenReturn(45);
        when(mockStartPeriod2.getSecond()).thenReturn(00);
        when(mockStartPeriod2.getOffset()).thenReturn(270);
        when(mockTimeWindowInformation2.getStartPeriod()).thenReturn(mockStartPeriod2);
        when(mockEndPeriod2.getYear()).thenReturn(2025);
        when(mockEndPeriod2.getMonth()).thenReturn(3);
        when(mockEndPeriod2.getDay()).thenReturn(30);
        when(mockEndPeriod2.getHour()).thenReturn(12);
        when(mockEndPeriod2.getMinute()).thenReturn(50);
        when(mockEndPeriod2.getSecond()).thenReturn(00);
        when(mockTimeWindowInformation2.getEndPeriod()).thenReturn(mockEndPeriod2);
        when(mockGeneralPeriod2.getGeneralPeriodValue()).thenReturn(GeneralPeriod.DAY);
        when(mockTimeWindowInformation2.getGeneralPeriod()).thenReturn(mockGeneralPeriod2);
        mockTimeWindowSet2 = Arrays.asList(mockTimeWindowInformation2);
        when(mockFixedTimeWindowCtrl2.getTimeWindowSet()).thenReturn(mockTimeWindowSet2);
        when(mockTimeRestrictions3.getChoice()).thenReturn((int) RGATimeRestrictions.TIME_WINDOW_ITEM_CONTROL);
        when(mockTimeRestrictions3.getFixedTimeWindowCtrl()).thenReturn(mockFixedTimeWindowCtrl2);
        when(mockIndvBikeLaneGeometryInfo2.getTimeRestrictions()).thenReturn(mockTimeRestrictions3);
        mockIndvBikeLaneGeometryInfoList2 = Arrays.asList(mockIndvBikeLaneGeometryInfo2);
        when(mockBicycleLaneGeometryLayer2.getLaneGeomLaneSet()).thenReturn(mockIndvBikeLaneGeometryInfoList2);
        when(mockGeometryContainer7.getGeometryContainerID())
                .thenReturn(GeometryContainer.BICYCLE_LANE_GEOMETRY_LAYER_ID);
        when(mockGeometryContainer7.getBicycleLaneGeometryLayer()).thenReturn(mockBicycleLaneGeometryLayer2);

        // CONTAINER 8
        when(mockNodeXYZOffsetValue10.getChoice()).thenReturn(NodeXYZOffsetValue.OFFSET_B13);
        when(mockNodeXYZOffsetValue10.getOffsetB13()).thenReturn((long) 10);
        when(mockNodeXYZOffsetValue11.getChoice()).thenReturn(NodeXYZOffsetValue.OFFSET_B14);
        when(mockNodeXYZOffsetValue11.getOffsetB14()).thenReturn((long) 15);
        when(mockNodeXYZOffsetValue12.getChoice()).thenReturn(NodeXYZOffsetValue.OFFSET_B16);
        when(mockNodeXYZOffsetValue12.getOffsetB16()).thenReturn((long) 20);
        when(mockNodeXYZOffsetInfo4.getNodeXOffsetValue()).thenReturn(mockNodeXYZOffsetValue10);
        when(mockNodeXYZOffsetInfo4.getNodeYOffsetValue()).thenReturn(mockNodeXYZOffsetValue11);
        when(mockNodeXYZOffsetInfo4.getNodeZOffsetValue()).thenReturn(mockNodeXYZOffsetValue12);
        when(mockIndividualXYZNodeGeometryInfo2.getNodeXYZOffsetInfo()).thenReturn(mockNodeXYZOffsetInfo4);
        when(mockWayWidth4.getChoice()).thenReturn((byte) WayWidth.DELTA_WIDTH);
        when(mockWayWidth4.getDeltaWidth()).thenReturn((int) 1);
        when(mockWayPlanarGeometryInfo5.getWayWidth()).thenReturn(mockWayWidth4);
        when(mockIndividualXYZNodeGeometryInfo2.getNodeLocPlanarGeomInfo()).thenReturn(mockWayPlanarGeometryInfo5);
        mockIndividualXYZNodeGeometryInfoList2 = Arrays.asList(mockIndividualXYZNodeGeometryInfo2);
        when(mockPhysicalXYZNodeInfo2.getNodeXYZGeometryNodeSet()).thenReturn(mockIndividualXYZNodeGeometryInfoList2);
        when(mockLaneConstructorType6.getChoice()).thenReturn(LaneConstructorType.PHYSICAL_NODE);
        when(mockLaneConstructorType6.getPhysicalXYZNodeInfo()).thenReturn(mockPhysicalXYZNodeInfo2);
        when(mockIndvBikeLaneGeometryInfo3.getLaneID()).thenReturn(16);
        when(mockIndvBikeLaneGeometryInfo3.getLaneConstructorType()).thenReturn(mockLaneConstructorType6);
        mockIndvBikeLaneGeometryInfoList3 = Arrays.asList(mockIndvBikeLaneGeometryInfo3);
        when(mockBicycleLaneGeometryLayer3.getLaneGeomLaneSet()).thenReturn(mockIndvBikeLaneGeometryInfoList3);
        when(mockGeometryContainer8.getGeometryContainerID())
                .thenReturn(GeometryContainer.BICYCLE_LANE_GEOMETRY_LAYER_ID);
        when(mockGeometryContainer8.getBicycleLaneGeometryLayer()).thenReturn(mockBicycleLaneGeometryLayer3);

        // CONTAINER 9
        when(mockDuplicateXYZNodeInfo3.getRefLaneID()).thenReturn(43);
        when(mockLaneConstructorType7.getChoice()).thenReturn(LaneConstructorType.DUPLICATE_NODE);
        when(mockLaneConstructorType7.getDuplicateXYZNodeInfo()).thenReturn(mockDuplicateXYZNodeInfo3);
        when(mockIndvCrosswalkLaneGeometryInfo1.getLaneID()).thenReturn(17);
        when(mockIndvCrosswalkLaneGeometryInfo1.getLaneConstructorType()).thenReturn(mockLaneConstructorType7);
        mockIndvCrosswalkLaneGeometryInfoList1 = Arrays.asList(mockIndvCrosswalkLaneGeometryInfo1);
        when(mockCrosswalkLaneGeometryLayer1.getLaneGeomLaneSet()).thenReturn(mockIndvCrosswalkLaneGeometryInfoList1);
        when(mockGeometryContainer9.getGeometryContainerID())
                .thenReturn(GeometryContainer.CROSSWALK_LANE_GEOMETRY_LAYER_ID);
        when(mockGeometryContainer9.getCrosswalkLaneGeometryLayer()).thenReturn(mockCrosswalkLaneGeometryLayer1);

        // CONTAINER 10
        when(mockNodeXYZOffsetValue13.getChoice()).thenReturn(NodeXYZOffsetValue.OFFSET_B10);
        when(mockNodeXYZOffsetValue13.getOffsetB10()).thenReturn((long) 2);
        when(mockNodeXYZOffsetValue14.getChoice()).thenReturn(NodeXYZOffsetValue.OFFSET_B11);
        when(mockNodeXYZOffsetValue14.getOffsetB11()).thenReturn((long) 3);
        when(mockNodeXYZOffsetValue15.getChoice()).thenReturn(NodeXYZOffsetValue.OFFSET_B12);
        when(mockNodeXYZOffsetValue15.getOffsetB12()).thenReturn((long) 4);
        when(mockNodeXYZOffsetInfo5.getNodeXOffsetValue()).thenReturn(mockNodeXYZOffsetValue13);
        when(mockNodeXYZOffsetInfo5.getNodeYOffsetValue()).thenReturn(mockNodeXYZOffsetValue14);
        when(mockNodeXYZOffsetInfo5.getNodeZOffsetValue()).thenReturn(mockNodeXYZOffsetValue15);
        when(mockComputedXYZNodeInfo3.getRefLaneID()).thenReturn(44);
        when(mockComputedXYZNodeInfo3.getLaneCenterLineXYZOffset()).thenReturn(mockNodeXYZOffsetInfo5);
        when(mockComputedXYZNodeInfo3.getLanePlanarGeomInfo()).thenReturn(mockWayPlanarGeometryInfo3);
        when(mockLaneConstructorType8.getChoice()).thenReturn(LaneConstructorType.COMPUTED_NODE);
        when(mockLaneConstructorType8.getComputedXYZNodeInfo()).thenReturn(mockComputedXYZNodeInfo3);
        when(mockIndvCrosswalkLaneGeometryInfo2.getLaneID()).thenReturn(18);
        when(mockIndvCrosswalkLaneGeometryInfo2.getLaneConstructorType()).thenReturn(mockLaneConstructorType8);
        mockIndvCrosswalkLaneGeometryInfoList2 = Arrays.asList(mockIndvCrosswalkLaneGeometryInfo2);
        when(mockCrosswalkLaneGeometryLayer2.getLaneGeomLaneSet()).thenReturn(mockIndvCrosswalkLaneGeometryInfoList2);
        when(mockGeometryContainer10.getGeometryContainerID())
                .thenReturn(GeometryContainer.CROSSWALK_LANE_GEOMETRY_LAYER_ID);
        when(mockGeometryContainer10.getCrosswalkLaneGeometryLayer()).thenReturn(mockCrosswalkLaneGeometryLayer2);

        // CONTAINER 11
        when(mockNodeXYZOffsetValue16.getChoice()).thenReturn(NodeXYZOffsetValue.OFFSET_B13);
        when(mockNodeXYZOffsetValue16.getOffsetB13()).thenReturn((long) 101);
        when(mockNodeXYZOffsetValue17.getChoice()).thenReturn(NodeXYZOffsetValue.OFFSET_B14);
        when(mockNodeXYZOffsetValue17.getOffsetB14()).thenReturn((long) 151);
        when(mockNodeXYZOffsetValue18.getChoice()).thenReturn(NodeXYZOffsetValue.OFFSET_B16);
        when(mockNodeXYZOffsetValue18.getOffsetB16()).thenReturn((long) 201);
        when(mockNodeXYZOffsetInfo6.getNodeXOffsetValue()).thenReturn(mockNodeXYZOffsetValue16);
        when(mockNodeXYZOffsetInfo6.getNodeYOffsetValue()).thenReturn(mockNodeXYZOffsetValue17);
        when(mockNodeXYZOffsetInfo6.getNodeZOffsetValue()).thenReturn(mockNodeXYZOffsetValue18);
        when(mockIndividualXYZNodeGeometryInfo3.getNodeXYZOffsetInfo()).thenReturn(mockNodeXYZOffsetInfo6);
        mockIndividualXYZNodeGeometryInfoList3 = Arrays.asList(mockIndividualXYZNodeGeometryInfo3);
        when(mockPhysicalXYZNodeInfo3.getNodeXYZGeometryNodeSet()).thenReturn(mockIndividualXYZNodeGeometryInfoList3);
        when(mockLaneConstructorType9.getChoice()).thenReturn(LaneConstructorType.PHYSICAL_NODE);
        when(mockLaneConstructorType9.getPhysicalXYZNodeInfo()).thenReturn(mockPhysicalXYZNodeInfo3);
        when(mockIndvCrosswalkLaneGeometryInfo3.getLaneID()).thenReturn(19);
        when(mockIndvCrosswalkLaneGeometryInfo3.getLaneConstructorType()).thenReturn(mockLaneConstructorType9);
        // TimeRestrictions
        when(mockDaysOfTheWeek3.getDaysOfTheWeekValue()).thenReturn((short) 5);
        when(mockTimeWindowInformation3.getDaysOfTheWeek()).thenReturn(mockDaysOfTheWeek3);
        when(mockStartPeriod3.getYear()).thenReturn(2023);
        when(mockStartPeriod3.getMonth()).thenReturn(12);
        when(mockStartPeriod3.getDay()).thenReturn(22);
        when(mockStartPeriod3.getHour()).thenReturn(10);
        when(mockStartPeriod3.getMinute()).thenReturn(30);
        when(mockStartPeriod3.getSecond()).thenReturn(00);
        when(mockStartPeriod3.getOffset()).thenReturn(90);
        when(mockTimeWindowInformation3.getStartPeriod()).thenReturn(mockStartPeriod3);
        when(mockEndPeriod3.getYear()).thenReturn(2025);
        when(mockEndPeriod3.getMonth()).thenReturn(5);
        when(mockEndPeriod3.getDay()).thenReturn(22);
        when(mockEndPeriod3.getHour()).thenReturn(10);
        when(mockEndPeriod3.getMinute()).thenReturn(00);
        when(mockEndPeriod3.getSecond()).thenReturn(00);
        when(mockTimeWindowInformation3.getEndPeriod()).thenReturn(mockEndPeriod3);
        when(mockGeneralPeriod3.getGeneralPeriodValue()).thenReturn(GeneralPeriod.NIGHT);
        when(mockTimeWindowInformation3.getGeneralPeriod()).thenReturn(mockGeneralPeriod3);
        mockTimeWindowSet3 = Arrays.asList(mockTimeWindowInformation3);
        when(mockFixedTimeWindowCtrl3.getTimeWindowSet()).thenReturn(mockTimeWindowSet3);
        when(mockTimeRestrictions4.getChoice()).thenReturn((int) RGATimeRestrictions.TIME_WINDOW_ITEM_CONTROL);
        when(mockTimeRestrictions4.getFixedTimeWindowCtrl()).thenReturn(mockFixedTimeWindowCtrl3);
        when(mockIndvCrosswalkLaneGeometryInfo3.getTimeRestrictions()).thenReturn(mockTimeRestrictions4);
        mockIndvCrosswalkLaneGeometryInfoList3 = Arrays.asList(mockIndvCrosswalkLaneGeometryInfo3);
        when(mockCrosswalkLaneGeometryLayer3.getLaneGeomLaneSet()).thenReturn(mockIndvCrosswalkLaneGeometryInfoList3);
        when(mockGeometryContainer11.getGeometryContainerID())
                .thenReturn(GeometryContainer.CROSSWALK_LANE_GEOMETRY_LAYER_ID);
        when(mockGeometryContainer11.getCrosswalkLaneGeometryLayer()).thenReturn(mockCrosswalkLaneGeometryLayer3);

        when(mockDaysOfTheWeek5.getDaysOfTheWeekValue()).thenReturn((short) 0);
        when(mockTimeWindowInformation5.getDaysOfTheWeek()).thenReturn(mockDaysOfTheWeek5);
        when(mockStartPeriod5.getYear()).thenReturn(2024);
        when(mockStartPeriod5.getMonth()).thenReturn(11);
        when(mockStartPeriod5.getDay()).thenReturn(25);
        when(mockStartPeriod5.getHour()).thenReturn(8);
        when(mockStartPeriod5.getMinute()).thenReturn(20);
        when(mockStartPeriod5.getSecond()).thenReturn(15);
        when(mockStartPeriod5.getOffset()).thenReturn(120);
        when(mockTimeWindowInformation5.getStartPeriod()).thenReturn(mockStartPeriod5);
        when(mockEndPeriod5.getYear()).thenReturn(2026);
        when(mockEndPeriod5.getMonth()).thenReturn(1);
        when(mockEndPeriod5.getDay()).thenReturn(27);
        when(mockEndPeriod5.getHour()).thenReturn(8);
        when(mockEndPeriod5.getMinute()).thenReturn(20);
        when(mockEndPeriod5.getSecond()).thenReturn(15);
        when(mockTimeWindowInformation5.getEndPeriod()).thenReturn(mockEndPeriod5);
        when(mockGeneralPeriod5.getGeneralPeriodValue()).thenReturn(GeneralPeriod.DAY);
        when(mockTimeWindowInformation5.getGeneralPeriod()).thenReturn(mockGeneralPeriod5);
        mockTimeWindowSet5 = Arrays.asList(mockTimeWindowInformation5);
        when(mockFixedTimeWindowCtrl5.getTimeWindowSet()).thenReturn(mockTimeWindowSet5);
        when(mockTimeRestrictions5.getChoice()).thenReturn((int) RGATimeRestrictions.TIME_WINDOW_ITEM_CONTROL);
        when(mockTimeRestrictions5.getFixedTimeWindowCtrl()).thenReturn(mockFixedTimeWindowCtrl5);
        when(mockWayDirectionOfTravelInfo1.getWayNodeDirectionOfTravel()).thenReturn((short)0b10000000);
        when(mockWayDirectionOfTravelInfo1.getTimeRestrictions()).thenReturn(mockTimeRestrictions5);
        mockWayDirectionOfTravelInfoList1 = Arrays.asList(mockWayDirectionOfTravelInfo1);
        when(mockIndividualWayDirectionsOfTravel1.getWayID()).thenReturn(14);
        when(mockIndividualWayDirectionsOfTravel1.getDirectionsOfTravelSet()).thenReturn(mockWayDirectionOfTravelInfoList1);       
        mockIndividualWayDirectionsOfTravelList1 = Arrays.asList(mockIndividualWayDirectionsOfTravel1);
        when(mockMtrVehLaneDirectionOfTravelLayer1.getLaneDirOfTravelLaneSet()).thenReturn(mockIndividualWayDirectionsOfTravelList1);
        when(mockMovementsContainer1.getMovementsContainerId()).thenReturn(MovementsContainer.MTR_VEH_LANE_DIRECTION_OF_TRAVEL_LAYER_ID);
        when(mockMovementsContainer1.getMtrVehLaneDirectionOfTravelLayer()).thenReturn(mockMtrVehLaneDirectionOfTravelLayer1);

        when(mockGeneralPeriod6.getGeneralPeriodValue()).thenReturn(GeneralPeriod.NIGHT);
        when(mockTimeWindowInformation6.getGeneralPeriod()).thenReturn(mockGeneralPeriod6);
        mockTimeWindowSet6 = Arrays.asList(mockTimeWindowInformation6);
        when(mockFixedTimeWindowCtrl6.getTimeWindowSet()).thenReturn(mockTimeWindowSet6);
        when(mockTimeRestrictions6.getChoice()).thenReturn((int) RGATimeRestrictions.TIME_WINDOW_ITEM_CONTROL);
        when(mockTimeRestrictions6.getFixedTimeWindowCtrl()).thenReturn(mockFixedTimeWindowCtrl6);
        when(mockWayDirectionOfTravelInfo2.getWayNodeDirectionOfTravel()).thenReturn((short)0b01000000);
        when(mockWayDirectionOfTravelInfo2.getTimeRestrictions()).thenReturn(mockTimeRestrictions6);
        mockWayDirectionOfTravelInfoList2 = Arrays.asList(mockWayDirectionOfTravelInfo2);
        when(mockIndividualWayDirectionsOfTravel2.getWayID()).thenReturn(15);
        when(mockIndividualWayDirectionsOfTravel2.getDirectionsOfTravelSet()).thenReturn(mockWayDirectionOfTravelInfoList2);       
        mockIndividualWayDirectionsOfTravelList2 = Arrays.asList(mockIndividualWayDirectionsOfTravel2);
        when(mockMtrVehLaneDirectionOfTravelLayer2.getLaneDirOfTravelLaneSet()).thenReturn(mockIndividualWayDirectionsOfTravelList2);
        when(mockMovementsContainer2.getMovementsContainerId()).thenReturn(MovementsContainer.MTR_VEH_LANE_DIRECTION_OF_TRAVEL_LAYER_ID);
        when(mockMovementsContainer2.getMtrVehLaneDirectionOfTravelLayer()).thenReturn(mockMtrVehLaneDirectionOfTravelLayer2);

        when(mockGeneralPeriod7.getGeneralPeriodValue()).thenReturn(GeneralPeriod.DAY);
        when(mockTimeWindowInformation7.getGeneralPeriod()).thenReturn(mockGeneralPeriod7);
        mockTimeWindowSet7 = Arrays.asList(mockTimeWindowInformation7);
        when(mockFixedTimeWindowCtrl7.getTimeWindowSet()).thenReturn(mockTimeWindowSet7);
        when(mockTimeRestrictions7.getChoice()).thenReturn((int) RGATimeRestrictions.TIME_WINDOW_ITEM_CONTROL);
        when(mockTimeRestrictions7.getFixedTimeWindowCtrl()).thenReturn(mockFixedTimeWindowCtrl7);
        when(mockWayType3.getWayTypeValue()).thenReturn(WayType.MOTOR_VEHICLE_LANE);
        when(mockLaneConnectionToInfo1.getWayType()).thenReturn(mockWayType3);
        when(mockLaneConnectionToInfo1.getWayID()).thenReturn(20);
        when(mockLaneConnectionToInfo1.getNodeToPosition()).thenReturn(LaneConnectionToInfo.FIRST_NODE);
        when(mockLaneConnectionFromInfo1.getNodeFromPosition()).thenReturn(LaneConnectionFromInfo.LAST_NODE);
        when(mockWayToWayConnectionInfo1.getLaneConnectionID()).thenReturn(101);
        when(mockWayToWayConnectionInfo1.getConnectionFromInfo()).thenReturn(mockLaneConnectionFromInfo1);
        when(mockWayToWayConnectionInfo1.getConnectionToInfo()).thenReturn(mockLaneConnectionToInfo1);
        when(mockWayToWayConnectionInfo1.getTimeRestrictions()).thenReturn(mockTimeRestrictions7);
        mockWayToWayConnectionInfoList1 = Arrays.asList(mockWayToWayConnectionInfo1);
        when(mockIndividualWayConnections1.getWayID()).thenReturn(19);
        when(mockIndividualWayConnections1.getConnectionsSet()).thenReturn(mockWayToWayConnectionInfoList1);
        mockIndividualWayConnectionsList1 = Arrays.asList(mockIndividualWayConnections1);
        when(mockMtrVehLaneConnectionsLayer1.getMtrVehLaneCnxnsLaneSet()).thenReturn(mockIndividualWayConnectionsList1);
        when(mockMovementsContainer3.getMovementsContainerId()).thenReturn(MovementsContainer.MTR_VEH_LANE_CONNECTIONS_LAYER_ID);
        when(mockMovementsContainer3.getMtrVehLnCnxnsLayer()).thenReturn(mockMtrVehLaneConnectionsLayer1);

        when(mockGeneralPeriod8.getGeneralPeriodValue()).thenReturn(GeneralPeriod.NIGHT);
        when(mockTimeWindowInformation8.getGeneralPeriod()).thenReturn(mockGeneralPeriod8);
        mockTimeWindowSet8 = Arrays.asList(mockTimeWindowInformation8);
        when(mockFixedTimeWindowCtrl8.getTimeWindowSet()).thenReturn(mockTimeWindowSet8);
        when(mockTimeRestrictions8.getChoice()).thenReturn((int) RGATimeRestrictions.TIME_WINDOW_ITEM_CONTROL);
        when(mockTimeRestrictions8.getFixedTimeWindowCtrl()).thenReturn(mockFixedTimeWindowCtrl8);
        when(mockWayType4.getWayTypeValue()).thenReturn(WayType.BICYCLE_LANE);
        when(mockLaneConnectionToInfo2.getWayType()).thenReturn(mockWayType4);
        when(mockLaneConnectionToInfo2.getWayID()).thenReturn(22);
        when(mockLaneConnectionToInfo2.getNodeToPosition()).thenReturn(LaneConnectionToInfo.LAST_NODE);
        when(mockLaneConnectionFromInfo2.getNodeFromPosition()).thenReturn(LaneConnectionFromInfo.FIRST_NODE);
        when(mockWayToWayConnectionInfo2.getLaneConnectionID()).thenReturn(102);
        when(mockWayToWayConnectionInfo2.getConnectionFromInfo()).thenReturn(mockLaneConnectionFromInfo2);
        when(mockWayToWayConnectionInfo2.getConnectionToInfo()).thenReturn(mockLaneConnectionToInfo2);
        when(mockWayToWayConnectionInfo2.getTimeRestrictions()).thenReturn(mockTimeRestrictions8);
        mockWayToWayConnectionInfoList2 = Arrays.asList(mockWayToWayConnectionInfo2);
        when(mockIndividualWayConnections2.getWayID()).thenReturn(21);
        when(mockIndividualWayConnections2.getConnectionsSet()).thenReturn(mockWayToWayConnectionInfoList2);
        mockIndividualWayConnectionsList2 = Arrays.asList(mockIndividualWayConnections2);
        when(mockBicycleLaneConnectionsLayer1.getBicycleLaneCnxnsLaneSet()).thenReturn(mockIndividualWayConnectionsList2);
        when(mockMovementsContainer4.getMovementsContainerId()).thenReturn(MovementsContainer.BIKE_LANE_CONNECTIONS_LAYER_ID);
        when(mockMovementsContainer4.getBikeLnCnxnsLayer()).thenReturn(mockBicycleLaneConnectionsLayer1);

        // MovementsContainer mockMovementsContainer5;
        // MtrVehLaneConnectionsManeuversLayer mockMtrVehLaneCnxnMnvrLayer1;
        // List<IndividualWayCnxnsManeuvers> mockIndividualLaneCnxnsManeuversLaneSet1;
        // List<WayCnxnManeuverInfo> mockCnxnManeuversSet1;
        // List<CnxnManeuverInfo> mockManeuversSet1;
        // WayCnxnManeuvers mockAllowedManeuvers1;
        // WayCnxnManeuverControlType mockManeuverControlType1;


        when(mockManeuverControlType1.getChoice()).thenReturn(1);
        when(mockAllowedManeuvers1.getWayCnxnManeuvers()).thenReturn((long)0);
        when(mockCnxnManeuverInfo1.getAllowedManeuvers()).thenReturn(mockAllowedManeuvers1);
        when(mockCnxnManeuverInfo1.getManeuverControlType()).thenReturn(mockManeuverControlType1);
        mockManeuversSet1 = Arrays.asList(mockCnxnManeuverInfo1);
        when(mockWayCnxnManeuverInfo1.getConnectionID()).thenReturn(1);
        when(mockWayCnxnManeuverInfo1.getManeuversSet()).thenReturn(mockManeuversSet1);
        mockCnxnManeuversSet1 = Arrays.asList(mockWayCnxnManeuverInfo1);
        when(mockIndidvidualWayCnxnsManeuvers1.getWayID()).thenReturn(2);
        when(mockIndidvidualWayCnxnsManeuvers1.getCnxnManeuversSet()).thenReturn(mockCnxnManeuversSet1);
        mockIndividualLaneCnxnsManeuversLaneSet1 = Arrays.asList(mockIndidvidualWayCnxnsManeuvers1);
        when(mockMtrVehLaneConnectionsManeuversLayer1.getMtrVehLaneConnectionsManeuversLayer()).thenReturn(mockIndividualLaneCnxnsManeuversLaneSet1);
        when(mockMovementsContainer5.getMtrVehLnCnxnxMnvrLayer()).thenReturn(mockMtrVehLaneConnectionsManeuversLayer1);




        when(mockRGA.getBaseLayer()).thenReturn(mockBaseLayer);
        mockGeometryContainerList = Arrays.asList(mockGeometryContainer1, mockGeometryContainer2,
                mockGeometryContainer3, mockGeometryContainer4, mockGeometryContainer5, mockGeometryContainer6,
                mockGeometryContainer7, mockGeometryContainer8, mockGeometryContainer9, mockGeometryContainer10,
                mockGeometryContainer11);
        mockMovementsContainerList = Arrays.asList(mockMovementsContainer1, mockMovementsContainer2, mockMovementsContainer3, mockMovementsContainer4);
        when(mockRGA.getGeometryContainers()).thenReturn(mockGeometryContainerList);
        when(mockRGA.getMovementsContainers()).thenReturn(mockMovementsContainerList);
    }

    @Test
    public void rgaEncodeTester() {
        ByteArrayObject res = encoder.encode(mockRGA);
        System.out.println(res.getMessage());
        byte[] expected = { 0, 43, -127, 31, 3, 0, 64, 73, -83, 39, 72, 59, 90, 78, -112, 80, -128, 107, -12, 69, 84,
                        16, 32, 19, 9, -20, 20, 2, 6, 12, 2, 8, 3, 4, 110, 42, 40, 27, -3, -6, 34, -83, -52, 0, 81, -92,
                        40, 2, 65, 72, 68, 0, 50, 0, 21, 56, 0, 0, 12, 5, 32, 0, 65, 0, 0, -76, 23, 4, -128, 1, 12, 33,
                        -127, 32, 34, -64, 51, 64, 34, 0, 9, 0, 60, 15, -3, -6, 34, -83, -52, 0, 81, -2, 127, 126, -120,
                        -84, -13, 0, 20, 105, 8, 71, -64, 16, -48, -109, 90, 78, -112, 118, -76, -99, 32, -95, 0, -41,
                        -24, -118, -128, 71, -125, 34, 32, -106, 84, 6, 66, 0, 10, 80, 4, 68, 96, 8, 16, 0, 28, -124,
                        32, -120, 0, 33, -28, 68, 36, 4, 88, 6, 104, 4, 88, 8, 0, -16, 47, -9, -25, -81, 50, -48, 0, 8,
                        -83, -3, -6, 79, -52, -56, 0, 1, -92, 0, 8, 52, 0, 32, 0, 8, -16, 10, 68, 1, -22, -128, 20, 88,
                        4, -63, 0, 4, 80, -84, 12, 40, 0, 72, -117, 4, -128, -117, 0, -51, 0, -128, -57, -64, 68, -64,
                        0, 30, 12, -88, -126, 93, 80, 25, 32, 7, -127, 127, -65, 62, 89, 79, 0, 0, 58, 47, -17, -46,
                        -74, 80, 0, 0, 13, 33, 3, 0, -72, 0, 56, 20, 0, 120, 3, -5, -12, 94, 80, -96, 0, 123, -64, -2,
                        -3, 67, -76, 40, 0, 30, -46, 0, 1, -128, 1, -32, -112, 0, 80, 2, 18, 0, 19, 5, -108, -112, 40,
                        0, 2, 0, 48, -112, 1, 80, 89, -127, 34, -56, 0, 40, 0 };

        Assert.assertArrayEquals(expected, res.getMessage()); 
    }
}