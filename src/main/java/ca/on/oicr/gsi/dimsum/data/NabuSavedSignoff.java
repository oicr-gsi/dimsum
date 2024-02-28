package ca.on.oicr.gsi.dimsum.data;

import java.time.ZonedDateTime;

public class NabuSavedSignoff extends NabuSignoff {

  private int id;
  private String caseIdentifier;
  private ZonedDateTime created;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getCaseIdentifier() {
    return caseIdentifier;
  }

  public void setCaseIdentifier(String caseIdentifier) {
    this.caseIdentifier = caseIdentifier;
  }

  public ZonedDateTime getCreated() {
    return created;
  }

  public void setCreated(ZonedDateTime created) {
    this.created = created;
  }

}
