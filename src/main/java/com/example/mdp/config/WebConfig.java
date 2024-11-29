package com.example.mdp.config;

import com.example.mdp.filter.AuthorizationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<AuthorizationFilter> loggingFilter(AuthorizationFilter authorizationFilter) {
        FilterRegistrationBean<AuthorizationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(authorizationFilter);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
