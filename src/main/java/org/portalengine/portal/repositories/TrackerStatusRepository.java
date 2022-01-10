package org.portalengine.portal.repositories;

import java.util.List;

import org.portalengine.portal.entities.Tracker;
import org.portalengine.portal.entities.TrackerField;
import org.portalengine.portal.entities.TrackerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackerStatusRepository extends JpaRepository<TrackerStatus, Long> {

	List<TrackerStatus> findByTracker(Tracker tracker);
	TrackerStatus findByTrackerAndName(Tracker tracker, String name);
	List<TrackerStatus> findByTrackerAndNameContainingIgnoreCase(Tracker tracker, String name);	
}