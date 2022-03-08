package org.portalengine.portal.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
public class SecurityHandlerConfig {
    
    @Bean
    public AuthenticationSuccessHandler loginSuccessHandler() {
        return new SavedRequestAwareAuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {
                super.onAuthenticationSuccess(request, response, authentication);
            }
        };
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new LogoutSuccessHandler() {
 
            @Override
            public void onLogoutSuccess(HttpServletRequest request,
                        HttpServletResponse response, Authentication authentication)
                    throws IOException, ServletException {
                SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
                String username = userDetails.getUsername();

                System.out.println("The user " + username + " has logged out.");

                response.sendRedirect(request.getContextPath());
            }
        };
    }
}
