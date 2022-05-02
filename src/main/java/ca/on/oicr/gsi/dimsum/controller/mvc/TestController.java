package ca.on.oicr.gsi.dimsum.controller.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

  @GetMapping("/test")
  public String getHome(Model model) {
    model.addAttribute("name", "Tester");
    return "test";
  }

}
