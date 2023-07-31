package ca.on.oicr.gsi.dimsum.service.filtering;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.MetricCategory;
import ca.on.oicr.gsi.cardea.data.Requisition;
import ca.on.oicr.gsi.cardea.data.RequisitionQc;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.cardea.data.Test;
import ca.on.oicr.gsi.dimsum.data.TestTableView;

public enum CompletedGate {
  // @formatter:off
  RECEIPT("Receipt") {
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
        return Helpers.isPassed(sample);
      } else {
        return true;
      }
    }
  },
  EXTRACTION("Extraction") {
    @Override
    public boolean qualifyCase(Case kase) {
      return Helpers.anyTest(this::qualifyTest).test(kase);
    }

    @Override
    public boolean qualifyTest(Test test) {
      return test.isExtractionSkipped() || Helpers.isCompleted(test.getExtractions());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.EXTRACTION) {
        return Helpers.isPassed(sample);
      } else {
        return true;
      }

    }
  },
  LIBRARY_PREPARATION("Library Preparation") {
    @Override
    public boolean qualifyCase(Case kase) {
      return Helpers.anyTest(this::qualifyTest).test(kase);
    }

    @Override
    public boolean qualifyTest(Test test) {
      return test.isLibraryPreparationSkipped()
          || Helpers.isCompleted(test.getLibraryPreparations());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.LIBRARY_PREP) {
        return Helpers.isPassed(sample);
      } else {
        return true;
      }
    }

  },
  LIBRARY_QUALIFICATION("Library Qualification") {
    @Override
    public boolean qualifyCase(Case kase) {
      return Helpers.anyTest(this::qualifyTest).test(kase);
    }

    @Override
    public boolean qualifyTest(Test test) {
      return Helpers.isCompleted(test.getLibraryQualifications());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.LIBRARY_QUALIFICATION) {
        return Helpers.isPassed(sample);
      } else {
        return true;
      }
    }
  },
  FULL_DEPTH_SEQUENCING("Full-Depth Sequencing") {
    @Override
    public boolean qualifyCase(Case kase) {
      return Helpers.anyTest(this::qualifyTest).test(kase);
    }
    
    @Override
    public boolean qualifyTest(Test test) {
      return Helpers.isCompleted(test.getFullDepthSequencings());
    }

    @Override
    public boolean qualifySample(Sample sample, MetricCategory requestCategory) {
      if (requestCategory == MetricCategory.FULL_DEPTH_SEQUENCING) {
        return Helpers.isPassed(sample);
      } else {
        return true;
      }
    }
  },
  INFORMATICS_REVIEW("Informatics Review") {
    @Override
    public boolean qualifyCase(Case kase) {
      return Helpers.isCompletedRequisitionQc(kase.getRequisition(), Requisition::getInformaticsReviews);
    }

    @Override
    public boolean qualifyRequisition(Requisition requisition) {
      return Helpers.isCompletedRequisitionQc(requisition, Requisition::getInformaticsReviews);

    }

  },
  DRAFT_REPORT("Draft Report") {
    @Override
    public boolean qualifyCase(Case kase) {
      return Helpers.isCompletedRequisitionQc(kase.getRequisition(), Requisition::getDraftReports);
    }

    @Override
    public boolean qualifyRequisition(Requisition requisition) {
      return Helpers.isCompletedRequisitionQc(requisition, Requisition::getDraftReports);
    }
  },
  FINAL_REPORT("Final Report") {
    @Override
    public boolean qualifyCase(Case kase) {
      return Helpers.isCompletedRequisitionQc(kase.getRequisition(), Requisition::getFinalReports);
    }

    @Override
    public boolean qualifyRequisition(Requisition requisition) {
      return Helpers.isCompletedRequisitionQc(requisition, Requisition::getFinalReports);
    }
  };
// @formatter:on

  private static final Map<String, CompletedGate> map = Stream.of(CompletedGate.values())
      .collect(Collectors.toMap(CompletedGate::getLabel, Function.identity()));

  public static CompletedGate getByLabel(String label) {
    return map.get(label);
  }

  private final String label;
  private final Predicate<Case> casePredicate = this::qualifyCase;
  private final Predicate<Test> testPredicate = this::qualifyTest;
  private final Predicate<Requisition> requisitionPredicate = this::qualifyRequisition;

  private CompletedGate(String label) {
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
    return kase.getTests().stream().allMatch(test -> qualifyTest(test));
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
    public static Predicate<Sample> passed = Helpers::isPassed;

    private static boolean isPassed(Sample sample) {
      return isTrue(sample.getQcPassed())
          && (sample.getRun() == null || isTrue(sample.getDataReviewPassed()));
    }

    private static boolean isTrue(Boolean value) {
      return Boolean.TRUE.equals(value);
    }

    public static boolean isReceiptCompleted(Case kase) {
      return kase.getReceipts().stream().anyMatch(passed)
          && kase.getReceipts().stream().noneMatch(pendingQc);
    }

    public static Predicate<Case> anyTest(Predicate<Test> testPredicate) {
      return kase -> kase.getTests().stream().anyMatch(testPredicate);
    }

    private static boolean isPendingQc(Sample sample) {
      return sample.getQcPassed() == null && !isTopUpRequired(sample);
    }

    private static boolean isTopUpRequired(Sample sample) {
      return TOP_UP_REASON.equals(sample.getQcReason());
    }

    public static boolean isCompletedRequisitionQc(Requisition requisition,
        Function<Requisition, List<RequisitionQc>> getQcs) {
      RequisitionQc reqQc = getQcs.apply(requisition).stream()
          .max(Comparator.comparing(RequisitionQc::getQcDate)).orElse(null);
      if (reqQc != null && reqQc.isQcPassed()) {
        return true;
      }
      return false;
    }

    public static boolean isCompleted(List<Sample> samples) {
      return samples.stream().anyMatch(passed);
    }

  }


}
