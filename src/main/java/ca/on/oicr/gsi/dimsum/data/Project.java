package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Project {

  private final String name;
  private final String pipeline;

  private Project(Builder builder) {
    this.name = requireNonNull(builder.name);
    this.pipeline = requireNonNull(builder.pipeline);
  }

  public String getName() {
    return name;
  }

  public String getPipeline() {
    return pipeline;
  }

  public static class Builder {

    private String name;
    private String pipeline;

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder pipeline(String pipeline) {
      this.pipeline = pipeline;
      return this;
    }

    public Project build() {
      return new Project(this);
    }

  }
}
