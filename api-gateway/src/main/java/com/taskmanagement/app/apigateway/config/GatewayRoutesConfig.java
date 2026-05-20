package com.taskmanagement.app.apigateway.config;

import com.taskmanagement.app.apigateway.filter.RateLimiterFilter;
import org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.stripPrefix;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@Configuration
public class GatewayRoutesConfig {

    private final RateLimiterFilter rateLimiterFilter;

    public GatewayRoutesConfig(RateLimiterFilter rateLimiterFilter) {
        this.rateLimiterFilter = rateLimiterFilter;
    }


    @Bean
    public RouterFunction<ServerResponse> testRoute() {
        return RouterFunctions.route()
                .GET("/test", request -> ServerResponse.ok().body("Gateway is routing!"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        return GatewayRouterFunctions.route("auth-service")
                .route(path("/auth-service/**"), HandlerFunctions.http())
                .filter(stripPrefix(1))
                .filter(LoadBalancerFilterFunctions.lb("AUTH-SERVICE"))
                .filter(rateLimiterFilter.toHandlerFilter())
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> workspaceServiceRoute() {
        return GatewayRouterFunctions.route("workspace-service")
                .route(path("/workspace-service/**"), HandlerFunctions.http())
                .filter(stripPrefix(1))
                .filter(LoadBalancerFilterFunctions.lb("WORKSPACE-SERVICE"))
                .filter(rateLimiterFilter.toHandlerFilter())
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> boardServiceRoute() {
        return GatewayRouterFunctions.route("board-service")
                .route(path("/board-service/**"), HandlerFunctions.http())
                .filter(stripPrefix(1))
                .filter(LoadBalancerFilterFunctions.lb("BOARD-SERVICE"))
                .filter(rateLimiterFilter.toHandlerFilter())
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> listServiceRoute() {
        return GatewayRouterFunctions.route("list-service")
                .route(path("/list-service/**"), HandlerFunctions.http())
                .filter(stripPrefix(1))
                .filter(LoadBalancerFilterFunctions.lb("LIST-SERVICE"))
                .filter(rateLimiterFilter.toHandlerFilter())
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> cardServiceRoute() {
        return GatewayRouterFunctions.route("card-service")
                .route(path("/card-service/**"), HandlerFunctions.http())
                .filter(stripPrefix(1))
                .filter(LoadBalancerFilterFunctions.lb("CARD-SERVICE"))
                .filter(rateLimiterFilter.toHandlerFilter())
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> commentServiceRoute() {
        return GatewayRouterFunctions.route("comment-service")
                .route(path("/comment-service/**"), HandlerFunctions.http())
                .filter(stripPrefix(1))
                .filter(LoadBalancerFilterFunctions.lb("COMMENT-SERVICE"))
                .filter(rateLimiterFilter.toHandlerFilter())
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> labelServiceRoute() {
        return GatewayRouterFunctions.route("label-service")
                .route(path("/label-service/**"), HandlerFunctions.http())
                .filter(stripPrefix(1))
                .filter(LoadBalancerFilterFunctions.lb("LABEL-SERVICE"))
                .filter(rateLimiterFilter.toHandlerFilter())
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> notificationServiceRoute() {
        return GatewayRouterFunctions.route("notification-service")
                .route(path("/notification-service/**"), HandlerFunctions.http())
                .filter(stripPrefix(1))
                .filter(LoadBalancerFilterFunctions.lb("NOTIFICATION-SERVICE"))
                .filter(rateLimiterFilter.toHandlerFilter())
                .build();
    }
}