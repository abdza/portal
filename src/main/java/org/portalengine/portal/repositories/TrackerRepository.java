package org.portalengine.portal.repositories;

import java.util.List;

import org.portalengine.portal.entities.PortalPage;
import org.portalengine.portal.entities.Tracker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TrackerRepository extends JpaRepository<Tracker, Long> {

	Tracker findByModuleAndSlug(String module,String slug);
	
	@Query("from Tracker pg where pg.name like :#{#search} or pg.dataTable like :#{#search} or pg.slug like :#{#search}")
	List<Tracker> findAllByQ(String search);
	
	List<Tracker> findAllByModule(String module);
	
	@Query("from Tracker pg where pg.name like :#{#search} or pg.dataTable like :#{#search} or pg.slug like :#{#search}")
	Page<Tracker> apiquery(String search, Pageable pageable);
	
	@Query("from Tracker pg where (pg.name like :#{#search} or pg.dataTable like :#{#search} or pg.slug like :#{#search}) and pg.module=:#{#module}")
	Page<Tracker> apimodulequery(String search, String module, Pageable pageable);
}
