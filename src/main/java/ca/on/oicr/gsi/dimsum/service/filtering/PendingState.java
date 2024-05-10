package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.MetricCategory;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.cardea.data.Test;
import ca.on.oicr.gsi.dimsum.data.TestTableView;
import ca.on.oicr.gsi.dimsum.util.DataUtils;

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
  RECEIPT_QC("Receipt QC", true) {
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
        return DataUtils.isPendingQc(sample);
      } else {
        return true;
      }
    }
  },
  EXTRACTION("Extraction", true) {
    @Override
    public boolean qualifyCase(Case kase) {
      return CompletedGate.RECEIPT.predicate()
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
        return DataUtils.isPassed(sample);
      } else {
        return true;
      }
    }
  },
  EXTRACTION_QC("Extraction QC Sign-Off", true) {
    @Override
    public boolean qualifyTest(Test test) {
      return Helpers.hasPendingQc(test.getExtractions());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.EXTRACTION) {
        return DataUtils.isPendingQc(sample);
      } else {
        return true;
      }
    }
  },
  LIBRARY_PREPARATION("Library Preparation", true) {
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
          return DataUtils.isPassed(sample);
        default:
          return true;
      }
    }
  },
  LIBRARY_QC("Library QC Sign-Off", true) {
    @Override
    public boolean qualifyTest(Test test) {
      return Helpers.hasPendingQc(test.getLibraryPreparations());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.LIBRARY_PREP) {
        return DataUtils.isPendingQc(sample);
      } else {
        return true;
      }
    }
  },
  LIBRARY_QUALIFICATION("Library Qualification", true) {
    @Override
    public boolean qualifyTest(Test test) {
      return !test.isLibraryQualificationSkipped()
          && Helpers.hasPendingWork(test.getLibraryQualifications(), test.getLibraryPreparations());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      switch (requestCategory) {
        case RECEIPT:
        case EXTRACTION:
        case LIBRARY_PREP:
          return DataUtils.isPassed(sample);
        default:
          return true;
      }
    }
  },
  LIBRARY_QUALIFICATION_QC("Library Qualification QC Sign-Off", true) {
    @Override
    public boolean qualifyTest(Test test) {
      return Helpers.hasPendingQc(test.getLibraryQualifications());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.LIBRARY_QUALIFICATION) {
        return DataUtils.isPendingQc(sample);
      } else {
        return true;
      }
    }
  },
  LIBRARY_QUALIFICATION_DATA_REVIEW("Library Qualification Data Review", true) {
    @Override
    public boolean qualifyTest(Test test) {
      return Helpers.hasPendingDataReview(test.getLibraryQualifications());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.LIBRARY_QUALIFICATION) {
        return DataUtils.isPendingDataReview(sample);
      } else {
        return true;
      }
    }
  },
  FULL_DEPTH_SEQUENCING("Full-Depth Sequencing", true) {
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
          return DataUtils.isPassed(sample);
        default:
          return true;
      }
    }
  },
  FULL_DEPTH_QC("Full-Depth Sequencing QC Sign-Off", true) {
    @Override
    public boolean qualifyTest(Test test) {
      return Helpers.hasPendingQc(test.getFullDepthSequencings());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.FULL_DEPTH_SEQUENCING) {
        return DataUtils.isPendingQc(sample);
      } else {
        return true;
      }
    }
  },
  FULL_DEPTH_DATA_REVIEW("Full-Depth Sequencing Data Review", true) {
    @Override
    public boolean qualifyTest(Test test) {
      return Helpers.hasPendingDataReview(test.getFullDepthSequencings());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.FULL_DEPTH_SEQUENCING) {
        return DataUtils.isPendingDataReview(sample);
      } else {
        return true;
      }
    }
  },
  ANALYSIS_REVIEW("Analysis Review", true) {
    @Override
    public boolean qualifyCase(Case kase) {
      return !DataUtils.isAnalysisReviewSkipped(kase)
          && CompletedGate.FULL_DEPTH_SEQUENCING.qualifyCase(kase)
          && !CompletedGate.ANALYSIS_REVIEW.qualifyCase(kase);
    }
  },
  ANALYSIS_REVIEW_DATA_RELEASE("Analysis Review - Data Release", true) {
    @Override
    public boolean qualifyCase(Case kase) {
      return !DataUtils.isAnalysisReviewSkipped(kase)
          && CompletedGate.FULL_DEPTH_SEQUENCING.qualifyCase(kase)
          && CompletedGate.ANALYSIS_REVIEW_DATA_RELEASE.isApplicable(kase)
          && !CompletedGate.ANALYSIS_REVIEW_DATA_RELEASE.qualifyCase(kase);
    }
  },
  ANALYSIS_REVIEW_CLINICAL_REPORT("Analysis Review - Clinical Report", true) {
    @Override
    public boolean qualifyCase(Case kase) {
      return !DataUtils.isAnalysisReviewSkipped(kase)
          && CompletedGate.FULL_DEPTH_SEQUENCING.qualifyCase(kase)
          && CompletedGate.ANALYSIS_REVIEW_CLINICAL_REPORT.isApplicable(kase)
          && !CompletedGate.ANALYSIS_REVIEW_CLINICAL_REPORT.qualifyCase(kase);
    }
  },
  RELEASE_APPROVAL("Release Approval", false) {

    @Override
    public boolean qualifyCase(Case kase) {
      return (kase.isStopped() && !CompletedGate.RELEASE_APPROVAL.qualifyCase(kase))
          || RELEASE_APPROVAL_DATA_RELEASE.qualifyCase(kase)
          || RELEASE_APPROVAL_CLINICAL_REPORT.qualifyCase(kase);
    }
  },
  RELEASE_APPROVAL_DATA_RELEASE("Release Approval - Data Release", false) {
    @Override
    public boolean qualifyCase(Case kase) {
      return Helpers.isPendingReleaseApproval(kase, CompletedGate.ANALYSIS_REVIEW_DATA_RELEASE,
          CompletedGate.RELEASE_APPROVAL_DATA_RELEASE);
    }
  },
  RELEASE_APPROVAL_CLINICAL_REPORT("Release Approval - Clinical Report", false) {
    @Override
    public boolean qualifyCase(Case kase) {
      return Helpers.isPendingReleaseApproval(kase, CompletedGate.ANALYSIS_REVIEW_CLINICAL_REPORT,
          CompletedGate.RELEASE_APPROVAL_CLINICAL_REPORT);
    }
  },
  RELEASE("Release", false) {
    @Override
    public boolean qualifyCase(Case kase) {
      return RELEASE_DATA_RELEASE.qualifyCase(kase) || RELEASE_CLINICAL_REPORT.qualifyCase(kase);
    }
  },
  RELEASE_DATA_RELEASE("Release - Data Release", false) {
    @Override
    public boolean qualifyCase(Case kase) {
      return CompletedGate.RELEASE_APPROVAL_DATA_RELEASE.qualifyCase(kase)
          && CompletedGate.RELEASE_DATA_RELEASE.isApplicable(kase)
          && !CompletedGate.RELEASE_DATA_RELEASE.qualifyCase(kase);
    }
  },
  RELEASE_CLINICAL_REPORT("Release - Clinical Report", false) {
    @Override
    public boolean qualifyCase(Case kase) {
      return CompletedGate.RELEASE_APPROVAL_CLINICAL_REPORT.qualifyCase(kase)
          && CompletedGate.RELEASE_CLINICAL_REPORT.isApplicable(kase)
          && !CompletedGate.RELEASE_CLINICAL_REPORT.qualifyCase(kase);
    }
  };
  // @formatter:on

  private static final Map<String, PendingState> map = Stream.of(PendingState.values())
      .collect(Collectors.toMap(PendingState::getLabel, Function.identity()));

  public static PendingState getByLabel(String label) {
    return map.get(label);
  }

  private final String label;
  private final boolean stoppable;
  private final Predicate<Case> casePredicate = this::qualifyCase;
  private final Predicate<Test> testPredicate = this::qualifyTest;

  private PendingState(String label, boolean stoppable) {
    this.label = label;
    this.stoppable = stoppable;
  }

  public String getLabel() {
    return label;
  }

  public boolean isStoppable() {
    return stoppable;
  }

  public Predicate<Case> predicate() {
    return casePredicate;
  }

  public Predicate<Test> testPredicate() {
    return testPredicate;
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

  protected Function<Case, Stream<Sample>> getFilteredSamples(MetricCategory requestCategory) {
    // override for all values where this is applicable
    throw new IllegalStateException("This gate does not apply to samples");
  }

  private static class Helpers {
    public static Predicate<Sample> pendingQc = DataUtils::isPendingQc;
    public static Predicate<Sample> pendingDataReview = DataUtils::isPendingDataReview;
    public static Predicate<Sample> pendingQcOrDataReview =
        sample -> DataUtils.isPendingQc(sample) || DataUtils.isPendingDataReview(sample);
    public static Predicate<Sample> possiblyCompleted =
        sample -> DataUtils.isPassed(sample) || DataUtils.isPendingQc(sample)
            || DataUtils.isPendingDataReview(sample);

    private static boolean isPendingReceiptQc(Case kase) {
      return kase.getReceipts().stream().anyMatch(pendingQc);
    }

    public static Predicate<Case> anyTest(Predicate<Test> testPredicate) {
      return kase -> kase.getTests().stream().anyMatch(testPredicate);
    }

    public static boolean hasPendingWork(List<Sample> gate) {
      return gate.stream().noneMatch(possiblyCompleted);
    }

    public static boolean hasPendingWork(List<Sample> gate, List<Sample> previousGate) {
      return gate.stream().noneMatch(possiblyCompleted)
          && previousGate.stream().anyMatch(DataUtils::isPassed)
          && previousGate.stream().noneMatch(pendingQcOrDataReview);
    }

    public static boolean hasPendingQc(List<Sample> samples) {
      return samples.stream().anyMatch(pendingQc);
    }

    public static boolean hasPendingDataReview(List<Sample> samples) {
      return samples.stream().anyMatch(pendingDataReview);
    }

    public static boolean isPendingReleaseApproval(Case kase, CompletedGate previousGate,
        CompletedGate gate) {
      if (!kase.isStopped() && !previousGate.qualifyCase(kase)) {
        return false;
      }
      return gate.isApplicable(kase) && !gate.qualifyCase(kase);
    }
  }

}
