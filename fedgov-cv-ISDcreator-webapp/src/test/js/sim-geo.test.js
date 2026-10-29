import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
    anchorScales, displayLonLatToLocal, localToDisplayLonLat, localToSimLonLat, simLonLatToLocal
} from '../../main/webapp/js/sim-geo.js';

const near = (actual, expected, tolerance, message) =>
    assert.ok(Math.abs(actual - expected) <= tolerance, `${message}: ${actual} is not within ${tolerance} of ${expected}`);

test('the sim origin is local (0, 0)', () => {
    assert.deepEqual(simLonLatToLocal([0, 0]), [0, 0]);
    assert.deepEqual(localToSimLonLat([0, 0]), [0, 0]);
});

test('sim lat/lon -> local meters -> sim lat/lon round-trips', () => {
    for (const lonLat of [[0.001, 0.002], [-0.0005, 0.0013], [0.01, -0.01]]) {
        const [lon, lat] = localToSimLonLat(simLonLatToLocal(lonLat));
        near(lon, lonLat[0], 1e-12, 'lon');
        near(lat, lonLat[1], 1e-12, 'lat');
    }
});

test('one sim degree is about 111 km east and 110.6 km north', () => {
    const [east] = simLonLatToLocal([1, 0]);
    const [, north] = simLonLatToLocal([0, 1]);
    near(east, 111319.49, 0.01, 'east');
    near(north, 110574.27, 1, 'north');
});

test('anchor scales match WGS84 meters per degree', () => {
    const equator = anchorScales({ lat: 0, lon: 0 });
    near(equator.lat, 110574.27, 1, 'lat scale at the equator');
    near(equator.lon, 111319.49, 0.01, 'lon scale at the equator');

    const sixty = anchorScales({ lat: 60, lon: 0 });
    near(sixty.lat, 111412.24, 1, 'lat scale at 60 degrees');
    near(sixty.lon, 55799.98, 1, 'lon scale at 60 degrees');
});

test('local meters -> display lat/lon -> local meters round-trips at an anchor', () => {
    const anchor = { lat: 42.3369, lon: -83.0508 };
    for (const local of [[0, 0], [125.5, -40.25], [-300, 800]]) {
        const [e, n] = displayLonLatToLocal(localToDisplayLonLat(local, anchor), anchor);
        near(e, local[0], 1e-6, 'east');
        near(n, local[1], 1e-6, 'north');
    }
});

test('the anchor is local (0, 0) and offsets are in meters', () => {
    const anchor = { lat: 42.3369, lon: -83.0508 };
    assert.deepEqual(localToDisplayLonLat([0, 0], anchor), [anchor.lon, anchor.lat]);

    const s = anchorScales(anchor);
    const [lon, lat] = localToDisplayLonLat([100, 50], anchor);
    near((lon - anchor.lon) * s.lon, 100, 1e-9, 'east offset');
    near((lat - anchor.lat) * s.lat, 50, 1e-9, 'north offset');
});
