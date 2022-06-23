package ca.on.oicr.gsi.dimsum.data;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Run {

  private final String name;

  private Run(Builder builder) {
    this.name = builder.name;
  }

  public String getName() {
    return name;
  }

  public static class Builder {

    private String name;

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Run build() {
      return new Run(this);
    }

  }

}
