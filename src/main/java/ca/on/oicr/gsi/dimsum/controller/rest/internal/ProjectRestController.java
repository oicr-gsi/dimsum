package ca.on.oicr.gsi.dimsum.controller.rest.internal;

import static ca.on.oicr.gsi.dimsum.controller.mvc.MvcUtils.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ca.on.oicr.gsi.cardea.data.MetricCategory;
import ca.on.oicr.gsi.cardea.data.OmittedRunSample;
import ca.on.oicr.gsi.dimsum.controller.rest.request.DataQuery;
import ca.on.oicr.gsi.dimsum.data.ProjectSummary;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.OmittedRunSampleSort;
import ca.on.oicr.gsi.dimsum.service.filtering.ProjectSummaryFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.ProjectSummarySort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;


@RestController
@RequestMapping("/rest/internal/projects")
public class ProjectRestController {

  @Autowired
  private CaseService caseService;

  @PostMapping
  public TableData<ProjectSummary> query(@RequestBody DataQuery query) {
    validateDataQuery(query);
    ProjectSummarySort sort = parseSort(query, ProjectSummarySort::getByLabel);
    boolean descending = parseDescending(query);
    List<ProjectSummaryFilter> filters = parseProjectSummaryFilters(query);
    return caseService.getProjects(query.getPageSize(), query.getPageNumber(), sort, descending,
        filters);
  }

  @PostMapping("/{projectName}/omissions/library-qualification")
  public TableData<OmittedRunSample> getLibraryQualificationOmissions(
      @PathVariable String projectName, @RequestBody DataQuery query) {
    return getSequencingOmissions(projectName, query, MetricCategory.LIBRARY_QUALIFICATION);
  }

  @PostMapping("/{projectName}/omissions/full-depth-sequencing")
  public TableData<OmittedRunSample> getFullDepthOmissions(
      @PathVariable String projectName, @RequestBody DataQuery query) {
    return getSequencingOmissions(projectName, query, MetricCategory.FULL_DEPTH_SEQUENCING);
  }

  public TableData<OmittedRunSample> getSequencingOmissions(String projectName, DataQuery query,
      MetricCategory sequencingType) {
    OmittedRunSampleSort sort = parseSort(query, OmittedRunSampleSort::getByLabel);
    boolean descending = parseDescending(query);
    return caseService.getOmittedRunSamplesForProject(projectName, sequencingType,
        query.getPageSize(), query.getPageNumber(), sort, descending);
  }
}
