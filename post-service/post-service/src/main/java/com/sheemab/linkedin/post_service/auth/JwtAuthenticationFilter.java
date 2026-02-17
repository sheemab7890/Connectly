package com.sheemab.linkedin.post_service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // DEBUG LOG
//        System.out.println("===== POST-SERVICE FILTER =====");
//        System.out.println("Authorization header: " + request.getHeader("Authorization"));
//        System.out.println("Request URI: " + request.getRequestURI());

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7); // get the token from header

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.getSubject(); // get userId

            //List<String> roles = claims.get("role", List.class);

//            List<GrantedAuthority> authorities =
//                    roles.stream()
//                            .map(role -> (GrantedAuthority)
//                                    new SimpleGrantedAuthority("ROLE_" + role))
//                            .toList();

            // If you don't have roles, just use an empty list
            List<GrantedAuthority> authorities = Collections.emptyList();

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userId,  // 👈 This becomes principal
                            token,  // Store token as credentials
                            authorities
                    );

            SecurityContextHolder.getContext()
                    .setAuthentication(auth); //set user in security context holder

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}

