package ca.on.oicr.gsi.dimsum.controller.rest.external;

import static ca.on.oicr.gsi.dimsum.controller.mvc.MvcUtils.*;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ca.on.oicr.gsi.dimsum.controller.rest.request.DataQuery;
import ca.on.oicr.gsi.dimsum.data.external.ExternalSample;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.SampleSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;

@RestController
@RequestMapping("/rest/external")
public class ExternalSampleRestController {

  @Autowired
  private CaseService caseService;

  @PostMapping("/receipts")
  public TableData<ExternalSample> queryReceipts(@RequestBody DataQuery query) {
    return getSamples(query, caseService::getExternalReceipts);
  }

  @PostMapping("/extractions")
  public TableData<ExternalSample> queryExtractions(@RequestBody DataQuery query) {
    return getSamples(query, caseService::getExternalExtractions);
  }

  @PostMapping("/library-preparations")
  public TableData<ExternalSample> queryLibraryPreparations(@RequestBody DataQuery query) {
    return getSamples(query, caseService::getExternalLibraryPreparations);
  }

  @PostMapping("/library-qualifications")
  public TableData<ExternalSample> queryLibraryQualifications(@RequestBody DataQuery query) {
    return getSamples(query, caseService::getExternalLibraryQualifications);
  }

  @PostMapping("/full-depth-sequencings")
  public TableData<ExternalSample> queryFullDepthSequencings(@RequestBody DataQuery query) {
    return getSamples(query, caseService::getExternalFullDepthSequencings);
  }

  @FunctionalInterface
  private interface SampleGetter {
    public TableData<ExternalSample> get(int pageSize, int pageNumber, SampleSort sort,
        boolean descending,
        CaseFilter baseFilter, Collection<CaseFilter> filters);
  }

  private TableData<ExternalSample> getSamples(DataQuery query, SampleGetter getter) {
    validateDataQuery(query);
    SampleSort sort = parseSort(query, SampleSort::getByLabel);
    boolean descending = parseDescending(query);
    CaseFilter baseFilter = parseBaseFilter(query);
    List<CaseFilter> filters = parseCaseFilters(query);
    return getter.get(query.getPageSize(), query.getPageNumber(), sort, descending, baseFilter,
        filters);
  }

}
