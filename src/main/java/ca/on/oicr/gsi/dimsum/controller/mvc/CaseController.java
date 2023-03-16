package ca.on.oicr.gsi.dimsum.controller.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ca.on.oicr.gsi.dimsum.controller.NotFoundException;
import ca.on.oicr.gsi.dimsum.data.Case;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilterKey;

@Controller
@RequestMapping("/cases")
public class CaseController {

  @Autowired
  private CaseService caseService;

  @GetMapping("/{caseId}")
  public String getCaseDetailsPage(@PathVariable String caseId, ModelMap model) {
    validateCaseId(caseId);
    model.put("title", "Case Details");
    model.put("detailType", CaseFilterKey.CASE_ID.name());
    model.put("detailValue", caseId);
    return "detail";
  }

  @GetMapping("/{caseId}/report")
  public String getCaseReportPage(@PathVariable String caseId, ModelMap model) {
    validateCaseId(caseId);
    model.put("caseId", caseId);
    return "case-report";
  }

  private void validateCaseId(String caseId) {
    Case kase = caseService.getCase(caseId);
    if (kase == null) {
      throw new NotFoundException(String.format("Case not found: %s", caseId));
    }
  }

}
