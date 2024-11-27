package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.CaseDeliverable;
import ca.on.oicr.gsi.cardea.data.DeliverableType;
import ca.on.oicr.gsi.cardea.data.MetricCategory;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.cardea.data.Test;
import ca.on.oicr.gsi.dimsum.data.TestTableView;
import ca.on.oicr.gsi.dimsum.util.DataUtils;

public enum CompletedGate {
  // @formatter:off
  RECEIPT("Receipt", true) {
    @Override
    public boolean qualifyCase(Case kase) {
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
  EXTRACTION("Extraction", true) {
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
  LIBRARY_PREPARATION("Library Preparation", true) {
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
  LIBRARY_QUALIFICATION("Library Qualification", true) {
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
  FULL_DEPTH_SEQUENCING("Full-Depth Sequencing", true) {
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
  ANALYSIS_REVIEW("Analysis Review", true) {
    @Override
    public boolean qualifyCase(Case kase) {
      if (DataUtils.isAnalysisReviewSkipped(kase)) {
        return FULL_DEPTH_SEQUENCING.qualifyCase(kase);
      } else {
        return qualifyCaseForDeliverableType(kase, null,
            CompletedGate::analysisReviewComplete);
      }
    }
  },
  ANALYSIS_REVIEW_DATA_RELEASE("Analysis Review - Data Release", true, DeliverableType.DATA_RELEASE) {
    @Override
    public boolean qualifyCase(Case kase) {
      if (DataUtils.isAnalysisReviewSkipped(kase)) {
        return FULL_DEPTH_SEQUENCING.qualifyCase(kase);
      } else {
        return qualifyCaseForDeliverableType(kase, DeliverableType.DATA_RELEASE,
            CompletedGate::analysisReviewComplete);
      }
    }
  },
  ANALYSIS_REVIEW_CLINICAL_REPORT("Analysis Review - Clinical Report", true,
      DeliverableType.CLINICAL_REPORT) {

    @Override
    public boolean qualifyCase(Case kase) {
      if (DataUtils.isAnalysisReviewSkipped(kase)) {
        return FULL_DEPTH_SEQUENCING.qualifyCase(kase);
      } else {
        return qualifyCaseForDeliverableType(kase, DeliverableType.CLINICAL_REPORT,
            CompletedGate::analysisReviewComplete);
      }
    }
  },
  RELEASE_APPROVAL("Release Approval", false) {
    @Override
    public boolean qualifyCase(Case kase) {
      return qualifyCaseForDeliverableType(kase, null, CompletedGate::releaseApprovalComplete);
    }
  },
  RELEASE_APPROVAL_DATA_RELEASE("Release Approval - Data Release", false, DeliverableType.DATA_RELEASE) {
    @Override
    public boolean qualifyCase(Case kase) {
      return qualifyCaseForDeliverableType(kase, DeliverableType.DATA_RELEASE,
          CompletedGate::releaseApprovalComplete);
    }
  },
  RELEASE_APPROVAL_CLINICAL_REPORT("Release Approval - Clinical Report", false, DeliverableType.CLINICAL_REPORT) {
    @Override
    public boolean qualifyCase(Case kase) {
      return qualifyCaseForDeliverableType(kase, DeliverableType.CLINICAL_REPORT,
          CompletedGate::releaseApprovalComplete);
    }
  },
  RELEASE("Release", false) {
    @Override
    public boolean qualifyCase(Case kase) {
      return qualifyCaseForDeliverableType(kase, null, CompletedGate::allReleasesComplete);
    }
  },
  RELEASE_DATA_RELEASE("Release - Data Release", false, DeliverableType.DATA_RELEASE) {
    @Override
    public boolean qualifyCase(Case kase) {
      return qualifyCaseForDeliverableType(kase, DeliverableType.DATA_RELEASE,
          CompletedGate::allReleasesComplete);
    }
  },
  RELEASE_CLINICAL_REPORT("Release - Clinical Report", false, DeliverableType.CLINICAL_REPORT) {
    @Override
    public boolean qualifyCase(Case kase) {
      return qualifyCaseForDeliverableType(kase, DeliverableType.CLINICAL_REPORT,
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
  private final boolean stoppable;
  private final Predicate<Case> casePredicate = this::qualifyCase;
  private final Predicate<Test> testPredicate = this::qualifyTest;
  private final DeliverableType deliverableType;

  private CompletedGate(String label, boolean stoppable, DeliverableType deliverableType) {
    this.label = label;
    this.stoppable = stoppable;
    this.deliverableType = deliverableType;
  }

  private CompletedGate(String label, boolean stoppable) {
    this(label, stoppable, null);
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
    return kase.getTests().stream().allMatch(test -> qualifyTest(test));
  }

  public boolean isApplicable(Case kase) {
    return deliverableType == null
        || kase.getDeliverables().stream().anyMatch(x -> x.getDeliverableType() == deliverableType);
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

  private static boolean qualifyCaseForDeliverableType(Case kase, DeliverableType deliverableType,
      Function<CaseDeliverable, Boolean> getQcPassed) {
    if (kase.getDeliverables() == null || kase.getDeliverables().isEmpty()) {
      return false;
    }
    Stream<CaseDeliverable> stream = null;
    if (deliverableType != null) {
      if (kase.getDeliverables().stream()
          .noneMatch(x -> x.getDeliverableType() == deliverableType)) {
        return false;
      }
      stream =
          kase.getDeliverables().stream().filter(x -> x.getDeliverableType() == deliverableType);
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
