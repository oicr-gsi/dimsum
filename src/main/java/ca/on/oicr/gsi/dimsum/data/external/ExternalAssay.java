package ca.on.oicr.gsi.dimsum.data.external;

import ca.on.oicr.gsi.cardea.data.Assay;

public record ExternalAssay(long id, String name, String version, String description) {

  public ExternalAssay(Assay from) {
    this(from.getId(), from.getName(), from.getVersion(), from.getDescription());
  }

}
