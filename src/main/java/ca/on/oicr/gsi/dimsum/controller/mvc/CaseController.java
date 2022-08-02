package ca.on.oicr.gsi.dimsum.controller.mvc;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ca.on.oicr.gsi.dimsum.controller.NotFoundException;
import ca.on.oicr.gsi.dimsum.data.Case;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilterKey;

@Controller
@RequestMapping("/cases")
public class CaseController {

  @Autowired
  private CaseService caseService;

  @GetMapping("/{caseId}")
  public String getProjectDetailsPage(@PathVariable String caseId, ModelMap model) {
    List<Case> cases = caseService.getCases(new CaseFilter(CaseFilterKey.CASE_ID, caseId));
    if (cases.isEmpty()) {
      throw new NotFoundException(String.format("Case not found: %s", caseId));
    }

    model.put("title", "Case Details");
    model.put("detailType", CaseFilterKey.CASE_ID.name());
    model.put("detailValue", caseId);
    return "detail";
  }

}
