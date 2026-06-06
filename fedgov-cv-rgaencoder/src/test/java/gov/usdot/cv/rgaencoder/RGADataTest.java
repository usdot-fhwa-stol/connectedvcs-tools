/*
 * Copyright (C) 2025 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.rgaencoder;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for the add() methods on RGAData, GeometryContainer, and related
 * container classes.
 *
 * WHY THESE TESTS HAVE VALUE:
 * The existing RGAEncodeTest mocks every object it touches, so the real
 * add*() methods are never called. Each add() method has a null guard
 * (if (x != null) { list.add(x); }) that is a real branch — adding null
 * should silently no-op rather than throw a NullPointerException.
 * These tests verify that contract and ensure the list semantics work
 * correctly end-to-end.
 */
public class RGADataTest {

    private RGAData rgaData;

    @Before
    public void setUp() {
        // Minimal BaseLayer required by RGAData constructor
        Position3D location = new Position3D();
        location.setLatitude(38955500);
        location.setLongitude(-77148900);
        location.setElevation(3800);

        DDate date = new DDate();
        date.setYear(2025);
        date.setMonth(6);
        date.setDay(1);

        BaseLayer baseLayer = new BaseLayer();
        baseLayer.setLocation(location);
        baseLayer.setTimeOfCalculation(date);

        rgaData = new RGAData(baseLayer);
    }

    // =========================================================================
    // RGAData.addGeometryContainer
    // =========================================================================

    @Test
    public void addGeometryContainer_nonNull_addsToList() {
        GeometryContainer gc = new GeometryContainer();
        gc.setGeometryContainerID(1);
        rgaData.addGeometryContainer(gc);

        List<GeometryContainer> list = rgaData.getGeometryContainers();
        assertEquals(1, list.size());
        assertSame(gc, list.get(0));
    }

    @Test
    public void addGeometryContainer_null_doesNotAdd() {
        rgaData.addGeometryContainer(null);
        assertEquals(0, rgaData.getGeometryContainers().size());
    }

    @Test
    public void addGeometryContainer_multipleItems_preservesOrder() {
        GeometryContainer gc1 = new GeometryContainer();
        gc1.setGeometryContainerID(1);
        GeometryContainer gc2 = new GeometryContainer();
        gc2.setGeometryContainerID(2);

        rgaData.addGeometryContainer(gc1);
        rgaData.addGeometryContainer(gc2);

        List<GeometryContainer> list = rgaData.getGeometryContainers();
        assertEquals(2, list.size());
        assertEquals(1, list.get(0).getGeometryContainerID());
        assertEquals(2, list.get(1).getGeometryContainerID());
    }

    // =========================================================================
    // RGAData.addMovementsContainer
    // =========================================================================

    @Test
    public void addMovementsContainer_nonNull_addsToList() {
        MovementsContainer mc = new MovementsContainer();
        rgaData.addMovementsContainer(mc);
        assertEquals(1, rgaData.getMovementsContainers().size());
    }

    @Test
    public void addMovementsContainer_null_doesNotAdd() {
        rgaData.addMovementsContainer(null);
        assertEquals(0, rgaData.getMovementsContainers().size());
    }

    // =========================================================================
    // RGAData.addWayUseContainer
    // =========================================================================

    @Test
    public void addWayUseContainer_nonNull_addsToList() {
        WayUseContainer wc = new WayUseContainer();
        rgaData.addWayUseContainer(wc);
        assertEquals(1, rgaData.getWayUseContainers().size());
    }

    @Test
    public void addWayUseContainer_null_doesNotAdd() {
        rgaData.addWayUseContainer(null);
        assertEquals(0, rgaData.getWayUseContainers().size());
    }

    // =========================================================================
    // RGAData.toString
    // =========================================================================

    @Test
    public void toString_emptyContainers_doesNotThrow() {
        String s = rgaData.toString();
        assertNotNull(s);
        assertTrue(s.contains("RGAData"));
    }

    @Test
    public void toString_withGeometryContainer_includesContainer() {
        GeometryContainer gc = new GeometryContainer();
        gc.setGeometryContainerID(42);
        rgaData.addGeometryContainer(gc);

        String s = rgaData.toString();
        assertNotNull(s);
        // With containers present, toString should include their representation
        assertFalse(s.contains("geometryContainers=[]"));
    }

    // =========================================================================
    // IndividualApproachGeometryInfo add methods
    // =========================================================================

    @Test
    public void addIndividualWayTypesSet_nonNull_addsToList() {
        IndividualApproachGeometryInfo info = new IndividualApproachGeometryInfo();
        ApproachWayTypeIDSet wayTypeSet = new ApproachWayTypeIDSet();
        info.addIndividualWayTypesSet(wayTypeSet);

        assertNotNull(info.getApproachWayTypeIDSets());
        assertEquals(1, info.getApproachWayTypeIDSets().size());
    }

    @Test
    public void addIndividualWayTypesSet_null_doesNotAdd() {
        IndividualApproachGeometryInfo info = new IndividualApproachGeometryInfo();
        info.addIndividualWayTypesSet(null);

        // Should be empty or null, not throw
        assertTrue(info.getApproachWayTypeIDSets() == null
                || info.getApproachWayTypeIDSets().isEmpty());
    }

    // =========================================================================
    // SpeedLimitInfo add methods
    // =========================================================================

    @Test
    public void addMaxSpeedLimit_nonNull_addsToList() {
        SpeedLimitInfo speedInfo = new SpeedLimitInfo();
        IndividualSpeedLimitSettings maxLimit = new IndividualSpeedLimitSettings();
        speedInfo.addMaxSpeedLimitSettingsSet(maxLimit);

        assertNotNull(speedInfo.getMaxSpeedLimitSettingsList());
        assertEquals(1, speedInfo.getMaxSpeedLimitSettingsList().size());
    }

    @Test
    public void addMinSpeedLimit_nonNull_addsToList() {
        SpeedLimitInfo speedInfo = new SpeedLimitInfo();
        IndividualSpeedLimitSettings minLimit = new IndividualSpeedLimitSettings();
        speedInfo.addMinSpeedLimitSettingsSet(minLimit);

        assertNotNull(speedInfo.getMinSpeedLimitSettingsList());
        assertEquals(1, speedInfo.getMinSpeedLimitSettingsList().size());
    }
}