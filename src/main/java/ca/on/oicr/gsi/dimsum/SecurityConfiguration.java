package ca.on.oicr.gsi.dimsum;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.metadata.OpenSaml4MetadataResolver;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.Saml2MetadataFilter;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.DispatcherType;

@Configuration
@Profile("!noauth")
public class SecurityConfiguration {

  private static final String LOGIN_URL = "/login";

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
        .anyRequest().authenticated())
        .saml2Login(saml -> saml.loginPage(LOGIN_URL))
        .saml2Logout(Customizer.withDefaults());
    return http.build();
  }

  @Bean
  public RelyingPartyRegistrationRepository repository(
      @Value("${saml.idpmetadataurl}") String metadataUrl, @Value("${baseurl}") String baseUrl,
      @Value("file://${saml.spkey}") RSAPrivateKey key,
      @Value("${saml.spcert}") File certificateFile) {
    Saml2X509Credential credential =
        Saml2X509Credential.signing(key, getCertificate(certificateFile));
    String logoutUrl = baseUrl + "/logout/saml2/slo";
    RelyingPartyRegistration registration =
        RelyingPartyRegistrations.fromMetadataLocation(metadataUrl)
            .registrationId("dimsum")
            .entityId(baseUrl + "/saml2/service-provider-metadata/dimsum")
            .assertionConsumerServiceLocation(baseUrl + "/login/saml2/sso/dimsum")
            .singleLogoutServiceLocation(logoutUrl)
            .signingX509Credentials(saml2X509Credentials -> saml2X509Credentials.add(credential))
            .build();
    return new InMemoryRelyingPartyRegistrationRepository(registration);
  }

  private X509Certificate getCertificate(File certificateFile) {
    try (InputStream input = new FileInputStream(certificateFile)) {
      return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(input);
    } catch (Exception e) {
      throw new RuntimeException("Failed to read SP certificate");
    }
  }

  @Bean
  public RelyingPartyRegistrationResolver relyingPartyRegistrationResolver(
      RelyingPartyRegistrationRepository registrations) {
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
  FilterRegistrationBean<Saml2MetadataFilter> metadata(
      RelyingPartyRegistrationResolver registrations) {
    Saml2MetadataFilter metadata =
        new Saml2MetadataFilter(registrations, new OpenSaml4MetadataResolver());
    FilterRegistrationBean<Saml2MetadataFilter> filter = new FilterRegistrationBean<>(metadata);
    filter.setOrder(-101);
    return filter;
  }

}
