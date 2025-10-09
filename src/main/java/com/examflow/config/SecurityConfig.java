package com.examflow.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    private final AdminAuthFilter adminAuthFilter;

    public SecurityConfig(AdminAuthFilter adminAuthFilter) {
        this.adminAuthFilter = adminAuthFilter;
    }

    @Bean
    public FilterRegistrationBean<AdminAuthFilter> adminFilter() {
        FilterRegistrationBean<AdminAuthFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(adminAuthFilter);
        // FIX: Apply this filter to the root /admin URL AND all sub-paths
        registrationBean.addUrlPatterns("/admin", "/admin/*");
        return registrationBean;
    }
}
