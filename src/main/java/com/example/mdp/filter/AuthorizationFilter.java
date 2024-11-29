package com.example.mdp.filter;

import com.example.mdp.repository.UsersRepository;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthorizationFilter implements Filter {

    private final UsersRepository usersRepository;

    public AuthorizationFilter(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String[] excludedPaths = {"/authorization", "/registration", "/css", "/img", "/js"};

        String requestURI = httpRequest.getRequestURI();

        for (String path : excludedPaths) {
            if (requestURI.startsWith(path)) {
                chain.doFilter(request, response);
                return;
            }
        }

        Cookie[] cookies = httpRequest.getCookies();
        String loggedInUser = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("loggedInUser".equals(cookie.getName())) {
                    loggedInUser = cookie.getValue();
                    break;
                }
            }
        }

        if (loggedInUser == null || usersRepository.getUserByLogin(loggedInUser) == null) {
            httpResponse.sendRedirect("/authorization/");
            return;
        }

        chain.doFilter(request, response);
    }
}
