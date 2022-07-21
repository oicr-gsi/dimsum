package ca.on.oicr.gsi.dimsum.controller.mvc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import ca.on.oicr.gsi.dimsum.controller.BadRequestException;
import ca.on.oicr.gsi.dimsum.controller.rest.request.DataQuery;
import ca.on.oicr.gsi.dimsum.controller.rest.request.KeyValuePair;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilterKey;

public class MvcUtils {

  public static void validateDataQuery(DataQuery query) {
    if (query.getPageNumber() < 1) {
      throw new BadRequestException(
          String.format("Invalid page number: %d", query.getPageNumber()));
    }
    if (query.getPageSize() < 1) {
      throw new BadRequestException(String.format("Invalid page size: %d", query.getPageSize()));
    }
  }

  public static <T> T parseSort(DataQuery query, Function<String, T> getSortByLabel) {
    String sortColumn = query.getSortColumn();
    if (sortColumn == null) {
      return null;
    }
    T sort = getSortByLabel.apply(sortColumn);
    if (sort != null) {
      return sort;
    }
    throw new BadRequestException(String.format("Invalid sort column: %s", sortColumn));
  }

  public static boolean parseDescending(DataQuery query) {
    return query.getDescending() == null ? false : query.getDescending();
  }

  public static CaseFilter parseBaseFilter(DataQuery query) {
    if (query.getBaseFilter() == null) {
      return null;
    }
    return parseCaseFilter(query.getBaseFilter());
  }

  public static List<CaseFilter> parseCaseFilters(DataQuery query) {
    List<KeyValuePair> queryFilters = query.getFilters();
    if (queryFilters == null || queryFilters.isEmpty()) {
      return null;
    }
    return queryFilters.stream().map(MvcUtils::parseCaseFilter).toList();
  }

  private static CaseFilter parseCaseFilter(KeyValuePair pair) {
    try {
      CaseFilterKey key = CaseFilterKey.valueOf(pair.getKey());
      return new CaseFilter(key, pair.getValue());
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(String.format("Invalid filter key: %s", pair.getKey()));
    }
  }

}
