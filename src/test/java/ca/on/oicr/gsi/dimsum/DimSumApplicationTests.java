package ca.on.oicr.gsi.dimsum;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"noauth", "test"})
class DimSumApplicationTests {

	@Test
	void contextLoads() {
	}

}
