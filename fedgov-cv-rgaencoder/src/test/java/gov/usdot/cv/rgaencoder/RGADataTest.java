/*
 * Copyright (C) 2025 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.rgaencoder;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
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
 * These tests verify that contract and ensure the list semantics work correctly.
 */
public class RGADataTest {

    private RGAData rgaData;

    @Before
    public void setUp() {
        // RGAData() default constructor initializes all lists to empty ArrayList
        rgaData = new RGAData();
    }

    // =========================================================================
    // RGAData.addGeometryContainer
    // =========================================================================

    @Test
    public void addGeometryContainer_nonNull_addsToList() {
        GeometryContainer gc = new GeometryContainer();
        rgaData.addGeometryContainer(gc);

        List<GeometryContainer> list = rgaData.getGeometryContainers();
        assertNotNull(list);
        assertEquals(1, list.size());
        assertSame(gc, list.get(0));
    }

    @Test
    public void addGeometryContainer_null_doesNotAdd() {
        rgaData.addGeometryContainer(null);
        List<GeometryContainer> list = rgaData.getGeometryContainers();
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    @Test
    public void addGeometryContainer_multipleItems_preservesOrder() {
        GeometryContainer gc1 = new GeometryContainer();
        GeometryContainer gc2 = new GeometryContainer();

        rgaData.addGeometryContainer(gc1);
        rgaData.addGeometryContainer(gc2);

        List<GeometryContainer> list = rgaData.getGeometryContainers();
        assertEquals(2, list.size());
        assertSame(gc1, list.get(0));
        assertSame(gc2, list.get(1));
    }

    // =========================================================================
    // RGAData.addMovementsContainer
    // =========================================================================

    @Test
    public void addMovementsContainer_nonNull_addsToList() {
        MovementsContainer mc = new MovementsContainer();
        rgaData.addMovementsContainer(mc);

        List<MovementsContainer> list = rgaData.getMovementsContainers();
        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    public void addMovementsContainer_null_doesNotAdd() {
        rgaData.addMovementsContainer(null);
        List<MovementsContainer> list = rgaData.getMovementsContainers();
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    // =========================================================================
    // RGAData.addWayUseContainer
    // =========================================================================

    @Test
    public void addWayUseContainer_nonNull_addsToList() {
        WayUseContainer wc = new WayUseContainer();
        rgaData.addWayUseContainer(wc);

        List<WayUseContainer> list = rgaData.getWayUseContainers();
        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    public void addWayUseContainer_null_doesNotAdd() {
        rgaData.addWayUseContainer(null);
        List<WayUseContainer> list = rgaData.getWayUseContainers();
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    // =========================================================================
    // IndividualApproachGeometryInfo.addIndividualWayTypesSet
    // =========================================================================

    @Test
    public void addIndividualWayTypesSet_nonNull_addsToList() {
        IndividualApproachGeometryInfo info = new IndividualApproachGeometryInfo(1, new ArrayList<>());
        ApproachWayTypeIDSet wayTypeSet = new ApproachWayTypeIDSet();
        info.addIndividualWayTypesSet(wayTypeSet);

        List<ApproachWayTypeIDSet> result = info.getApproachWayTypeIDSet();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void addIndividualWayTypesSet_null_doesNotAdd() {
        IndividualApproachGeometryInfo info = new IndividualApproachGeometryInfo(1, new ArrayList<>());
        info.addIndividualWayTypesSet(null);

        List<ApproachWayTypeIDSet> result = info.getApproachWayTypeIDSet();
        assertTrue(result == null || result.isEmpty());
    }

    // =========================================================================
    // SpeedLimitInfo.addMaxSpeedLimitSettingsSet / addMinSpeedLimitSettingsSet
    // =========================================================================

    @Test
    public void addMaxSpeedLimit_nonNull_addsToList() {
        SpeedLimitInfo speedInfo = new SpeedLimitInfo();
        IndividualSpeedLimitSettings maxLimit = new IndividualSpeedLimitSettings();
        speedInfo.addMaxSpeedLimitSettingsSet(maxLimit);

        List<IndividualSpeedLimitSettings> result = speedInfo.getMaxSpeedLimitSettingsSet();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void addMinSpeedLimit_nonNull_addsToList() {
        SpeedLimitInfo speedInfo = new SpeedLimitInfo();
        IndividualSpeedLimitSettings minLimit = new IndividualSpeedLimitSettings();
        speedInfo.addMinSpeedLimitSettingsSet(minLimit);

        List<IndividualSpeedLimitSettings> result = speedInfo.getMinSpeedLimitSettingsSet();
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}