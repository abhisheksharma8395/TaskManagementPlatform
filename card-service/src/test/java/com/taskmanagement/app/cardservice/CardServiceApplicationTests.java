package com.taskmanagement.app.cardservice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Disabled("Requires full environment")
@SpringBootTest
@ActiveProfiles("local")
class CardServiceApplicationTests {
    @Test
    void contextLoads() {
        // Verifies Spring context starts without errors
    }
}
