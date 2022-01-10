package org.portalengine.portal.forms;

import org.portalengine.portal.entities.User;
import org.portalengine.portal.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.Data;

@Data
public class RegistrationForm {
	
	private Integer id;
	private String username;
	private String staffid;
	private String name;
	private String email;
	private String password;
	private String confirm;
	
	public User toUser(PasswordEncoder passwordEncoder, UserService service) {		
		if(id!=null) {
			User curuser = service.getRepo().getOne((long)id);
			curuser.setName(name);
			curuser.setUsername(username);
			curuser.setEmail(email);
			curuser.setPassword(passwordEncoder.encode(password));
			return curuser;
		}
		else {
			if(username==null||username.equals("")) {
				username = staffid;
			}
			if(username==null||username.equals("")) {
				username = email;
			}
			return new User(username, staffid, name, email, passwordEncoder.encode(password), false);
		}
	}

}
