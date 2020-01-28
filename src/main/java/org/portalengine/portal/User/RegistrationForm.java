package org.portalengine.portal.User;

import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.Data;

@Data
public class RegistrationForm {
	
	private String username;
	private String staffid;
	private String name;
	private String email;
	private String password;
	private String confirm;
	
	public User toUser(PasswordEncoder passwordEncoder) {
		username=staffid;
		return new User(username, staffid, name, email, passwordEncoder.encode(password));
	}

}
