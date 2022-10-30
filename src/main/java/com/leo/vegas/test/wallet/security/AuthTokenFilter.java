package com.leo.vegas.test.wallet.security;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthTokenFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(AuthTokenFilter.class);

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        /*
          In real production code, jwt token signature/expiry should be verified,
          that step is skipped to reduce the scope; In current code any non-null value is accepted
         */
        try {
            String jwt = this.parseJwt(request);

            if(StringUtils.hasText(jwt)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(jwt, jwt, null);
                authentication.setDetails((new WebAuthenticationDetailsSource()).buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception ex) {
            log.error("Cannot set user authentication ", ex);
        }


        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        return StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ") ? headerAuth.substring(7) : null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request)  {
        return request.getRequestURI().contains("/swagger-ui") ||
                request.getRequestURI().contains("/swagger-config") ||
                request.getRequestURI().contains("/v3/api-docs") ||
                request.getRequestURI().contains("/actuator") ||
                request.getRequestURI().contains("/actuator/health") ||
                request.getRequestURI().contains("/h2-console")
                ;
    }
}

