package ca.on.oicr.gsi.dimsum.data;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

@Immutable
public class CaseData {

  private final List<Case> cases;
  private final Map<String, RunAndLibraries> runsByName;
  private final ZonedDateTime timestamp;

  public CaseData(List<Case> cases, Map<String, RunAndLibraries> runsByName,
      ZonedDateTime timestamp) {
    this.cases = unmodifiableList(cases);
    this.runsByName = Collections.unmodifiableMap(runsByName);
    this.timestamp = requireNonNull(timestamp);
  }

  public List<Case> getCases() {
    return cases;
  }

  public RunAndLibraries getRunAndLibraries(String runName) {
    return runsByName.get(runName);
  }

  public ZonedDateTime getTimestamp() {
    return timestamp;
  }

}
