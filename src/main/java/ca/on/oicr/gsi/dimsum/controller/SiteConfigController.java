package ca.on.oicr.gsi.dimsum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ca.on.oicr.gsi.dimsum.FrontEndConfig;
import tools.jackson.databind.json.JsonMapper;

@Controller
public class SiteConfigController {

  @Autowired
  private FrontEndConfig frontEndConfig;

  @Autowired
  private JsonMapper jsonMapper;

  @GetMapping(path = "/js/site-config.js", produces = "text/javascript")
  @ResponseBody
  public String getSiteConfigScript() {
    return String.format("window.siteConfig = %s;",
        jsonMapper.writeValueAsString(frontEndConfig));
  }


}
