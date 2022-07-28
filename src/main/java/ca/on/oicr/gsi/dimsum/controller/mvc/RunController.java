package ca.on.oicr.gsi.dimsum.controller.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ca.on.oicr.gsi.dimsum.controller.NotFoundException;
import ca.on.oicr.gsi.dimsum.data.RunAndLibraries;
import ca.on.oicr.gsi.dimsum.service.CaseService;

@Controller
@RequestMapping("/runs")
public class RunController {

  @Autowired
  private CaseService caseService;

  @GetMapping("/{runName}")
  public String getRunPage(@PathVariable String runName, ModelMap model) {
    RunAndLibraries runAndLibraries = caseService.getRunAndLibraries(runName);
    if (runAndLibraries == null) {
      throw new NotFoundException(String.format("No data found for run %s", runName));
    }
    model.put("title", String.format("%s Run Details", runName));
    model.put("runName", runName);
    // TODO: add sequencing attributes to display at top of page
    model.put("showLibraryQualifications", !runAndLibraries.getLibraryQualifications().isEmpty());
    model.put("showFullDepthSequencings", !runAndLibraries.getFullDepthSequencings().isEmpty());
    return "run";
  }

}
