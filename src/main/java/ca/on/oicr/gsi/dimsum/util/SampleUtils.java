package ca.on.oicr.gsi.dimsum.util;

import java.util.function.Predicate;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.Sample;

public class SampleUtils {
  public static final String TOP_UP_REASON = "Top-up Required";

  public static Predicate<Sample> pendingQc = SampleUtils::isPendingQc;
  public static Predicate<Sample> pendingDataReview = SampleUtils::isPendingDataReview;
  public static Predicate<Sample> pendingQcOrDataReview =
      sample -> isPendingQc(sample) || isPendingDataReview(sample);
  public static Predicate<Sample> passed = SampleUtils::isPassed;
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

  private static boolean isTrue(Boolean value) {
    return Boolean.TRUE.equals(value);
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

  public static boolean isAnalysisReviewSkipped(Case kase) {
    return kase.getProjects().stream().allMatch(project -> project.isAnalysisReviewSkipped());
  }
}
