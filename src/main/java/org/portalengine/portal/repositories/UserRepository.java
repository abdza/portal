package org.portalengine.portal.repositories;

import java.util.List;
import java.util.Optional;

import org.portalengine.portal.entities.Tracker;
import org.portalengine.portal.entities.TrackerTransition;
import org.portalengine.portal.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
	
	Optional<User> findByStaffid(String staffid);
	
	Optional<User> findByUsername(String username);

	@Query("from User tt where tt.username like :#{#search} or tt.name like :#{#search} or tt.email like :#{#search} or tt.staffid like :#{#search}")
	Page<User> apiquery(String search, Pageable pageable);

}
