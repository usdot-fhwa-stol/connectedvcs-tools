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
 * Status Bar Module
 *
 * Drives the slim, always-visible tool-hint bar that sits between the map and
 * the bottom control toolbar. It shows a short, plain-English hint describing
 * what the active tool does and how to use it, updating automatically as the
 * user switches modes.
 *
 * Usage (ES module):
 *   import { initStatusBar, setStatusHintForState } from '/private-resources/js/status-bar.js';
 *   initStatusBar('isd');                 // or 'tim'
 *   setStatusHintForState('bar');         // wired into toggleControlsOn(state)
 *
 * Any new tool state can be wired in by calling setStatusHint() /
 * setStatusHintForState() at the relevant event hook.
 */

// Per-app hint maps. `state` is the toolbar button's `value` attribute, which is
// passed straight through to toggleControlsOn(state) in each webapp.
const HINTS = {
  isd: {
    none: 'Create or import a parent/child MAP to get started',
    bar: 'Click and drag to draw an approach box. Double-click or release to finish',
    edit: 'Drag the center point to move · Drag the corner point to rotate/resize · Press Escape to exit',
    line: 'Click to add lane nodes · Double-click to finish the lane · Press Escape to cancel',
    modify: 'Click a lane to select it · Shift+Click a node to delete it · Press Escape to deselect',
    del: 'Click any lane, approach, or marker to delete it',
    measure: 'Click to start measuring · Double-click to finish',
    drag: 'Click a marker to lock or unlock it for dragging',
    placeComputed: 'Click on the map to place the computed lanes',
  },
  tim: {
    none: 'Add a verified point marker to get started or import an existing TIM',
    line: 'Click to add region nodes · Double-click to finish the region · Press Escape to cancel',
    modify: 'Click a region to select it · Shift+Click a node to delete it · Press Escape to deselect',
    polygon: 'Click to add polygon nodes · Double-click to finish the polygon · Press Escape to cancel',
    circle: 'Click and drag to draw a circle · Release to finish',
    change: 'Drag a node to reshape the polygon · Press Escape to deselect',
    dragPoly: 'Click and drag a region to move it',
    del: 'Click any region, polygon, or marker to delete it',
    measure: 'Click to start measuring · Double-click to finish',
    drag: 'Click a marker to lock or unlock it for dragging',
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
 */
export function setStatusHintForState(state) {
  const map = HINTS[appId] || {};
  const text = map[state] != null ? map[state] : map.none;
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
    popStatusHint,
  };
}
