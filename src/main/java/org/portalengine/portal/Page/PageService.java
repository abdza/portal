package org.portalengine.portal.Page;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.TemplateSpec;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;

@Service
public class PageService {

	@Autowired
	private PageRepository repo;
	
	@Autowired
	private SpringTemplateEngine templateEngine;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private HttpServletResponse response;
	
	@Autowired
	private ServletContext servletContext;
	
	private final static String TEMPLATE_LOCAL = "US";
	
	@Autowired
	public PageService() {
	}

	public PageRepository getRepo() {
		return repo;
	}

	public void setRepo(PageRepository repo) {
		this.repo = repo;
	}
	
	public String getTemplateFromMap(String htmlContent, Map<String, Object> dynamicAttributesMap) {

		String template = null;
		
		final WebContext wctx = new WebContext(request, response, servletContext);
		
		if(!CollectionUtils.isEmpty(dynamicAttributesMap)) {
			dynamicAttributesMap.forEach((k,v)->wctx.setVariable(k, v));
		}
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("locale",TEMPLATE_LOCAL);
		final TemplateSpec tempspec = new TemplateSpec(htmlContent, attributes);
		if(null != templateEngine) {
			template = templateEngine.process(tempspec, wctx);
		}
		return template;
	}
	
	public String renderPage(String slug) {	
		return renderPage("portal",slug);
	}
	
	public String renderPage(String module, String slug) {		
		try {
			PortalPage curpage = this.repo.findOneByModuleAndSlug(module, slug);
			if(curpage!=null) {
				String toreturn = getTemplateFromMap(curpage.getContent(), null);
				if(toreturn.trim().length()>0) {
					return toreturn;
				}
				else {					
					return "<!-- -->";
				}
			}
		}
		catch(Exception exp) {
			System.out.println("Slug not found:" + slug);
		}		
		return "<!-- -->";
	}
	
	public boolean pageExist(String slug) {
		try {
			PortalPage curpage = this.repo.findOneByModuleAndSlug("portal", slug);
			if(curpage!=null) {
				return true;
			}
		}
		catch(Exception exp) {
			System.out.println("Slug not found:" + slug);
		}
		return false;
	}
	
	public String urlParamUpdate(String param, String value) {		
		ServletUriComponentsBuilder urlBuilder = ServletUriComponentsBuilder.fromCurrentRequest();
		urlBuilder.replaceQueryParam(param, value);		
		String result = urlBuilder.build().toUriString();
		return result;
	}
	
	public String urlChangePath(String newPath) {
		ServletUriComponentsBuilder urlBuilder = ServletUriComponentsBuilder.fromCurrentRequest();
		urlBuilder.replacePath(newPath);		
		String result = urlBuilder.build().toUriString();
		return result;
	}
}
