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
import { fetchGnisFeatures } from "./api.js";

/**
 * TIM packetID is a 9-byte UniqueMSGID, stored as 18 hex characters:
 * the 3-byte GNIS feature ID of the TIM location followed by 6 random bytes.
 */
const PACKET_ID_PATTERN = /^[0-9A-F]{18}$/i;
const GNIS_HEX_LENGTH = 6;
const UNIQUE_ID_BYTES = 6;

let gnisLookupSeq = 0;

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
  const details = feature.distanceMiles == null ? feature.type : `${feature.type}, ${feature.distanceMiles} mi`;
  return `${parts.join(', ')} (${details}) - GNIS ${feature.gnisId}`;
}

function appendGnisGroup(select, label, features) {
  if (!features.length) return;
  const group = $('<optgroup>').attr('label', label);
  features.forEach(f => group.append(new Option(gnisLabel(f), f.gnisId)));
  select.append(group);
}

function updatePacketId() {
  const gnisId = Number($('#packet_gnis').val()) || 0;
  $('#packet_id').val(toHex(gnisId, GNIS_HEX_LENGTH) + $('#packet_unique').val());
}

/**
 * Purpose: fill the GNIS dropdown with the features containing lat/lon, then nearby features by distance
 * @params preferredGnisId stays selected even if it is outside this area (packetID is
 *         shared by every frame); otherwise the most specific feature is selected
 */
async function loadGnisOptions(lat, lon, preferredGnisId) {
  const select = $('#packet_gnis');
  const seq = ++gnisLookupSeq;
  select.empty().append(new Option('Loading GNIS codes...', preferredGnisId ?? ''));
  select.prop('disabled', true);
  updatePacketId();

  const features = await fetchGnisFeatures(lat, lon);
  // A newer lookup started while this one was in flight
  if (seq !== gnisLookupSeq) return;

  select.empty();
  appendGnisGroup(select, 'Contains this location', features.filter(f => f.distanceMiles == null));
  appendGnisGroup(select, 'Nearby (closest first)', features.filter(f => f.distanceMiles != null));
  if (preferredGnisId && !features.some(f => f.gnisId === preferredGnisId)) {
    select.append(new Option(`GNIS ${preferredGnisId} (saved)`, preferredGnisId));
  }
  if (!select.find('option').length) {
    select.append(new Option('No GNIS codes found for this location', ''));
  }
  select.val(preferredGnisId || features[0]?.gnisId || '');
  select.prop('disabled', false);
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
  $('#packet_gnis').on('change', updatePacketId);

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
