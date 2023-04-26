package ca.on.oicr.gsi.dimsum.service.filtering;

import java.time.LocalDate;
import java.util.function.Function;
import java.util.function.Predicate;
import ca.on.oicr.gsi.dimsum.data.RequisitionQc;
import ca.on.oicr.gsi.dimsum.data.Sample;


public enum DateFilterKey {
  // @formatter:off
  AFTER_DATE(string -> {
    return sample -> sample.getQcDate() != null &&!sample.getQcDate().isBefore(LocalDate.parse(string)); 
  }) {
      @Override 
      public Function<String, Predicate<RequisitionQc>> requisitionQcPredicate() {
        return string -> requisitionQc -> requisitionQc.getQcDate() != null && !requisitionQc.getQcDate().isBefore(LocalDate.parse(string)); 
      }
  }, 
  BEFORE_DATE(string -> {
    return sample -> sample.getQcDate() != null && !sample.getQcDate().isAfter(LocalDate.parse(string)); 
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

  public Function<String, Predicate<Sample>> create() {
    return create;
  }

  public Function<String, Predicate<RequisitionQc>> requisitionQcPredicate() {
    return string -> requisitionQC -> true;
  }

  @Override
  public String toString() {
    switch (this) {
      case AFTER_DATE:
        return "AFTER_DATE";
      case BEFORE_DATE:
        return "BEFORE_DATE";
      default:
        return ""; // return empty string for unknown values
    }
  }
}
