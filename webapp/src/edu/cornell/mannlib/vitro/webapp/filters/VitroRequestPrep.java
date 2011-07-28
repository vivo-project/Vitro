/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import static edu.cornell.mannlib.vitro.webapp.controller.VitroRequest.SPECIAL_WRITE_MODEL;
import static edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary.CONTEXT_DISPLAY_TBOX;
import static edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary.SWITCH_TO_DISPLAY_MODEL;
import static edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary.USE_DISPLAY_MODEL_PARAM;
import static edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary.USE_MODEL_PARAM;
import static edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary.USE_TBOX_MODEL_PARAM;

import java.io.IOException;
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
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.AccessSpecialDataModels;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManageMenus;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.WebappDaoFactoryFiltering;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.FilterFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.HideFromDisplayByPolicyFilter;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;

/**
 * This sets up several objects in the Request scope for each
 * incoming HTTP request.  This is done in a Filter so
 * that controllers and JSPs get the same setup.
 *
 * This code configures the WebappDaoFactory for each request.
 * 
 * @author bdc34
 *
 */
public class VitroRequestPrep implements Filter {
	private static final Log log = LogFactory.getLog(VitroRequestPrep.class.getName());

	/**
	 * The filter will be applied to all incoming requests, but should skip any
	 * request whose URI matches any of these patterns. These are matched
	 * against the requestURI without query parameters, e.g. "/vitro/index.jsp"
	 * "/vitro/themes/enhanced/css/edit.css"
	 */
    private static final Pattern[] skipPatterns = {
            Pattern.compile(".*\\.(gif|GIF|jpg|jpeg)$"),
            Pattern.compile(".*\\.css$"),
            Pattern.compile(".*\\.js$"),
            Pattern.compile("/.*/themes/.*/site_icons/.*"),
            Pattern.compile("/.*/images/.*")
    };

    private ServletContext _context;
    private ApplicationBean _appbean;    
    
    @Override
	public void init(FilterConfig filterConfig) throws ServletException {
        _context = filterConfig.getServletContext();
        
        Object o =  _context.getAttribute("applicationBean");
        if (o instanceof ApplicationBean) {
            _appbean = (ApplicationBean) o; 
        } else {
            _appbean = new ApplicationBean();
        }
        log.debug("VitroRequestPrep: AppBean theme " + _appbean.getThemeDir());
    }    
    
    @Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
    	// If this isn't an HttpServletRequest, we might as well fail now.
    	HttpServletRequest req = (HttpServletRequest) request;
    	HttpServletResponse resp = (HttpServletResponse) response;
    	logRequestUriForDebugging(req);

        //don't waste time running this filter again.
        if( req.getAttribute("VitroRequestPrep.setup") != null ){
            log.debug("VitroRequestPrep has already been executed at least once, not re-executing.");
            Integer a =(Integer) req.getAttribute("VitroRequestPrep.setup");
            req.setAttribute("VitroRequestPrep.setup", new Integer( a + 1 ) );
            chain.doFilter(req, response);
            return;
        }

        // don't run this filter for image files, CSS files, etc.
        for( Pattern skipPattern : skipPatterns){
            Matcher match =skipPattern.matcher( req.getRequestURI() );
            if( match.matches()  ){
                log.debug("request matched a skipPattern, skipping VitroRequestPrep"); 
                chain.doFilter(req, response);
                return;
            }
        }

        // If we're not authorized for this request, skip the chain and redirect.
        if (!authorizedForSpecialModel(req)) {
        	VitroHttpServlet.redirectUnauthorizedRequest(req, resp);
        	return;
        }
        
        VitroRequest vreq = new VitroRequest(req);
        
        //-- setup appBean --//
        vreq.setAppBean(_appbean);
        
        //-- setup DAO factory --//
        WebappDaoFactory wdf = getWebappDaoFactory(vreq);
        //TODO: get accept-language from request and set as preferred languages
        
        // if there is a WebappDaoFactory in the session, use it
    	Object o = req.getSession().getAttribute("webappDaoFactory");
    	if (o instanceof WebappDaoFactory) {
    		wdf = (WebappDaoFactory) o;
    		log.debug("Found a WebappDaoFactory in the session and using it for this request");
    	}
    	
