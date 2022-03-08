package org.portalengine.portal.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.server.authentication.WebFilterChainServerAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {	

	protected UserDetailsService userDetailsService;
	protected AuthenticationSuccessHandler authenticationSuccessHandler;
	protected LogoutSuccessHandler logoutSuccessHandler;

	@Autowired
	PortalAuthenticationProvider portalAuthenticationProvider;

	@Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
    	return new SimpleUrlAuthenticationFailureHandler();
    }	

	@Autowired
	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	@Autowired
	public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler authenticationSuccessHandler) {
		this.authenticationSuccessHandler = authenticationSuccessHandler;
	}

	@Autowired
	public void setLogoutSuccessHandler(LogoutSuccessHandler logoutSuccessHandler) {
		this.logoutSuccessHandler = logoutSuccessHandler;
	}

	@Bean
	public static PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}	
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder authbuilder, DataSource dataSource) { 
		try {

			authbuilder.authenticationProvider(portalAuthenticationProvider);

			/* authbuilder.ldapAuthentication()
			.userDnPatterns("uid={0},ou=people")
			.groupSearchBase("ou=groups")
			.contextSource()
			.url("ldap://localhost:2389/dc=springframework,dc=org")
			.and()
			.passwordCompare()
			.passwordEncoder(new BCryptPasswordEncoder())
			.passwordAttribute("userPassword"); */
			

			/*  auth.jdbcAuthentication()
			.dataSource(dataSource)
			.withDefaultSchema()
			.withUser(User.withUsername("user")
						.password(encoder().encode("pass"))
			.roles("USER"));  */
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Configuration
	@Order(1)                                                       
	public class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {		
		
		protected void configure(HttpSecurity http) throws Exception {
			http
				.antMatcher("/api/**")                               
				.authorizeRequests()
				.anyRequest().authenticated()
			.and()
				.httpBasic()
			.and()
					.csrf()
						.disable();
		}
	}
	
	@Configuration                                                  
	public class FormLoginWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {	
		
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
				.addFilterAfter(switchUserFilter(), FilterSecurityInterceptor.class)
				.authorizeRequests()
				.antMatchers("/","/register","/login","/logout","/libs/**","/images/**","/setup","/restore_user").permitAll()
				.antMatchers("/admin/**").hasAuthority("ROLE_SYSTEM_ADMIN")
			.and()
				.formLogin()
				.loginPage("/login")
			.and()
				.logout()
				.logoutSuccessUrl("/");
		}

	}

	@Bean
	protected SwitchUserFilter switchUserFilter() {
		SwitchUserFilter filter = new SwitchUserFilter();
		filter.setUserDetailsService(userDetailsService);
		filter.setSuccessHandler(authenticationSuccessHandler);
		filter.setSwitchUserUrl("/impersonate");
		filter.setExitUserUrl("/restore_user");
		filter.setUsernameParameter("username");
		filter.setFailureHandler(authenticationFailureHandler());
		return filter;
	}	
}