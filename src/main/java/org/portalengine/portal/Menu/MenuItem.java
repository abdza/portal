package org.portalengine.portal.Menu;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.portalengine.portal.Auditable;
import org.portalengine.portal.FileLink.FileLink;
import org.portalengine.portal.Setting.Setting;
import org.portalengine.portal.Tracker.Tracker;
import org.portalengine.portal.Tree.TreeNode;

import lombok.Data;

@Data
@Entity
@Table(name = "portal_menu_item")
public class MenuItem extends Auditable<String> {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	private String title;
	
	private Integer pos_num;
	
	@OneToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "tree_node_id" )
	private TreeNode treeNode;
	
	@ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "menu_category_id" )
	private MenuCategory category;
	
	@ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "menu_id" )
	private Menu menu;

}
