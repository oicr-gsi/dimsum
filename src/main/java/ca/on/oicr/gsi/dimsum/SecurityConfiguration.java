package ca.on.oicr.gsi.dimsum;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.saml2.provider.service.metadata.OpenSamlMetadataResolver;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.Saml2MetadataFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("!noauth")
public class SecurityConfiguration {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .antMatchers("/favicon.ico").permitAll()
            .antMatchers("/css/**").permitAll()
            .antMatchers("/js/**").permitAll()
            .antMatchers("/login").permitAll()
            .anyRequest().authenticated())
        .saml2Login().loginPage("/login");
    return http.build();
  }

  @Bean
  public RelyingPartyRegistrationResolver relyingPartyRegistrationResolver(RelyingPartyRegistrationRepository registrations) {
    return new DefaultRelyingPartyRegistrationResolver(registrations);
  }

  /**
   * Add filter to publish SAML service provider metadata at the default location:
   * /saml2/service-provider-metadata/{registrationId}
   *
   * @param registrations
   * @return
   */
  @Bean
  FilterRegistrationBean<Saml2MetadataFilter> metadata(RelyingPartyRegistrationResolver registrations) {
    Saml2MetadataFilter metadata = new Saml2MetadataFilter(registrations, new OpenSamlMetadataResolver());
    FilterRegistrationBean<Saml2MetadataFilter> filter = new FilterRegistrationBean<>(metadata);
    filter.setOrder(-101);
    return filter;
  }

}
