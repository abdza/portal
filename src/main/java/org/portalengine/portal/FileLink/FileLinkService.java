package org.portalengine.portal.FileLink;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

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
	
	public void deleteById(Long id) {
		FileLink fileLink = repo.findById(id).orElse(null);
		if(fileLink!=null) {
			File fpath = new File(fileLink.getPath());			
			if(!fpath.exists()) {
				fpath.delete();
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
				System.out.println("Filepath:" + filepath);
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
				FileLink cfile = repo.getOne(filelink.getId());
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
