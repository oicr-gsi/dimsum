package ca.on.oicr.gsi.dimsum.data;

import static java.util.Objects.requireNonNull;

import groovy.transform.Immutable;

@Immutable
public class Donor {

  private final String id;
  private final String name;
  private final String externalName;

  private Donor(Builder builder) {
    this.id = requireNonNull(builder.id);
    this.name = requireNonNull(builder.name);
    this.externalName = requireNonNull(builder.externalName);
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getExternalName() {
    return externalName;
  }

  public static class Builder {

    private String id;
    private String name;
    private String externalName;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder externalName(String externalName) {
      this.externalName = externalName;
      return this;
    }

    public Donor build() {
      return new Donor(this);
    }

  }
}
