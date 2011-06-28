/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.Set;
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
import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.WebappDaoFactoryFiltering;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.FilterFactory;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.HideFromDisplayByPolicyFilter;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;
import org.apache.commons.dbcp.BasicDataSource;

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
    ServletContext _context;
    ApplicationBean _appbean;    
    
    static FilterFactory filterFactory = null;
    
    /**
     * The filter will be applied to all incoming urls,
     this is a list of URI patterns to skip.  These are
     matched against the requestURI sans query paramerts,
     * ex
     * "/vitro/index.jsp"
     * "/vitro/themes/enhanced/css/edit.css"
     *
    */
    Pattern[] skipPatterns = {
            Pattern.compile(".*\\.(gif|GIF|jpg|jpeg)$"),
            Pattern.compile(".*\\.css$"),
            Pattern.compile(".*\\.js$"),
            Pattern.compile("/.*/themes/.*/site_icons/.*"),
            Pattern.compile("/.*/images/.*")
    };

    private static final Log log = LogFactory.getLog(VitroRequestPrep.class.getName());

    @Override
	public void doFilter(ServletRequest  request,
                          ServletResponse response,
                          FilterChain     chain)
    throws IOException, ServletException {
    	// If this isn't an HttpServletRequest, we might as well fail now.
    	HttpServletRequest req = (HttpServletRequest) request;

        //don't waste time running this filter again.
        if( req.getAttribute("VitroRequestPrep.setup") != null ){
            log.debug("VitroRequestPrep has already been executed at least once, not re-executing.");
            Integer a =(Integer) req.getAttribute("VitroRequestPrep.setup");
            req.setAttribute("VitroRequestPrep.setup", new Integer( a + 1 ) );
            chain.doFilter(req, response);
            return;
        }

        for( Pattern skipPattern : skipPatterns){
            Matcher match =skipPattern.matcher( req.getRequestURI() );
            if( match.matches()  ){
                log.debug("request matched a skipPattern, skipping VitroRequestPrep"); 
                chain.doFilter(req, response);
                return;
            }
        }
        
        VitroRequest vreq = new VitroRequest(req);

		if (log.isDebugEnabled()) {
			try {
				String logRequestStr = vreq.getRequestURI();
				if ( (vreq.getQueryString() != null) && (vreq.getQueryString().length()>0) ) {
					logRequestStr += "?" + vreq.getQueryString();
				}
				log.debug("RequestURI: "+logRequestStr);
			} catch (Exception e) {
				// Just in case something goes horribly wrong
				// Don't want logging to kill the request
			}
		}

        //-- setup appBean --//
        vreq.setAppBean(_appbean);
        
        log.debug("VitroRequestPrep: AppBean theme " + vreq.getAppBean().getThemeDir());

        //-- setup DAO factory --//
        WebappDaoFactory wdf = getWebappDaoFactory(vreq);
        //TODO: get accept-language from request and set as preferred languages
        
    	Object o = req.getSession().getAttribute("webappDaoFactory");
    	if (o instanceof WebappDaoFactory) {
    		wdf = (WebappDaoFactory) o;
    		log.debug("Found a WebappDaoFactory in the session and using it for this request");
    	}
    	//This will replace the WebappDaoFactory with a different version if menu management parameter is found
    	wdf = checkForSpecialWDF(vreq, wdf);
    	
        VitroFilters filters = null;
		        
        filters = getFiltersFromContextFilterFactory(req, wdf);
        
        if( filters != null ){
            log.debug("Wrapping WebappDaoFactory in filters");
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
        
        req.setAttribute("VitroRequestPrep.setup", new Integer(1));
        chain.doFilter(req, response);
    }

    private WebappDaoFactory getWebappDaoFactory(VitroRequest vreq){
    	WebappDaoFactory webappDaoFactory = vreq.getWebappDaoFactory();
        return (webappDaoFactory != null) ? webappDaoFactory :
        	(WebappDaoFactory) _context.getAttribute("webappDaoFactory");
    }

    @Override
	public void init(FilterConfig filterConfig) throws ServletException {
    	
        _context = filterConfig.getServletContext();
        
        Object o =  _context.getAttribute("applicationBean");
        if (o instanceof ApplicationBean) {
            _appbean = (ApplicationBean) o; 
        } else {
            _appbean = new ApplicationBean();
        }
    }    
    
    public VitroFilters getFiltersFromContextFilterFactory( HttpServletRequest request, WebappDaoFactory wdf){
        FilterFactory ff = getFilterFactory();
        if( ff == null ){ 
            return null;
        } else {
            return ff.getFilters(request, wdf);
        }
    }
    
    public FilterFactory getFilterFactory(){
        return(FilterFactory)_context.getAttribute("FilterFactory");        
    }
    public static void setFilterFactory(ServletContext sc, FilterFactory ff){
        sc.setAttribute("FilterFactory", ff);
    }

    @Override
	public void destroy() {
    	// Nothing to do.
    }

    //check if special model - this is for enabling the use of a different model for menu management 
    //Also enables the use of a completely different model and tbox if uris are passed
    private WebappDaoFactory checkForSpecialWDF(VitroRequest vreq, WebappDaoFactory inputWadf) {
    	String useMenuModelParam = vreq.getParameter(DisplayVocabulary.SWITCH_TO_DISPLAY_MODEL);
    	boolean useMenu = (useMenuModelParam != null);
    	//other parameters to be passed in in case want to use specific models
    	String useMainModelUri = vreq.getParameter(DisplayVocabulary.USE_MODEL_PARAM);
    	String useTboxModelUri = vreq.getParameter(DisplayVocabulary.USE_TBOX_MODEL_PARAM);
    	String useDisplayModelUri = vreq.getParameter(DisplayVocabulary.USE_DISPLAY_MODEL_PARAM);
    	
    	if(useMenu || (useMainModelUri != null && !useMainModelUri.isEmpty() && useTboxModelUri != null && !useTboxModelUri.isEmpty())) {    		
    		log.debug("Menu switching parameters exist, Use Menu: " + useMenu + " - UseModelUri:" + useMainModelUri + " - UseTboxModelUri:" + useTboxModelUri + " - useDisplayModelUri:" + useDisplayModelUri);
    		if(inputWadf instanceof WebappDaoFactoryJena) {
    			//Create a copy of the input WADF to be sent over and then set
        		WebappDaoFactoryJena wadfj = new WebappDaoFactoryJena((WebappDaoFactoryJena) inputWadf);
        		log.debug("Created copy of input webapp dao factory to be overwritten");
    			//WebappDaoFactoryJena wadfj = (WebappDaoFactoryJena) wadf;
    			OntModel useMainOntModel = null, useTboxOntModel = null, useDisplayOntModel = null;
    			Model tboxModel = null, displayModel = null;
    			BasicDataSource bds = JenaDataSourceSetupBase.getApplicationDataSource(_context);
	        	String dbType = ConfigurationProperties.getBean(_context).getProperty( // database type
	        				"VitroConnection.DataSource.dbtype", "MySQL");
    			if(useMenu) {
    				log.debug("Display model editing mode");
    				//if using special models for menu management, get main menu model from context and set tbox and display uris to be used
	    			useMainOntModel = (OntModel) _context.getAttribute("displayOntModel");
	    			//Hardcoding tbox model uri for now
	        		useTboxModelUri =  DisplayVocabulary.DISPLAY_TBOX_MODEL_URI;
	        		useDisplayModelUri = DisplayVocabulary.DISPLAY_DISPLAY_MODEL_URI;
	        		//Get tbox and display display model from servlet context otherwise load in directly from database
	    			useTboxOntModel = (OntModel) _context.getAttribute(DisplayVocabulary.CONTEXT_DISPLAY_TBOX);
	    			useDisplayOntModel = (OntModel) _context.getAttribute(DisplayVocabulary.CONTEXT_DISPLAY_DISPLAY);
    			} else {
    				log.debug("Display model editing mode not triggered, using model uri " + useMainModelUri);
    				//If main model uri passed as parameter then retrieve model from parameter
    				Model mainModel = JenaDataSourceSetupBase.makeDBModel(bds, useMainModelUri, OntModelSpec.OWL_MEM, JenaDataSourceSetupBase.TripleStoreType.RDB, dbType, _context);
    				//if this uri exists and model exists, then set up ont model version
    				if(mainModel != null) {
    					log.debug("main model uri exists");
    					useMainOntModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, mainModel);
    				} else {
    					log.error("Main Model Uri " + useMainModelUri + " did not retrieve model");
    				}
    			}
    			
    			
    			if(!useMenu || useTboxOntModel == null){
		        	tboxModel = JenaDataSourceSetupBase.makeDBModel(bds, useTboxModelUri, OntModelSpec.OWL_MEM, JenaDataSourceSetupBase.TripleStoreType.RDB, dbType, _context);
		        	useTboxOntModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, tboxModel);
    			} 
    			if(!useMenu || useDisplayOntModel == null) {
		    		//Set "display model" for display model
		        	displayModel = JenaDataSourceSetupBase.makeDBModel(bds, useDisplayModelUri, OntModelSpec.OWL_MEM, JenaDataSourceSetupBase.TripleStoreType.RDB, dbType, _context);
		        	useDisplayOntModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, displayModel);
    			}
    			//Set special model for wadfj
	        	if(useMainOntModel != null) {
	        		log.debug("Switching to use of input model");
	        		//If menu model, preserve existing display model so the navigation elements remain
	        		if(useMenu) {
	        			useDisplayOntModel = null;
	        		}
    				//Changes will be made to the copy, not the original from the servlet context
	        		wadfj.setSpecialDataModel(useMainOntModel, useTboxOntModel, useDisplayOntModel);
	        		return wadfj;
	        	}
    		}
    	}
    	//if no parameters exist for switching models, return the original webapp dao factory object
    	return inputWadf;
    }
    
    
}
