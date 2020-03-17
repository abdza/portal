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
import org.portalengine.portal.FileLink.FileLink;
import org.portalengine.portal.FileLink.FileLinkService;
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

import com.google.gson.Gson;

@Controller
@RequestMapping("/dataupdates")
public class DataUpdateController {
	
	@Autowired
	private DataUpdateService service;
	
	@Autowired
	private TrackerService trackerservice;
	
	@Autowired
	private FileLinkService fileservice;
	
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
		model.addAttribute("dataupdates", service.getRepo().findAll(PageRequest.of(page, size)));
		return "dataupdate/list.html";
	}
	
	@GetMapping("/create")
	public String create(Model model) {
		List<Tracker> trackers = trackerservice.getRepo().findAll();
		model.addAttribute("dataupdate", new DataUpdate());
		model.addAttribute("trackers",trackers);
		return "dataupdate/form.html";
	}
	
	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Long id, Model model) {
		DataUpdate dataupdate = service.getRepo().findById(id).orElse(null);
		model.addAttribute("dataupdate", dataupdate);
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
		fileservice.getRepo().save(filelink);
		fileservice.SaveFile(file, filelink);
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
		Gson gson = new Gson();
		DataUpdate dataupdate = service.getRepo().getOne(id);
		Map<String, String[]> postdata = request.getParameterMap();
		HashMap<String, String> savedfield = new HashMap<String, String>();
		System.out.println("Request:" + gson.toJson(postdata));
		dataupdate.getTracker().getFields().forEach(field->{
			if(postdata.get("col_" + field.getName())!=null) {
				savedfield.put(field.getName(), postdata.get("col_" + field.getName())[0]);
			}
		});
		dataupdate.setSavedParams(gson.toJson(savedfield));
		dataupdate.setUploadStatus((long)1);
		service.getRepo().save(dataupdate);
		System.out.println("Fields:" + gson.toJson(savedfield));
		return "redirect:/dataupdates/runupdate/" + id.toString();
	}
	
	@GetMapping("/runupdate/{id}")
	public String runupdate(@PathVariable Long id, Model model) {		
		DataUpdate dataupdate = service.getRepo().getOne(id);
		service.runupdate(dataupdate,fileservice);
		return "redirect:/dataupdates/display/" + id.toString();
	}
	
	@GetMapping("/setparam/{id}")
	public String setparam(@PathVariable Long id, Model model) {
		HashMap<Integer, String> columns = new HashMap<Integer, String>();
		DataUpdate dataupdate = service.getRepo().getOne(id);
		try {
			Workbook workbook = WorkbookFactory.create(fileservice.getFile(dataupdate.getFilelink()));
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rows = sheet.rowIterator();
			Row drow;
			Boolean stoprow=false;
			while(rows.hasNext() && !stoprow) {
				drow = rows.next();
				if(drow.getRowNum()<dataupdate.getHeaderEnd()) {
					if(drow.getRowNum()>=dataupdate.getHeaderStart()-1) {
						Iterator<Cell> cells = drow.cellIterator();					
						Cell cell;
						while(cells.hasNext()) {
							cell = cells.next();
							columns.put(cell.getColumnIndex(), cell.getStringCellValue());
							System.out.println("Name contents:" + cell.getStringCellValue());						
						}
					}
				}
				else {
					stoprow = true;
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
		model.addAttribute("columns",columns);
		model.addAttribute("dataupdate", dataupdate);
		return "dataupdate/setparam.html";
	}
}
