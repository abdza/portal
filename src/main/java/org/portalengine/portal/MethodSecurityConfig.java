package org.portalengine.portal;

import org.portalengine.portal.FileLink.FileLinkRepository;
import org.portalengine.portal.Page.PageRepository;
import org.portalengine.portal.Setting.SettingRepository;
import org.portalengine.portal.Tracker.TrackerRepository;
import org.portalengine.portal.Tracker.Role.TrackerRoleRepository;
import org.portalengine.portal.Tree.TreeRepository;
import org.portalengine.portal.User.Role.UserRoleRepository;
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
    	CustomMethodSecurityExpressionHandler expressionHandler = 
          new CustomMethodSecurityExpressionHandler(repos);
        expressionHandler.setPermissionEvaluator(new PermissionEvaluator());
        return expressionHandler;
    }   
    
}