package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Project {

  private final String name;

  public Project(String name) {
    this.name = requireNonNull(name);
  }

  public String getName() {
    return name;
  }
}
