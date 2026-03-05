package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import ca.on.oicr.gsi.cardea.data.Run;
import ca.on.oicr.gsi.cardea.data.Sample;

/**
 * Immutable RunAndLibraries
 */
public class RunAndLibraries {

  private final Set<SampleAndRelated> fullDepthSequencings;
  private final Set<SampleAndRelated> libraryQualifications;
  private final Run run;

  private RunAndLibraries(Builder builder) {
    this.run = requireNonNull(builder.run);
    this.libraryQualifications = builder.libraryQualifications.values().stream()
        .map(SampleAndRelated.Builder::build)
        .collect(Collectors.toUnmodifiableSet());
    this.fullDepthSequencings = builder.fullDepthSequencings.values().stream()
        .map(SampleAndRelated.Builder::build)
        .collect(Collectors.toUnmodifiableSet());
  }

  public Set<SampleAndRelated> getFullDepthSequencings() {
    return fullDepthSequencings;
  }

  public Set<SampleAndRelated> getLibraryQualifications() {
    return libraryQualifications;
  }

  public Run getRun() {
    return run;
  }

  public static class Builder {

    private Map<String, SampleAndRelated.Builder> fullDepthSequencings = new HashMap<>();
    private Map<String, SampleAndRelated.Builder> libraryQualifications = new HashMap<>();
    private Run run;

    public Builder addFullDepthSequencing(Sample sample, Collection<Sample> relatedSamples) {
      if (fullDepthSequencings.containsKey(sample.getId())) {
        fullDepthSequencings.get(sample.getId()).addRelatedSamples(relatedSamples);
      } else {
        fullDepthSequencings.put(sample.getId(),
            new SampleAndRelated.Builder(sample, relatedSamples));
      }
      return this;
    }

    public Builder addLibraryQualification(Sample sample, Collection<Sample> relatedSamples) {
      if (libraryQualifications.containsKey(sample.getId())) {
        libraryQualifications.get(sample.getId()).addRelatedSamples(relatedSamples);
      } else {
        libraryQualifications.put(sample.getId(),
            new SampleAndRelated.Builder(sample, relatedSamples));
      }
      return this;
    }

    public RunAndLibraries build() {
      return new RunAndLibraries(this);
    }

    public Builder run(Run run) {
      this.run = run;
      return this;
    }

  }

}
