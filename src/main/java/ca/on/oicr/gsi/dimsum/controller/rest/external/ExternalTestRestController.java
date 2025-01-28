package ca.on.oicr.gsi.dimsum.controller.rest.external;

import static ca.on.oicr.gsi.dimsum.controller.mvc.MvcUtils.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ca.on.oicr.gsi.dimsum.controller.rest.request.DataQuery;
import ca.on.oicr.gsi.dimsum.data.external.ExternalTestTableView;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;
import ca.on.oicr.gsi.dimsum.service.filtering.TestTableViewSort;

@RestController
@RequestMapping("/rest/external/tests")
public class ExternalTestRestController {

  @Autowired
  private CaseService caseService;

  @PostMapping
  public TableData<ExternalTestTableView> query(@RequestBody DataQuery query) {
    validateDataQuery(query);
    TestTableViewSort sort = parseSort(query, TestTableViewSort::getByLabel);
    boolean descending = parseDescending(query);
    CaseFilter baseFilter = parseBaseFilter(query);
    List<CaseFilter> filters = parseCaseFilters(query);
    return caseService.getExternalTestTableViews(query.getPageSize(), query.getPageNumber(), sort,
        descending, baseFilter, filters);
  }
}
