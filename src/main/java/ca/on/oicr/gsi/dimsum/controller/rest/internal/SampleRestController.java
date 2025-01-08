package ca.on.oicr.gsi.dimsum.controller.rest.internal;

import static ca.on.oicr.gsi.dimsum.controller.mvc.MvcUtils.*;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.dimsum.controller.rest.request.DataQuery;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.SampleSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;

@RestController
@RequestMapping("/rest/internal")
public class SampleRestController {

  @Autowired
  private CaseService caseService;

  @PostMapping("/receipts")
  public TableData<Sample> queryReceipts(@RequestBody DataQuery query) {
    return getSamples(query, caseService::getReceipts);
  }

  @PostMapping("/extractions")
  public TableData<Sample> queryExtractions(@RequestBody DataQuery query) {
    return getSamples(query, caseService::getExtractions);
  }

  @PostMapping("/library-preparations")
  public TableData<Sample> queryLibraryPreparations(@RequestBody DataQuery query) {
    return getSamples(query, caseService::getLibraryPreparations);
  }

  @PostMapping("/library-qualifications")
  public TableData<Sample> queryLibraryQualifications(@RequestBody DataQuery query) {
    return getSamples(query, caseService::getLibraryQualifications);
  }

  @PostMapping("/full-depth-sequencings")
  public TableData<Sample> queryFullDepthSequencings(@RequestBody DataQuery query) {
    return getSamples(query, caseService::getFullDepthSequencings);
  }

  @FunctionalInterface
  private interface SampleGetter {
    public TableData<Sample> get(int pageSize, int pageNumber, SampleSort sort, boolean descending,
        CaseFilter baseFilter, Collection<CaseFilter> filters);
  }

  private TableData<Sample> getSamples(DataQuery query, SampleGetter getter) {
    validateDataQuery(query);
    SampleSort sort = parseSort(query, SampleSort::getByLabel);
    boolean descending = parseDescending(query);
    CaseFilter baseFilter = parseBaseFilter(query);
    List<CaseFilter> filters = parseCaseFilters(query);
    return getter.get(query.getPageSize(), query.getPageNumber(), sort, descending, baseFilter,
        filters);
  }

}
