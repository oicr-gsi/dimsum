package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.concurrent.Immutable;

@Immutable
public class RunAndLibraries {

  private Run run;
  private Set<Sample> libraryQualifications;
  private Set<Sample> fullDepthSequencings;

  private RunAndLibraries(Builder builder) {
    this.run = requireNonNull(builder.run);
    this.libraryQualifications = Collections.unmodifiableSet(builder.libraryQualifications);
    this.fullDepthSequencings = Collections.unmodifiableSet(builder.fullDepthSequencings);
  }

  public Run getRun() {
    return run;
  }

  public Set<Sample> getLibraryQualifications() {
    return libraryQualifications;
  }

  public Set<Sample> getFullDepthSequencings() {
    return fullDepthSequencings;
  }

  public static class Builder {

    private Run run;
    private Set<Sample> libraryQualifications = new HashSet<>();
    private Set<Sample> fullDepthSequencings = new HashSet<>();

    public Builder run(Run run) {
      this.run = run;
      return this;
    }

    public Builder addLibraryQualification(Sample sample) {
      libraryQualifications.add(sample);
      return this;
    }

    public Builder addFullDepthSequencing(Sample sample) {
      fullDepthSequencings.add(sample);
      return this;
    }

    public RunAndLibraries build() {
      return new RunAndLibraries(this);
    }

  }

}
