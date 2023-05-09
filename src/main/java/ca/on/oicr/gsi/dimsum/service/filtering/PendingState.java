package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ca.on.oicr.gsi.dimsum.data.Case;
import ca.on.oicr.gsi.dimsum.data.MetricCategory;
import ca.on.oicr.gsi.dimsum.data.Requisition;
import ca.on.oicr.gsi.dimsum.data.RequisitionQc;
import ca.on.oicr.gsi.dimsum.data.Sample;
import ca.on.oicr.gsi.dimsum.data.Test;
import ca.on.oicr.gsi.dimsum.data.TestTableView;

/**
 * <pre>
 * When to consider items pending:
 * 
 * Cases: whenever at least one item qualifies, as described below
 * 
 * Pending work (e.g. EXTRACTION)
 * * test qualification: previous gate completed and target gate not (completed or pending QC)
 * * target gate: all items (may include failed attempts and items needing top-up)
 * * upstream gates: all QC-passed items
 * * downstream gates: include all items for qualifying tests, but there should be none
 * 
 * Pending QC (e.g. LIBRARY_QC)
 * * test qualification: at least one item in target gate is pending QC
 * * target gate: items that are pending QC
 * 
 * Pending data review (e.g. LIBRARY_QUALIFICATION_DATA_REVIEW)
 * * test qualification: at least one item in target gate is pending data review
 * * target gate:
 * ** if run-library: items that are pending data review
 * ** else: n/a
 * 
 * For all states, unless otherwise noted:
 * * upstream gates: include all items for qualifying tests
 * * downstream gates: include all items for qualifying tests
 * </pre>
 */
public enum PendingState {

