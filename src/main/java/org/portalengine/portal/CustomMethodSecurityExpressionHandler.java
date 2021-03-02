package org.portalengine.portal;

import org.aopalliance.intercept.MethodInvocation;
import org.portalengine.portal.FileLink.FileLinkRepository;
import org.portalengine.portal.Page.PageRepository;
import org.portalengine.portal.Setting.SettingRepository;
import org.portalengine.portal.Tracker.TrackerRepository;
import org.portalengine.portal.Tree.TreeRepository;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

public class CustomMethodSecurityExpressionHandler 
extends DefaultMethodSecurityExpressionHandler {
	
	private PageRepository pageRepository;
	private FileLinkRepository fileRepository;
	private	TrackerRepository trackerRepository;
	private TreeRepository treeRepository;
	private	SettingRepository settingRepository;
	
  private AuthenticationTrustResolver trustResolver = 
    new AuthenticationTrustResolverImpl();
  
  public CustomMethodSecurityExpressionHandler(PageRepository pageRepository,
		  FileLinkRepository fileRepository,TrackerRepository trackerRepository,TreeRepository treeRepository,
		  SettingRepository settingRepository) {
	  super();
	  this.pageRepository = pageRepository;
	  this.fileRepository = fileRepository;
      this.trackerRepository = trackerRepository;
      this.treeRepository = treeRepository;
      this.settingRepository = settingRepository;
  }

  @Override
  protected MethodSecurityExpressionOperations createSecurityExpressionRoot(
    Authentication authentication, MethodInvocation invocation) {
      CustomMethodSecurityExpressionRoot root = 
        new CustomMethodSecurityExpressionRoot(authentication,this.pageRepository,
      		  this.fileRepository,this.trackerRepository,this.treeRepository,
      		this.settingRepository);
      root.setPermissionEvaluator(getPermissionEvaluator());
      root.setTrustResolver(this.trustResolver);
      root.setRoleHierarchy(getRoleHierarchy());
      return root;
  }
}
