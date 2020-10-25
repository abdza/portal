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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.portalengine.portal.Auditable;
import org.springframework.beans.factory.annotation.Value;

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
	
	@JsonIgnore
	@ManyToOne( fetch = FetchType.EAGER )
	@JoinColumn( name = "tree_id" )	
	private Tree tree; 
	
	@NotNull
	private String name;
	
	@NotNull
	private String slug;
	
	@NotNull
	private String fullPath;
	
	private String objectType;
	
	private String status;
	
	private Long objectId;
	
	private Long recordId;
	
	@OneToMany(
			mappedBy = "node",
			orphanRemoval = true)
	private List<TreeUser> users = new ArrayList<>();	
	
	@OneToMany(
			mappedBy = "parent",
			orphanRemoval = true)
	@OrderBy("lft ASC")
	private List<TreeNode> children = new ArrayList<>();	
	
	@JsonIgnore
	@ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "parent_id" )	
	private TreeNode parent;
	
	@NotNull
	private Long lft;
	
	@NotNull
	private Long rgt;	

}
