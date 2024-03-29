package org.portalengine.portal.services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import org.portalengine.portal.entities.FileLink;
import org.portalengine.portal.repositories.FileLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

@Service
public class FileLinkService {
	
	@Autowired
	private FileLinkRepository repo;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Value("${portal.uploadroot}")
	private String uploadroot;
	
	public FileLinkRepository getRepo() {
		return repo;
	}

	public void setRepo(FileLinkRepository repo) {
		this.repo = repo;
	}

	@Autowired
	public FileLinkService() {
	}
	
	public Resource getResource(FileLink filelink) {
		Resource dresource = resourceLoader.getResource("file:" + filelink.getPath());
		return dresource;
	}
	
	public File getFile(FileLink filelink) {
		File file = new File(filelink.getPath());
		return file;
	}
	
	public String base64(FileLink filelink) {
		return base64(filelink.getPath());
	}
	
	public String base64(String path) {
		Path fpath = Paths.get(path);
		String encodedString = null;
		byte[] data;
		try {
			data = Files.readAllBytes(fpath);
			encodedString = Base64.getEncoder().encodeToString(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		 
		return encodedString;
	}
	
	public void deleteById(Long id) {
		FileLink fileLink = repo.findById(id).orElse(null);
		if(fileLink!=null) {
			if(fileLink.getPath()!=null && fileLink.getPath().length()>0) {
				File fpath = new File(fileLink.getPath());			
				if(fpath.exists()) {
					fpath.delete();
				}
			}
			repo.deleteById(id);
		}
	}
	
	public String SaveTmpFile(MultipartFile file) {
		String filepath=null;
		if(file.getOriginalFilename().length()>0) {
			String curfolder = System.getProperty("user.dir");
			if(!Paths.get(uploadroot).isAbsolute()) {
				uploadroot = curfolder + "/" + uploadroot;
			}		
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
	
	public FileLink SaveFile(InputStream file, FileLink filelink) {
		try {
			if(file.available()>0) {
				String curfolder = System.getProperty("user.dir");
				if(!Paths.get(uploadroot).isAbsolute()) {
					uploadroot = curfolder + "/" + uploadroot;
				}			
				String targetpath = uploadroot + "/" + filelink.getModule();			
				File fpath = new File(targetpath);			
				if(!fpath.exists()) {
					fpath.mkdirs();
				}
				String slug = filelink.getSlug();
				if(slug.length()>6) {
					slug = slug.substring(slug.length()/2);
				}
				String filepath = targetpath + "/" + slug +  filelink.getName();

				FileOutputStream fout = new FileOutputStream(filepath);
				file.transferTo(fout);
				filelink.setPath(filepath);
			}
			else {
				if(filelink.getId()!=null) {
					FileLink cfile = repo.getById(filelink.getId());
					if(cfile!=null) {
						filelink.setName(cfile.getName());
						filelink.setPath(cfile.getPath());
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return filelink;
	}
	
	public FileLink SaveFile(MultipartFile file, FileLink filelink) {
		if(file.getOriginalFilename().length()>0) {
			String curfolder = System.getProperty("user.dir");
			if(!Paths.get(uploadroot).isAbsolute()) {
				uploadroot = curfolder + "/" + uploadroot;
			}			
			String targetpath = uploadroot + "/" + filelink.getModule();			
			File fpath = new File(targetpath);			
			if(!fpath.exists()) {
				fpath.mkdirs();
			}
			String filepath = targetpath + "/" + file.getOriginalFilename();
			try {
				file.transferTo(new File(filepath));
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			filelink.setName(file.getOriginalFilename());
			filelink.setPath(filepath);
		}
		else {
			if(filelink.getId()!=null) {
				FileLink cfile = repo.getById(filelink.getId());
				if(cfile!=null) {
					filelink.setName(cfile.getName());
					filelink.setPath(cfile.getPath());
				}
			}
		}
		return filelink;
	}
	
	public static BufferedImage generateQRCodeImage(String barcodeText) throws Exception {
		return generateQRCodeImage(barcodeText,200,200);
	}
	
	public static BufferedImage generateQRCodeImage(String barcodeText, Integer width, Integer height) throws Exception {
	    QRCodeWriter barcodeWriter = new QRCodeWriter();
	    BitMatrix bitMatrix = 
	      barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, width, height);
	 
	    return MatrixToImageWriter.toBufferedImage(bitMatrix);
	    
	}

}
