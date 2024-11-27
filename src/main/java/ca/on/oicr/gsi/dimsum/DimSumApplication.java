package ca.on.oicr.gsi.dimsum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;

@SpringBootApplication
@EnableScheduling
public class DimSumApplication {

	public static void main(String[] args) {
		SpringApplication.run(DimSumApplication.class, args);
	}

	@Bean
	public LayoutDialect layoutDialect() {
		// This is required to support hierarchical-style layouts via Thymeleaf Layout Dialect
		return new LayoutDialect();
	}

}
