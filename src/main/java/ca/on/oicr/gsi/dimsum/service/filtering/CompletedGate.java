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
import ca.on.oicr.gsi.dimsum.util.SampleUtils;

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
        return SampleUtils.isPassed(sample);
      } else {
        return true;
      }
    }
  },
  EXTRACTION("Extraction", true) {
    @Override
    public boolean qualifyTest(Test test) {
      return test.isExtractionSkipped() || Helpers.isCompleted(test.getExtractions());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.EXTRACTION) {
        return SampleUtils.isPassed(sample);
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
        return SampleUtils.isPassed(sample);
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
        return SampleUtils.isPassed(sample);
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
        return SampleUtils.isPassed(sample);
      } else {
        return true;
      }
    }
  },
  ANALYSIS_REVIEW("Analysis Review", true) {
    @Override
    public boolean qualifyCase(Case kase) {
      return kase.getDeliverables() != null && !kase.getDeliverables().isEmpty()
          && kase.getDeliverables().stream().allMatch(x -> Boolean.TRUE.equals(x.getAnalysisReviewQcPassed()));
    }

  },
  RELEASE_APPROVAL("Release Approval", false) {
    @Override
    public boolean qualifyCase(Case kase) {
      return kase.getDeliverables() != null && !kase.getDeliverables().isEmpty()
          && kase.getDeliverables().stream().allMatch(x -> Boolean.TRUE.equals(x.getReleaseApprovalQcPassed()));
    }
  },
  RELEASE("Release", false) {
    @Override
    public boolean qualifyCase(Case kase) {
      return kase.getDeliverables() != null && !kase.getDeliverables().isEmpty()
          && kase.getDeliverables().stream().allMatch(deliverable -> deliverable.getReleases() != null
              && !deliverable.getReleases().isEmpty()
              && deliverable.getReleases().stream().allMatch(x -> Boolean.TRUE.equals(x.getQcPassed())));
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

  private CompletedGate(String label, boolean stoppable) {
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
    return kase.getTests().stream().allMatch(test -> qualifyTest(test));
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
    private static Predicate<Sample> pendingQc = SampleUtils::isPendingQc;

    public static boolean isReceiptCompleted(Case kase) {
      return kase.getReceipts().stream().anyMatch(SampleUtils::isPassed)
          && kase.getReceipts().stream().noneMatch(pendingQc);
    }

    public static boolean isCompleted(List<Sample> samples) {
      return samples.stream().anyMatch(SampleUtils::isPassed);
    }

  }


}
