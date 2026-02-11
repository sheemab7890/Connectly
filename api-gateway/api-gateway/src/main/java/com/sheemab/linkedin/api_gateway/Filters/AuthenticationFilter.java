package com.sheemab.linkedin.api_gateway.Filters;

import com.sheemab.linkedin.api_gateway.Service.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtService jwtService;

    public AuthenticationFilter(JwtService jwtService) {
        super(Config.class);
        this.jwtService = jwtService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            log.info("Login request: {}", exchange.getRequest().getURI());

            final String tokenHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (tokenHeader == null || !tokenHeader.startsWith("Bearer")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                log.info("Authorization token header not found");
                return exchange.getResponse().setComplete();
            }

            final String token = tokenHeader.split("Bearer ")[1];

            try {
                String userId = jwtService.getUserIdFromToken(token);

                /*
                 * ServerWebExchange is a wrapper around both the incoming HTTP request
                 * and the outgoing HTTP response, along with other relevant information
                 * like cookies, attributes, and session data, giving you a centralized way
                 * to work with them.
                 */
                ServerWebExchange modifiedExchange = exchange
                        .mutate() // This allows you to modify properties of the exchange
                        .request(r -> r.header("X-User-Id", userId)) // Adding a new HTTP header "X-User-Id" with userId value
                        .build();

                return chain.filter(modifiedExchange); // Ensure you pass modifiedExchange, not the original exchange
            } catch (JwtException e) {
                log.error("JWT Exception: {}", e.getLocalizedMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    public static class Config {
        // Add any configuration-related fields or methods if needed
    }
}
