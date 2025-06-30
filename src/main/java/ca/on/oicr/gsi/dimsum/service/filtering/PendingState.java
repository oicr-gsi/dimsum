package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.CaseDeliverable;
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
  RECEIPT_QC("Receipt QC", true, false) {
    @Override
    public boolean qualifyCase(Case kase, String deliverableCategory) {
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
  EXTRACTION("Extraction", true, false) {
    @Override
    public boolean qualifyCase(Case kase, String deliverableCategory) {
      return CompletedGate.RECEIPT.qualifyCase(kase, deliverableCategory)
          && Helpers.anyTest(this::qualifyTest).test(kase);
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
  EXTRACTION_QC("Extraction QC Sign-Off", true, false) {
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
  EXTRACTION_TRANSFER("Extraction Transfer", true, false) {
    @Override
    public boolean qualifyTest(Test test) {
      return test.getExtractions().stream()
          .noneMatch(sample -> DataUtils.isPassed(sample) && sample.getTransferDate() != null)
          && test.getExtractions().stream()
              .anyMatch(sample -> DataUtils.isPassed(sample) && sample.getTransferDate() == null);
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.EXTRACTION) {
        // Without the test, we don't know if another extraction was already transferred,
        // so assume we need to transfer all
        return DataUtils.isPassed(sample) && sample.getTransferDate() == null;
      } else {
        return true;
      }
    }
  },
  LIBRARY_PREPARATION("Library Preparation", true, false) {
    @Override
    public boolean qualifyTest(Test test) {
      return !test.isLibraryPreparationSkipped()
          && Helpers.hasPendingWork(test.getLibraryPreparations(), test.getExtractions(), true);
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
  LIBRARY_QC("Library QC Sign-Off", true, false) {
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
  LIBRARY_QUALIFICATION("Library Qualification", true, false) {
    @Override
    public boolean qualifyTest(Test test) {
      return !test.isLibraryQualificationSkipped()
          && Helpers.hasPendingWork(test.getLibraryQualifications(), test.getLibraryPreparations(),
              false);
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
  LIBRARY_QUALIFICATION_QC("Library Qualification QC Sign-Off", true, false) {
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
  LIBRARY_QUALIFICATION_DATA_REVIEW("Library Qualification Data Review", true, false) {
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
  FULL_DEPTH_SEQUENCING("Full-Depth Sequencing", true, false) {
    @Override
    public boolean qualifyTest(Test test) {
      return Helpers.hasPendingWork(test.getFullDepthSequencings(), test.getLibraryQualifications(),
          false);
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
  FULL_DEPTH_QC("Full-Depth Sequencing QC Sign-Off", true, false) {
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
  FULL_DEPTH_DATA_REVIEW("Full-Depth Sequencing Data Review", true, false) {
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
  ANALYSIS_REVIEW("Analysis Review", true, true) {
    @Override
    public boolean qualifyCase(Case kase, String deliverableCategory) {
      if (DataUtils.isAnalysisReviewSkipped(kase)
          || !CompletedGate.FULL_DEPTH_SEQUENCING.qualifyCase(kase, deliverableCategory)) {
        return false;
      }
      if (deliverableCategory == null) {
        return kase.getDeliverables().stream()
            .map(CaseDeliverable::getDeliverableCategory)
            .anyMatch(category -> !CompletedGate.ANALYSIS_REVIEW.qualifyCase(kase, category));
      } else {
        return CompletedGate.ANALYSIS_REVIEW.isApplicable(kase, deliverableCategory)
            && !CompletedGate.ANALYSIS_REVIEW.qualifyCase(kase, deliverableCategory);
      }
    }
  },
  RELEASE_APPROVAL("Release Approval", false, true) {

    @Override
    public boolean qualifyCase(Case kase, String deliverableCategory) {
      if (deliverableCategory == null) {
        // any deliverable has incomplete release approval and (complete analyis review or case stopped)
        return kase.getDeliverables().stream()
            .map(CaseDeliverable::getDeliverableCategory)
            .anyMatch(category -> (kase.isStopped() || CompletedGate.ANALYSIS_REVIEW.qualifyCase(kase, category))
                && !CompletedGate.RELEASE_APPROVAL.qualifyCase(kase, category));
      } else {
        return CompletedGate.RELEASE_APPROVAL.isApplicable(kase, deliverableCategory)
            && (kase.isStopped() || CompletedGate.ANALYSIS_REVIEW.qualifyCase(kase, deliverableCategory))
            && !CompletedGate.RELEASE_APPROVAL.qualifyCase(kase, deliverableCategory);
      }
    }
  },
  RELEASE("Release", false, true) {
    @Override
    public boolean qualifyCase(Case kase, String deliverableCategory) {
      if (deliverableCategory == null) {
        // any deliverable is completed release approval and has a pending release
        return kase.getDeliverables().stream()
            .map(CaseDeliverable::getDeliverableCategory)
            .anyMatch(category -> CompletedGate.RELEASE_APPROVAL.qualifyCase(kase, category)
            && !CompletedGate.RELEASE.qualifyCase(kase, category));
      } else {
        // specified deliverable is completed release approval and has a pending release
        return CompletedGate.RELEASE_APPROVAL.qualifyCase(kase, deliverableCategory)
            && CompletedGate.RELEASE.isApplicable(kase, deliverableCategory)
            && !CompletedGate.RELEASE.qualifyCase(kase, deliverableCategory);
      }
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
  private final boolean considerDeliverableCategory;
  private final Predicate<Test> testPredicate = this::qualifyTest;

  private PendingState(String label, boolean stoppable, boolean considerDeliverableCategory) {
    this.label = label;
    this.stoppable = stoppable;
    this.considerDeliverableCategory = considerDeliverableCategory;
  }

  public String getLabel() {
    return label;
  }

  public boolean isStoppable() {
    return stoppable;
  }

  public boolean considerDeliverableCategory() {
    return considerDeliverableCategory;
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

  /**
   * Checks whether a case has pending work or QC represented by this PendingState
   * 
   * @param kase the case to check
   * @param deliverableCategory the specific deliverable category to check for steps where this is
   *        relevant, or null for all
   * @return true if the step is pending; false otherwise
   */
  public boolean qualifyCase(Case kase, String deliverableCategory) {
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

    public static boolean hasPendingWork(List<Sample> gate, List<Sample> previousGate,
        boolean previousRequiresTransfer) {
      return gate.stream().noneMatch(possiblyCompleted)
          && previousGate.stream().anyMatch(sample -> DataUtils.isPassed(sample)
              && (!previousRequiresTransfer || sample.getTransferDate() != null))
          && previousGate.stream().noneMatch(pendingQcOrDataReview);
    }

    public static boolean hasPendingQc(List<Sample> samples) {
      return samples.stream().anyMatch(pendingQc);
    }

    public static boolean hasPendingDataReview(List<Sample> samples) {
      return samples.stream().anyMatch(pendingDataReview);
    }
  }

}
