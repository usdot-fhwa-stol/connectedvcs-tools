package gov.usdot.cv.azuremap;

import gov.usdot.cv.azuremap.controllers.TileProxyController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TileProxyController.class)
@TestPropertySource(properties = "azure.map.api.key=fake-api-key,azure.map.tileset.url=https://atlas.microsoft.com/map/tile?api-version=2.1&tilesetId=microsoft.imagery&zoom=%d&x=%d&y=%d&subscription-key=%s,allowed.origins=*")
public class TileProxyControllerTest {
  @MockBean
  private TileProxyController tileProxyController;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void getTileProxyShouldReturnMessageFromService() throws Exception {
    try {
      this.mockMvc.perform(get("/azuremap/api/proxy/tileset/1/1/1"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.IMAGE_PNG));
    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
      throw e;
    }
  }
}
