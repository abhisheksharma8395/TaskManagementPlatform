package com.taskmanagement.app.authservice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Context loads smoke test.
 * 
 * NOTE: This test requires full environment configuration (GOOGLE_CLIENT_ID,
 * GOOGLE_CLIENT_SECRET,
 * database connection etc.) to pass. It is disabled in the standard test run.
 * Use the service and controller layer unit tests for CI coverage.
 */
@SpringBootTest
@Disabled("Requires full environment: GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, PostgreSQL. Run integration tests instead.")
class AuthServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
