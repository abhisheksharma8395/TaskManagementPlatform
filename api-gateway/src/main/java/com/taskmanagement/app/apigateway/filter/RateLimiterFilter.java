package com.taskmanagement.app.apigateway.filter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;


@Component
public class RateLimiterFilter {

    private final RateLimiter defaultRateLimiter;
    private final RateLimiter authRateLimiter;

    public RateLimiterFilter(RateLimiter defaultRateLimiter, RateLimiter authRateLimiter) {
        this.defaultRateLimiter = defaultRateLimiter;
        this.authRateLimiter = authRateLimiter;
    }

    public HandlerFilterFunction<ServerResponse, ServerResponse> toHandlerFilter() {
        return (request, next) -> {
            RateLimiter limiter = isAuthPath(request) ? authRateLimiter : defaultRateLimiter;
            try {
                return RateLimiter.decorateCheckedSupplier(limiter, () -> next.handle(request)).get();
            } catch (RequestNotPermitted e) {
                return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                        .header("Retry-After", "1")
                        .body("{\"status\":429,\"error\":\"Too Many Requests\","
                                + "\"message\":\"Rate limit exceeded. Please slow down.\"}");
            } catch (Throwable t) {
                if (t instanceof RuntimeException re) throw re;
                throw new RuntimeException(t);
            }
        };
    }

    private boolean isAuthPath(ServerRequest request) {
        return request.path().startsWith("/auth-service");
    }
}
