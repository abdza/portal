package org.portalengine.portal.User.Notification;

import java.util.List;

import org.portalengine.portal.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
	
	List<UserNotification> findByUser(User user);

}
