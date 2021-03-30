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
import org.portalengine.portal.Page.Page;
import org.portalengine.portal.Page.PageService;
import org.portalengine.portal.Tracker.Tracker;
import org.portalengine.portal.Tracker.Field.TrackerField;
import org.portalengine.portal.Tracker.Field.TrackerFieldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
		
		System.out.println("Running update for " + dataupdate.getId().toString());
		System.out.println("Current json is:" + dataupdate.getSavedParams());
		
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
		
	}
}
