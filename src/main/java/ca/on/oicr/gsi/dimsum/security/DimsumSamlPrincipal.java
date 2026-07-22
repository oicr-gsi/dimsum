package ca.on.oicr.gsi.dimsum.security;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.security.saml2.provider.service.authentication.Saml2ResponseAssertionAccessor;

/**
 * Wrapper around {@link Saml2ResponseAssertionAccessor} to preserve data required for SAML2 single
 * signoff while also providing the benefits of {@link DimsumPrincipal}
 */
public class DimsumSamlPrincipal extends DimsumPrincipal implements Saml2ResponseAssertionAccessor {

  private final Saml2ResponseAssertionAccessor assertionAccessor;

  public DimsumSamlPrincipal(Saml2ResponseAssertionAccessor assertionAccessor, String displayName,
      boolean internal, Set<String> projects) {
    super(assertionAccessor.getNameId(), displayName, internal, projects);
    this.assertionAccessor = assertionAccessor;
  }

  @Override
  public <A> List<A> getAttribute(String name) {
    return assertionAccessor.getAttribute(name);
  }

  @Override
  public Map<String, List<Object>> getAttributes() {
    return assertionAccessor.getAttributes();
  }

  @Override
  public <A> A getFirstAttribute(String name) {
    return assertionAccessor.getFirstAttribute(name);
  }

  @Override
  public List<String> getSessionIndexes() {
    return assertionAccessor.getSessionIndexes();
  }

  @Override
  public String getNameId() {
    return assertionAccessor.getNameId();
  }

  @Override
  public String getResponseValue() {
    return assertionAccessor.getResponseValue();
  }

}
