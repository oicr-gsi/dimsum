package ca.on.oicr.gsi.dimsum;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

  public static final String AUTHORITY_INTERNAL = "ROLE_INTERNAL";
  public static final String AUTHORITY_EXTERNAL = "ROLE_EXTERNAL";

  @Autowired
  private Environment environment;

  @Value("${saml.firstnameattribute:#{null}}")
  private String samlFirstNameAttribute;

  @Value("${saml.lastnameattribute:#{null}}")
  private String samlLastNameAttribute;

  @Value("${saml.projectsattribute:#{null}}")
  private String samlProjectsAttribute;

  public boolean authenticationDisabled() {
    String[] profiles = environment.getActiveProfiles();
    if (profiles == null) {
      return false;
    }
    for (String profile : profiles) {
      if ("noauth".equals(profile)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isAuthenticated() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth != null && auth.isAuthenticated()
        && !(auth instanceof AnonymousAuthenticationToken);
  }

  public String getUserFullname() {
    if (authenticationDisabled()) {
      return "Unauthenticated";
    } else {
      Saml2AuthenticatedPrincipal principal = getSamlPrincipal();
      return principal.getFirstAttribute(samlFirstNameAttribute) + " "
          + principal.getFirstAttribute(samlLastNameAttribute);
    }
  }

  public boolean isInternalUser() {
    if (authenticationDisabled()) {
      return true;
    } else if (!isAuthenticated()) {
      return false;
    } else {
      return getAuthorities().contains(AUTHORITY_INTERNAL);
    }
  }

  public Set<String> getUserProjects() {
    List<Object> projectValues = getSamlPrincipal().getAttribute(samlProjectsAttribute);
    if (projectValues == null) {
      return Collections.emptySet();
    }
    return projectValues.stream()
        .map(String.class::cast)
        .collect(Collectors.toSet());
  }

  private static Saml2AuthenticatedPrincipal getSamlPrincipal() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof Saml2AuthenticatedPrincipal) {
      return (Saml2AuthenticatedPrincipal) principal;
    } else {
      throw new IllegalStateException(
          "Unexpected principal class: " + principal.getClass().getName());
    }
  }

  private static Set<String> getAuthorities() {
    return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
        .map(authority -> authority.getAuthority()).collect(Collectors.toSet());
  }

}