    	//replace the WebappDaoFactory with a different version if menu management parameter is found
    	wdf = checkForSpecialWDF(vreq, wdf);
    	
    	//get any filters from the ContextFitlerFactory
        VitroFilters filters = getFiltersFromContextFilterFactory(req, wdf);
        if( filters != null ){
            log.debug("Wrapping WebappDaoFactory in filters from ContextFitlerFactory");
            wdf = new WebappDaoFactoryFiltering(wdf, filters);
        }
                               
		/*
		 * display filtering happens now at any level, all the time; editing
		 * pages get their WebappDaoFactories differently
		 */
		HideFromDisplayByPolicyFilter filter = new HideFromDisplayByPolicyFilter(
				RequestIdentifiers.getIdBundleForRequest(req),
				ServletPolicyList.getPolicies(_context));
		vreq.setWebappDaoFactory(new WebappDaoFactoryFiltering(wdf, filter));
		
        // support for Dataset interface if using Jena in-memory model
        if (vreq.getDataset() == null) {
        	Dataset dataset = WebappDaoFactoryJena.makeInMemoryDataset(
        	        vreq.getAssertionsOntModel(), vreq.getInferenceOntModel());
        	vreq.setDataset(dataset);
        }
        
        vreq.setUnfilteredWebappDaoFactory(new WebappDaoFactorySDB(
                ModelContext.getUnionOntModelSelector(
                        vreq.getSession().getServletContext()),
                        vreq.getDataset()));
        
