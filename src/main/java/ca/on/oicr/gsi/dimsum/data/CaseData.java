package ca.on.oicr.gsi.dimsum.data;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class CaseData {

  private List<Case> cases;
  private LocalDateTime timestamp;

  public CaseData(List<Case> cases, LocalDateTime timestamp) {
    this.cases = Collections.unmodifiableList(cases);
    this.timestamp = timestamp;
  }

  public List<Case> getCases() {
    return cases;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

}
