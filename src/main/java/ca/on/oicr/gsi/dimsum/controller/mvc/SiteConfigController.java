package ca.on.oicr.gsi.dimsum.controller.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ca.on.oicr.gsi.dimsum.FrontEndConfig;

@Controller
public class SiteConfigController {

  @Autowired
  private FrontEndConfig frontEndConfig;

  @Autowired
  private ObjectMapper objectMapper;

  @GetMapping(path = "/js/site-config.js", produces = "text/javascript")
  @ResponseBody
  public String getSiteConfigScript() throws JsonProcessingException {
    return String.format("window.siteConfig = %s;",
        objectMapper.writeValueAsString(frontEndConfig));
  }


}
