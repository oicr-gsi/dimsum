package ca.on.oicr.gsi.dimsum.controller.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ca.on.oicr.gsi.dimsum.controller.BadRequestException;
import ca.on.oicr.gsi.dimsum.controller.rest.request.DataQuery;
import ca.on.oicr.gsi.dimsum.data.Case;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilterKey;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;

@RestController
@RequestMapping("/rest/cases")
public class CaseRestController {

  @Autowired
  private CaseService caseService;

  @PostMapping
  public TableData<Case> query(@RequestBody DataQuery query) {
    if (query.getPageNumber() < 1) {
      throw new BadRequestException(
          String.format("Invalid page number: %d", query.getPageNumber()));
    }
    if (query.getPageSize() < 1) {
      throw new BadRequestException(String.format("Invalid page size: %d", query.getPageSize()));
    }
    CaseSort sort = parseSort(query.getSortColumn());
    boolean descending = query.getDescending() == null ? false : query.getDescending();
    List<CaseFilter> filters = parseFilters(query.getFilters());
    return caseService.getCases(query.getPageSize(), query.getPageNumber(), sort, descending,
        filters);
  }

  private CaseSort parseSort(String sortColumn) {
    if (sortColumn == null) {
      return null;
    }
    CaseSort sort = CaseSort.getByLabel(sortColumn);
    if (sort != null) {
      return sort;
    }
    throw new BadRequestException(String.format("Invalid sort column: %s", sortColumn));
  }

  private List<CaseFilter> parseFilters(Map<String, String> queryFilters) {
    if (queryFilters == null || queryFilters.isEmpty()) {
      return null;
    }
    List<CaseFilter> filters = new ArrayList<>();
    for (String keyString : queryFilters.keySet()) {
      try {
        CaseFilterKey key = CaseFilterKey.valueOf(keyString);
        filters.add(new CaseFilter(key, queryFilters.get(keyString)));
      } catch (IllegalArgumentException e) {
        throw new BadRequestException(String.format("Invalid filter key: %s", keyString));
      }
    }
    return filters;
  }

}
