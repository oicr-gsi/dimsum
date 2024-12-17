package ca.on.oicr.gsi.dimsum;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ca.on.oicr.gsi.cardea.data.Assay;
import ca.on.oicr.gsi.cardea.data.CaseQc;
import ca.on.oicr.gsi.cardea.data.CaseQc.AnalysisReviewQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseQc.ReleaseApprovalQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseQc.ReleaseQcStatus;
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

  private final Map<String, ObjectNode> analysisReviewQcStatuses;
  private final Map<String, ObjectNode> releaseApprovalQcStatuses;
  private final Map<String, ObjectNode> releaseQcStatuses;

  private Set<String> pipelines;
  private Map<Long, Assay> assaysById;
  private Set<String> libraryDesigns;
  private Set<String> deliverables;

  public FrontEndConfig(ObjectMapper objectMapper) {
    this.analysisReviewQcStatuses =
        mapCaseQcs(AnalysisReviewQcStatus.values(), AnalysisReviewQcStatus::name, objectMapper);
    this.releaseApprovalQcStatuses =
        mapCaseQcs(ReleaseApprovalQcStatus.values(), ReleaseApprovalQcStatus::name, objectMapper);
    this.releaseQcStatuses =
        mapCaseQcs(ReleaseQcStatus.values(), ReleaseQcStatus::name, objectMapper);
  }

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

  public List<String> getCompletedGates() {
    return completedGates;
  }

  public Map<String, ObjectNode> getAnalysisReviewQcStatuses() {
    return analysisReviewQcStatuses;
  }

  public Map<String, ObjectNode> getReleaseApprovalQcStatuses() {
    return releaseApprovalQcStatuses;
  }

  public Map<String, ObjectNode> getReleaseQcStatuses() {
    return releaseQcStatuses;
  }

  public void setLibraryDesigns(Set<String> libraryDesigns) {
    this.libraryDesigns = libraryDesigns;
  }

  public Set<String> getLibraryDesigns() {
    return libraryDesigns;
  }

  public Set<String> getDeliverables() {
    return deliverables;
  }

  public void setDeliverables(Set<String> deliverables) {
    this.deliverables = deliverables;
  }

  private ObjectNode toDto(ObjectMapper objectMapper, String name, CaseQc qc) {
    ObjectNode node = objectMapper.createObjectNode();
    node.put("name", name);
    node.put("label", qc.getLabel());
    node.put("qcPassed", qc.getQcPassed());
    node.put("release", qc.getRelease());
    return node;
  }

  private <T extends CaseQc> Map<String, ObjectNode> mapCaseQcs(T[] values,
      Function<T, String> getName, ObjectMapper mapper) {
    Map<String, ObjectNode> map = new TreeMap<>();
    for (T value : values) {
      String name = getName.apply(value);
      map.put(name, toDto(mapper, name, value));
    }
    return map;
  }

}
