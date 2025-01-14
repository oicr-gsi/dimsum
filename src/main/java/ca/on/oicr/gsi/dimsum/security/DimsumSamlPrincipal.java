package ca.on.oicr.gsi.dimsum.security;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;

/**
 * Wrapper around {@link Saml2AuthenticatedPrincipal} to preserve data required for SAML2 single
 * signoff while also providing the benefits of {@link DimsumPrincipal}
 */
public class DimsumSamlPrincipal extends DimsumPrincipal implements Saml2AuthenticatedPrincipal {

  private final Saml2AuthenticatedPrincipal samlPrincipal;

  public DimsumSamlPrincipal(Saml2AuthenticatedPrincipal samlPrincipal, String displayName,
      boolean internal, Set<String> projects) {
    super(samlPrincipal.getName(), displayName, internal, projects);
    this.samlPrincipal = samlPrincipal;
  }

  @Override
  public <A> List<A> getAttribute(String name) {
    return samlPrincipal.getAttribute(name);
  }

  @Override
  public Map<String, List<Object>> getAttributes() {
    return samlPrincipal.getAttributes();
  }

  @Override
  public <A> A getFirstAttribute(String name) {
    return samlPrincipal.getFirstAttribute(name);
  }

  @Override
  public String getRelyingPartyRegistrationId() {
    return samlPrincipal.getRelyingPartyRegistrationId();
  }

  @Override
  public List<String> getSessionIndexes() {
    return samlPrincipal.getSessionIndexes();
  }

}
