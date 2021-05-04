package org.portalengine.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}	
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) { 
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
			.antMatchers("/","/register","/login","/logout","/libs/**","/images/**").permitAll()
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