        req.setAttribute("VitroRequestPrep.setup", new Integer(1));
        chain.doFilter(req, response);
    }

	private WebappDaoFactory getWebappDaoFactory(VitroRequest vreq){
    	WebappDaoFactory webappDaoFactory = vreq.getWebappDaoFactory();
        return (webappDaoFactory != null) ? webappDaoFactory :
        	(WebappDaoFactory) _context.getAttribute("webappDaoFactory");
    }

    private VitroFilters getFiltersFromContextFilterFactory( HttpServletRequest request, WebappDaoFactory wdf){
        FilterFactory ff = (FilterFactory)_context.getAttribute("FilterFactory");
        if( ff == null ){ 
            return null;
        } else {
            return ff.getFilters(request, wdf);
        }
    }
    
	private boolean authorizedForSpecialModel(HttpServletRequest req) {
		if (isParameterPresent(req, SWITCH_TO_DISPLAY_MODEL)) {
			return PolicyHelper.isAuthorizedForActions(req, new ManageMenus());
		} else if (anyOtherSpecialProperties(req)){
			return PolicyHelper.isAuthorizedForActions(req, new AccessSpecialDataModels());
		} else {
			return true;
		}
	}

    @Override
	public void destroy() {
    	// Nothing to do.
    }

	/**
	 * Check if special model is requested - this is for enabling the use of a different
	 * model for menu management. Also enables the use of a completely different
	 * model and tbox if uris are passed.
	 */
    private WebappDaoFactory checkForSpecialWDF(VitroRequest vreq, WebappDaoFactory inputWadf) {
        //TODO: Does the dataset in the vreq get set when using a special WDF? Does it need to?
        //TODO: Does the unfiltered WDF get set when using a special WDF? Does it need to?
        
    	// If this isn't a Jena WADF, then there's nothing to be done.
    	if (!(inputWadf instanceof WebappDaoFactoryJena)) {
    		log.warn("Can't set special models: " +
    				"WebappDaoFactory is not a WebappDaoFactoryJena");
        	removeSpecialWriteModel(vreq);
    		return inputWadf;
    	}

    	WebappDaoFactoryJena wadf = (WebappDaoFactoryJena) inputWadf;
    	
    	// If they asked for the display model, give it to them.
		if (isParameterPresent(vreq, SWITCH_TO_DISPLAY_MODEL)) {
			OntModel mainOntModel = (OntModel)_context.getAttribute("displayOntModel");
			OntModel tboxOntModel = (OntModel) _context.getAttribute(CONTEXT_DISPLAY_TBOX);
	   		setSpecialWriteModel(vreq, mainOntModel);
			return createNewWebappDaoFactory(wadf, mainOntModel, tboxOntModel, null);
		}
    	
		// If they asked for other models by URI, set them.
		if (anyOtherSpecialProperties(vreq)) {
			BasicDataSource bds = JenaDataSourceSetupBase.getApplicationDataSource(_context);
			String dbType = ConfigurationProperties.getBean(_context)
					.getProperty("VitroConnection.DataSource.dbtype", "MySQL");

	    	OntModel mainOntModel = createSpecialModel(vreq, USE_MODEL_PARAM, bds, dbType);
	    	OntModel tboxOntModel = createSpecialModel(vreq, USE_TBOX_MODEL_PARAM, bds, dbType);
	    	OntModel displayOntModel = createSpecialModel(vreq, USE_DISPLAY_MODEL_PARAM, bds, dbType);
	   		setSpecialWriteModel(vreq, mainOntModel);
	    	return createNewWebappDaoFactory(wadf, mainOntModel, tboxOntModel, displayOntModel);
		}
		
		// Otherwise, there's nothing special about this request.
    	removeSpecialWriteModel(vreq);
		return wadf;

    }

	private boolean anyOtherSpecialProperties(HttpServletRequest req) {
		return isParameterPresent(req, USE_MODEL_PARAM)
				|| isParameterPresent(req, USE_TBOX_MODEL_PARAM)
				|| isParameterPresent(req, USE_DISPLAY_MODEL_PARAM);
	}

	/**
	 * If the request asks for a special model by URI, create it from the
	 * Database.
	 * 
	 * @return the model they asked for, or null if they didn't ask for one.
	 * @throws IllegalStateException
	 *             if it's not found.
	 */
	private OntModel createSpecialModel(VitroRequest vreq, String key,
			BasicDataSource bds, String dbType) {
		if (!isParameterPresent(vreq, key)) {
			return null;
		}
		
		String modelUri = vreq.getParameter(key);
		Model model = JenaDataSourceSetupBase.makeDBModel(bds, modelUri,
				OntModelSpec.OWL_MEM,
				JenaDataSourceSetupBase.TripleStoreType.RDB, dbType, _context);
		if (model != null) {
			return ModelFactory
					.createOntologyModel(OntModelSpec.OWL_MEM, model);
		} else {
			throw new IllegalStateException("Main Model Uri " + modelUri
					+ " did not retrieve model");
		}
	}

	private void removeSpecialWriteModel(VitroRequest vreq) {
		if (vreq.getAttribute(SPECIAL_WRITE_MODEL) != null) {
			vreq.removeAttribute(SPECIAL_WRITE_MODEL);
		}
	}
	
	private void setSpecialWriteModel(VitroRequest vreq, OntModel mainOntModel) {
		if (mainOntModel != null) {
			vreq.setAttribute(SPECIAL_WRITE_MODEL, mainOntModel);
		}
	}

	/**
	 * The goal here is to return a new WDF that is set to
	 * have the mainOntModel as its ABox, the tboxOntModel as it
	 * TBox and displayOntModel as it display model.
	 * 
	 * Right now this is achieved by creating a copy of 
	 * the WADF, and setting the special models onto it.
	 *  
	 * If a model is null, it will have no effect.
	 */
	private WebappDaoFactory createNewWebappDaoFactory(
			WebappDaoFactoryJena inputWadf, OntModel mainOntModel,
			OntModel tboxOntModel, OntModel displayOntModel) {
	    
		WebappDaoFactoryJena wadfj = new WebappDaoFactoryJena(inputWadf);
		wadfj.setSpecialDataModel(mainOntModel, tboxOntModel, displayOntModel);
		return wadfj;
	}

	private boolean isParameterPresent(HttpServletRequest req, String key) {
		return getNonEmptyParameter(req, key) != null;
	}

	/**
	 * Return a non-empty parameter from the request, or a null.
	 */
	private String getNonEmptyParameter(HttpServletRequest req, String key) {
		String value = req.getParameter(key);
		if ((value == null) || value.isEmpty()) {
			return null;
		} else {
			return value;
		}
	}
	
	private void logRequestUriForDebugging(HttpServletRequest req) {
		if (log.isDebugEnabled()) {
			try {
				String uriString = req.getRequestURI();
				String queryString = req.getQueryString();
				if ((queryString != null) && (queryString.length() > 0)) {
					uriString += "?" + queryString;
				}
				log.debug("RequestURI: " + uriString);
			} catch (Exception e) {
				// Don't want to kill the request if the logging fails.
			}
		}
	}
	
}
