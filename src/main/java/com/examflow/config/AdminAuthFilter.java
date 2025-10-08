package com.examflow.config;

import java.io.IOException;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AdminAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String requestURI = req.getRequestURI();

        // Check if the request is for an admin page (but not the login/verify pages themselves)
        boolean isAdminAction = requestURI.startsWith("/admin/") && 
                                !requestURI.equals("/admin/login");

        boolean isAuthenticated = (session != null && session.getAttribute("isAdmin") != null && (Boolean) session.getAttribute("isAdmin"));

        if (isAdminAction && !isAuthenticated) {
            // If it's an admin action and the user is not authenticated, redirect to home.
            res.sendRedirect("/");
            return;
        }

        // If checks pass, continue with the request.
        chain.doFilter(request, response);
    }
}
