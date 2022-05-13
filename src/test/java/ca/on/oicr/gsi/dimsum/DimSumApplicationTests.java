package ca.on.oicr.gsi.dimsum;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {"spring.config.name=test"})
@ActiveProfiles("noauth")
class DimSumApplicationTests {

	@Test
	void contextLoads() {
	}

}
