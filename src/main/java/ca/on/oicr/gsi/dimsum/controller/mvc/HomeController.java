package ca.on.oicr.gsi.dimsum.controller.mvc;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import ca.on.oicr.gsi.dimsum.security.DimsumPrincipal;

@Controller
public class HomeController {

  @GetMapping("/")
  public String getHomePage() {
    return "index";
  }

  @GetMapping("/login")
  public String getLoginPage(ModelMap model, @AuthenticationPrincipal DimsumPrincipal principal) {
    model.put("hideNav", true);
    if (principal != null) {
      return "redirect:/";
    } else {
      return "login";
    }
  }

}
