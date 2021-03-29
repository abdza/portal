package org.portalengine.portal.User.Role;

import java.util.List;

import org.portalengine.portal.Tracker.Tracker;
import org.portalengine.portal.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

		List<UserRole> findByUser(User user);
		
		List<UserRole> findByUserAndModuleIgnoreCase(User user, String module);
		
		List<UserRole> findByRoleAndModuleIgnoreCase(String role, String module);
		
		UserRole findByUserAndModuleAndRoleIgnoreCase(User user, String module, String role);
		
}
