package ca.on.oicr.gsi.dimsum;

import static ca.on.oicr.gsi.dimsum.SecurityUtils.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.DispatcherType;

@Configuration
@Profile("!noauth")
public class SecurityConfiguration {

  private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

  private static final String LOGIN_URL = "/login";

  @Value("${saml.rolesattribute}")
  private String SAML_ROLES_ATTRIBUTE;

  @Value("${saml.roles.internal}")
  private String SAML_ROLE_INTERNAL;

  @Value("${saml.roles.external}")
  private String SAML_ROLE_EXTERNAL;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
        .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
        .requestMatchers("/favicon.ico").permitAll()
        .requestMatchers("/css/**").permitAll()
        .requestMatchers("/js/**").permitAll()
        .requestMatchers("/img/**").permitAll()
        .requestMatchers("/libs/**").permitAll()
        .requestMatchers("/metrics").permitAll()
        .requestMatchers(LOGIN_URL).permitAll()
        .requestMatchers("/rest/external").hasAuthority(AUTHORITY_EXTERNAL)
        .requestMatchers("/rest/internal").hasAuthority(AUTHORITY_INTERNAL)
        .anyRequest().hasAnyAuthority(AUTHORITY_INTERNAL, AUTHORITY_EXTERNAL))
        .exceptionHandling(exceptions -> exceptions.accessDeniedPage("/error"))
        .saml2Login(saml -> saml.loginPage(LOGIN_URL)
            .authenticationManager(new ProviderManager(makeAuthenticationProvider())))
        .saml2Metadata(Customizer.withDefaults())
        .saml2Logout(Customizer.withDefaults());
    return http.build();
  }

  private OpenSaml4AuthenticationProvider makeAuthenticationProvider() {
    OpenSaml4AuthenticationProvider provider = new OpenSaml4AuthenticationProvider();
    provider.setResponseAuthenticationConverter(token -> {
      Saml2Authentication auth = OpenSaml4AuthenticationProvider
          .createDefaultResponseAuthenticationConverter().convert(token);
      Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) auth.getPrincipal();
      log.debug("Principal {} attributes: {}", principal.getName(), principal.getAttributes());

      List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
      List<Object> roles = principal.getAttributes().get(SAML_ROLES_ATTRIBUTE);
      if (roles != null) {
        for (Object role : roles) {
          if (Objects.equals(role, SAML_ROLE_INTERNAL)) {
            grantedAuthorities.add(new SimpleGrantedAuthority(AUTHORITY_INTERNAL));
          } else if (Objects.equals(role, SAML_ROLE_EXTERNAL)) {
            grantedAuthorities.add(new SimpleGrantedAuthority(AUTHORITY_EXTERNAL));
          }
        }
      }
      log.debug("Authorities mapped: {}", grantedAuthorities);

      return new Saml2Authentication(principal, auth.getSaml2Response(), grantedAuthorities);
    });
    return provider;
  }

}
