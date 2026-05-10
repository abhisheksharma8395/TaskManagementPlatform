package com.taskmanagement.app.apigateway.filter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class RateLimiterFilter implements Filter {

    private final RateLimiter defaultRateLimiter;
    private final RateLimiter authRateLimiter;

    public RateLimiterFilter(RateLimiter defaultRateLimiter, RateLimiter authRateLimiter) {
        this.defaultRateLimiter = defaultRateLimiter;
        this.authRateLimiter = authRateLimiter;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        RateLimiter limiter = path.startsWith("/auth-service")
                ? authRateLimiter
                : defaultRateLimiter;

        try {
            RateLimiter.waitForPermission(limiter);
            chain.doFilter(request, response);
        } catch (RequestNotPermitted e) {
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("""
                    {
                        "status": 429,
                        "error": "Too Many Requests",
                        "message": "Rate limit exceeded. Please slow down."
                    }
                    """);
        }
    }
}