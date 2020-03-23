package org.portalengine.portal.DataUpdate;

import java.util.List;

import org.portalengine.portal.Tracker.Tracker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataUpdateRepository extends JpaRepository<DataUpdate, Long> {

	List<DataUpdate> findAllByTracker(Tracker tracker);

}
