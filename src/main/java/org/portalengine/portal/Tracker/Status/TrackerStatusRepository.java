package org.portalengine.portal.Tracker.Status;

import java.util.List;

import org.portalengine.portal.Tracker.Tracker;
import org.portalengine.portal.Tracker.Field.TrackerField;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackerStatusRepository extends JpaRepository<TrackerStatus, Long> {

	List<TrackerStatus> findByTracker(Tracker tracker);
	TrackerStatus findByTrackerAndName(Tracker tracker, String name);
	List<TrackerStatus> findByTrackerAndNameContainingIgnoreCase(Tracker tracker, String name);	
}