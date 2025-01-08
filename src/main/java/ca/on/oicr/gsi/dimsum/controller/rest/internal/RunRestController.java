package ca.on.oicr.gsi.dimsum.controller.rest.internal;

import static ca.on.oicr.gsi.dimsum.controller.mvc.MvcUtils.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ca.on.oicr.gsi.cardea.data.OmittedRunSample;
import ca.on.oicr.gsi.cardea.data.Run;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.dimsum.controller.NotFoundException;
import ca.on.oicr.gsi.dimsum.controller.rest.request.DataQuery;
import ca.on.oicr.gsi.dimsum.data.RunAndLibraries;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.OmittedRunSampleSort;
import ca.on.oicr.gsi.dimsum.service.filtering.RunFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.RunSort;
import ca.on.oicr.gsi.dimsum.service.filtering.SampleSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;

@RestController
@RequestMapping("/rest/internal/runs")
public class RunRestController {

  @Autowired
  private CaseService caseService;

  @PostMapping
  public TableData<Run> query(@RequestBody DataQuery query) {
    validateDataQuery(query);
    RunSort sort = parseSort(query, RunSort::getByLabel);
    boolean descending = parseDescending(query);
    List<RunFilter> filters = parseRunFilters(query);
    return caseService.getRuns(query.getPageSize(), query.getPageNumber(), sort, descending,
        filters);
  }

  @PostMapping("/{runName}/library-qualifications")
  public TableData<Sample> getLibraryQualifications(@PathVariable String runName,
      @RequestBody DataQuery query) {
    checkRunExists(runName);
    SampleSort sort = parseSort(query, SampleSort::getByLabel);
    boolean descending = parseDescending(query);
    List<CaseFilter> filters = parseCaseFilters(query);
    return caseService.getLibraryQualificationsForRun(runName, query.getPageSize(),
        query.getPageNumber(), sort, descending, filters);
  }

  @PostMapping("/{runName}/full-depth-sequencings")
  public TableData<Sample> getFullDepthSequencings(@PathVariable String runName,
      @RequestBody DataQuery query) {
    checkRunExists(runName);
    SampleSort sort = parseSort(query, SampleSort::getByLabel);
    boolean descending = parseDescending(query);
    List<CaseFilter> filters = parseCaseFilters(query);
    return caseService.getFullDepthSequencingsForRun(runName, query.getPageSize(),
        query.getPageNumber(), sort, descending, filters);
  }

  @PostMapping("/{runName}/omissions")
  public TableData<OmittedRunSample> getOmitted(@PathVariable String runName,
      @RequestBody DataQuery query) {
    checkRunExists(runName);
    OmittedRunSampleSort sort = parseSort(query, OmittedRunSampleSort::getByLabel);
    boolean descending = parseDescending(query);
    return caseService.getOmittedSamplesForRun(runName, query.getPageSize(),
        query.getPageNumber(), sort, descending);
  }

  private void checkRunExists(String runName) {
    RunAndLibraries runAndLibraries = caseService.getRunAndLibraries(runName);
    if (runAndLibraries == null) {
      throw new NotFoundException(String.format("No data found for run %s", runName));
    }
  }

}
