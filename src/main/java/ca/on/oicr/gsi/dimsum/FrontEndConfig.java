package ca.on.oicr.gsi.dimsum;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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

  private final List<String> pendingStates =
      Stream.of(PendingState.values()).map(PendingState::getLabel).toList();

  private Set<String> pipelines;

  public String getMisoUrl() {
    return misoUrl;
  }

  public String getDashiUrl() {
    return dashiUrl;
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

}
