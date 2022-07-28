package ca.on.oicr.gsi.dimsum.controller.rest;

import static ca.on.oicr.gsi.dimsum.controller.mvc.MvcUtils.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ca.on.oicr.gsi.dimsum.controller.NotFoundException;
import ca.on.oicr.gsi.dimsum.controller.rest.request.DataQuery;
import ca.on.oicr.gsi.dimsum.data.RunAndLibraries;
import ca.on.oicr.gsi.dimsum.data.Sample;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.SampleSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;

@RestController
@RequestMapping("/rest/runs")
public class RunRestController {

  @Autowired
  private CaseService caseService;

  @GetMapping("/{runName}/library-qualifications")
  public TableData<Sample> getLibraryQualifications(@PathVariable String runName,
      @RequestBody DataQuery query) {
    checkRunExists(runName);
    SampleSort sort = parseSort(query, SampleSort::getByLabel);
    boolean descending = parseDescending(query);
    return caseService.getLibraryQualificationsForRun(runName, query.getPageSize(),
        query.getPageNumber(), sort, descending);
  }

  @GetMapping("/{runName}/full-depth-sequencings")
  public TableData<Sample> getFullDepthSequencings(@PathVariable String runName,
      @RequestBody DataQuery query) {
    checkRunExists(runName);
    SampleSort sort = parseSort(query, SampleSort::getByLabel);
    boolean descending = parseDescending(query);
    return caseService.getFullDepthSequencingsForRun(runName, query.getPageSize(),
        query.getPageNumber(), sort, descending);
  }

  private void checkRunExists(String runName) {
    RunAndLibraries runAndLibraries = caseService.getRunAndLibraries(runName);
    if (runAndLibraries == null) {
      throw new NotFoundException(String.format("No data found for run %s", runName));
    }
  }

}
