package ca.on.oicr.gsi.dimsum.service.filtering;

import static java.util.Objects.requireNonNull;
import java.util.function.Predicate;
import ca.on.oicr.gsi.cardea.data.Case;
import ca.on.oicr.gsi.cardea.data.MetricCategory;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.cardea.data.Test;
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

  public String getValue() {
    return value;
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

  public Predicate<TestTableView> testTableViewPredicate() {
    return key.testTableViewPredicate().apply(value);
  }

}
