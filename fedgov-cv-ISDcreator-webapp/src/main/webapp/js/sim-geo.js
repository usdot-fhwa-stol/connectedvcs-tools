/*
 * Copyright (C) 2026 LEIDOS.
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

/**
 * Sim mode coordinate conversions (used by sim-overlay.js).
 *
 * Positions are related through local meters (E, N) from the sim origin:
 *
 *   sim lat/lon  <->  (E, N)  <->  display lat/lon at the anchor
 *
 * Pure functions with no browser or OpenLayers dependencies, so they can be unit tested.
 */

const WGS84_A = 6378137.0;
const WGS84_E2 = 0.00669437999014;

function toRad(deg) { return deg * Math.PI / 180; }
function toDeg(rad) { return rad * 180 / Math.PI; }

/** Sim lat/lon -> local meters. Transverse Mercator at 0,0 on WGS84 (the OSM geoReference). */
export function simLonLatToLocal([lon, lat]) {
    return [WGS84_A * toRad(lon), WGS84_A * (1 - WGS84_E2) * toRad(lat)];
}

export function localToSimLonLat([e, n]) {
    return [toDeg(e / WGS84_A), toDeg(n / (WGS84_A * (1 - WGS84_E2)))];
}

/** Meters per degree of latitude and longitude at the display anchor (local tangent plane). */
export function anchorScales(anchor) {
    const phi = toRad(anchor.lat);
    const w = 1 - WGS84_E2 * Math.sin(phi) ** 2;
    const meridional = WGS84_A * (1 - WGS84_E2) / Math.pow(w, 1.5);
    const primeVertical = WGS84_A / Math.sqrt(w);
    return {
        lat: toRad(meridional),
        lon: toRad(primeVertical * Math.cos(phi))
    };
}

export function localToDisplayLonLat([e, n], anchor) {
    const s = anchorScales(anchor);
    return [anchor.lon + e / s.lon, anchor.lat + n / s.lat];
}

export function displayLonLatToLocal([lon, lat], anchor) {
    const s = anchorScales(anchor);
    return [(lon - anchor.lon) * s.lon, (lat - anchor.lat) * s.lat];
}
