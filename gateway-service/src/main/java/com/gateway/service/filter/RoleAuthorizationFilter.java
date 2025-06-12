package com.gateway.service.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Filter role-based authorization for API Gateway
 * This filter checks if the user has the required roles to access a resource.
 */
@Component
public class RoleAuthorizationFilter extends AbstractGatewayFilterFactory<RoleAuthorizationFilter.Config> {
    public RoleAuthorizationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String rolesHeader = request.getHeaders().getFirst("X-User-Roles");
            if (rolesHeader == null) { return unauthorizedResponse(exchange); }
            List<String> userRoles = Arrays.asList(rolesHeader.split(","));
            boolean hasRequiredRole = false;
            for (String requiredRole : config.getRoles()) {
                for (String userRole : userRoles) {
                    String normalizedRequiredRole = requiredRole.trim().replace("ROLE_", "");
                    String normalizedUserRole = userRole.trim().replace("ROLE_", "");
                    if (normalizedRequiredRole.equalsIgnoreCase(normalizedUserRole)) {
                        hasRequiredRole = true;
                        break;
                    }
                }
                if (hasRequiredRole) { break; }
            }
            if (!hasRequiredRole) { return unauthorizedResponse(exchange); }
            return chain.filter(exchange);
        };
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    public static class Config {
        private List<String> roles;

        public List<String> getRoles() { return roles; }

        public void setRoles(List<String> roles) { this.roles = roles; }

        @Override
        public String toString() {
            return "Config{roles=" + roles + "}";
        }
    }
}
