package org.portalengine.portal.DataUpdate;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.validation.Valid;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.portalengine.portal.PoiExcel;
import org.portalengine.portal.FileLink.FileLink;
import org.portalengine.portal.FileLink.FileLinkService;
import org.portalengine.portal.Page.PortalPage;
import org.portalengine.portal.Page.PageService;
import org.portalengine.portal.Tracker.Tracker;
import org.portalengine.portal.Tracker.TrackerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/dataupdates")
public class DataUpdateController {
	
	@Autowired
	private DataUpdateService service;
	
	@Autowired
	private TrackerService trackerService;
	
	@Autowired
	private PageService pageService;
	
	@Autowired
	private FileLinkService fileService;
	
	private DataSource datasource;
	
	private NamedParameterJdbcTemplate jdbctemplate;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	public DataUpdateController(DataSource datasource) {
		this.datasource = datasource;
		this.jdbctemplate = new NamedParameterJdbcTemplate(datasource);
	}

	@GetMapping
	public String list(HttpServletRequest request, Model model) {
		int page = 0;
		int size = 10;
		if(request.getParameter("page") != null && !request.getParameter("page").isEmpty()) {
			page = Integer.parseInt(request.getParameter("page"));
		}
		if(request.getParameter("size") != null && !request.getParameter("size").isEmpty()) {
			size = Integer.parseInt(request.getParameter("size"));
		}
		model.addAttribute("pageTitle","Data Update Listing");
		model.addAttribute("dataupdates", service.getRepo().findAll(PageRequest.of(page, size)));
		return "dataupdate/list.html";
	}
		
	@GetMapping(value={"/create","/edit/{id}"})
	public String form(@PathVariable(required=false) Long id, Model model) {
		if(id!=null) {
			DataUpdate dataupdate = service.getRepo().findById(id).orElse(null);
			model.addAttribute("pageTitle","Edit Data Update - " + dataupdate.getTracker().getName());
			model.addAttribute("dataupdate", dataupdate);
		}
		else {
			model.addAttribute("pageTitle","Create Data Update");
			model.addAttribute("dataupdate", new DataUpdate());
		}
		
		model.addAttribute("trackers",trackerService.getRepo().findAll());
		return "dataupdate/form.html";
	}
	
	@PostMapping("/delete/{id}")
	public String delete(@PathVariable Long id, Model model) {
		DataUpdate dataupdate = service.getRepo().findById(id).orElse(null);
		if(dataupdate!=null) {
			service.deleteUpdate(dataupdate);
		}
		return "redirect:/dataupdates";
	}
	
	@GetMapping("/display/{id}")
	public String display(@PathVariable Long id, Model model) {
		DataUpdate dataupdate = service.getRepo().getOne(id);
		model.addAttribute("pageTitle","Data Update - " + dataupdate.getTracker().getName());
		model.addAttribute("dataupdate", dataupdate);
		return "dataupdate/display.html";
	}
	
	@PostMapping("/save")
	public String save(@RequestParam("file") MultipartFile file, @Valid DataUpdate dataupdate, Model model) {
		FileLink filelink = new FileLink();
		filelink.setModule(dataupdate.getTracker().getModule());
		filelink.setType("system");
		Date curdate = new Date();
		filelink.setSlug(dataupdate.getTracker().getModule() + "_upload_" + curdate.toString().replace(" ", "_"));
		filelink.setName(dataupdate.getTracker().getModule() + "_upload_" + curdate.toString().replace(" ", "_"));
		fileService.getRepo().save(filelink);
		fileService.SaveFile(file, filelink);
		dataupdate.setFilelink(filelink);
		dataupdate.setUploadStatus((long) 0);
		if(dataupdate.getHeaderStart() == null || dataupdate.getHeaderStart()<1) {
			dataupdate.setHeaderStart((long) 1);
		}
		if(dataupdate.getHeaderEnd() == null || dataupdate.getHeaderEnd()<1) {
			dataupdate.setHeaderEnd((long) 1);
		}
		if(dataupdate.getDataRow() == null || dataupdate.getDataRow()<1) {
			dataupdate.setDataRow(dataupdate.getHeaderEnd()+1);
		}
		service.getRepo().save(dataupdate);
		return "redirect:/dataupdates/setparam/" + dataupdate.getId().toString();
	}
	
	@PostMapping("/setparam/{id}")
	public String saveparam(@PathVariable Long id, Model model) {
		DataUpdate dataupdate = service.getRepo().getOne(id);
		Map<String, String[]> postdata = request.getParameterMap();
		HashMap<String, Object> savedfield = new HashMap<String, Object>();
		
		dataupdate.getTracker().getFields().forEach(field->{
			boolean savefield = false;
			HashMap<String, String> fieldparams = new HashMap<String, String>();
			if(postdata.get("col_" + field.getName())!=null) {
				fieldparams.put("column", postdata.get("col_" + field.getName())[0]);
				savefield = true;
			}
			if(postdata.get("manual_" + field.getName())!=null) {
				fieldparams.put("manual", postdata.get("manual_" + field.getName())[0]);
			}
			if(postdata.get("key_" + field.getName())!=null) {
				fieldparams.put("key", postdata.get("key_" + field.getName())[0]);
			}
			if(savefield) {
				savedfield.put(field.getName(),fieldparams);
			}
		});
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			dataupdate.setSavedParams(mapper.writeValueAsString(savedfield));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dataupdate.setUploadStatus((long)1);
		service.getRepo().save(dataupdate);
		return "redirect:/dataupdates/runupdate/" + id.toString();
	}
	
	@GetMapping("/runupdate/{id}")
	public String runupdate(@PathVariable Long id, Model model) {		
		DataUpdate dataupdate = service.getRepo().getOne(id);
		service.runupdate(dataupdate);		
		PortalPage postpage = pageService.getRepo().findOneByModuleAndSlug(dataupdate.getTracker().getModule(),dataupdate.getTracker().getPostDataUpdate());
		if(postpage!=null) {			
			return "redirect:/view/" + dataupdate.getTracker().getModule() + "/" + postpage.getSlug() + "/" + id.toString();
		}
		else {			
			return "redirect:/dataupdates/display/" + id.toString();
		}
	}
	
	@GetMapping("/setparam/{id}")
	public String setparam(@PathVariable Long id, Model model) {
		HashMap<Integer, String> columns = new HashMap<Integer, String>();
		
		DataUpdate dataupdate = service.getRepo().getOne(id);
		PoiExcel poiExcel = new PoiExcel();
		poiExcel.setLimits(dataupdate.getHeaderStart().intValue(), dataupdate.getHeaderEnd().intValue(), dataupdate.getHeaderEnd().intValue()+1);
		
		try {
			List<Object> fields = poiExcel.getHeaders(dataupdate.getFilelink().getPath());
			for(Object field: fields) {
				HashMap<String, String> cfield = (HashMap<String, String>)field;
				columns.put(Integer.parseInt(cfield.get("col")), cfield.get("text"));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		model.addAttribute("pageTitle","Set Columns - " + dataupdate.getTracker().getName());
		model.addAttribute("columns",columns);
		model.addAttribute("dataupdate", dataupdate);
		return "dataupdate/setparam.html";
	}
}
