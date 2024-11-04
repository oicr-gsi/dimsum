package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import ca.on.oicr.gsi.cardea.data.OmittedRunSample;
import ca.on.oicr.gsi.cardea.data.Run;
import ca.on.oicr.gsi.cardea.data.Sample;

/**
 * Immutable RunAndLibraries
 */
public class RunAndLibraries {

  private final Set<Sample> fullDepthSequencings;
  private final Set<Sample> libraryQualifications;
  private final Set<OmittedRunSample> omittedSamples;
  private final Run run;

  private RunAndLibraries(Builder builder) {
    this.run = requireNonNull(builder.run);
    this.libraryQualifications = Collections.unmodifiableSet(builder.libraryQualifications);
    this.fullDepthSequencings = Collections.unmodifiableSet(builder.fullDepthSequencings);
    this.omittedSamples = Collections.unmodifiableSet(builder.omittedSamples);
  }

  public Set<Sample> getFullDepthSequencings() {
    return fullDepthSequencings;
  }

  public Set<Sample> getLibraryQualifications() {
    return libraryQualifications;
  }

  public Set<OmittedRunSample> getOmittedSamples() {
    return omittedSamples;
  }

  public Run getRun() {
    return run;
  }

  public static class Builder {

    private Set<Sample> fullDepthSequencings = new HashSet<>();
    private Set<Sample> libraryQualifications = new HashSet<>();
    private Set<OmittedRunSample> omittedSamples = new HashSet<>();
    private Run run;

    public Builder addFullDepthSequencing(Sample sample) {
      fullDepthSequencings.add(sample);
      return this;
    }

    public Builder addLibraryQualification(Sample sample) {
      libraryQualifications.add(sample);
      return this;
    }

    public Builder addOmittedSample(OmittedRunSample sample) {
      omittedSamples.add(sample);
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
