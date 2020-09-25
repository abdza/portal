package org.portalengine.portal.Menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Data;

@Service
@Data
public class MenuService {
	
	@Autowired
	private MenuRepository menuRepo;
	
	@Autowired
	private MenuItemRepository menuItemRepo;
	
	@Autowired
	private MenuCategoryRepository menuCategoryRepo;
	
	@Autowired
	public MenuService() {
	}

}
