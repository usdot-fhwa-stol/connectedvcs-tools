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
import { fetchGnisFeatures, searchGnisFeatures } from "./api.js";

/**
 * TIM packetID is a 9-byte UniqueMSGID, stored as 18 hex characters:
 * the 3-byte GNIS feature ID of the TIM location followed by 6 random bytes.
 */
const PACKET_ID_PATTERN = /^[0-9A-F]{18}$/i;
const GNIS_HEX_LENGTH = 6;
const UNIQUE_ID_BYTES = 6;
// Matches the names the proxy accepts, with an optional ", ST" to search another state
const GNIS_SEARCH_PATTERN = /^([A-Za-z0-9 .'-]{2,50}?)\s*(?:,\s*([A-Za-z]{2}))?$/;
const GNIS_SEARCH_PLACEHOLDER = 'Search a state, county or place, e.g. Texas or Springfield, IL';

let gnisLookupSeq = 0;
let gnisSearchSeq = 0;
// Place, county and state containing the current marker, and that state's code
let locationFeatures = [];
let locationState = null;
// Labels of every GNIS code shown this session, so a saved code chosen from a search keeps its name
const gnisLabels = new Map();

function toHex(value, length) {
  return value.toString(16).toUpperCase().padStart(length, '0');
}

function generatePacketUniqueId() {
  const bytes = crypto.getRandomValues(new Uint8Array(UNIQUE_ID_BYTES));
  return Array.from(bytes, b => toHex(b, 2)).join('');
}

function parsePacketId(packetID) {
  if (typeof packetID !== 'string' || !PACKET_ID_PATTERN.test(packetID)) {
    return null;
  }
  return {
    gnisId: parseInt(packetID.slice(0, GNIS_HEX_LENGTH), 16),
    uniqueId: packetID.slice(GNIS_HEX_LENGTH).toUpperCase()
  };
}

function gnisLabel(feature) {
  const parts = [feature.name];
  if (feature.county && feature.county !== feature.name) parts.push(feature.county);
  if (feature.type !== 'State') parts.push(feature.state);
  const label = `${parts.join(', ')} (${feature.type}) - GNIS ${feature.gnisId}`;
  gnisLabels.set(feature.gnisId, label);
  return label;
}

function toSelect2Item(feature) {
  return { id: String(feature.gnisId), text: gnisLabel(feature) };
}

function updatePacketId() {
  const gnisId = Number($('#packet_gnis').val()) || 0;
  $('#packet_id').val(toHex(gnisId, GNIS_HEX_LENGTH) + $('#packet_unique').val());
}

/**
 * Purpose: supply Select2 with the location's codes, followed by state, county and place search results
 *          once the user types
 */
function gnisSearchTransport(params, success) {
  const term = (params.data.term || '').trim();
  const results = [];
  if (locationFeatures.length) {
    results.push({ text: 'This location', children: locationFeatures.map(toSelect2Item) });
  }
  if (term.length < 2) {
    success({ results });
    return;
  }

  const match = term.match(GNIS_SEARCH_PATTERN);
  if (!match) {
    results.push({ text: 'Search results', children: [
      { id: '', text: 'Use letters, numbers, spaces, periods, apostrophes or hyphens', disabled: true }] });
    success({ results });
    return;
  }

  const name = match[1];
  const state = match[2]?.toUpperCase() ?? locationState;
  const heading = state
    ? `States, and counties and places in ${state} (add ", ST" for another state)`
    : 'States, counties and places';
  const seq = ++gnisSearchSeq;
  searchGnisFeatures(name, state)
    .then(features => features.length
      ? features.map(toSelect2Item)
      : [{ id: '', text: `Nothing named "${name}" found`, disabled: true }])
    .catch(() => [{ id: '', text: 'GNIS search is unavailable, try again later', disabled: true }])
    .then(children => {
      // Only the latest search updates the list
      if (seq !== gnisSearchSeq) return;
      results.push({ text: heading, children });
      success({ results });
    });
}

/**
 * Purpose: load the place, county and state GNIS codes containing lat/lon
 * @params preferredGnisId stays selected even if it is not one of them (it may have come from a
 *         search, and packetID is shared by every frame); otherwise the most specific code is selected
 */
async function loadGnisOptions(lat, lon, preferredGnisId) {
  const select = $('#packet_gnis');
  const seq = ++gnisLookupSeq;
  locationFeatures = [];
  select.empty().append(new Option('Loading GNIS codes...', preferredGnisId ?? '', true, true));
  select.prop('disabled', true).trigger('change.select2');
  updatePacketId();

  const features = await fetchGnisFeatures(lat, lon);
  // A newer lookup started while this one was in flight
  if (seq !== gnisLookupSeq) return;

  locationFeatures = features;
  locationState = features.find(f => f.state)?.state ?? null;
  select.empty();
  features.forEach(f => select.append(new Option(gnisLabel(f), f.gnisId)));
  if (preferredGnisId && !features.some(f => f.gnisId === preferredGnisId)) {
    select.append(new Option(gnisLabels.get(preferredGnisId) ?? `GNIS ${preferredGnisId} (saved)`, preferredGnisId));
  }
  if (!select.find('option').length) {
    select.append(new Option('No GNIS codes found for this location, search by name', ''));
  }
  select.val(preferredGnisId || features[0]?.gnisId || '');
  select.prop('disabled', false).trigger('change.select2');
  updatePacketId();
}

/**
 * Purpose: populate packet ID fields from an existing packetID, generating a new unique ID if there is none
 */
function loadPacketIdFields(packetID, lat, lon) {
  const parsed = parsePacketId(packetID);
  $('#packet_unique').val(parsed?.uniqueId ?? generatePacketUniqueId());
  loadGnisOptions(lat, lon, parsed?.gnisId);
}

function initPacketIdControls() {
  $('#packet_gnis').select2({
    width: '100%',
    dropdownParent: $('#packet_gnis_container'),
    minimumInputLength: 0,
    ajax: {
      delay: 300,
      transport: gnisSearchTransport
    }
  }).on('select2:open', () => {
    $('#packet_gnis_container .select2-search__field').attr('placeholder', GNIS_SEARCH_PLACEHOLDER);
  }).on('change', updatePacketId);

  $('#regenerate_packet_unique').on('click', () => {
    $('#packet_unique').val(generatePacketUniqueId());
    updatePacketId();
  });

  // Refresh GNIS choices when a TIM marker's position is typed in
  $('#lat, #long').on('change', () => {
    const lat = parseFloat($('#lat').val());
    const lon = parseFloat($('#long').val());
    if ($('.content-tab').is(':visible') && !isNaN(lat) && !isNaN(lon)) {
      loadGnisOptions(lat, lon, Number($('#packet_gnis').val()) || undefined);
    }
  });
}

export {
  initPacketIdControls,
  loadPacketIdFields
}
