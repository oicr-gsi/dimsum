package ca.on.oicr.gsi.dimsum.data;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.concurrent.Immutable;

@Immutable
public class CaseData {

  private final List<Case> cases;
  private final Map<String, RunAndLibraries> runsByName;
  private final ZonedDateTime timestamp;
  private final Set<String> assayNames;
  private final Set<String> requisitionNames;
  private final Set<String> projectsNames;
  private final Set<String> donorNames;



  public CaseData(List<Case> cases, Map<String, RunAndLibraries> runsByName,
      ZonedDateTime timestamp, Set<String> assays, Set<String> requisitions,
      Set<String> projects, Set<String> donors) {
    this.cases = unmodifiableList(cases);
    this.runsByName = Collections.unmodifiableMap(runsByName);
    this.timestamp = requireNonNull(timestamp);
    this.assayNames = Collections.unmodifiableSet(assays);
    this.requisitionNames = Collections.unmodifiableSet(requisitions);
    this.projectsNames = Collections.unmodifiableSet(projects);
    this.donorNames = Collections.unmodifiableSet(donors);
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

  public Set<String> getAssayNames() {
    return assayNames;
  }

  public Set<String> getRequisitionNames() {
    return requisitionNames;
  }

  public Set<String> getProjectNames() {
    return projectsNames;
  }

  public Set<String> getDonorNames() {
    return donorNames;
  }

}
