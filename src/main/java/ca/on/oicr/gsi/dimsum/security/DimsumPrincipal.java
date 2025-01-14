package ca.on.oicr.gsi.dimsum.security;

import java.util.Collections;
import java.util.Set;
import org.springframework.security.core.AuthenticatedPrincipal;

/**
 * Principal that contains all authorization data relevant to Dimsum for convenient access
 */
public class DimsumPrincipal implements AuthenticatedPrincipal {

  private final String name;
  private final String displayName;
  private final boolean internal;
  private final Set<String> projects;

  public DimsumPrincipal(String name, String displayName, boolean internal, Set<String> projects) {
    this.name = name;
    this.displayName = displayName;
    this.internal = internal;
    this.projects = Collections.unmodifiableSet(projects);
  }

  @Override
  public String getName() {
    return name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public boolean isInternal() {
    return internal;
  }

  public Set<String> getProjects() {
    return projects;
  }

}
