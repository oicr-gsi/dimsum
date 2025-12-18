package ca.on.oicr.gsi.dimsum.controller.mvc;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.dimsum.controller.NotFoundException;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilterKey;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/cases")
public class CaseController {

  private static final Pattern OLD_CASE_ID_PATTERN = Pattern.compile("^(R\\d+_)([^a].+)");

  @Autowired
  private CaseService caseService;

  @GetMapping
  public String getCaseListPage() {
    return "case-list";
  }

  @GetMapping("/{caseId}")
  public String getCaseDetailsPage(@PathVariable String caseId, ModelMap model,
      HttpServletRequest request) {
    String redirectId = getRedirectIdOrValidate(caseId);
    if (redirectId != null) {
      request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.MOVED_PERMANENTLY);
      return "redirect:/cases/" + redirectId;
    }
    Case kase = caseService.getCase(caseId);
    if (kase == null) {
      throw new NotFoundException("No data found for case: " + caseId);
    }
    model.put("title", String.format("%s  Case Details", caseId));
    model.put("detailType", CaseFilterKey.CASE_ID.name());
    model.put("detailValue", caseId);
    return "detail";
  }

  @GetMapping("/{caseId}/report")
  public String getCaseReportPage(@PathVariable String caseId, ModelMap model,
      HttpServletRequest request) {
    String redirectId = getRedirectIdOrValidate(caseId);
    if (redirectId != null) {
      request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.MOVED_PERMANENTLY);
      return "redirect:/cases/" + redirectId + "/report";
    }
    model.put("caseId", caseId);
    return "case-report";
  }

  /**
   * Gets the case ID to redirect to if an old format case ID was requested; otherwise validate the
   * provided case ID
   * 
   * @param caseId case ID requested
   * @return null if a valid case ID was provided; the updated case ID if an old format case ID was
   *         requested
   * @throws NotFoundException if a matching case could not be found
   */
  private String getRedirectIdOrValidate(String caseId) {
    Case kase = caseService.getCase(caseId);
    if (kase == null) {
      Matcher m = OLD_CASE_ID_PATTERN.matcher(caseId);
      if (m.matches()) {
        String updatedIdPattern = "^%sa\\d+_%s$".formatted(m.group(1), m.group(2));
        List<Case> matchingCases =
            caseService.getCaseStream(null)
                .filter(x -> Pattern.matches(updatedIdPattern, x.getId()))
                .toList();
        if (matchingCases.size() == 1) {
          return matchingCases.get(0).getId();
        }
        throw new NotFoundException(String.format("Case not found: %s", caseId));
      }
    }
    return null;
  }

}
