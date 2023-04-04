package ca.on.oicr.gsi.dimsum.controller.rest;

import static ca.on.oicr.gsi.dimsum.controller.mvc.MvcUtils.*;

import java.util.List;
import javax.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ca.on.oicr.gsi.dimsum.controller.rest.request.DataQuery;
import ca.on.oicr.gsi.dimsum.data.Case;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;

@RestController
@RequestMapping("/rest/cases")
public class CaseRestController {

  @Autowired
  private CaseService caseService;

  @PostMapping
  public TableData<Case> query(@RequestBody DataQuery query) {
    validateDataQuery(query);
    CaseSort sort = parseSort(query, CaseSort::getByLabel);
    boolean descending = parseDescending(query);
    CaseFilter baseFilter = parseBaseFilter(query);
    List<CaseFilter> filters = parseCaseFilters(query);
    return caseService.getCases(query.getPageSize(), query.getPageNumber(), sort, descending,
        baseFilter, filters);
  }

  @GetMapping("/{id}")
  public Case getCase(@PathVariable String id) {
    Case kase = caseService.getCase(id);
    if (kase == null) {
      throw new NotFoundException("Case not found");
    }
    return kase;
  }



}
