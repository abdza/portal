package org.portalengine.portal.security;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.hibernate.boot.model.relational.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {	

	@Autowired
	PortalAuthenticationProvider portalAuthenticationProvider;

	@Bean
	public static PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}	
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder authbuilder, DataSource dataSource) { 
		try {

			authbuilder.authenticationProvider(portalAuthenticationProvider);

			authbuilder.ldapAuthentication()
			.userDnPatterns("uid={0},ou=people")
			.groupSearchBase("ou=groups")
			.contextSource()
			.url("ldap://localhost:2389/dc=springframework,dc=org")
			.and()
			.passwordCompare()
			.passwordEncoder(new BCryptPasswordEncoder())
			.passwordAttribute("userPassword");
			

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
	public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {		
		
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
	public static class FormLoginWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {	
		
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
				.authorizeRequests()
				.antMatchers("/","/register","/login","/logout","/libs/**","/images/**","/setup").permitAll()
				.antMatchers("/admin/**").hasAuthority("ROLE_SYSTEM_ADMIN")
			.and()
				.formLogin()
				.loginPage("/login")
			.and()
				.logout()
				.logoutSuccessUrl("/")
			;
		}
	}
}
