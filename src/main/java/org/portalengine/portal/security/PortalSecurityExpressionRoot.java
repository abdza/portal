package org.portalengine.portal.security;

import java.util.List;

import org.portalengine.portal.RepoCollection;
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
import org.portalengine.portal.repositories.UserRepository;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class PortalSecurityExpressionRoot
		extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {

	private Authentication authentication;
	private RepoCollection repos;

	public PortalSecurityExpressionRoot(Authentication authentication, RepoCollection repos) {
		super(authentication);
		this.authentication = authentication;
		this.repos = repos;
	}

	public boolean trackerPermission(String module, String slug, String permission) {
		Tracker curtracker = repos.getTrackerRepository().findOneByModuleAndSlug(module, slug);

		if (curtracker == null) {
			return false;
		}

		Object userobj = this.authentication.getPrincipal();
		System.out.println("username:" + this.authentication.getName());
		System.out.println("password:" + this.authentication.getCredentials());
		SecurityUser secuser = null;
		User curuser = null;
		List<UserRole> mr = null;

		if (userobj == null) {
			System.out.println("User is anon");
		} else {
			if (userobj instanceof String) {
				System.out.println("User obj is " + userobj);
				curuser = repos.getUserRepository().findByUsername((String)userobj).orElse(null);
			} else if (userobj instanceof SecurityUser) {
				secuser = (SecurityUser) userobj;
				curuser = secuser.getUser();
				System.out.println("Got user");				
			}
		}
		if(curuser!=null){
			mr = this.repos.getUserRoleRepository().findByUserAndModuleIgnoreCase(curuser,
			curtracker.getModule());
		}

		StringBuilder rlist = new StringBuilder();		

		if (permission.equals("list")) {
			rlist.append(curtracker.getViewListRoles());
		} else if (permission.equals("add")) {
			rlist.append(curtracker.getAddRoles());
		} else if (permission.equals("detail")) {
			rlist.append(curtracker.getDetailRoles());
		} else if (permission.equals("delete")) {
			rlist.append(curtracker.getDeleteRoles());
		} else if (permission.equals("edit")) {
			rlist.append(curtracker.getEditRoles());
		} else if (permission.equals("save")) {
			rlist.append(curtracker.getEditRoles());
			rlist.append(",");
			rlist.append(curtracker.getAddRoles());
		}

		String rolelist = rlist.toString();

		if (rolelist == null || rolelist.length() == 0) {
			// if the role 'All' is not specified, then default to 'Authenticated' 
			rolelist = "Authenticated";
		}		

		for (String fname : rolelist.split(",")) {
			if (fname.trim().equals("All")) {
				return true;
			} else if (fname.trim().equals("None")) {
				return false;
			} else if (fname.trim().equals("Authenticated")) {
				if (curuser != null) {
					return true;
				}
			} else {
				// Need to check user roles here
				if (mr.size() > 0) {
					for (UserRole cr : mr) {
						if (fname.trim().equals(cr.getRole())) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public boolean pagePermission(String module, String slug) {
		// TODO Auto-generated method stub
		// System.out.println(this.authentication.getPrincipal());

		PortalPage curpage = repos.getPageRepository().findOneByModuleAndSlug(module, slug);

		if (curpage != null) {
			if (curpage.getPublished() != true) {
				return false;
			}
			Object curuser = this.authentication.getPrincipal();
			if (curuser instanceof String) {
				System.out.println("User is anon");
				if (curpage.getRequireLogin()) {
					return false;
				}
			} else if (curuser instanceof User) {
				System.out.println("Got user");
			}
			if (curpage != null) {
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