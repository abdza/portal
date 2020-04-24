package org.portalengine.portal.Tree;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.portalengine.portal.Auditable;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Data;

@Data
@Entity
@Table(name = "portal_tree_node")
public class TreeNode extends Auditable<String> {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@ManyToOne( fetch = FetchType.EAGER )
	@JoinColumn( name = "tree_id" )
	@JsonBackReference
	private Tree tree; 
	
	@NotNull
	private String name;
	
	@NotNull
	private String slug;
	
	@NotNull
	private String fullpath;
	
	private String objectType;
	
	private Long objectId;
	
	@OneToMany(
			mappedBy = "node",
			orphanRemoval = true)
	private List<TreeUser> users = new ArrayList<>();
	
	@OneToMany(
			mappedBy = "parent",
			orphanRemoval = true)
	@OrderBy("lft ASC")
	@JsonManagedReference
	@JsonBackReference
	private List<TreeNode> children = new ArrayList<>();
	
	@ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "parent_id" )
	@JsonIgnore
	private TreeNode parent;
	
	@NotNull
	private Long lft;
	
	@NotNull
	private Long rgt;
}
