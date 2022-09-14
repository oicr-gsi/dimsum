package ca.on.oicr.gsi.dimsum.controller.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RunListController {
  @GetMapping("/run-list")
  public String getRunListPage() {
    return "run-list";
  }
}
