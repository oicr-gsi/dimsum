package ca.on.oicr.gsi.dimsum;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ca.on.oicr.gsi.cardea.data.Assay;
import ca.on.oicr.gsi.cardea.data.CaseQc;
import ca.on.oicr.gsi.cardea.data.CaseQc.AnalysisReviewQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseQc.ReleaseApprovalQcStatus;
import ca.on.oicr.gsi.cardea.data.CaseQc.ReleaseQcStatus;
import ca.on.oicr.gsi.dimsum.data.external.ExternalAssay;
import ca.on.oicr.gsi.dimsum.security.DimsumPrincipal;
import ca.on.oicr.gsi.dimsum.security.SecurityManager;
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

  @Autowired
  private SecurityManager securityManager;

  private final Map<String, ObjectNode> analysisReviewQcStatuses;
  private final Map<String, ObjectNode> releaseApprovalQcStatuses;
  private final Map<String, ObjectNode> releaseQcStatuses;

  private Set<String> pipelines;
  private Map<Long, Assay> internalAssaysById;
  private Map<Long, ExternalAssay> externalAssaysById;
  private Set<String> libraryDesigns;
  private Set<String> deliverableCategories;
  private Set<String> deliverables;

  private List<String> pendingStates =
      Stream.of(PendingState.values()).map(PendingState::getLabel).toList();

  private List<String> completedGates =
      Stream.of(CompletedGate.values()).map(CompletedGate::getLabel).toList();

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

  public Map<Long, ?> getAssaysById() {
    DimsumPrincipal principal = securityManager.getPrincipal();
    if (principal != null && principal.isInternal()) {
      return internalAssaysById;
    }
    return externalAssaysById;
  }

  public void setAssaysById(Map<Long, Assay> assaysById) {
    this.internalAssaysById = assaysById;
    this.externalAssaysById = assaysById.entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, entry -> new ExternalAssay(entry.getValue())));
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

  @JsonIgnore
  public Set<String> getDeliverableCategories() {
    return deliverableCategories;
  }

  public void setDeliverableCategories(Set<String> deliverableCategories) {
    this.deliverableCategories = deliverableCategories;
    completedGates = Stream.of(CompletedGate.values())
        .flatMap(gate -> {
          Stream<String> stream = Stream.of(gate.getLabel());
          if (gate.considerDeliverableCategory()) {
            stream = Stream.concat(stream, deliverableCategories.stream()
                .map(category -> gate.getLabel() + " - " + category));
          }
          return stream;
        })
        .toList();
    pendingStates = Stream.of(PendingState.values())
        .flatMap(state -> {
          Stream<String> stream = Stream.of(state.getLabel());
          if (state.considerDeliverableCategory()) {
            stream = Stream.concat(stream, deliverableCategories.stream()
                .map(category -> state.getLabel() + " - " + category));
          }
          return stream;
        })
        .toList();
  }

  public Set<String> getDeliverables() {
    return deliverables;
  }

  public void setDeliverables(Set<String> deliverables) {
    this.deliverables = deliverables;
  }

  private static ObjectNode toDto(ObjectMapper objectMapper, String name, CaseQc qc) {
    ObjectNode node = objectMapper.createObjectNode();
    node.put("name", name);
    node.put("label", qc.getLabel());
    node.put("qcPassed", qc.getQcPassed());
    node.put("release", qc.getRelease());
    return node;
  }

  private static <T extends CaseQc> Map<String, ObjectNode> mapCaseQcs(T[] values,
      Function<T, String> getName, ObjectMapper mapper) {
    Map<String, ObjectNode> map = new TreeMap<>();
    for (T value : values) {
      String name = getName.apply(value);
      map.put(name, toDto(mapper, name, value));
    }
    return map;
  }

}
