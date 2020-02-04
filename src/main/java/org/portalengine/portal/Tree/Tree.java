package org.portalengine.portal.Tree;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
@Entity
@Table(name = "portal_tree")
public class Tree {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@NotNull
	private String module;
	
	@NotNull
	private String slug;
	
	@NotNull
	private String name;
	
	@OneToMany(
			mappedBy = "tree",
			orphanRemoval = true)
	private List<TreeNode> nodes = new ArrayList<>();
	
	@OneToOne(cascade = CascadeType.ALL)
    @JoinTable(name = "portal_tree_root", 
      joinColumns = 
        { @JoinColumn(name = "tree_id", referencedColumnName = "id") },
      inverseJoinColumns = 
        { @JoinColumn(name = "node_id", referencedColumnName = "id") })
    private TreeNode root;
	
}
