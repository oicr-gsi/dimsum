package ca.on.oicr.gsi.dimsum.controller.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/omissions")
public class OmissionsController {

  @GetMapping
  public String getOmissionsPage() {
    return "omissions";
  }

}
