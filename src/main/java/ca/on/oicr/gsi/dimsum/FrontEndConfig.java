package ca.on.oicr.gsi.dimsum;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ca.on.oicr.gsi.dimsum.data.Assay;
import ca.on.oicr.gsi.dimsum.service.filtering.CompletedGate;
import ca.on.oicr.gsi.dimsum.service.filtering.PendingState;

/**
 * This class contains configuration needed on the front-end and is served as a JavaScript file by
 * SiteConfigController. Some values are updated by CaseService when new data is loaded
 */
@Component
public class FrontEndConfig {

  @Value("${miso.url}")
  private String misoUrl;

  @Value("${dashi.url}")
  private String dashiUrl;

  @Value("${jira.baseurl:#{null}}")
  private String jiraUrl;

  private final List<String> pendingStates =
      Stream.of(PendingState.values()).map(PendingState::getLabel).toList();

  private final List<String> completedGates =
      Stream.of(CompletedGate.values()).map(CompletedGate::getLabel).toList();

  private final List<String> stopStatus = Arrays.asList("Yes", "No");

  private Set<String> pipelines;
  private Map<Long, Assay> assaysById;

  public String getMisoUrl() {
    return misoUrl;
  }

  public String getDashiUrl() {
    return dashiUrl;
  }

  public String getJiraUrl() {
    return jiraUrl;
  }

  public List<String> getPendingStates() {
    return pendingStates;
  }

  public Set<String> getPipelines() {
    return pipelines;
  }

  public void setPipelines(Set<String> pipelines) {
    this.pipelines = pipelines;
  }

  public Map<Long, Assay> getAssaysById() {
    return assaysById;
  }

  public void setAssaysById(Map<Long, Assay> assaysById) {
    this.assaysById = assaysById;
  }

  public List<String> getStopStatus() {
    return stopStatus;
  }

  public List<String> getCompletedGates() {
    return completedGates;
  }

}
