package org.portalengine.portal.Page;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PageRepository extends JpaRepository<Page, Long> {

	Page findOneByModuleAndSlug(String module,String slug);
	
	@Query("from Page pg where pg.title like :#{#search} or pg.content like :#{#search} or pg.slug like :#{#search}")
	List<Page> findAllByQ(String search);
}
