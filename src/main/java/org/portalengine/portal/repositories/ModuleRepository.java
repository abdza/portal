package org.portalengine.portal.repositories;

import java.util.List;

import org.portalengine.portal.entities.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ModuleRepository extends JpaRepository<Module, Long> {

}
