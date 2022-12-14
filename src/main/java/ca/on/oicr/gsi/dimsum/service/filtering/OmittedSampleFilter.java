package ca.on.oicr.gsi.dimsum.service.filtering;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;
import ca.on.oicr.gsi.dimsum.data.OmittedSample;

public class OmittedSampleFilter {

  private final OmittedSampleFilterKey key;
  private final String value;

  public OmittedSampleFilter(OmittedSampleFilterKey key, String value) {
    requireNonNull(key);
    requireNonNull(value);
    this.key = key;
    this.value = value;
  }

  public OmittedSampleFilterKey getKey() {
    return key;
  }

  public Predicate<OmittedSample> predicate() {
    return key.create().apply(value);
  }

}
