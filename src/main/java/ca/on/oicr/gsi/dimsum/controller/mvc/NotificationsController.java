package ca.on.oicr.gsi.dimsum.controller.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NotificationsController {

  @GetMapping("/notifications")
  public String getNotificationsPage() {
    return "notifications";
  }

}
