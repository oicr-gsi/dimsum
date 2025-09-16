package ca.on.oicr.gsi.dimsum.util;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.CaseDeliverable;
import ca.on.oicr.gsi.cardea.data.CaseQc;
import ca.on.oicr.gsi.cardea.data.CaseRelease;
import ca.on.oicr.gsi.cardea.data.OmittedRunSample;
import ca.on.oicr.gsi.cardea.data.Run;
import ca.on.oicr.gsi.cardea.data.Sample;

public class DataUtils {
  public static final String TOP_UP_REASON = "Top-up Required";

  public static Predicate<Sample> pendingQc = DataUtils::isPendingQc;
  public static Predicate<Sample> pendingDataReview = DataUtils::isPendingDataReview;
  public static Predicate<Sample> pendingQcOrDataReview =
      sample -> isPendingQc(sample) || isPendingDataReview(sample);
  public static Predicate<Sample> passed = DataUtils::isPassed;
  public static Predicate<Sample> possiblyCompleted =
      sample -> isPassed(sample) || isPendingQc(sample) || isPendingDataReview(sample);

  public static boolean isPassed(Sample sample) {
    if (!isTrue(sample.getQcPassed())) {
      return false;
    }
    if (sample.getRun() == null) {
      return true;
    }
    return isTrue(sample.getQcPassed()) && isTrue(sample.getDataReviewPassed())
        && isTrue(sample.getRun().getQcPassed()) && isTrue(sample.getRun().getDataReviewPassed());
  }

  public static boolean passedOrTopUpConfirmed(Sample sample) {
    return DataUtils.isPassed(sample) || (DataUtils.isTopUpRequired(sample)
        && Boolean.TRUE.equals(sample.getDataReviewPassed()));
  }

  public static boolean isFailed(Sample sample) {
    Run run = sample.getRun();
    if (run != null) {
      if (sample.getDataReviewDate() == null || run.getDataReviewDate() == null) {
        // Data review pass/fail are considered the same in the case of failed QC, but data review
        // must be completed to confirm the failure
        return false;
      }
      if (isFalse(run.getQcPassed()) || isFalse(run.getDataReviewPassed())) {
        return true;
      }
    }
    return isFalse(sample.getQcPassed()) || isFalse(sample.getDataReviewPassed());
  }

  private static boolean isTrue(Boolean value) {
    return Boolean.TRUE.equals(value);
  }

  private static boolean isFalse(Boolean value) {
    return Boolean.FALSE.equals(value);
  }

  public static boolean isComplete(CaseQc caseQc) {
    return caseQc != null && !caseQc.isPending();
  }

  public static boolean isPassed(CaseQc caseQc) {
    return caseQc != null && isTrue(caseQc.getQcPassed());
  }

  public static boolean isPending(CaseQc caseQc) {
    return caseQc == null || caseQc.isPending();
  }

  public static boolean isPendingQc(Sample sample) {
    return sample.getQcUser() == null
        || (sample.getRun() != null && sample.getRun().getQcPassed() == null);
  }

  public static boolean isPendingDataReview(Sample sample) {
    return sample.getRun() != null
        && ((sample.getQcUser() != null && sample.getDataReviewPassed() == null)
            || (sample.getRun().getQcPassed() != null
                && sample.getRun().getDataReviewPassed() == null));
  }

  public static boolean isTopUpRequired(Sample sample) {
    return TOP_UP_REASON.equals(sample.getQcReason());
  }

  public static boolean isTopUpRequired(OmittedRunSample sample) {
    return TOP_UP_REASON.equals(sample.getQcReason());
  }

  public static boolean isAnalysisReviewSkipped(Case kase) {
    return kase.getDeliverables().stream()
        .allMatch(deliverable -> deliverable.isAnalysisReviewSkipped());
  }

  public static LocalDate getCompletionDate(Case kase) {
    List<CaseDeliverable> deliverables = kase.getDeliverables();
    if (deliverables.isEmpty()) {
      return null;
    }

    List<CaseRelease> releases = deliverables.stream()
        .flatMap(deliverable -> deliverable.getReleases().stream())
        .collect(Collectors.toList());

    if (releases.isEmpty()
        || releases.stream().anyMatch(release -> isPending(release.getQcStatus()))) {
      return null;
    }

    return releases.stream()
        .map(CaseRelease::getQcDate)
        .max(LocalDate::compareTo)
        .orElse(null);
  }
}
