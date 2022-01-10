package org.portalengine.portal.entities;

import java.util.HashMap;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import lombok.Data;

@Data
public class CustomQuery {	
	
	private MapSqlParameterSource paramsource;	
	
	private NamedParameterJdbcTemplate namedjdbctemplate;
	
	public CustomQuery(NamedParameterJdbcTemplate namedjdbctemplate) {
		this.namedjdbctemplate = namedjdbctemplate;
		this.paramsource = new MapSqlParameterSource();
	}
	
	public void update(String query) {
		namedjdbctemplate.update(query, paramsource);
	}
	
	public void addValue(String name, Object data, int type) {
		this.paramsource.addValue(name, data, type);
	}
	
	public void addValue(String name, Object data) {
		this.paramsource.addValue(name, data);
	}
	
	public SqlRowSet query(String query) {
		SqlRowSet toret = this.namedjdbctemplate.queryForRowSet(query, this.paramsource);
		return toret;
	}

}
