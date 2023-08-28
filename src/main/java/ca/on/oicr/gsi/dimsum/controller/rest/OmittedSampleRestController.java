package ca.on.oicr.gsi.dimsum.controller.rest;

import static ca.on.oicr.gsi.dimsum.controller.mvc.MvcUtils.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ca.on.oicr.gsi.dimsum.controller.rest.request.DataQuery;
import ca.on.oicr.gsi.cardea.data.OmittedSample;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.OmittedSampleFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.OmittedSampleSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;

@RestController
@RequestMapping("/rest/omissions")
public class OmittedSampleRestController {

  @Autowired
  private CaseService caseService;

  @PostMapping
  public TableData<OmittedSample> query(@RequestBody DataQuery query) {
    validateDataQuery(query);
    OmittedSampleSort sort = parseSort(query, OmittedSampleSort::getByLabel);
    boolean descending = parseDescending(query);
    List<OmittedSampleFilter> filters = parseOmittedSampleFilters(query);
    return caseService.getOmittedSamples(query.getPageSize(), query.getPageNumber(), sort,
        descending, filters);
  }

}
