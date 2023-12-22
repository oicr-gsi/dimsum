package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ca.on.oicr.gsi.cardea.data.Assay;
import ca.on.oicr.gsi.cardea.data.AssayTargets;
import ca.on.oicr.gsi.cardea.data.Case;

public enum CaseSort {

  // @formatter:off
  URGENCY("Urgency", null) {
    @Override
    public Comparator<Case> comparator(Map<Long, Assay> assaysById) {
      return (a, b) -> {
        // If one case is inactive (paused/complete), the other is more urgent
        // If both cases are inactive, consider them equal
        if (isInactive(a)) {
          if (isInactive(b)) {
            return 0;
          } else {
            return -1;
          }
        } else if (isInactive(b)) {
          return 1;
        }
        
        AssayTargets aTargets = assaysById.get(a.getAssayId()).getTargets();
        AssayTargets bTargets = assaysById.get(b.getAssayId()).getTargets();
        
        // Whichever case is further overdue is more urgent
        int aCaseOverdue = calculateCaseDaysOverdue(a, aTargets);
        int bCaseOverdue = calculateCaseDaysOverdue(b, bTargets);
        if (aCaseOverdue != bCaseOverdue) {
          return Integer.compare(aCaseOverdue, bCaseOverdue);
        }
        // Whichever case is furthest behind on the current step is more urgent
        int aStepOverdue = calculateStepDaysOverdue(a, aTargets);
        int bStepOverdue = calculateStepDaysOverdue(b, bTargets);
        if (aStepOverdue != bStepOverdue) {
          return Integer.compare(aStepOverdue, bStepOverdue);
        }
        // Whichever case has fewer days remaining on the current step is more urgent
        int aStepRemaining = calculateStepDaysRemaining(a, aTargets);
        int bStepRemaining = calculateStepDaysRemaining(b, bTargets);
        if (aStepRemaining != bStepRemaining) {
          return Integer.compare(bStepRemaining, aStepRemaining);
        }
        // Everything equal so far. Sort by days remaining for case
        // If cases are equally overdue, this will return equal negative values
        int aCaseRemaining = calculateCaseDaysRemaining(a, aTargets);
        int bCaseRemaining = calculateCaseDaysRemaining(b, bTargets);
        return Integer.compare(bCaseRemaining, aCaseRemaining);
      };
    }

    private static boolean isInactive(Case kase) {
      return kase.getRequisition().isPaused()
          || CompletedGate.RELEASE.qualifyCase(kase);
    }

    private static int calculateCaseDaysOverdue(Case kase, AssayTargets targets) {
      if (targets.getCaseDays() == null || kase.getCaseDaysSpent() < targets.getCaseDays()) {
        return 0;
      }
      return kase.getCaseDaysSpent() - targets.getCaseDays();
    }

    private static int calculateStepDaysOverdue(Case kase, AssayTargets targets) {
      // find earliest gate that has a target and is overdue
      if (isStepBehind(targets.getReceiptDays(), kase, CompletedGate.RECEIPT)) {
        return kase.getCaseDaysSpent() - targets.getReceiptDays();
      } else if (isStepBehind(targets.getExtractionDays(), kase, CompletedGate.EXTRACTION)) {
        return kase.getCaseDaysSpent() - targets.getExtractionDays();
      } else if (isStepBehind(targets.getLibraryPreparationDays(), kase, CompletedGate.LIBRARY_PREPARATION)) {
        return kase.getCaseDaysSpent() - targets.getLibraryPreparationDays();
      } else if (isStepBehind(targets.getLibraryQualificationDays(), kase, CompletedGate.LIBRARY_QUALIFICATION)) {
        return kase.getCaseDaysSpent() - targets.getLibraryQualificationDays();
      } else if (isStepBehind(targets.getFullDepthSequencingDays(), kase, CompletedGate.FULL_DEPTH_SEQUENCING)) {
        return kase.getCaseDaysSpent() - targets.getFullDepthSequencingDays();
      } else if (isStepBehind(targets.getAnalysisReviewDays(), kase, CompletedGate.ANALYSIS_REVIEW)) {
        return kase.getCaseDaysSpent() - targets.getAnalysisReviewDays();
      } else if (isStepBehind(targets.getReleaseApprovalDays(), kase, CompletedGate.RELEASE_APPROVAL)) {
        return kase.getCaseDaysSpent() - targets.getReleaseApprovalDays();
      } else if (isStepBehind(targets.getReleaseDays(), kase, CompletedGate.RELEASE)) {
        return kase.getCaseDaysSpent() - targets.getReleaseDays();
      }
      return 0;
    }

    private static boolean isStepBehind(Integer target, Case kase, CompletedGate completedGate) {
      return target != null && kase.getCaseDaysSpent() > target
          && (!kase.getRequisition().isStopped() || !completedGate.isStoppable())
          && !completedGate.qualifyCase(kase);
    }

    private static int calculateStepDaysRemaining(Case kase, AssayTargets targets) {
      if (!kase.getRequisition().isStopped()) {
        if (!CompletedGate.RECEIPT.qualifyCase(kase)) {
          return calculateDaysRemaining(kase, targets.getReceiptDays());
        } else if (!CompletedGate.EXTRACTION.qualifyCase(kase)) {
          return calculateDaysRemaining(kase, targets.getExtractionDays());
        } else if (!CompletedGate.LIBRARY_PREPARATION.qualifyCase(kase)) {
          return calculateDaysRemaining(kase, targets.getLibraryPreparationDays());
        } else if (!CompletedGate.LIBRARY_QUALIFICATION.qualifyCase(kase)) {
          return calculateDaysRemaining(kase, targets.getLibraryQualificationDays());
        } else if (!CompletedGate.FULL_DEPTH_SEQUENCING.qualifyCase(kase)) {
          return calculateDaysRemaining(kase, targets.getFullDepthSequencingDays());
        } else if (!CompletedGate.ANALYSIS_REVIEW.qualifyCase(kase)) {
          return calculateDaysRemaining(kase, targets.getAnalysisReviewDays());
        }
      }
      if (!CompletedGate.RELEASE_APPROVAL.qualifyCase(kase)) {
        return calculateDaysRemaining(kase, targets.getReleaseApprovalDays());
      } else if (!CompletedGate.RELEASE.qualifyCase(kase)) {
        return calculateDaysRemaining(kase, targets.getReleaseDays());
      } else {
        throw new IllegalStateException("Checking completed case for step days remaining?");
      }
    }

     private static int calculateDaysRemaining(Case kase, Integer target) {
      if (target == null) {
        return 1000000;
      }
      // Returns negative if case is overdue
      return target - kase.getCaseDaysSpent();
    }

    private static int calculateCaseDaysRemaining(Case kase, AssayTargets targets) {
      return calculateDaysRemaining(kase, targets.getCaseDays());
    }
  },
  ASSAY("Assay", Comparator.comparing(Case::getAssayName)),
  DONOR("Donor", Comparator.comparing(kase -> kase.getDonor().getName())),
  START_DATE("Start Date", Comparator.comparing(Case::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()))),
  LAST_ACTIVITY("Latest Activity", Comparator.comparing(Case::getLatestActivityDate, Comparator.nullsLast(Comparator.naturalOrder())));
  // @formatter:on

  private static final Map<String, CaseSort> map = Stream.of(CaseSort.values())
      .collect(Collectors.toMap(CaseSort::getLabel, Function.identity()));

  public static CaseSort getByLabel(String label) {
    return map.get(label);
  }

  private final String label;
  private final Comparator<Case> comparator;

  private CaseSort(String label, Comparator<Case> comparator) {
    this.label = label;
    this.comparator = comparator;
  }

  public String getLabel() {
    return label;
  }

  public Comparator<Case> comparator(Map<Long, Assay> assaysById) {
    return comparator;
  }

}
