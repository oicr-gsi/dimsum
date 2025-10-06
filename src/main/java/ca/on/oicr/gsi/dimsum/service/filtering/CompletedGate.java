package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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

public enum CompletedGate {
  // @formatter:off
  RECEIPT("Receipt", true, true, false) {
    @Override
    public boolean qualifyCase(Case kase, String deliverableCategory) {
      return Helpers.isReceiptCompleted(kase);
    }

    @Override
    public boolean qualifyTest(Test test) {
      return true;
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
  EXTRACTION("Extraction", false, true, false) {
    @Override
    public boolean qualifyTest(Test test) {
      return test.isExtractionSkipped() || test.getExtractions().stream().anyMatch(sample ->
          DataUtils.isPassed(sample) && sample.getTransferDate() != null);
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.EXTRACTION) {
        return DataUtils.isPassed(sample);
      } else {
        return true;
      }
    }
  },
  LIBRARY_PREPARATION("Library Preparation", false, true, false) {
    @Override
    public boolean qualifyTest(Test test) {
      return test.isLibraryPreparationSkipped()
          || Helpers.isCompleted(test.getLibraryPreparations());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.LIBRARY_PREP) {
        return DataUtils.isPassed(sample);
      } else {
        return true;
      }
    }

  },
  LIBRARY_QUALIFICATION("Library Qualification", false, true, false) {
    @Override
    public boolean qualifyTest(Test test) {
      return test.isLibraryQualificationSkipped()
          || Helpers.isCompleted(test.getLibraryQualifications());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.LIBRARY_QUALIFICATION) {
        return DataUtils.isPassed(sample);
      } else {
        return true;
      }
    }
  },
  FULL_DEPTH_SEQUENCING("Full-Depth Sequencing", false, true, false) {
    @Override
    public boolean qualifyTest(Test test) {
      return Helpers.isCompleted(test.getFullDepthSequencings());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.FULL_DEPTH_SEQUENCING) {
        return DataUtils.isPassed(sample);
      } else {
        return true;
      }
    }
  },
  ANALYSIS_REVIEW("Analysis Review", true, true, true) {
    @Override
    public boolean qualifyCase(Case kase, String deliverableCategory) {
      if (DataUtils.isAnalysisReviewSkipped(kase)) {
        return FULL_DEPTH_SEQUENCING.qualifyCase(kase, deliverableCategory);
      } else {
        return qualifyCaseForDeliverableType(kase, deliverableCategory,
            CompletedGate::analysisReviewComplete);
      }
    }
  },
  RELEASE_APPROVAL("Release Approval", true, false, true) {
    @Override
    public boolean qualifyCase(Case kase, String deliverableCategory) {
      return qualifyCaseForDeliverableType(kase, deliverableCategory,
          CompletedGate::releaseApprovalComplete);
    }
  },
  RELEASE("Release", true, false, true) {
    @Override
    public boolean qualifyCase(Case kase, String deliverableCategory) {
      return qualifyCaseForDeliverableType(kase, deliverableCategory,
          CompletedGate::allReleasesComplete);
    }
  };
// @formatter:on

  private static final Map<String, CompletedGate> map = Stream.of(CompletedGate.values())
      .collect(Collectors.toMap(CompletedGate::getLabel, Function.identity()));

  public static CompletedGate getByLabel(String label) {
    return map.get(label);
  }

  private final String label;
  private final boolean caseLevel;
  private final boolean stoppable;
  private final boolean considerDeliverableCategory;
  private final Predicate<Test> testPredicate = this::qualifyTest;

  private CompletedGate(String label, boolean caseLevel, boolean stoppable,
      boolean considerDeliverableCategory) {
    this.label = label;
    this.caseLevel = caseLevel;
    this.stoppable = stoppable;
    this.considerDeliverableCategory = considerDeliverableCategory;
  }

  public String getLabel() {
    return label;
  }

  public boolean isCaseLevel() {
    return caseLevel;
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
   * Check whether a case is completed the step represented by this CompletedGate
   * 
   * @param kase the case to check
   * @param deliverableCategory the specific deliverable category to check for steps where this is
   *        relevant (see {@link #isApplicable(Case, String)}), or null for all
   * @return true if the step is completed; false otherwise
   */
  public boolean qualifyCase(Case kase, String deliverableCategory) {
    return kase.getTests().stream().allMatch(test -> qualifyTest(test));
  }

  public boolean isApplicable(Case kase, String deliverableCategory) {
    return deliverableCategory == null
        || kase.getDeliverables().stream()
            .anyMatch(x -> Objects.equals(x.getDeliverableCategory(), deliverableCategory));
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

  private static boolean qualifyCaseForDeliverableType(Case kase, String deliverableCategory,
      Function<CaseDeliverable, Boolean> getQcPassed) {
    if (kase.getDeliverables() == null || kase.getDeliverables().isEmpty()) {
      return false;
    }
    Stream<CaseDeliverable> stream = null;
    if (deliverableCategory != null) {
      if (kase.getDeliverables().stream()
          .noneMatch(x -> Objects.equals(x.getDeliverableCategory(), deliverableCategory))) {
        return false;
      }
      stream =
          kase.getDeliverables().stream()
              .filter(x -> Objects.equals(x.getDeliverableCategory(), deliverableCategory));
    } else {
      stream = kase.getDeliverables().stream();
    }
    return stream.allMatch(x -> Boolean.TRUE.equals(getQcPassed.apply(x)));
  }

  private static boolean analysisReviewComplete(CaseDeliverable deliverable) {
    return DataUtils.isComplete(deliverable.getAnalysisReviewQcStatus());
  }

  private static boolean releaseApprovalComplete(CaseDeliverable deliverable) {
    return DataUtils.isComplete(deliverable.getReleaseApprovalQcStatus());
  }

  private static boolean allReleasesComplete(CaseDeliverable deliverable) {
    return !deliverable.getReleases().isEmpty() && deliverable.getReleases().stream()
        .allMatch(x -> DataUtils.isComplete(x.getQcStatus()));
  }

  private static class Helpers {
    private static Predicate<Sample> pendingQc = DataUtils::isPendingQc;

    public static boolean isReceiptCompleted(Case kase) {
      return kase.getReceipts().stream().anyMatch(DataUtils::isPassed)
          && kase.getReceipts().stream().noneMatch(pendingQc);
    }

    public static boolean isCompleted(List<Sample> samples) {
      return samples.stream().anyMatch(DataUtils::isPassed);
    }

  }


}
