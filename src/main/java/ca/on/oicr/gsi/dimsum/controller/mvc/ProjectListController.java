package ca.on.oicr.gsi.dimsum.controller.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProjectListController {
  @GetMapping("/projects")
  public String getProjectListPage() {
    return "project-list";
  }
}
