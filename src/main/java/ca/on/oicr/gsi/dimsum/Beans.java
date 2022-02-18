package ca.on.oicr.gsi.dimsum;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Beans {

  @Bean
  public LayoutDialect layoutDialect() {
    return new LayoutDialect();
  }

}
