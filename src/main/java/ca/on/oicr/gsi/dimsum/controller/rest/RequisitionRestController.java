package ca.on.oicr.gsi.dimsum.controller.rest;

import static ca.on.oicr.gsi.dimsum.controller.mvc.MvcUtils.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ca.on.oicr.gsi.dimsum.controller.rest.request.DataQuery;
import ca.on.oicr.gsi.dimsum.data.Requisition;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.RequisitionSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;

@RestController
@RequestMapping("/rest/requisitions")
public class RequisitionRestController {

  @Autowired
  private CaseService caseService;

  @PostMapping
  public TableData<Requisition> queryRequisitions(@RequestBody DataQuery query) {
    validateDataQuery(query);
    RequisitionSort sort = parseSort(query, RequisitionSort::getByLabel);
    boolean descending = parseDescending(query);
    List<CaseFilter> filters = parseCaseFilters(query);
    return caseService.getRequisitions(query.getPageSize(), query.getPageNumber(), sort, descending,
        filters);
  }


}
