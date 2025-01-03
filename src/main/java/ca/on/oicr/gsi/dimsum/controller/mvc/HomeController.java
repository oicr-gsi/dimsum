package ca.on.oicr.gsi.dimsum.controller.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import ca.on.oicr.gsi.dimsum.SecurityUtils;

@Controller
public class HomeController {

  @Autowired
  private SecurityUtils securityUtils;

  @GetMapping("/")
  public String getHomePage() {
    return "index";
  }

  @GetMapping("/login")
  public String getLoginPage(ModelMap model) {
    model.put("hideNav", true);
    if (SecurityUtils.isAuthenticated() || securityUtils.authenticationDisabled()) {
      return "redirect:/";
    } else {
      return "login";
    }
  }

}
