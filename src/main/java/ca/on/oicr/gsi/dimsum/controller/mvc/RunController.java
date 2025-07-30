package ca.on.oicr.gsi.dimsum.controller.mvc;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ca.on.oicr.gsi.cardea.data.Run;
import ca.on.oicr.gsi.cardea.data.Sample;
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
    String runStatus = getRunStatus(runAndLibraries.getRun());
    model.put("title", String.format("%s Run Details", runName));
    model.put("run", runAndLibraries.getRun());
    model.put("runStatus", runStatus);
    model.put("libraryDesigns",
        Stream
            .concat(runAndLibraries.getLibraryQualifications().stream(),
                runAndLibraries.getFullDepthSequencings().stream())
            .map(Sample::getLibraryDesignCode)
            .distinct()
            .collect(Collectors.joining(",")));
    return "run-detail";
  }

  private static String getRunStatus(Run run) {
    // must return a QcStatusKey from qc-status.ts
    if (run.getQcPassed() != null) {
      if (run.getDataReviewPassed() == null) {
        return "dataReview";
      } else if (!run.getDataReviewPassed()) {
        return "failed";
      }
    }
    if (run.getQcPassed() == null) {
      return "qc";
    } else if (run.getQcPassed()) {
      return "passed";
    } else {
      return "failed";
    }
  }

}
