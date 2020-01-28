package portal.Tracker.Status;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import portal.Tracker.Tracker;

public interface TrackerStatusRepository extends JpaRepository<TrackerStatus, Long> {

	List<TrackerStatus> findByTracker(Tracker tracker);
	TrackerStatus findByTrackerAndName(Tracker tracker, String name);
	
}