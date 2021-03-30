package org.portalengine.portal.User;

import java.util.List;

import org.portalengine.portal.Tracker.Tracker;
import org.portalengine.portal.Tracker.Transition.TrackerTransition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
	
	User findByStaffid(String staffid);
	
	User findOneByStaffid(String staffid);
	
	User findByUsername(String username);
	
	User findOneByUsername(String username);

	@Query("from User tt where tt.username like :#{#search} or tt.name like :#{#search} or tt.email like :#{#search} or tt.staffid like :#{#search}")
	Page<User> apiquery(String search, Pageable pageable);

}
