package ca.on.oicr.gsi.dimsum.data;

import java.util.List;

public class NabuBulkSignoff extends NabuSignoff {

  private List<String> caseIdentifiers;

  public List<String> getCaseIdentifiers() {
    return caseIdentifiers;
  }

  public void setCaseIdentifiers(List<String> caseIdentifiers) {
    this.caseIdentifiers = caseIdentifiers;
  }

}
