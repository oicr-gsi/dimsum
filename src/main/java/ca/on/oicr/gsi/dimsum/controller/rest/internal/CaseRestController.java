package ca.on.oicr.gsi.dimsum.controller.rest.internal;

import static ca.on.oicr.gsi.dimsum.controller.mvc.MvcUtils.*;
import java.util.List;
import java.util.function.BiFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import ca.on.oicr.gsi.dimsum.security.DimsumPrincipal;
import ca.on.oicr.gsi.dimsum.service.CaseService;
import ca.on.oicr.gsi.dimsum.service.NabuService;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseFilter;
import ca.on.oicr.gsi.dimsum.service.filtering.CaseSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;

@RestController
@RequestMapping("/rest/internal/cases")
public class CaseRestController {

  @Autowired
  private CaseService caseService;
  @Autowired(required = false)
  private NabuService nabuService;

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
      @RequestBody List<NabuBulkSignoff> data, @AuthenticationPrincipal DimsumPrincipal principal) {
    if (nabuService == null) {
      throw new BadRequestException("Nabu integration is not enabled");
    }
    for (NabuBulkSignoff signoff : data) {
      if (signoff.getCaseIdentifiers() == null || signoff.getCaseIdentifiers().isEmpty()) {
        throw new BadRequestException("No case IDs specified");
      }
      for (String caseId : signoff.getCaseIdentifiers()) {
        Case kase = caseService.getCase(caseId);
        if (kase == null) {
          throw new BadRequestException("No case found with ID '%s'".formatted(caseId));
        }
      }
      if (signoff.getDeliverableType() == null) {
        throw new BadRequestException("Deliverable type not specified");
      }
      if (signoff.getSignoffStepName() == null) {
        throw new BadRequestException("QC step not specified");
      }
      switch (signoff.getSignoffStepName()) {
        case ANALYSIS_REVIEW:
          validateQcStatus(signoff, AnalysisReviewQcStatus::of);
          break;
        case RELEASE_APPROVAL:
          validateQcStatus(signoff, ReleaseApprovalQcStatus::of);
          break;
        case RELEASE:
          validateQcStatus(signoff, ReleaseQcStatus::of);
          break;
        default:
          throw new BadRequestException("QC step invalid or not specified");
      }
      if (signoff.getSignoffStepName() == NabuSignoffStep.RELEASE) {
        if (signoff.getDeliverable() == null) {
          throw new BadRequestException("Release deliverable not specified");
        }
      } else {
        if (signoff.getDeliverable() != null) {
          throw new BadRequestException("Deliverable not applicable to selected QC step");
        }
      }
      signoff.setUsername(principal.getDisplayName());
    }
    for (NabuBulkSignoff signoff : data) {
      nabuService.postSignoff(signoff);
    }
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

}
