package ca.on.oicr.gsi.dimsum.security;

import java.util.Collections;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("noauth")
public class SecurityConfigurationNoAuth {

  @Value("${testuser.external:false}")
  private boolean DEVUSER_EXTERNAL;
  @Value("${testuser.projects:}")
  private Set<String> DEVUSER_PROJECTS;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    String authority = DEVUSER_EXTERNAL ? SecurityConfiguration.AUTHORITY_EXTERNAL
        : SecurityConfiguration.AUTHORITY_INTERNAL;

    return SecurityConfiguration.setupCommon(http)
        .anonymous(anon -> anon.authorities(authority).principal(makePrincipal()))
        .build();
  }

  private DimsumPrincipal makePrincipal() {
    Set<String> projects = DEVUSER_EXTERNAL ? DEVUSER_PROJECTS : Collections.emptySet();
    return new DimsumPrincipal("test", "Test User", !DEVUSER_EXTERNAL, projects);
  }

}
