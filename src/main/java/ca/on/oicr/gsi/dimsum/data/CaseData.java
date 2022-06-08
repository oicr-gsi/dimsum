package ca.on.oicr.gsi.dimsum.data;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

import java.time.ZonedDateTime;
import java.util.List;

import javax.annotation.concurrent.Immutable;

@Immutable
public class CaseData {

  private final List<Case> cases;
  private final ZonedDateTime timestamp;

  public CaseData(List<Case> cases, ZonedDateTime timestamp) {
    this.cases = unmodifiableList(cases);
    this.timestamp = requireNonNull(timestamp);
  }

  public List<Case> getCases() {
    return cases;
  }

  public ZonedDateTime getTimestamp() {
    return timestamp;
  }

}
