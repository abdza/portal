package org.portalengine.portal.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.portalengine.portal.entities.TrackerFile;
import org.portalengine.portal.services.TrackerFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin/trackers/files")
public class TrackerFileController {
	@Autowired
	private TrackerFileService service;
	
	@Autowired
	public TrackerFileController() {
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
		model.addAttribute("files", service.getRepo().findAll(PageRequest.of(page, size)));
		return "file/list.html";
	}
	
	@GetMapping(value= {"/create","/edit/{id}"})
	public String form(@RequestParam(required=false) Long id, Model model) {
		TrackerFile curfile;
		if(id!=null) {
			curfile = service.getRepo().getOne(id);
		}
		else {
			curfile = new TrackerFile();
		}
		model.addAttribute("filelink", curfile);
		return "file/form.html";
	}
	
	@GetMapping("/download/{id}")
	@ResponseBody
	public ResponseEntity<Resource> download(@PathVariable Long id, Model model) {
		TrackerFile curfile = service.getRepo().getOne(id);
		Resource resfile = service.getResource(curfile);
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=" + curfile.getName()).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resfile);
	}
	
	@PostMapping("/save")
	public String save(@RequestParam("file") MultipartFile file, @Valid TrackerFile trackerFile,Model model) {
		if(file != null) {
			trackerFile.setType("user");
			trackerFile = service.SaveFile(file, trackerFile);
		}
		service.getRepo().save(trackerFile);
		return "redirect:/admin/trackers/files";
	}
	
	@PostMapping("/delete/{id}")
	public String delete(@PathVariable Long id, Model model) {
		service.getRepo().deleteById(id);
		return "redirect:/admin/trackers/files";
	}

}
