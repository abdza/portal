package org.portalengine.portal.repositories;

import java.util.List;

import org.portalengine.portal.entities.Module;
import org.portalengine.portal.entities.PortalPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ModuleRepository extends JpaRepository<Module, Long> {

	@Query("from Module pg where pg.name like :#{#search}")
	Page<Module> apiquery(String search, Pageable pageable);
}
