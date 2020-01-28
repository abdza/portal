package portal.User;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.Data;

@Service
@Data
public class UserService implements UserDetailsService {

	@Autowired
	private UserRepository repo;

	@Autowired
	private UserNotificationRepository userNotificationRepo;

	@Autowired
	public UserService() {
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		User user = repo.findByUsername(username);
		if (user != null) {
			return user;
		}
		throw new UsernameNotFoundException("User '" + username + "' not found");
	}

	public List<UserNotification> currentNotifications() {
		Object secuser = SecurityContextHolder.getContext().getAuthentication().getPrincipal();		
		if(secuser!=null){
			User duser = repo.findById(((User)secuser).getId()).orElse(null);
			List<UserNotification> toret = userNotificationRepo.findByUser(duser);
			return toret;
		}
		else{
			return null;
		}
	}
}
