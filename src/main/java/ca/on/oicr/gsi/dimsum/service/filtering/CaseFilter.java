package ca.on.oicr.gsi.dimsum.service.filtering;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;
import ca.on.oicr.gsi.dimsum.data.Case;
import ca.on.oicr.gsi.dimsum.data.MetricCategory;
import ca.on.oicr.gsi.dimsum.data.Requisition;
import ca.on.oicr.gsi.dimsum.data.Sample;
import ca.on.oicr.gsi.dimsum.data.Test;
import ca.on.oicr.gsi.dimsum.data.TestTableView;

public class CaseFilter {

  private final CaseFilterKey key;
  private final String value;

  public CaseFilter(CaseFilterKey key, String value) {
    requireNonNull(key);
    requireNonNull(value);
    this.key = key;
    this.value = value;
  }

  public CaseFilterKey getKey() {
    return key;
  }

  public Predicate<Case> casePredicate() {
    return key.create().apply(value);
  }

  public Predicate<Test> testPredicate() {
    return key.testPredicate().apply(value);
  }

  public Predicate<Sample> samplePredicate(MetricCategory requestCategory) {
    return key.samplePredicate(requestCategory).apply(value);
  }

  public Predicate<Requisition> requisitionPredicate() {
    return key.requisitionPredicate().apply(value);
  }

  public Predicate<TestTableView> testTableViewPredicate() {
    return key.testTableViewPredicate().apply(value);
  }

}
