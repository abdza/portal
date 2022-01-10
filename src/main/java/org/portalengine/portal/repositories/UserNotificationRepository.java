package org.portalengine.portal.repositories;

import java.util.List;

import org.portalengine.portal.entities.User;
import org.portalengine.portal.entities.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
	
	List<UserNotification> findByUser(User user);

}
