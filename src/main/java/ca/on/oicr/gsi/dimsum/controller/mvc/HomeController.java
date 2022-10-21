package ca.on.oicr.gsi.dimsum.controller.mvc;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

  @GetMapping("/")
  public String getHomePage() {
    return "index";
  }

  @GetMapping("/login")
  public String getLoginPage(ModelMap model) {
    model.put("hideNav", true);
    if (isAuthenticated()) {
      return "redirect:/";
    } else {
      return "login";
    }
  }

  private static boolean isAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken);
  }

}
