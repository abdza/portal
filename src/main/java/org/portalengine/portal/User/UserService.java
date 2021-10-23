package org.portalengine.portal.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.portalengine.portal.User.Message.UserMessage;
import org.portalengine.portal.User.Message.UserMessageRepository;
import org.portalengine.portal.User.Notification.UserNotification;
import org.portalengine.portal.User.Notification.UserNotificationRepository;
import org.portalengine.portal.User.Role.UserRole;
import org.portalengine.portal.User.Role.UserRoleRepository;
import org.portalengine.portal.User.Task.UserTask;
import org.portalengine.portal.User.Task.UserTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.Data;

@Service
@Data
public class UserService implements UserDetailsService {

	@Autowired
	private UserRepository repo;

	@Autowired
	private UserNotificationRepository notificationRepo;
	
	@Autowired
	private UserMessageRepository messageRepo;
	
	@Autowired
	private UserTaskRepository taskRepo;
	
	@Autowired
	private UserRoleRepository roleRepo;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

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
	
	public boolean hasRole(User curuser, String module, String role) {
		UserRole userRole = roleRepo.findByUserAndModuleAndRoleIgnoreCase(curuser, module, role);
		if(userRole != null && userRole instanceof UserRole) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean hasRole(User curuser, String module) {
		List<UserRole> userRoles = roleRepo.findByUserAndModuleIgnoreCase(curuser, module);
		if(userRoles.size()>0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public List<UserRole> module_roles(User user, String module) {
		List<UserRole> userRoles = roleRepo.findByUserAndModuleIgnoreCase(user, module);
		return userRoles;
	}

	public List<UserNotification> currentNotifications() {
		Object secuser = SecurityContextHolder.getContext().getAuthentication().getPrincipal();		
		if(secuser!=null){
			User duser = repo.findById(((User)secuser).getId()).orElse(null);
			List<UserNotification> toret = notificationRepo.findByUser(duser);
			return toret;
		}
		else{
			return null;
		}
	}
	
	public User currentUser() {
		Object secuser = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(secuser!=null && secuser instanceof User){	
			return (User)secuser;
		}
		else{
			return null;
		}
	}
	
	public User setPassword(User user,String password) {
		user.setPassword(passwordEncoder.encode(password));
		return user;
	}
	
	public List<UserMessage> currentMessages() {
		Object secuser = SecurityContextHolder.getContext().getAuthentication().getPrincipal();		
		if(secuser!=null){
			User duser = repo.findById(((User)secuser).getId()).orElse(null);
			List<UserMessage> toret = messageRepo.findByUser(duser);
			return toret;
		}
		else{
			return null;
		}
	}
	
	public List<UserTask> currentTasks() {
		Object secuser = SecurityContextHolder.getContext().getAuthentication().getPrincipal();		
		if(secuser!=null){
			User duser = repo.findById(((User)secuser).getId()).orElse(null);
			List<UserTask> toret = taskRepo.findByUser(duser);
			return toret;
		}
		else{
			return null;
		}
	}
}
