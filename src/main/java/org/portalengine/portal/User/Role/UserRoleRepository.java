package org.portalengine.portal.User.Role;

import java.util.List;

import org.portalengine.portal.Tracker.Tracker;
import org.portalengine.portal.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

		List<UserRole> findByUser(User user);
		
		List<UserRole> findByUserAndModule(User user, String module);
		List<UserRole> findByUserAndModuleAndSlug(User user, String module,String slug);
		
		List<UserRole> findByRoleAndModule(String role, String module);
		List<UserRole> findByRoleAndModuleAndSlug(String role, String module, String slug);
		
}
