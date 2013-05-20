/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryConfig;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB.SDBDatasetMode;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.filter.LanguageFilteringRDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.filter.LanguageFilteringUtils;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

public class WebappDaoFactorySDBPrep implements Filter {
	
	private final static Log log = LogFactory.getLog(WebappDaoFactorySDBPrep.class);
	
	ServletContext _ctx;

    /**
     * The filter will be applied to all incoming urls,
     this is a list of URI patterns to skip.  These are
     matched against the requestURI sans query parameters,
     * e.g.
     * "/vitro/index.jsp"
     * "/vitro/themes/enhanced/css/edit.css"
     *
     * These patterns are from VitroRequestPrep.java
    */
    Pattern[] skipPatterns = {
            Pattern.compile(".*\\.(gif|GIF|jpg|jpeg)$"),
            Pattern.compile(".*\\.css$"),
            Pattern.compile(".*\\.js$"),
            Pattern.compile("/.*/themes/.*/site_icons/.*"),
            Pattern.compile("/.*/images/.*")
    };
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		
		if ( request.getAttribute("WebappDaoFactorySDBPrep.setup") != null ) {
			// don't run multiple times
		    filterChain.doFilter(request, response);
			return;
		}
		
		for( Pattern skipPattern : skipPatterns){
            Matcher match =skipPattern.matcher( ((HttpServletRequest)request).getRequestURI() );
            if( match.matches()  ){
                log.debug("request matched a skipPattern, skipping VitroRequestPrep"); 
                filterChain.doFilter(request, response);
                return;
            }
        }
		
        String defaultNamespace = (String) _ctx.getAttribute("defaultNamespace");
		WebappDaoFactory wadf = null;
		VitroRequest vreq = new VitroRequest((HttpServletRequest) request);

		log.debug("Accept-Language: " + vreq.getHeader("Accept-Language"));
		List<String> langs = LanguageFilteringUtils.localesToLanguages(vreq.getLocales());
        
        WebappDaoFactoryConfig config = new WebappDaoFactoryConfig();
        config.setDefaultNamespace(defaultNamespace);
        config.setPreferredLanguages(langs);
		
		RDFServiceFactory factory = RDFServiceUtils.getRDFServiceFactory(_ctx);
		
		//RDFService rdfService = factory.getRDFService();
		RDFService unfilteredRDFService = factory.getShortTermRDFService();
		RDFService rdfService = null;
		
		if (Boolean.valueOf(ConfigurationProperties.getBean(vreq).getProperty(
				"RDFService.languageFilter", "true"))) {
			rdfService = new LanguageFilteringRDFService(unfilteredRDFService, langs);

			OntModel rawDisplayModel = ModelAccess.on(vreq.getSession()).getDisplayModel();
			OntModel filteredDisplayModel = LanguageFilteringUtils.wrapOntModelInALanguageFilter(rawDisplayModel, request);
			ModelAccess.on(vreq).setDisplayModel(filteredDisplayModel);
		} else {
			rdfService = unfilteredRDFService;
		}
		
		Dataset dataset = new RDFServiceDataset(rdfService);

		OntModelSelector oms = ModelAccess.on(_ctx).getUnionOntModelSelector();
		wadf = new WebappDaoFactorySDB(rdfService, oms, config);
	    
		OntModelSelector baseOms = ModelAccess.on(_ctx).getBaseOntModelSelector();
		WebappDaoFactory assertions = new WebappDaoFactorySDB(
	            rdfService, baseOms, config, SDBDatasetMode.ASSERTIONS_ONLY);
		
	    vreq.setRDFService(rdfService);
	    vreq.setUnfilteredRDFService(unfilteredRDFService);
		vreq.setWebappDaoFactory(wadf);
		vreq.setAssertionsWebappDaoFactory(assertions);
		vreq.setFullWebappDaoFactory(wadf);
        vreq.setUnfilteredWebappDaoFactory(new WebappDaoFactorySDB(
                rdfService, ModelAccess.on(_ctx).getUnionOntModelSelector()));
		vreq.setDataset(dataset);
		
		ModelAccess.on(vreq).setJenaOntModel(
				ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, dataset.getDefaultModel()));
					
		request.setAttribute("WebappDaoFactorySDBPrep.setup", 1);
		
		try {
			filterChain.doFilter(request, response);
			return;
		} finally {
			if (wadf != null) {
			    wadf.close();
			}
		}
		
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		try {
			_ctx = filterConfig.getServletContext();
		} catch (Throwable t) {
			log.error("Unable to initialize WebappDaoFactorySDBPrep", t);
		}		
	}
	
	@Override
	public void destroy() {
		// no destroy actions
	}

}
