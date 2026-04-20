package com.app.fisiolab_system.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.app.fisiolab_system.service.JwtService;
import com.app.fisiolab_system.service.UserDetailService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Get the Authorization header from the HTTP request
        String authHeader = request.getHeader("Authorization");

        // 2. Validate if the header is present and starts with "Bearer "
        //    If not, continue with the filter chain without authentication
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 3. Extract the JWT token (remove "Bearer " prefix)
            String token = authHeader.substring(7);

            // 4. Extract the username (or email) from the token
            String username = jwtService.extractUsername(token);

            // 5. Check if username exists and no authentication is already set in the context
            if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 6. Load user details from the database using the username
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 7. Validate the token using user details
                if (jwtService.isTokenValid(token, userDetails)) {

                    // 8. Create an authentication token with user details and authorities (roles)
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    // 9. Attach request details (IP, session, etc.) to the authentication object
                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // 10. Set the authentication in the SecurityContext (user is now authenticated)
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
        }

        // 11. Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}