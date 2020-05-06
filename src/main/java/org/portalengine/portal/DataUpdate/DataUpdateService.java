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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.portalengine.portal.PoiExcel;
import org.portalengine.portal.FileLink.FileLinkService;
import org.portalengine.portal.Tracker.Tracker;
import org.portalengine.portal.Tracker.Field.TrackerField;
import org.portalengine.portal.Tracker.Field.TrackerFieldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Service
public class DataUpdateService {
	
	@Autowired
	private DataUpdateRepository repo;
	
	@Autowired
	private TrackerFieldRepository fieldRepo;
	
	@Autowired
	private NamedParameterJdbcTemplate jdbctemplate;
	
	@Autowired
	private FileLinkService fileservice;
	
	@Autowired
	public DataUpdateService() {
	}

	public DataUpdateRepository getRepo() {
		return repo;
	}

	public void setRepo(DataUpdateRepository repo) {
		this.repo = repo;
	}
	
	public void deleteUpdateByTracker(Tracker tracker) {
		List<DataUpdate> updates = repo.findAllByTracker(tracker);
		for(DataUpdate update:updates) {
			Long linkId = update.getFilelink().getId();
			repo.deleteById(update.getId());
			if(linkId!=null) {
				fileservice.deleteById(linkId);
			}			
		}
	}
	
	public void deleteUpdate(DataUpdate dataupdate) {
		MapSqlParameterSource paramsource = new MapSqlParameterSource();
		String delquery = "delete from " + dataupdate.getTracker().getDataTable() + " where dataupdate_id=" + dataupdate.getId();
		jdbctemplate.update(delquery,paramsource);
		repo.deleteById(dataupdate.getId());
	}
	
	public void runupdate(DataUpdate dataupdate) {
		// Gson gson = new Gson();
		// Type type = new TypeToken<HashMap<String,String>>(){}.getType();
		// HashMap<String, String> savedfield = gson.fromJson(dataupdate.getSavedParams(), type);
		
		System.out.println("Running update for " + dataupdate.getId().toString());
		System.out.println("Current json is:" + dataupdate.getSavedParams());
		
		
		//ArrayList<String> fieldnames = new ArrayList<String>(); 
		
		ArrayList<TrackerField> fields = new ArrayList<TrackerField>();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode savefield;
		try {
			savefield = mapper.readTree(dataupdate.getSavedParams());			
			System.out.println("savefield is:" + savefield.toString());
			Iterator<Entry<String, JsonNode>> nodes = savefield.fields();
			while (nodes.hasNext()) {
				Map.Entry<String, JsonNode> field = (Map.Entry<String, JsonNode>) nodes.next();
				System.out.println("Curnode:" + field.toString());
				//Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) field;
				if(!field.getValue().asText().equals("ignore")) {
					TrackerField curfield = fieldRepo.findByTrackerAndName(dataupdate.getTracker(), field.getKey());
					fields.add(curfield);
					System.out.println("Adding field:" + curfield.getName());
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PoiExcel poiExcel = new PoiExcel();
		poiExcel.setLimits(dataupdate.getHeaderStart().intValue(), dataupdate.getHeaderEnd().intValue(), dataupdate.getHeaderEnd().intValue()+1);
		JsonNode savedparams;
		try {
			savedparams = mapper.readTree(dataupdate.getSavedParams());
			poiExcel.loadData(dataupdate.getFilelink().getPath(), jdbctemplate, savedparams, fields, dataupdate.getTracker().getDataTable(), dataupdate.getId().intValue(), false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		/* savedfield.forEach((field,column)->{
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
												DataFormatter fmt = new DataFormatter();
												String curcontent = fmt.formatCellValue(ccell);
												paramsource.addValue(field, curcontent,Types.VARCHAR);
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
		}*/
	}
}
