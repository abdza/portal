package org.portalengine.portal;

import java.util.List;

import org.portalengine.portal.entities.PortalPage;
import org.portalengine.portal.entities.Tracker;
import org.portalengine.portal.entities.TrackerField;
import org.portalengine.portal.entities.User;
import org.portalengine.portal.entities.UserRole;
import org.portalengine.portal.repositories.FileLinkRepository;
import org.portalengine.portal.repositories.PageRepository;
import org.portalengine.portal.repositories.SettingRepository;
import org.portalengine.portal.repositories.TrackerRepository;
import org.portalengine.portal.repositories.TreeRepository;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class CustomMethodSecurityExpressionRoot 
extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {
	
	private Authentication authentication;	
	private RepoCollection repos;
	
  public CustomMethodSecurityExpressionRoot(Authentication authentication, RepoCollection repos) {	  
      super(authentication);
      this.authentication = authentication;
      this.repos = repos;
  }
  
    public boolean trackerPermission(String module, String slug, String permission) {
    	Tracker curtracker = repos.getTrackerRepository().findOneByModuleAndSlug(module, slug);
    	
    	if(curtracker!=null) {
    		String rolelist="";
    		
    		if(permission.equals("list")) {
    			rolelist = curtracker.getViewListRoles();    			
    		}
    		else if(permission.equals("add")) {
    			rolelist = curtracker.getAddRoles();
    		}
    		else if(permission.equals("detail")) {
    			rolelist = curtracker.getDetailRoles();
    		}
    		else if(permission.equals("delete")) {
    			rolelist = curtracker.getDeleteRoles();
    		}
    		else if(permission.equals("edit")) {
    			rolelist = curtracker.getEditRoles();
    		}
    		if(rolelist==null || rolelist.length()==0) {
				rolelist = "Authenticated";
			}
    		
    		Object curuser = this.authentication.getPrincipal();
    		List<UserRole> mr = null;
			if(curuser instanceof String) {
				System.out.println("User is anon");				
			}
			else if(curuser instanceof User) {
				System.out.println("Got user");
				mr = this.repos.getUserRoleRepository().findByUserAndModuleIgnoreCase((User)curuser, curtracker.getModule());				
			}
			
			for(String fname:rolelist.split(",")) {
				if(fname.equals("All")) {
					return true;
				}
				else if(fname.equals("None")) {
					return false;
				}
				else if(fname.equals("Authenticated")) {
					if(curuser instanceof User) {
						return true;
					}
				}
				else {						
					// Need to check user roles here
					if(mr.size()>0) {						
						for(UserRole cr:mr) {
							if(fname.equals(cr.getRole())) {
								return true;
							}
						}
					}
				}
			}
    	}
    	
    	return false;
    }
  
	public boolean pagePermission(String module, String slug) {
		// TODO Auto-generated method stub
		//System.out.println(this.authentication.getPrincipal());
		
		PortalPage curpage = repos.getPageRepository().findOneByModuleAndSlug(module, slug);
		
		if(curpage!=null) {
			if(curpage.getPublished()!=true) {
				return false;
			}
			Object curuser = this.authentication.getPrincipal();
			if(curuser instanceof String) {
				System.out.println("User is anon");
				if(curpage.getRequireLogin()) {
					return false;
				}
			}
			else if(curuser instanceof User) {
				System.out.println("Got user");
			}
			if(curpage!=null) {
				System.out.println("Got page:" + curpage.getTitle());
			}	
			return true;
		}		
		
		System.out.println("In has permission for module:" + module + " and slug:" + slug);
		return false;
	}

@Override
public void setFilterObject(Object filterObject) {
	// TODO Auto-generated method stub
	
}

@Override
public Object getFilterObject() {
	// TODO Auto-generated method stub
	return null;
}

@Override
public void setReturnObject(Object returnObject) {
	// TODO Auto-generated method stub
	
}

@Override
public Object getReturnObject() {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Object getThis() {
	// TODO Auto-generated method stub
	return null;
}

}