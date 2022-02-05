package org.portalengine.portal.security;

import org.aopalliance.intercept.MethodInvocation;
import org.portalengine.portal.RepoCollection;
import org.portalengine.portal.repositories.FileLinkRepository;
import org.portalengine.portal.repositories.PageRepository;
import org.portalengine.portal.repositories.SettingRepository;
import org.portalengine.portal.repositories.TrackerRepository;
import org.portalengine.portal.repositories.TrackerRoleRepository;
import org.portalengine.portal.repositories.TreeRepository;
import org.portalengine.portal.repositories.UserRoleRepository;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

public class PortalSecurityExpressionHandler 
extends DefaultMethodSecurityExpressionHandler {
	
	private RepoCollection repos;
	
  private AuthenticationTrustResolver trustResolver = 
    new AuthenticationTrustResolverImpl();
  
  public PortalSecurityExpressionHandler(RepoCollection repos) {
	  super();
	  this.repos = repos;
  }

  @Override
  protected MethodSecurityExpressionOperations createSecurityExpressionRoot(
    Authentication authentication, MethodInvocation invocation) {
      PortalSecurityExpressionRoot root = 
        new PortalSecurityExpressionRoot(authentication,this.repos);
      root.setPermissionEvaluator(getPermissionEvaluator());
      root.setTrustResolver(this.trustResolver);
      root.setRoleHierarchy(getRoleHierarchy());
      return root;
  }
}
