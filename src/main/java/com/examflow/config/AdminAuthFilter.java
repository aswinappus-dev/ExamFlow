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
        
        // Define which pages are part of the public-facing login flow
        boolean isLoginPage = requestURI.equals("/admin/login");

        // Define which pages are secure admin actions
        boolean isAdminAction = requestURI.startsWith("/admin") && !isLoginPage;

        boolean isAuthenticated = (session != null && session.getAttribute("isAdmin") != null && (Boolean) session.getAttribute("isAdmin"));

       // if (isAdminAction && !isAuthenticated) {
            // If it's a secure admin action and the user is NOT authenticated, redirect to the homepage.
           // res.sendRedirect("/");
      //      return;
      //  }

        // NEW SECURITY FIX:
        // If the user IS authenticated and accessing a secure page, add headers to prevent browser caching.
        if (isAuthenticated && isAdminAction) {
            res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
            res.setHeader("Pragma", "no-cache"); // HTTP 1.0.
            res.setHeader("Expires", "0"); // Proxies.
        }

        // If all checks pass, continue with the original request.
        chain.doFilter(request, response);
    }
}

