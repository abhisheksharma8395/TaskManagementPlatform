package com.taskmanagement.app.notificationservice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Disabled("Requires full environment")
@SpringBootTest
@ActiveProfiles("local")
class NotificationServiceApplicationTests {
    @Test
    void contextLoads() {
    }
}
