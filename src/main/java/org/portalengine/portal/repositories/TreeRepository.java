package org.portalengine.portal.repositories;

import java.util.List;

import org.portalengine.portal.entities.Tree;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TreeRepository extends JpaRepository<Tree, Long> {

	@Query("from Tree tt where tt.name like :#{#search} or tt.module like :#{#search} or tt.slug like :#{#search}")
	Page<Tree> apiquery(String search, Pageable pageable);
	
	Tree findByName(String name);
	
	Tree findBySlug(String slug);
	
	Tree findByModuleAndSlug(String module,String slug);
	
	List<Tree> findAllByModule(String module);
}