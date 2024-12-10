package ca.on.oicr.gsi.dimsum.controller.rest;

import static ca.on.oicr.gsi.dimsum.controller.mvc.MvcUtils.*;
import java.util.List;
import java.util.function.BiFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.CaseQc;
import ca.on.oicr.gsi.cardea.data.CaseQc.AnalysisReviewQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseQc.ReleaseApprovalQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseQc.ReleaseQcStatus;
import ca.on.oicr.gsi.dimsum.controller.BadRequestException;
import ca.on.oicr.gsi.dimsum.controller.NotFoundException;
import ca.on.oicr.gsi.dimsum.controller.rest.request.DataQuery;
import ca.on.oicr.gsi.dimsum.data.NabuBulkSignoff;
import ca.on.oicr.gsi.dimsum.data.NabuSignoff;
import ca.on.oicr.gsi.dimsum.data.NabuSignoff.NabuSignoffStep;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.NabuService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;

@RestController
@RequestMapping("/rest/cases")
public class CaseRestController {

  @Autowired
  private CaseService caseService;
  @Autowired(required = false)
  private NabuService nabuService;

  @Autowired
  private Environment environment;

  @Value("${saml.usernameattribute:#{null}}")
  private String samlUsernameAttribute;

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

  @PostMapping("/bulk-signoff")
  public @ResponseStatus(HttpStatus.NO_CONTENT) void postSignoffs(
      @RequestBody NabuBulkSignoff data) {
    if (nabuService == null) {
      throw new BadRequestException("Nabu integration is not enabled");
    }
    if (data.getCaseIdentifiers() == null || data.getCaseIdentifiers().isEmpty()) {
      throw new BadRequestException("No case IDs specified");
    }
    for (String caseId : data.getCaseIdentifiers()) {
      Case kase = caseService.getCase(caseId);
      if (kase == null) {
        throw new BadRequestException("No case found with ID '%s'".formatted(caseId));
      }
    }
    if (data.getDeliverableType() == null) {
      throw new BadRequestException("Deliverable type not specified");
    }
    if (data.getSignoffStepName() == null) {
      throw new BadRequestException("QC step not specified");
    }
    switch (data.getSignoffStepName()) {
      case ANALYSIS_REVIEW:
        validateQcStatus(data, AnalysisReviewQcStatus::of);
        break;
      case RELEASE_APPROVAL:
        validateQcStatus(data, ReleaseApprovalQcStatus::of);
        break;
      case RELEASE:
        validateQcStatus(data, ReleaseQcStatus::of);
        break;
      default:
        throw new BadRequestException("QC step invalid or not specified");
    }
    if (data.getSignoffStepName() == NabuSignoffStep.RELEASE) {
      if (data.getDeliverable() == null) {
        throw new BadRequestException("Release deliverable not specified");
      }
    } else {
      if (data.getDeliverable() != null) {
        throw new BadRequestException("Deliverable not applicable to selected QC step");
      }
    }
    data.setUsername(getUsername());
    nabuService.postSignoff(data);
  }

  private <T extends CaseQc> void validateQcStatus(NabuSignoff data,
      BiFunction<Boolean, Boolean, T> of) {
    try {
      of.apply(data.getQcPassed(), data.getRelease());
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Invalid QC combination: step=%s; qcPassed=%s; release=%s"
          .formatted(data.getSignoffStepName(), data.getQcPassed(), data.getRelease()));
    }
  }

  private String getUsername() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (authenticationDisabled()) {
      return "Unauthenticated";
    } else if (principal instanceof Saml2AuthenticatedPrincipal) {
      Saml2AuthenticatedPrincipal samlPrincipal = (Saml2AuthenticatedPrincipal) principal;
      List<Object> values = samlPrincipal.getAttribute(samlUsernameAttribute);
      if (values == null || values.isEmpty()) {
        throw new IllegalStateException("User display name attribute not found");
      }
      // It is possible (but weird) to have multiple display names. We're only using the first
      return (String) values.get(0);
    } else {
      throw new IllegalStateException(
          "Unexpected principal class: " + principal.getClass().getName());
    }
  }

  private boolean authenticationDisabled() {
    String[] profiles = environment.getActiveProfiles();
    if (profiles == null) {
      return false;
    }
    for (String profile : profiles) {
      if ("noauth".equals(profile)) {
        return true;
      }
    }
    return false;
  }

}
