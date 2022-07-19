package ca.on.oicr.gsi.dimsum.service.filtering;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import ca.on.oicr.gsi.dimsum.data.Case;

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

  public Predicate<Case> predicate() {
    return key.create().apply(value);
  }

}
