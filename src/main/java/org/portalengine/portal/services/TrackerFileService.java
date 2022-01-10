package org.portalengine.portal.services;

import java.io.File;
import java.io.IOException;

import org.portalengine.portal.entities.TrackerFile;
import org.portalengine.portal.repositories.TrackerFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Service
@Data
public class TrackerFileService {

	@Autowired
	private TrackerFileRepository repo;
	
	@Autowired
	private ResourceLoader resourceLoader;

	@Value("${portal.uploadroot}")
	private String uploadroot;

	@Autowired
	public TrackerFileService() {
	}
	
	public Resource getResource(TrackerFile trackerFile) {
		Resource dresource = resourceLoader.getResource("file:" + trackerFile.getPath());
		return dresource;
	}
	
	public File getFile(TrackerFile trackerFile) {
		File file = new File(trackerFile.getPath());
		return file;
	}
	
	public String SaveTmpFile(MultipartFile file) {
		String filepath=null;
		if(file.getOriginalFilename().length()>0) {
			String targetpath = uploadroot + "/tmp";			
			File fpath = new File(targetpath);			
			if(!fpath.exists()) {
				fpath.mkdirs();
			}
			filepath = targetpath + "/" + file.getOriginalFilename();
			try {
				file.transferTo(new File(filepath));
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return filepath;
	}
	
	public TrackerFile SaveFile(MultipartFile file, TrackerFile trackerFile) {
		if(file.getOriginalFilename().length()>0) {
			String targetpath = uploadroot + "/" + trackerFile.getTracker().getModule();			
			File fpath = new File(targetpath);			
			if(!fpath.exists()) {
				fpath.mkdirs();
			}
			String filepath = targetpath + "/" + file.getOriginalFilename();
			try {
				System.out.println("Filepath:" + filepath);
				file.transferTo(new File(filepath));
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			trackerFile.setName(file.getOriginalFilename());
			trackerFile.setPath(filepath);
		}
		else {
			if(trackerFile.getId()!=null) {
				TrackerFile cfile = repo.getOne(trackerFile.getId());
				if(cfile!=null) {
					trackerFile.setName(cfile.getName());
					trackerFile.setPath(cfile.getPath());
				}
			}
		}
		return trackerFile;
	}
	
}