  // @formatter:off
  RECEIPT_QC("Receipt QC") {
    @Override
    public boolean qualifyCase(Case kase) {
      return Helpers.isPendingReceiptQc(kase);
    }

    @Override
    public boolean qualifyTest(Test test) {
      return true;
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.RECEIPT) {
        return Helpers.isPendingQc(sample);
      } else {
        return true;
      }
    }
  },
  EXTRACTION("Extraction") {
    @Override
    public boolean qualifyCase(Case kase) {
      return Helpers.isReceiptPassed
          .and(Helpers.anyTest(this::qualifyTest))
          .test(kase);
    }

    @Override
    public boolean qualifyTest(Test test) {
      return !test.isExtractionSkipped() && Helpers.hasPendingWork(test.getExtractions());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.RECEIPT) {
        return Helpers.isPassed(sample);
      } else {
        return true;
      }
    }
  },
  EXTRACTION_QC("Extraction QC Sign-Off") {
    @Override
    public boolean qualifyTest(Test test) {
      return Helpers.hasPendingQc(test.getExtractions());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.EXTRACTION) {
        return Helpers.isPendingQc(sample);
      } else {
        return true;
      }
    }
  },
  LIBRARY_PREPARATION("Library Preparation") {
    @Override
    public boolean qualifyTest(Test test) {
      return !test.isLibraryPreparationSkipped()
          && Helpers.hasPendingWork(test.getLibraryPreparations(), test.getExtractions());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      switch (requestCategory) {
        case RECEIPT:
        case EXTRACTION:
          return Helpers.isPassed(sample);
        default:
          return true;
      }
    }
  },
  LIBRARY_QC("Library QC Sign-Off") {
    @Override
    public boolean qualifyTest(Test test) {
      return Helpers.hasPendingQc(test.getLibraryPreparations());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.LIBRARY_PREP) {
        return Helpers.isPendingQc(sample);
      } else {
        return true;
      }
    }
  },
  LIBRARY_QUALIFICATION("Library Qualification") {
    @Override
    public boolean qualifyTest(Test test) {
      return Helpers.hasPendingWork(test.getLibraryQualifications(), test.getLibraryPreparations());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      switch (requestCategory) {
        case RECEIPT:
        case EXTRACTION:
        case LIBRARY_PREP:
          return Helpers.isPassed(sample);
        default:
          return true;
      }
    }
  },
  LIBRARY_QUALIFICATION_QC("Library Qualification QC Sign-Off") {
    @Override
    public boolean qualifyTest(Test test) {
      return Helpers.hasPendingQc(test.getLibraryQualifications());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.LIBRARY_QUALIFICATION) {
        return Helpers.isPendingQc(sample);
      } else {
        return true;
      }
    }
  },
  LIBRARY_QUALIFICATION_DATA_REVIEW("Library Qualification Data Review") {
    @Override
    public boolean qualifyTest(Test test) {
      return Helpers.hasPendingDataReview(test.getLibraryQualifications());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.LIBRARY_QUALIFICATION) {
        return Helpers.isPendingDataReview(sample);
      } else {
        return true;
      }
    }
  },
  FULL_DEPTH_SEQUENCING("Full-Depth Sequencing") {
    @Override
    public boolean qualifyTest(Test test) {
      return Helpers.hasPendingWork(test.getFullDepthSequencings(), test.getLibraryQualifications());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      switch (requestCategory) {
        case RECEIPT:
        case EXTRACTION:
        case LIBRARY_PREP:
        case LIBRARY_QUALIFICATION:
          return Helpers.isPassed(sample);
        default:
          return true;
      }
    }
  },
  FULL_DEPTH_QC("Full-Depth Sequencing QC Sign-Off") {
    @Override
    public boolean qualifyTest(Test test) {
      return Helpers.hasPendingQc(test.getFullDepthSequencings());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.FULL_DEPTH_SEQUENCING) {
        return Helpers.isPendingQc(sample);
      } else {
        return true;
      }
    }
  },
  FULL_DEPTH_DATA_REVIEW("Full-Depth Sequencing Data Review") {
    @Override
    public boolean qualifyTest(Test test) {
      return Helpers.hasPendingDataReview(test.getFullDepthSequencings());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.FULL_DEPTH_SEQUENCING) {
        return Helpers.isPendingDataReview(sample);
      } else {
        return true;
      }
    }
  },
  INFORMATICS_REVIEW("Informatics Review") {
    @Override
    public boolean qualifyCase(Case kase) {
      return kase.getTests().stream().allMatch(Helpers.isCompleted(Test::getFullDepthSequencings))
          && this.qualifyRequisition(kase.getRequisition());
    }

    @Override
    public boolean qualifyRequisition(Requisition requisition) {
      return requisition.getInformaticsReviews().isEmpty();
    }
  },
  DRAFT_REPORT("Draft Report") {

    @Override
    public boolean qualifyCase(Case kase) {
      return Helpers.isCompletedRequisitionQc(kase, Requisition::getInformaticsReviews)
          && this.qualifyRequisition(kase.getRequisition());
    }

    @Override
    public boolean qualifyRequisition(Requisition requisition) {
      return requisition.getDraftReports().isEmpty();
    }
  },
  FINAL_REPORT("Final Report") {
    @Override
    public boolean qualifyCase(Case kase) {
      return Helpers.isCompletedRequisitionQc(kase, Requisition::getDraftReports)
          && this.qualifyRequisition(kase.getRequisition());
    }

    @Override
    public boolean qualifyRequisition(Requisition requisition) {
      return requisition.getFinalReports().isEmpty();
    }
  };
  // @formatter:on

  private static final Map<String, PendingState> map = Stream.of(PendingState.values())
      .collect(Collectors.toMap(PendingState::getLabel, Function.identity()));

  public static PendingState getByLabel(String label) {
    return map.get(label);
  }

  private final String label;
  private final Predicate<Case> casePredicate = this::qualifyCase;
  private final Predicate<Test> testPredicate = this::qualifyTest;
  private final Predicate<Requisition> requisitionPredicate = this::qualifyRequisition;

  private PendingState(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public Predicate<Case> predicate() {
    return casePredicate;
  }

  public Predicate<Test> testPredicate() {
    return testPredicate;
  }

  public Predicate<Requisition> requisitionPredicate() {
    return requisitionPredicate;
  }

  public Predicate<TestTableView> testTableViewPredicate() {
    return view -> qualifyTest(view.getTest());
  }

  public Predicate<Sample> samplePredicate(MetricCategory requestCategory) {
    return sample -> qualifySample(sample, requestCategory);
  }

  public boolean qualifyCase(Case kase) {
    return kase.getTests().stream().anyMatch(test -> qualifyTest(test));
  }

  public boolean qualifyTest(Test test) {
    return true;
  }

  public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
    return true;
  }

  public boolean qualifyRequisition(Requisition requisition) {
    return true;
  }

  protected Function<Case, Stream<Sample>> getFilteredSamples(MetricCategory requestCategory) {
    // override for all values where this is applicable
    throw new IllegalStateException("This gate does not apply to samples");
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

    private static boolean isPendingReceiptQc(Case kase) {
      return kase.getReceipts().stream().anyMatch(pendingQc);
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

    public static boolean hasPendingWork(List<Sample> gate) {
      return gate.stream().noneMatch(possiblyCompleted);
    }

    public static boolean hasPendingWork(List<Sample> gate, List<Sample> previousGate) {
      return gate.stream().noneMatch(possiblyCompleted)
          && previousGate.stream().anyMatch(passed)
          && previousGate.stream().noneMatch(pendingQcOrDataReview);
    }

    public static boolean hasPendingQc(List<Sample> samples) {
      return samples.stream().anyMatch(pendingQc);
    }

    public static boolean hasPendingDataReview(List<Sample> samples) {
      return samples.stream().anyMatch(pendingDataReview);
    }

    public static Predicate<Test> isCompleted(Function<Test, List<Sample>> getGateItems) {
      return test -> getGateItems.apply(test).stream().anyMatch(passed)
          && getGateItems.apply(test).stream().noneMatch(pendingQcOrDataReview);
    }

    public static boolean isCompletedRequisitionQc(Case kase,
        Function<Requisition, List<RequisitionQc>> getQcs) {
      return getQcs.apply(kase.getRequisition()).stream()
          .anyMatch(qc -> qc.isQcPassed());
    }
  }

}
