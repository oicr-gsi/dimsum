package ca.on.oicr.gsi.dimsum.controller.rest;

import static ca.on.oicr.gsi.dimsum.controller.mvc.MvcUtils.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ca.on.oicr.gsi.dimsum.controller.rest.request.DataQuery;
import ca.on.oicr.gsi.dimsum.data.Run;
import ca.on.oicr.gsi.dimsum.service.filtering.RunFilter;
import ca.on.oicr.gsi.dimsum.service.RunListManager;
import ca.on.oicr.gsi.dimsum.service.filtering.RunSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;

@RestController
@RequestMapping("/rest/runs")
public class RunListRestController {

  @Autowired
  private RunListManager runListManager;

  @PostMapping
  public TableData<Run> query(@RequestBody DataQuery query) {
    validateDataQuery(query);
    RunSort sort = parseSort(query, RunSort::getByLabel);
    boolean descending = parseDescending(query);
    RunFilter baseFilter = parseBaseRunFilter(query);
    List<RunFilter> filters = parseRunFilters(query);
    return runListManager.getRuns(query.getPageSize(), query.getPageNumber(), sort, descending,
        baseFilter, filters);
  }
}
