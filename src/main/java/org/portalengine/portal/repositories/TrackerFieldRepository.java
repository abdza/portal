package org.portalengine.portal.repositories;

import java.util.List;

import org.portalengine.portal.entities.Tracker;
import org.portalengine.portal.entities.TrackerField;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackerFieldRepository extends JpaRepository<TrackerField, Long> {

		List<TrackerField> findByTracker(Tracker tracker);
		TrackerField findByTrackerAndName(Tracker tracker, String name);
		List<TrackerField> findByTrackerAndNameContainingIgnoreCase(Tracker tracker, String name);
}
