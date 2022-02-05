package org.portalengine.portal.security;

import org.portalengine.portal.RepoCollection;
import org.portalengine.portal.repositories.FileLinkRepository;
import org.portalengine.portal.repositories.PageRepository;
import org.portalengine.portal.repositories.SettingRepository;
import org.portalengine.portal.repositories.TrackerRepository;
import org.portalengine.portal.repositories.TrackerRoleRepository;
import org.portalengine.portal.repositories.TreeRepository;
import org.portalengine.portal.repositories.UserRepository;
import org.portalengine.portal.repositories.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
	
	@Autowired
	PageRepository pageRepository;
	
	@Autowired
	FileLinkRepository fileRepository;
	
	@Autowired
	TrackerRepository trackerRepository;
	
	@Autowired
	TreeRepository treeRepository;
	
	@Autowired
	SettingRepository settingRepository;
	
	@Autowired
	TrackerRoleRepository trackerRoleRepository;
	
	@Autowired
	UserRoleRepository userRoleRepository;

	@Autowired
	UserRepository userRepository;

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
    	RepoCollection repos = new RepoCollection();
  	  repos.setPageRepository(pageRepository);
  	  repos.setFileRepository(fileRepository);
  	  repos.setTrackerRoleRepository(trackerRoleRepository);
  	  repos.setTrackerRepository(trackerRepository);
      repos.setTreeRepository(treeRepository);
      repos.setSettingRepository(settingRepository); 
      repos.setUserRoleRepository(userRoleRepository);
	  repos.setUserRepository(userRepository);

    	PortalSecurityExpressionHandler expressionHandler = 
          new PortalSecurityExpressionHandler(repos);
        expressionHandler.setPermissionEvaluator(new PermissionEvaluator());
        return expressionHandler;
    }   
    
}