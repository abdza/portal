package portal.User;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {
	
	@Autowired
	private UserService service;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	public UserController() {
	}
	
	@GetMapping("/register")
	public String registerPage(Model model) {
		return "user/register.html";
	}
	
	@PostMapping("/register")
	public String register(@Valid RegistrationForm userreg, Model model) {
		service.getRepo().save(userreg.toUser(this.passwordEncoder));
		return "redirect:/";
	}

}
