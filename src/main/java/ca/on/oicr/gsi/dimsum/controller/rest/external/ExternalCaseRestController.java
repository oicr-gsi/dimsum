package ca.on.oicr.gsi.dimsum.controller.rest.external;

import static ca.on.oicr.gsi.dimsum.controller.mvc.MvcUtils.*;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ca.on.oicr.gsi.dimsum.controller.BadRequestException;
import ca.on.oicr.gsi.dimsum.controller.rest.request.DataQuery;
import ca.on.oicr.gsi.dimsum.data.external.ExternalCase;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;

@RestController
@RequestMapping("/rest/external/cases")
public class ExternalCaseRestController {

  @Autowired
  private CaseService caseService;

  @PostMapping
  public TableData<ExternalCase> query(@RequestBody DataQuery query) {
    validateDataQuery(query);
    CaseSort sort = parseSort(query, CaseSort::getByLabel);
    boolean descending = parseDescending(query);
    CaseFilter baseFilter = parseBaseFilter(query);
    authorizeFilter(baseFilter);
    List<CaseFilter> filters = parseCaseFilters(query);
    authorizeFilters(filters);
    return caseService.getExternalCases(query.getPageSize(), query.getPageNumber(), sort,
        descending, baseFilter, filters);
  }

  private static void authorizeFilters(Collection<CaseFilter> filters) {
    if (filters == null) {
      return;
    }
    for (CaseFilter filter : filters) {
      authorizeFilter(filter);
    }
  }

  private static void authorizeFilter(CaseFilter filter) {
    if (filter == null) {
      return;
    }
    if (!filter.getKey().allowExternal()) {
      throw new BadRequestException(String.format("Invalid filter key: %s", filter.getKey()));
    }
  }

}
