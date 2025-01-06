package ca.on.oicr.gsi.dimsum.data.external;

import ca.on.oicr.gsi.cardea.data.Assay;

public class ExternalAssay {

  private final long id;
  private final String name;
  private final String version;
  private final String description;

  public ExternalAssay(Assay from) {
    id = from.getId();
    name = from.getName();
    version = from.getVersion();
    description = from.getDescription();
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getVersion() {
    return version;
  }

  public String getDescription() {
    return description;
  }

}
