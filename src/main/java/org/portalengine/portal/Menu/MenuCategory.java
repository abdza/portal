package org.portalengine.portal.Menu;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.portalengine.portal.Auditable;
import org.portalengine.portal.Tree.TreeNode;

import lombok.Data;

@Data
@Entity
@Table(name = "portal_menu_category")
public class MenuCategory extends Auditable<String> {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	private String title;
	
	private Integer pos_num;
	
	@ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "menu_id" )
	private Menu menu;

}
