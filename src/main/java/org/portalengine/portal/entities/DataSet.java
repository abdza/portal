package org.portalengine.portal.entities;

import lombok.Data;

@Data
public class DataSet {
	
	private Integer number;
	private Integer totalPages;
	private Object[] dataRows;

}
