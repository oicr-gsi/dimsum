package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import javax.annotation.concurrent.Immutable;

@Immutable
public class Run {

  private final String name;

  private Run(Builder builder) {
    this.name = requireNonNull(builder.name);
  }

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Run other = (Run) obj;
    return Objects.equals(name, other.name);
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
