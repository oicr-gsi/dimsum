package ca.on.oicr.gsi.dimsum.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * This class can be autowired into other components to simplify retrieval of authentication objects
 * and prevent dependence on static state
 */
@Component
public class SecurityManager {

  public DimsumPrincipal getPrincipal() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth.getPrincipal() instanceof DimsumPrincipal dimsumPrincipal) {
      return dimsumPrincipal;
    } else {
      return null;
    }
  }

}
