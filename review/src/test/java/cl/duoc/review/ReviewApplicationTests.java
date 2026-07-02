package cl.duoc.review;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;

class ReviewApplicationTests {

	@Test
	void mainMethodExists() {
		assertDoesNotThrow(() -> ReviewApplication.class.getDeclaredMethod("main", String[].class));
	}

}
