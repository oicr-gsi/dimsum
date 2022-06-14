package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ca.on.oicr.gsi.dimsum.data.Case;
import ca.on.oicr.gsi.dimsum.data.Requisition;
import ca.on.oicr.gsi.dimsum.data.RequisitionQc;
import ca.on.oicr.gsi.dimsum.data.Sample;
import ca.on.oicr.gsi.dimsum.data.Test;

public enum PendingState {

  // @formatter:off
  RECEIPT_QC("Receipt QC", Helpers.isPendingReceiptQc),
  EXTRACTION("Extraction", Helpers.isReceiptPassed
      .and(Helpers.anyTest(Helpers.hasPendingWork(Test::getExtractions)))),
  EXTRACTION_QC("Extraction QC Sign-Off",
      Helpers.anyTest(Helpers.hasPendingQc(Test::getExtractions))),
  LIBRARY_PREPARATION("Library Preparation",
      Helpers.anyTest(Helpers.hasPendingWork(Test::getLibraryPreparations, Test::getExtractions))),
  LIBRARY_QC("Library QC Sign-Off",
      Helpers.anyTest(Helpers.hasPendingQc(Test::getLibraryPreparations))),
  LIBRARY_QUALIFICATION("Library Qualification",
      Helpers.anyTest(
          Helpers.hasPendingWork(Test::getLibraryQualifications, Test::getLibraryPreparations))),
  LIBRARY_QUALIFICATION_QC("Library Qualification QC Sign-Off",
      Helpers.anyTest(Helpers.hasPendingQc(Test::getLibraryQualifications))),
  LIBRARY_QUALIFICATION_DATA_REVIEW("Library Qualification Data Review",
      Helpers.anyTest(Helpers.hasPendingDataReview(Test::getLibraryQualifications))),
  FULL_DEPTH_SEQUENCING("Full-Depth Sequencing",
      Helpers.anyTest(
          Helpers.hasPendingWork(Test::getFullDepthSequencings, Test::getLibraryQualifications))),
  FULL_DEPTH_QC("Full-Depth Sequencing QC Sign-Off",
      Helpers.anyTest(Helpers.hasPendingQc(Test::getFullDepthSequencings))),
  FULL_DEPTH_DATA_REVIEW("Full-Depth Sequencing Data Review",
      Helpers.anyTest(Helpers.hasPendingDataReview(Test::getFullDepthSequencings))),
  INFORMATICS_REVIEW("Informatics Review",
      Helpers.allTests(Helpers.isCompleted(Test::getFullDepthSequencings))
          .and(Helpers.isPendingRequisitionQc(Requisition::getInformaticsReviews))),
  DRAFT_REPORT("Draft Report", Helpers.isCompletedRequisitionQc(Requisition::getInformaticsReviews)
      .and(Helpers.isPendingRequisitionQc(Requisition::getDraftReports))),
  FINAL_REPORT("Final Report", Helpers.isCompletedRequisitionQc(Requisition::getDraftReports)
      .and(Helpers.isPendingRequisitionQc(Requisition::getFinalReports)));
  // @formatter:on

  private static final Map<String, PendingState> map = Stream.of(PendingState.values())
      .collect(Collectors.toMap(PendingState::getLabel, Function.identity()));

  public static PendingState getByLabel(String label) {
    return map.get(label);
  }

  private final String label;
  private final Predicate<Case> predicate;

  private PendingState(String label, Predicate<Case> predicate) {
    this.label = label;
    this.predicate = predicate;
  }

  public String getLabel() {
    return label;
  }

  public Predicate<Case> predicate() {
    return predicate;
  }

  private static class Helpers {
    public static String TOP_UP_REASON = "Top-up Required";
    public static Predicate<Sample> pendingQc = Helpers::isPendingQc;
    public static Predicate<Sample> pendingDataReview = Helpers::isPendingDataReview;
    public static Predicate<Sample> pendingQcOrDataReview =
        sample -> isPendingQc(sample) || isPendingDataReview(sample);
    public static Predicate<Sample> passed = Helpers::isPassed;
    public static Predicate<Sample> possiblyCompleted =
        sample -> isPassed(sample) || isPendingQc(sample) || isPendingDataReview(sample);
    public static Predicate<Case> isReceiptPassed =
        kase -> kase.getReceipts().stream().anyMatch(passed)
            && kase.getReceipts().stream().noneMatch(pendingQc);
    public static Predicate<Case> isPendingReceiptQc =
        kase -> kase.getReceipts().stream().anyMatch(pendingQc);

    private static boolean isPassed(Sample sample) {
      return isTrue(sample.getQcPassed())
          && (sample.getRun() == null || isTrue(sample.getDataReviewPassed()));
    }

    private static boolean isTrue(Boolean value) {
      return Boolean.TRUE.equals(value);
    }

    private static boolean isPendingQc(Sample sample) {
      return sample.getQcPassed() == null && !isTopUpRequired(sample);
    }

    private static boolean isPendingDataReview(Sample sample) {
      return sample.getQcUser() != null && sample.getRun() != null
          && sample.getDataReviewPassed() == null;
    }

    private static boolean isTopUpRequired(Sample sample) {
      return TOP_UP_REASON.equals(sample.getQcReason());
    }

    public static Predicate<Case> anyTest(Predicate<Test> testPredicate) {
      return kase -> kase.getTests().stream().anyMatch(testPredicate);
    }

    public static Predicate<Case> allTests(Predicate<Test> testPredicate) {
      return kase -> kase.getTests().stream().allMatch(testPredicate);
    }

    public static Predicate<Test> hasPendingWork(Function<Test, List<Sample>> getGate) {
      return test -> getGate.apply(test).stream().noneMatch(possiblyCompleted);
    }

    public static Predicate<Test> hasPendingWork(Function<Test, List<Sample>> getGate,
        Function<Test, List<Sample>> getPrevious) {
      return test -> getGate.apply(test).stream().noneMatch(possiblyCompleted)
          && getPrevious.apply(test).stream().anyMatch(passed)
          && getPrevious.apply(test).stream().noneMatch(pendingQcOrDataReview);
    }

    public static Predicate<Test> hasPendingQc(Function<Test, List<Sample>> getGateItems) {
      return test -> getGateItems.apply(test).stream().anyMatch(pendingQc);
    }

    public static Predicate<Test> hasPendingDataReview(Function<Test, List<Sample>> getGateItems) {
      return test -> getGateItems.apply(test).stream().anyMatch(pendingDataReview);
    }

    public static Predicate<Test> isCompleted(Function<Test, List<Sample>> getGateItems) {
      return test -> getGateItems.apply(test).stream().anyMatch(passed)
          && getGateItems.apply(test).stream().noneMatch(pendingQcOrDataReview);
    }

    public static Predicate<Case> isPendingRequisitionQc(
        Function<Requisition, List<RequisitionQc>> getQcs) {
      return kase -> kase.getRequisitions().stream().anyMatch(req -> getQcs.apply(req).isEmpty());
    }

    public static Predicate<Case> isCompletedRequisitionQc(
        Function<Requisition, List<RequisitionQc>> getQcs) {
      return kase -> kase.getRequisitions().stream()
          .allMatch(req -> getQcs.apply(req).stream().anyMatch(RequisitionQc::isQcPassed));
    }
  }

}
