package com.innowise.sivachenko.security.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
@Profile("!test")
public class ApiGatewayFilter implements Filter {

    public static final String IS_PROXY_REQUEST_VALUE = "true";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (Objects.equals(req.getHeader("Is-Proxy-Request"), IS_PROXY_REQUEST_VALUE)) {
            filterChain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).setStatus(407);
        }
    }
}
