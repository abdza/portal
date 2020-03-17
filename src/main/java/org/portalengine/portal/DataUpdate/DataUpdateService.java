package org.portalengine.portal.DataUpdate;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.portalengine.portal.FileLink.FileLinkService;
import org.portalengine.portal.Tracker.Field.TrackerField;
import org.portalengine.portal.Tracker.Field.TrackerFieldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Service
public class DataUpdateService {
	
	@Autowired
	private DataUpdateRepository repo;
	
	@Autowired
	private TrackerFieldRepository trackerfieldrepo;
	
	@Autowired
	private NamedParameterJdbcTemplate jdbctemplate;
	
	@Autowired
	public DataUpdateService() {
	}

	public DataUpdateRepository getRepo() {
		return repo;
	}

	public void setRepo(DataUpdateRepository repo) {
		this.repo = repo;
	}
	
	public void deleteUpdate(DataUpdate dataupdate) {
		MapSqlParameterSource paramsource = new MapSqlParameterSource();
		String delquery = "delete from " + dataupdate.getTracker().getDataTable() + " where dataupdate_id=" + dataupdate.getId();
		jdbctemplate.update(delquery,paramsource);
		repo.deleteById(dataupdate.getId());
	}
	
	public void runupdate(DataUpdate dataupdate,FileLinkService fileservice) {
		Gson gson = new Gson();
		//System.out.println("Running update for " + dataupdate.getId().toString());
		
		Type type = new TypeToken<HashMap<String,String>>(){}.getType();
		HashMap<String, String> savedfield = gson.fromJson(dataupdate.getSavedParams(), type);
		ArrayList<String> fieldnames = new ArrayList<String>(); 
		savedfield.forEach((field,column)->{
			//System.out.println("Got field: " + field + " using column:" + column);
			if(!column.equals("ignore")) {
				fieldnames.add(field);
			}
		});
		
		String insertquery = "insert into " + dataupdate.getTracker().getDataTable() + " (dataupdate_id," + String.join(",",  fieldnames) + ") values (:dataupdate_id,:" + String.join(",:",fieldnames) + ")";
		//System.out.println("Query:" + insertquery);
		
		try {
			Workbook workbook = WorkbookFactory.create(fileservice.getFile(dataupdate.getFilelink()));
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rows = sheet.rowIterator();
			Row drow;
			Boolean stoprow=false;
			HashMap<String, TrackerField> tfields = new HashMap<String, TrackerField>();
			for(String field:fieldnames) {
				TrackerField tfs = trackerfieldrepo.findByTrackerAndName(dataupdate.getTracker(), field);
				tfields.put(field,tfs);
			}
			while(rows.hasNext() && !stoprow) {
				drow = rows.next();
				//System.out.println("Drow:" + drow.getRowNum());
				if(dataupdate.getDataEnd()==null || drow.getRowNum()<dataupdate.getDataEnd()) {
					if(drow.getRowNum()>=dataupdate.getDataRow()-1) {		
						MapSqlParameterSource paramsource = new MapSqlParameterSource();
						for(String field:fieldnames) {						
							//System.out.println("Field " + field + " got data:" + drow.getCell(Integer.parseInt(savedfield.get(field))));
							
							boolean gotdata = false;
							TrackerField tf = tfields.get(field);
							if(tf!=null) {
								Cell ccell = drow.getCell(Integer.parseInt(savedfield.get(field)));
								if(ccell!=null) {
									if(tf.getFieldType().contentEquals("String")||tf.getFieldType().contentEquals("Text")) {									
										if(ccell.getCellType()==CellType.NUMERIC) {
											if(String.valueOf(ccell.getNumericCellValue())!=null) {
												paramsource.addValue(field, String.valueOf(ccell.getNumericCellValue()),Types.VARCHAR);
												gotdata = true;
											}
										}
										else {
											if(ccell.getStringCellValue()!=null && ccell.getStringCellValue().length()>0) {
												paramsource.addValue(field, ccell.getStringCellValue(),Types.VARCHAR);
												gotdata = true;
											}
										}
									}
									else if(tf.getFieldType().contentEquals("Integer")||tf.getFieldType().contentEquals("Number")) {
										paramsource.addValue(field, ccell.getNumericCellValue(),Types.NUMERIC);
										gotdata = true;
									}
									else if(tf.getFieldType().contentEquals("Date")||tf.getFieldType().contentEquals("DateTime")) {
										if(ccell!=null) {
											if(ccell.getCellType()==CellType.NUMERIC) {
												if(ccell.getDateCellValue()!=null) {
													paramsource.addValue(field, ccell.getDateCellValue(),Types.DATE);
													gotdata = true;
												}
											}
											else {
												DateFormat format = new SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH);
												Date date;
												try {
													date = format.parse(ccell.getStringCellValue());
													if(date!=null) {
														paramsource.addValue(field, date,Types.DATE);
														gotdata = true;
													}
												} catch (ParseException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}										
											}
										}
									}
								}
							}
							if(!gotdata) {
								paramsource.addValue(field, null);
							}
						};
						paramsource.addValue("dataupdate_id", dataupdate.getId(),Types.NUMERIC);
						jdbctemplate.update(insertquery,paramsource);
					}
				}
			}
			workbook.close();
		} catch (EncryptedDocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
