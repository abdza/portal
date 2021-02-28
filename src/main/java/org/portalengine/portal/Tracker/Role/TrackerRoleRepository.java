package org.portalengine.portal.Tracker.Role;

import java.util.List;

import org.portalengine.portal.Tracker.Tracker;
import org.portalengine.portal.Tracker.Field.TrackerField;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackerRoleRepository extends JpaRepository<TrackerRole, Long> {

		List<TrackerRole> findByTracker(Tracker tracker);
		TrackerRole findByTrackerAndName(Tracker tracker, String name);
		List<TrackerRole> findByTrackerAndNameContainingIgnoreCase(Tracker tracker, String name);
}
