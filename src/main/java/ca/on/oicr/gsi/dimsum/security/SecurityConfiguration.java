package ca.on.oicr.gsi.dimsum.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider.ResponseAuthenticationConverter;
import org.springframework.security.saml2.provider.service.authentication.Saml2AssertionAuthentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;
import org.springframework.security.saml2.provider.service.authentication.Saml2ResponseAssertionAccessor;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.DispatcherType;

@Configuration
@Profile("!noauth")
public class SecurityConfiguration {

  private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

  public static final String AUTHORITY_INTERNAL = "ROLE_INTERNAL";
  public static final String AUTHORITY_EXTERNAL = "ROLE_EXTERNAL";
  private static final String LOGIN_URL = "/login";

  @Value("${saml.firstnameattribute}")
  private String samlFirstNameAttribute;

  @Value("${saml.lastnameattribute}")
  private String samlLastNameAttribute;

  @Value("${saml.rolesattribute}")
  private String SAML_ROLES_ATTRIBUTE;

  @Value("${saml.roles.internal}")
  private String SAML_ROLE_INTERNAL;

  @Value("${saml.roles.external}")
  private String SAML_ROLE_EXTERNAL;

  @Value("${saml.projectsattribute}")
  private String samlProjectsAttribute;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return setupCommon(http)
        .saml2Login(saml -> saml.loginPage(LOGIN_URL)
            .authenticationManager(new ProviderManager(makeAuthenticationProvider())))
        .saml2Metadata(Customizer.withDefaults())
        .saml2Logout(Customizer.withDefaults())
        .build();
  }

  public static HttpSecurity setupCommon(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(auth -> auth
        .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
        .requestMatchers("/favicon.ico").permitAll()
        .requestMatchers("/css/**").permitAll()
        .requestMatchers("/js/**").permitAll()
        .requestMatchers("/img/**").permitAll()
        .requestMatchers("/libs/**").permitAll()
        .requestMatchers("/metrics").permitAll()
        .requestMatchers(LOGIN_URL, "/logout").permitAll()
        .requestMatchers("/rest/external/**").hasAuthority(AUTHORITY_EXTERNAL)
        .requestMatchers("/rest/common/**").hasAnyAuthority(AUTHORITY_INTERNAL, AUTHORITY_EXTERNAL)
        .requestMatchers("/", "/cases", "/cases/*", "/projects", "/projects/*",
            "/requisitions/*", "/donors/*")
        .hasAnyAuthority(AUTHORITY_INTERNAL, AUTHORITY_EXTERNAL)
        .anyRequest().hasAuthority(AUTHORITY_INTERNAL))
        .exceptionHandling(exceptions -> exceptions.accessDeniedPage("/error"));
  }

  private OpenSaml5AuthenticationProvider makeAuthenticationProvider() {
    OpenSaml5AuthenticationProvider provider = new OpenSaml5AuthenticationProvider();
    provider.setResponseAuthenticationConverter(token -> {
      Saml2AuthenticationToken authenticationToken = token.getToken();
      RelyingPartyRegistration registration = authenticationToken.getRelyingPartyRegistration();
      Saml2Authentication auth = new ResponseAuthenticationConverter().convert(token);
      Saml2ResponseAssertionAccessor assertionAccessor =
          (Saml2ResponseAssertionAccessor) auth.getCredentials();
      log.debug("Assertion nameId: {}, attributes: {}", assertionAccessor.getNameId(),
          assertionAccessor.getAttributes());

      boolean internal = false;
      List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
      List<Object> roles = assertionAccessor.getAttributes().get(SAML_ROLES_ATTRIBUTE);
      if (roles != null) {
        for (Object role : roles) {
          if (Objects.equals(role, SAML_ROLE_INTERNAL)) {
            grantedAuthorities.add(new SimpleGrantedAuthority(AUTHORITY_INTERNAL));
            internal = true;
          } else if (Objects.equals(role, SAML_ROLE_EXTERNAL)) {
            grantedAuthorities.add(new SimpleGrantedAuthority(AUTHORITY_EXTERNAL));
          }
        }
      }
      log.debug("Authorities mapped: {}", grantedAuthorities);
      DimsumSamlPrincipal dimsumPrincipal = new DimsumSamlPrincipal(assertionAccessor,
          getDisplayName(assertionAccessor), internal, getProjects(assertionAccessor));
      log.debug("DimsumSamlPrincipal name='%s', displayName='%s', internal=%s, projects=%s"
          .formatted(dimsumPrincipal.getName(), dimsumPrincipal.getDisplayName(),
              Boolean.toString(dimsumPrincipal.isInternal()), dimsumPrincipal.getProjects()));
      return new Saml2AssertionAuthentication(dimsumPrincipal, assertionAccessor,
          grantedAuthorities, registration.getRegistrationId());
    });
    return provider;
  }

  private String getDisplayName(Saml2ResponseAssertionAccessor assertionAccessor) {
    String firstName = assertionAccessor.getFirstAttribute(samlFirstNameAttribute);
    String lastName = assertionAccessor.getFirstAttribute(samlLastNameAttribute);
    if (firstName == null || lastName == null) {
      String errorMessage =
          "Null first or last name detected for principal. Attributes may not be configured correctly.";
      // Logging explicitly because the exception was not showing up in logs
      log.error(errorMessage);
      throw new IllegalStateException(errorMessage);
    }
    return firstName + " " + lastName;
  }

  private Set<String> getProjects(Saml2ResponseAssertionAccessor assertionAccessor) {
    List<Object> projectValues = assertionAccessor.getAttribute(samlProjectsAttribute);
    if (projectValues == null) {
      return Collections.emptySet();
    }
    return projectValues.stream()
        .map(String.class::cast)
        .collect(Collectors.toUnmodifiableSet());
  }

}
