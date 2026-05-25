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
package gov.usdot.cv.azuremap;

import gov.usdot.cv.azuremap.controllers.TileProxyController;
import gov.usdot.cv.azuremap.services.TileProxyService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.logging.log4j.Logger;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(TileProxyController.class)
@TestPropertySource(properties = {
  "azure.map.api.key=fake-api-key",
  "azure.map.tileset.url=https://atlas.microsoft.com/map/tile?api-version=2.1&tilesetId=microsoft.imagery&zoom=%d&x=%d&y=%d&subscription-key=%s",
  "aws.s3.accessKey=unknown",
  "aws.s3.secretKey=unknown",
  "aws.s3.bucket=unknown"
})
public class TileProxyControllerTest {
  @MockBean
  private TileProxyService tileProxyService;

  @Autowired
  private MockMvc mockMvc;

  private Logger logger = org.apache.logging.log4j.LogManager.getLogger(TileProxyControllerTest.class);

  @Test
  void getTileProxyShouldReturnMessageFromService() throws Exception {
  try {
    byte[] png = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47 };
    when(tileProxyService.fetchTileSets("microsoft.imagery", 1, 1, 1)).thenReturn(png);
    this.mockMvc.perform(get("/azuremap/api/proxy/tileset/microsoft.imagery/1/1/1"))
    .andExpect(status().isOk())
    .andExpect(content().contentType(MediaType.IMAGE_PNG));
  } catch (Exception e) {
    logger.error("Error occurred while fetching tile proxy: {}", e.getMessage());
    e.printStackTrace();
    throw e;
  }
  }
}
