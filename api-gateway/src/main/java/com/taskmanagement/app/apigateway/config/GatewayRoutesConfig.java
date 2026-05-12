package com.taskmanagement.app.apigateway.config;

import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.stripPrefix;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouterFunction<ServerResponse> testRoute() {
        return RouterFunctions.route()
                .GET("/test", request -> ServerResponse.ok().body("Gateway is routing!"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        return GatewayRouterFunctions.route("auth-service")
                .route(path("/auth-service/**"),
                        HandlerFunctions.http())
                .filter(stripPrefix(1))
                .filter(LoadBalancerFilterFunctions.lb("AUTH-SERVICE"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        "auth-service",
                        URI.create("forward:/fallback/auth-service")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> workspaceServiceRoute() {
        return GatewayRouterFunctions.route("workspace-service")
                .route(path("/workspace-service/**"),
                        HandlerFunctions.http())
                .filter(stripPrefix(1))
                .filter(LoadBalancerFilterFunctions.lb("WORKSPACE-SERVICE"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        "workspace-service",
                        URI.create("forward:/fallback/workspace-service")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> boardServiceRoute() {
        return GatewayRouterFunctions.route("board-service")
                .route(path("/board-service/**"),
                        HandlerFunctions.http())
                .filter(stripPrefix(1))
                .filter(LoadBalancerFilterFunctions.lb("BOARD-SERVICE"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        "board-service",
                        URI.create("forward:/fallback/board-service")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> cardServiceRoute() {
        return GatewayRouterFunctions.route("card-service")
                .route(path("/card-service/**"),
                        HandlerFunctions.http())
                .filter(stripPrefix(1))
                .filter(LoadBalancerFilterFunctions.lb("CARD-SERVICE"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        "card-service",
                        URI.create("forward:/fallback/card-service")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> listServiceRoute() {
        return GatewayRouterFunctions.route("list-service")
                .route(path("/list-service/**"),
                        HandlerFunctions.http())
                .filter(stripPrefix(1))
                .filter(LoadBalancerFilterFunctions.lb("LIST-SERVICE"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        "list-service",
                        URI.create("forward:/fallback/list-service")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> commentServiceRoute() {
        return GatewayRouterFunctions.route("comment-service")
                .route(path("/comment-service/**"),
                        HandlerFunctions.http())
                .filter(stripPrefix(1))
                .filter(LoadBalancerFilterFunctions.lb("COMMENT-SERVICE"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        "comment-service",
                        URI.create("forward:/fallback/comment-service")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> labelServiceRoute() {
        return GatewayRouterFunctions.route("label-service")
                .route(path("/label-service/**"),
                        HandlerFunctions.http())
                .filter(stripPrefix(1))
                .filter(LoadBalancerFilterFunctions.lb("LABEL-SERVICE"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        "label-service",
                        URI.create("forward:/fallback/label-service")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> notificationServiceRoute() {
        return GatewayRouterFunctions.route("notification-service")
                .route(path("/notification-service/**"),
                        HandlerFunctions.http())
                .filter(stripPrefix(1))
                .filter(LoadBalancerFilterFunctions.lb("NOTIFICATION-SERVICE"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        "notification-service",
                        URI.create("forward:/fallback/notification-service")))
                .build();
    }
}