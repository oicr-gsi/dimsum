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
  private final Map<Long, Assay> assaysById;
  private final ZonedDateTime timestamp;
  private final Set<String> assayNames;
  private final Set<String> requisitionNames;
  private final Set<String> projectsNames;
  private final Set<String> donorNames;
  private final Set<String> runNames;



  public CaseData(List<Case> cases, Map<String, RunAndLibraries> runsByName,
      Map<Long, Assay> assaysById, ZonedDateTime timestamp, Set<String> assays,
      Set<String> requisitions, Set<String> projects, Set<String> donors, Set<String> runs) {
    this.cases = unmodifiableList(cases);
    this.runsByName = Collections.unmodifiableMap(runsByName);
    this.assaysById = Collections.unmodifiableMap(assaysById);
    this.timestamp = requireNonNull(timestamp);
    this.assayNames = Collections.unmodifiableSet(assays);
    this.requisitionNames = Collections.unmodifiableSet(requisitions);
    this.projectsNames = Collections.unmodifiableSet(projects);
    this.donorNames = Collections.unmodifiableSet(donors);
    this.runNames = Collections.unmodifiableSet(runs);
  }

  public List<Case> getCases() {
    return cases;
  }

  public RunAndLibraries getRunAndLibraries(String runName) {
    return runsByName.get(runName);
  }

  public Map<String, RunAndLibraries> getRunsAndLibrariesByName() {
    return runsByName;
  }

  public Map<Long, Assay> getAssaysById() {
    return assaysById;
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

  public Set<String> getRunNames() {
    return runNames;
  }
}
