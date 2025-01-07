package ca.on.oicr.gsi.dimsum;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
        .saml2Metadata(Customizer.withDefaults())
        .saml2Logout(Customizer.withDefaults());
    return http.build();
  }

}
