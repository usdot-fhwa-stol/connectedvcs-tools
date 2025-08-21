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
package gov.usdot.cv.azuremap.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import gov.usdot.cv.azuremap.services.TileProxyService;

@RestController
@RequestMapping("/azuremap/api")
public class TileProxyController {
  
  private static final Logger logger = LogManager.getLogger(TileProxyController.class);

  private final TileProxyService tileProxyService;

  @Value("${tileset.response.cache.control}")
  private String cacheControlValue;

  public TileProxyController(TileProxyService tileProxyService) {
    this.tileProxyService = tileProxyService;
  }

  @GetMapping("/proxy/tileset/{tilesetId}/{z}/{x}/{y}")
  public ResponseEntity<byte[]> getTile(@PathVariable String tilesetId, @PathVariable int z, @PathVariable int x, @PathVariable int y) {
    try {
      // Fetch the tile from Azure Maps
      byte[] tileBytes = tileProxyService.fetchTileSets(tilesetId, z, x, y);
      
      if (tileBytes == null || tileBytes.length == 0) {
        logger.error("Tile not found for tilesetId: {}, z: {}, x: {}, y: {}", tilesetId, z, x, y);
        return ResponseEntity.notFound().build();
      }
      // Return the tile data back to the client
      return ResponseEntity.ok()
          .header("Content-Type", "image/png")
          .header("Cache-Control", cacheControlValue)
          .body(tileBytes);
    } catch (Exception e) {
      logger.error("Error fetching tile from Azure Maps", e);
      return ResponseEntity.status(500).body(null);
    }
  }
}
