package ca.on.oicr.gsi.dimsum.service.filtering;

import java.time.LocalDate;
import java.util.function.Function;
import java.util.function.Predicate;
import ca.on.oicr.gsi.cardea.data.RequisitionQc;
import ca.on.oicr.gsi.cardea.data.Sample;


public enum DateFilterKey {
  // @formatter:off
  AFTER_DATE(string -> {
    return sample -> {
      if (sample.getRun() == null) {
        return sample.getQcDate() != null && !sample.getQcDate().isBefore(LocalDate.parse(string));
      } else {
        // run-library; completion based on data review
        return sample.getDataReviewDate() != null
            && !sample.getDataReviewDate().isBefore(LocalDate.parse(string));
      }
    }; 
  }) {
      @Override 
      public Function<String, Predicate<RequisitionQc>> requisitionQcPredicate() {
        return string -> requisitionQc -> requisitionQc.getQcDate() != null && !requisitionQc.getQcDate().isBefore(LocalDate.parse(string)); 
      }
  }, 
  BEFORE_DATE(string -> {
    return sample -> {
      if (sample.getRun() == null) {
        return sample.getQcDate() != null && !sample.getQcDate().isAfter(LocalDate.parse(string));
      } else {
        // run-library; completion based on data review
        return sample.getDataReviewDate() != null
            && !sample.getDataReviewDate().isAfter(LocalDate.parse(string));
      }
    }; 
  }) {
      @Override 
      public Function<String, Predicate<RequisitionQc>> requisitionQcPredicate() {
        return string -> requisitionQc -> requisitionQc.getQcDate() != null && !requisitionQc.getQcDate().isAfter(LocalDate.parse(string)); 
      }
  };
// @formatter:on
  private final Function<String, Predicate<Sample>> create;

  private DateFilterKey(Function<String, Predicate<Sample>> create) {
    this.create = create;
  }

  public Function<String, Predicate<Sample>> samplePredicate() {
    return create;
  }

  public Function<String, Predicate<RequisitionQc>> requisitionQcPredicate() {
    return string -> requisitionQC -> true;
  }
}
