package ca.on.oicr.gsi.dimsum.controller.rest.external;

import static ca.on.oicr.gsi.dimsum.controller.mvc.MvcUtils.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ca.on.oicr.gsi.dimsum.controller.rest.request.DataQuery;
import ca.on.oicr.gsi.dimsum.data.external.ExternalProjectSummary;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.ProjectSummaryFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.ProjectSummarySort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;


@RestController
@RequestMapping("/rest/external/projects")
public class ExternalProjectRestController {

  @Autowired
  private CaseService caseService;

  @PostMapping
  public TableData<ExternalProjectSummary> query(@RequestBody DataQuery query) {
    validateDataQuery(query);
    ProjectSummarySort sort = parseSort(query, ProjectSummarySort::getByLabel);
    boolean descending = parseDescending(query);
    List<ProjectSummaryFilter> filters = parseProjectSummaryFilters(query);
    return caseService.getExternalProjects(query.getPageSize(), query.getPageNumber(), sort,
        descending, filters);
  }
}
