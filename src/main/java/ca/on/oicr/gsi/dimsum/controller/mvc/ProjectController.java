package ca.on.oicr.gsi.dimsum.controller.mvc;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ca.on.oicr.gsi.dimsum.FrontEndConfig;
import ca.on.oicr.gsi.dimsum.controller.NotFoundException;
import ca.on.oicr.gsi.dimsum.data.Case;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilterKey;

@Controller
@RequestMapping("/projects")
public class ProjectController {

  @Autowired
  private CaseService caseService;
  @Autowired
  private FrontEndConfig frontEndConfig;

  @GetMapping("/{projectName}")
  public String getProjectDetailsPage(@PathVariable String projectName, ModelMap model) {
    List<Case> cases = caseService.getCases(new CaseFilter(CaseFilterKey.PROJECT, projectName));
    if (cases.isEmpty()) {
      throw new NotFoundException(String.format("No data found for project: %s", projectName));
    }

    model.put("title", String.format("%s Project Details", projectName));
    model.put("titleLink", makeMisoProjectUrl(projectName));
    model.put("detailType", CaseFilterKey.PROJECT.name());
    model.put("detailValue", projectName);
    return "project-detail";
  }

  @GetMapping
  public String getProjectListPage() {
    return "project-list";
  }

  private String makeMisoProjectUrl(String projectName) {
    return String.format("%s/miso/project/shortname/%s", frontEndConfig.getMisoUrl(), projectName);
  }

}
