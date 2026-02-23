package com.sheemab.linkedin.api_gateway.Filters;

import com.sheemab.linkedin.api_gateway.Service.JwtService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;


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

            String path = exchange.getRequest().getURI().getPath();
            String method = exchange.getRequest().getMethod().name();

            log.info("Incoming request: {} {}", method, path);

            String token = null;

            // 1️⃣ Try Authorization header
            String header = exchange.getRequest()
                    .getHeaders()
                    .getFirst("Authorization");

            if (header != null && header.startsWith("Bearer ")) {
                token = header.substring(7);
            }

            // 2️⃣ If not found, try cookie
            if (token == null) {
                List<HttpCookie> cookies =
                        exchange.getRequest().getCookies().get("access_token");

                if (cookies != null && !cookies.isEmpty()) {
                    token = cookies.get(0).getValue();
                }
            }

            if (token == null) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            try {
                jwtService.validate(token);

                log.info("JWT validation successful for request: {} {}", method, path);

                // ADD THIS: Forward the Authorization header to downstream service
                ServerHttpRequest modifiedRequest = exchange.getRequest()
                        .mutate()
                        .header("Authorization", "Bearer " + token)
                        .build();

                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(modifiedRequest)
                        .build();

                return chain.filter(modifiedExchange);

            } catch (Exception e) {

                log.error("JWT validation failed for {} {}. Reason: {}",
                        method, path, e.getMessage());

                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }


    public static class Config {
        // Add any configuration-related fields or methods if needed
    }

}
