package com.examflow.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    private final AdminAuthFilter adminAuthFilter;

    public SecurityConfig(AdminAuthFilter adminAuthFilter) {
        this.adminAuthFilter = adminAuthFilter;
    }

    /*
     * FOR TESTING: This method is commented out to completely disable the AdminAuthFilter.
     * To re-enable security, uncomment this entire @Bean method.
     */
    // @Bean
    // public FilterRegistrationBean<AdminAuthFilter> adminFilter() {
    //     FilterRegistrationBean<AdminAuthFilter> registrationBean = new FilterRegistrationBean<>();
    //     registrationBean.setFilter(adminAuthFilter);
    //     // Apply this filter to all URLs starting with /admin
    //     registrationBean.addUrlPatterns("/admin/*", "/admin"); 
    //     return registrationBean;
    // }
}
