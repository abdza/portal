package org.portalengine.portal.Menu;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.portalengine.portal.Auditable;
import org.portalengine.portal.Tree.TreeNode;

import lombok.Data;

@Data
@Entity
@Table(name = "portal_menu")
public class Menu extends Auditable<String> {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	private String title;

}
