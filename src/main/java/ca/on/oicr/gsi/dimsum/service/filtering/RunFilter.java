package ca.on.oicr.gsi.dimsum.service.filtering;

import static java.util.Objects.requireNonNull;

import ca.on.oicr.gsi.cardea.data.Run;
import java.util.function.Predicate;

public class RunFilter {

  private final RunFilterKey key;
  private final String value;

  public RunFilter(RunFilterKey key, String value) {
    requireNonNull(key);
    requireNonNull(value);
    this.key = key;
    this.value = value;
  }

  public RunFilterKey getKey() {
    return key;
  }

  public Predicate<Run> predicate() {
    return key.create().apply(value);
  }
}
