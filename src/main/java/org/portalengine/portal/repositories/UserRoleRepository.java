package org.portalengine.portal.repositories;

import java.util.List;

import org.portalengine.portal.entities.Tracker;
import org.portalengine.portal.entities.User;
import org.portalengine.portal.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

	List<UserRole> findByUser(User user);
	
	List<UserRole> findByUserAndModuleIgnoreCase(User user, String module);
	
	List<UserRole> findByRoleAndModuleIgnoreCase(String role, String module);
	
	UserRole findByUserAndModuleAndRoleIgnoreCase(User user, String module, String role);

	UserRole findByUserAndModuleIgnoreCaseAndRoleIgnoreCase(User user, String module, String role);

	@Query("from UserRole pg where pg.role like :#{#search} or pg.module like :#{#search} or pg.user in (select user from User user where user.name like :#{#search})")
	Page<UserRole> apiquery(String search, Pageable pageable);
	
	@Query("from UserRole pg where (pg.role like :#{#search} or pg.user in (select user from User user where user.name like :#{#search})) and pg.module=:#{#module}")
	Page<UserRole> apimodulequery(String search, String module, Pageable pageable);
		
}
