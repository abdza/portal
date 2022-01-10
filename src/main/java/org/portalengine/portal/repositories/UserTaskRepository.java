package org.portalengine.portal.repositories;

import java.util.List;

import org.portalengine.portal.entities.User;
import org.portalengine.portal.entities.UserTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTaskRepository extends JpaRepository<UserTask, Long> {
	
	List<UserTask> findByUser(User user);

}
