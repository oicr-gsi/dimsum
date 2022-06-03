package ca.on.oicr.gsi.dimsum.data;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

public class CaseData {

  private List<Case> cases;
  private ZonedDateTime timestamp;

  public CaseData(List<Case> cases, ZonedDateTime timestamp) {
    this.cases = Collections.unmodifiableList(cases);
    this.timestamp = timestamp;
  }

  public List<Case> getCases() {
    return cases;
  }

  public ZonedDateTime getTimestamp() {
    return timestamp;
  }

}
