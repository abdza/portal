package org.portalengine.portal.repositories;

import java.util.List;

import org.portalengine.portal.entities.Tracker;
import org.portalengine.portal.entities.TrackerTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrackerTransitionRepository extends JpaRepository<TrackerTransition, Long> {

	List<TrackerTransition> findByTracker(Tracker tracker);
	TrackerTransition findByTrackerAndName(Tracker tracker, String name);
	
	@Query("select tt from TrackerTransition tt where (tt.prevStatus is null or tt.prevStatus='') and tt.name=:#{#tracker.initialStatus} and tt.tracker=:#{#tracker}")
	TrackerTransition createTransition(@Param("tracker") Tracker tracker);
}