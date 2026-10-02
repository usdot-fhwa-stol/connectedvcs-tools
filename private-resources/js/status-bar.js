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

// Per-app hint maps. `state` is the toolbar button's `value` attribute, which is
// passed straight through to toggleControlsOn(state) in each webapp.
const HINTS = {
  isd: {
    none: 'Use File → New Parent/Child Map, then Show Builder to drag the Reference and Verified Point Markers onto the intersection',
    noneLoadedParent: 'Click a marker to select it, then drag to reposition · Delete to remove one · File → Save when done',
    noneLoaded: 'Choose a tool below to add approaches and lanes, or edit the map · File → Save when done',
    bar: 'Click and drag to draw an approach (stop bar) box, sized to the stop bar · Release to finish. Click Draw Approaches again to cancel',
    edit: 'Drag the center point to move the approach · Drag the corner point to rotate/resize it · Click Edit Approaches again to exit',
    line: 'Click to add lane nodes · Double-click the final node to finish the lane · Click Draw Lanes again to cancel',
    modify: 'Click a lane to select it · Drag a node to reshape the lane · Shift+Click a node to delete it · Click Edit Lanes again to exit',
    del: 'Click any lane, approach, or marker to delete it',
    measure: 'Click to start measuring · Double-click to finish',
    drag: 'Click a marker to select it for dragging, locking the others in place',
    placeComputed: 'Click the map where the computed lane should be created',
    connections: 'Open the Builder, then drag a maneuver icon onto Allowed Maneuvers or a Connections row to assign it · Click Done when finished'
  },
  tim: {
    none: 'Drag a Verified Point Marker onto a known, surveyed location and then drag a Road Sign onto the map to begin',
    noneLoaded: 'Choose a tool below to add or edit regions · File → Save when done',
    line: 'Click to add region nodes · Double-click the final node to finish the region · Click the tool again to cancel',
    modify: 'Click a region to select it · Drag a node to reshape it · Shift+Click a node to delete it · Click the tool again to exit',
    polygon: 'Click to add polygon nodes · Click the first node again to finish the polygon · Click the tool again to cancel',
    circle: 'Click and drag to draw a circle · Release to finish . Click the tool again to cancel',
    change: 'Drag a node to reshape the polygon · Click the tool again to exit',
    dragPoly: 'Click and drag a region to move it',
    del: 'Click any region, polygon, or marker to delete it',
    measure: 'Click to start measuring · Double-click to finish',
    drag: 'Click a marker to select it for dragging, locking the others in place',
  },
};

let appId = null;
// Stack used to save/restore the prior hint (e.g. around the georeferencing modal).
const hintStack = [];

function getEl() {
  return document.getElementById('tool-hint-text');
}

/**
 * Initialize the status bar for a given app and show its idle/default hint.
 * @param {('isd'|'tim')} app
 */
export function initStatusBar(app) {
  appId = app;
  setStatusHintForState('none');
}

/**
 * Set the hint text directly.
 * @param {string} text
 */
export function setStatusHint(text) {
  const el = getEl();
  if (el) {
    el.textContent = text || '';
  }
}

/**
 * Look up a hint for the given tool state and display it. Unknown states fall
 * back to the idle hint so the bar is never blank.
 * @param {string} state
 * @param {boolean} [hasContent] - When state is 'none', pass true if a map/TIM is
 *   already loaded (markers placed, file opened, etc.) to show the "pick a tool"
 *   hint instead of the initial "get started" hint.
 * @param {('parent'|'child')} [mapType] - ISD only. 'parent' shows the parent-map-specific
 *   idle hint (no lane/approach tools available), otherwise the general noneLoaded hint is used.
 */
export function setStatusHintForState(state, hasContent, mapType) {
  if (hintStack.length > 0) {
    return;
  }
  const map = HINTS[appId] || {};
  let text;
  if (state === 'none' && hasContent) {
    text = (mapType === 'parent' && map.noneLoadedParent != null)
      ? map.noneLoadedParent
      : map.noneLoaded;
  }
  if (text == null) {
    text = map[state] != null ? map[state] : map.none;
  }
  setStatusHint(text);
}

/**
 * Save the current hint and display a new one. Use this when entering a modal or
 * sub-flow (e.g. georeferencing) so the previous tool hint can be restored later.
 * @param {string} [text]
 */
export function pushStatusHint(text) {
  const el = getEl();
  hintStack.push(el ? el.textContent : '');
  if (text != null) {
    setStatusHint(text);
  }
}

/**
 * Save the current hint and display the hint registered for `state` in HINTS, so callers
 * don't need to hardcode hint copy themselves. No-op display-wise (but still pushes) if
 * `state` has no entry.
 * @param {string} state
 */
export function pushStatusHintForState(state) {
  const map = HINTS[appId] || {};
  pushStatusHint(map[state]);
}

/** Restore the most recently saved hint. */
export function popStatusHint() {
  if (hintStack.length) {
    setStatusHint(hintStack.pop());
  }
}

// Also expose on window for any non-module call sites.
if (typeof window !== 'undefined') {
  window.StatusBar = {
    init: initStatusBar,
    setStatusHint,
    setStatusHintForState,
    pushStatusHint,
    pushStatusHintForState,
    popStatusHint,
  };
}
