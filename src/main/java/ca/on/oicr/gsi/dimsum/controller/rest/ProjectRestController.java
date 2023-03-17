package ca.on.oicr.gsi.dimsum.controller.rest;

import static ca.on.oicr.gsi.dimsum.controller.mvc.MvcUtils.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ca.on.oicr.gsi.dimsum.controller.rest.request.DataQuery;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;
import java.util.List;
import ca.on.oicr.gsi.dimsum.data.ProjectSummary;
import ca.on.oicr.gsi.dimsum.data.ProjectSummaryRow;
import ca.on.oicr.gsi.dimsum.service.filtering.ProjectSummaryFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.ProjectSummarySort;

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

  @GetMapping("/{name}/summary")
  public List<ProjectSummaryRow> getProjectSummary(@PathVariable String name) {
    return caseService.getProjectSummaryRows(name);
  }
}
