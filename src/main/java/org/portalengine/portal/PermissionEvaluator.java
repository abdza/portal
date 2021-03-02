package org.portalengine.portal;

import java.io.Serializable;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("permissionEvaluator")
public class PermissionEvaluator implements org.springframework.security.access.PermissionEvaluator {

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		System.out.println("in target domain");
		if (authentication != null && permission instanceof String) {
			
		}
		
		return false;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
			Object permission) {
		System.out.println("in serializable");
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean hasPermission(Authentication authentication, String objtype, String module, String slug) {
		// TODO Auto-generated method stub
		System.out.println("In has permission for " + objtype + " for module:" + module + " and slug:" + slug);
		return true;
	}

}
