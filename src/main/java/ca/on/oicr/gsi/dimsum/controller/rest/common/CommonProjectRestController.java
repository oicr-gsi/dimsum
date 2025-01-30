package ca.on.oicr.gsi.dimsum.controller.rest.common;

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
import ca.on.oicr.gsi.dimsum.data.ProjectSummaryRow;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;


@RestController
@RequestMapping("/rest/common/projects")
public class CommonProjectRestController {

  @Autowired
  private CaseService caseService;

  @PostMapping("/{projectName}/summary")
  public TableData<ProjectSummaryRow> getProjectSummary(@PathVariable String projectName,
      @RequestBody DataQuery query) {
    List<CaseFilter> filters = parseCaseFiltersForProject(query);
    LocalDate afterDate = parseAfterDate(query);
    LocalDate beforDate = parseBeforeDate(query);
    return caseService.getProjectSummaryRows(projectName, filters, afterDate, beforDate);
  }
}
