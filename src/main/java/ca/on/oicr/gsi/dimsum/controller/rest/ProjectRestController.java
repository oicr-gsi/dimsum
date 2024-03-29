package ca.on.oicr.gsi.dimsum.controller.rest;

import static ca.on.oicr.gsi.dimsum.controller.mvc.MvcUtils.*;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ca.on.oicr.gsi.dimsum.controller.rest.request.DataQuery;
import ca.on.oicr.gsi.dimsum.data.ProjectSummary;
import ca.on.oicr.gsi.dimsum.data.ProjectSummaryRow;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.ProjectSummaryFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.ProjectSummarySort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;


@RestController
@RequestMapping("/rest/projects")
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

  @PostMapping("/{projectName}/summary")
  public TableData<ProjectSummaryRow> getProjectSummary(@PathVariable String projectName,
      @RequestBody DataQuery query) {
    List<CaseFilter> filters = parseCaseFiltersForProject(query);
    LocalDate afterDate = parseAfterDate(query);
    LocalDate beforDate = parseBeforeDate(query);
    return caseService.getProjectSummaryRows(projectName, filters, afterDate, beforDate);
  }
}
