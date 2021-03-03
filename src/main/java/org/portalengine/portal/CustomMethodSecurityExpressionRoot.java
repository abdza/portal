package org.portalengine.portal;

import org.portalengine.portal.FileLink.FileLinkRepository;
import org.portalengine.portal.Page.Page;
import org.portalengine.portal.Page.PageRepository;
import org.portalengine.portal.Setting.SettingRepository;
import org.portalengine.portal.Tracker.Tracker;
import org.portalengine.portal.Tracker.TrackerRepository;
import org.portalengine.portal.Tracker.Field.TrackerField;
import org.portalengine.portal.Tree.TreeRepository;
import org.portalengine.portal.User.User;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class CustomMethodSecurityExpressionRoot 
extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {
	
	private Authentication authentication;	
	private PageRepository pageRepository;
	private FileLinkRepository fileRepository;
	private	TrackerRepository trackerRepository;
	private TreeRepository treeRepository;
	private	SettingRepository settingRepository;
	
  public CustomMethodSecurityExpressionRoot(Authentication authentication,PageRepository pageRepository,
		  FileLinkRepository fileRepository,TrackerRepository trackerRepository,TreeRepository treeRepository,
		  SettingRepository settingRepository) {	  
      super(authentication);
      this.authentication = authentication;
      this.pageRepository = pageRepository;
      this.fileRepository = fileRepository;
      this.trackerRepository = trackerRepository;
      this.treeRepository = treeRepository;
      this.settingRepository = settingRepository;
  }
  
    public boolean trackerPermission(String module, String slug, String permission) {
    	Tracker curtracker = trackerRepository.findOneByModuleAndSlug(module, slug);
    	
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
    		if(rolelist==null) {
				rolelist = "Authenticated";
			}
    		
    		Object curuser = this.authentication.getPrincipal();
			if(curuser instanceof String) {
				System.out.println("User is anon");				
			}
			else if(curuser instanceof User) {
				System.out.println("Got user");
			}
			
			for(String fname:rolelist.split(",")) {
				if(fname.equals("All")) {
					return true;
				}
				else if(fname.equals("Authenticated")) {
					if(curuser instanceof User) {
						return true;
					}
				}
				else {
					// Need to check user roles here
				}
			}
    	}
    	
    	return false;
    }
  
	public boolean pagePermission(String module, String slug) {
		// TODO Auto-generated method stub
		//System.out.println(this.authentication.getPrincipal());
		
		Page curpage = pageRepository.findOneByModuleAndSlug(module, slug);
		
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