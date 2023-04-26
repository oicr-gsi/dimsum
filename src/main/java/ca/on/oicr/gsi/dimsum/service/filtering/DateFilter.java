package ca.on.oicr.gsi.dimsum.service.filtering;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import ca.on.oicr.gsi.dimsum.data.Sample;
import ca.on.oicr.gsi.dimsum.data.RequisitionQc;



public class DateFilter {
  private final DateFilterKey key;
  private final String value;

  public DateFilter(DateFilterKey key, String value) {
    requireNonNull(key);
    requireNonNull(value);
    this.key = key;
    this.value = value;
  }

  public DateFilterKey getKey() {
    return key;
  }

  public Predicate<Sample> samplePredicate() {
    return key.create().apply(value);
  }

  public Predicate<RequisitionQc> requisitionQcPredicate() {
    return key.requisitionQcPredicate().apply(value);
  }

}
