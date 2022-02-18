package org.portalengine.portal.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.portalengine.portal.entities.FileLink;
import org.portalengine.portal.entities.Tracker;
import org.portalengine.portal.services.FileLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/admin/files")
public class FileLinkController {
	
		@Autowired
		private FileLinkService service;
		
		@Autowired
		public FileLinkController() {
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
			model.addAttribute("pageTitle","File Listing");
			
			String search = "";
			Page<FileLink> toreturn = null;
			if(request.getParameter("q")!=null||(request.getParameter("module")!=null && !request.getParameter("module").equals("All"))) {
				System.out.println("doing query");
				String module = request.getParameter("module");
				search = "%" + request.getParameter("q").replace(" " , "%") + "%";
				Pageable pageable = PageRequest.of(page, size);
				if(module.equals("All")) {
					toreturn = service.getRepo().apiquery(search,pageable);
				}
				else {
					toreturn = service.getRepo().apimodulequery(search, module, pageable);
				}
			}
			else {
				toreturn = service.getRepo().findAll(PageRequest.of(page, size));
			}
			
			model.addAttribute("files", toreturn);
			
			//model.addAttribute("files", service.getRepo().findAll(PageRequest.of(page, size)));
			return "file/list.html";
		}
		
		@GetMapping(value={"/create","/edit/{id}"})
		public String form(@PathVariable(required=false) Long id, Model model) {
			if(id!=null) {
				FileLink curfile = service.getRepo().getById(id);
				model.addAttribute("pageTitle","Update File - " + curfile.getName());
				model.addAttribute("filelink", curfile);
			}
			else {
				model.addAttribute("pageTitle","Upload File");
				model.addAttribute("filelink", new FileLink());
			}
			return "file/form.html";
		}
		
		@GetMapping("/download/{id}")
		@ResponseBody
		public ResponseEntity<Resource> download(@PathVariable Long id, Model model) {
			FileLink curfile = service.getRepo().getById(id);
			Resource resfile = service.getResource(curfile);
			System.out.println("curfile:" + curfile.getName());
			if(curfile.getFileType().equals("Css")||curfile.getFileType().equals("Javascript")){
				System.out.println("scripters");
				return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + curfile.getName() + "\"").contentType(MediaType.TEXT_PLAIN).body(resfile);	
			}
			if(curfile.getFileType().equals("Image")){
				System.out.println("imagers");
				return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + curfile.getName() + "\"").contentType(MediaType.IMAGE_JPEG).body(resfile);	
			}
			System.out.println("just default");
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=\"" + curfile.getName() + "\"").contentType(MediaType.APPLICATION_OCTET_STREAM).body(resfile);
		}
		
		@PostMapping("/save")
		public String save(@RequestParam("file") MultipartFile file, @Valid FileLink filelink,Model model) {
			if(file != null) {
				filelink = service.SaveFile(file, filelink);
			}
			service.getRepo().save(filelink);
			return "redirect:/admin/files";
		}
		
		@PostMapping("/delete/{id}")
		public String delete(@PathVariable Long id, Model model) {
			service.deleteById(id);
			return "redirect:/admin/files";
		}
}
