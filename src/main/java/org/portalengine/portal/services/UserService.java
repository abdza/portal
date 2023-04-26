package org.portalengine.portal.services;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.portalengine.portal.entities.User;
import org.portalengine.portal.entities.UserMessage;
import org.portalengine.portal.entities.UserNotification;
import org.portalengine.portal.entities.UserRole;
import org.portalengine.portal.entities.UserTask;
import org.portalengine.portal.repositories.UserMessageRepository;
import org.portalengine.portal.repositories.UserNotificationRepository;
import org.portalengine.portal.repositories.UserRepository;
import org.portalengine.portal.repositories.UserRoleRepository;
import org.portalengine.portal.repositories.UserTaskRepository;
import org.portalengine.portal.security.SecurityUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.Data;
import lombok.var;

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
	public UserDetails loadUserByUsername(String username) 
			throws UsernameNotFoundException {
		var o = repo.findByUsername(username);
		User user = o.orElseThrow(() -> new UsernameNotFoundException("Username not found"));
		return new SecurityUser(user);
	}
	
	public boolean hasRole(User curuser, String module, String role) {
		UserRole userRole = roleRepo.findByUserAndModuleIgnoreCaseAndRoleIgnoreCase(curuser, module, role);
		if(userRole==null) {
			return false;
		}
		else {
			return true;
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
		User duser = currentUser();	
		if(duser!=null){			
			List<UserNotification> toret = notificationRepo.findByUser(duser);
			return toret;
		}
		else{
			return null;
		}
	}
	
	public User currentUser() {
		Object secuser = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(secuser!=null){
			if(secuser instanceof SecurityUser){	
				return ((SecurityUser) secuser).getUser();
			}
			if(secuser instanceof User){	
				return (User)secuser;
			}
			if(secuser instanceof String){
				User dbuser = repo.findByUsername((String)secuser).orElse(null);
				return dbuser;
			}
		}
		return null;
	}

	public Boolean switchUser(User target, HttpServletRequest req){
		
		return true;
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
