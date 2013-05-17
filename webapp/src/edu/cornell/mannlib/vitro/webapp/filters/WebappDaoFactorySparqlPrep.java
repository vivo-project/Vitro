/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
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

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelID;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryConfig;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SparqlDatasetGraph;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SparqlGraphMultilingual;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;

public class WebappDaoFactorySparqlPrep implements Filter {
	
	private final static Log log = LogFactory.getLog(WebappDaoFactorySparqlPrep.class);
	
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
		
        javax.sql.DataSource ds = JenaDataSourceSetupBase.getApplicationDataSource(_ctx);
        StoreDesc storeDesc = (StoreDesc) _ctx.getAttribute("storeDesc");
        String defaultNamespace = (String) _ctx.getAttribute("defaultNamespace");
        Connection sqlConn = null;
		SDBConnection conn = null;
		Store store = null;
		Dataset dataset = null;
		WebappDaoFactory wadf = null;
		
		try {		
		    if (ds == null || storeDesc == null) {
		        throw new RuntimeException("SDB store not property set up");
		    }
		    
			try {
			    sqlConn = ds.getConnection();
				conn = new SDBConnection(sqlConn) ;
			} catch (SQLException sqe) {
				throw new RuntimeException("Unable to connect to database", sqe);
			}
			if (conn != null) {
				store = SDBFactory.connectStore(conn, storeDesc);
				dataset = SDBFactory.connectDataset(store);
				VitroRequest vreq = new VitroRequest((HttpServletRequest) request);
				
				log.info("---------");
				
				Enumeration<String> headStrs = vreq.getHeaderNames();
				while (headStrs.hasMoreElements()) {
				    String head = headStrs.nextElement(); 
				    log.info(head + " : "  + vreq.getHeader(head)); 				    
				}
				
				List<String> langs = new ArrayList<String>();
				
				log.info("Accept-Language: " + vreq.getHeader("Accept-Language"));
				Enumeration<Locale> locs = vreq.getLocales();
				while (locs.hasMoreElements()) {
				    Locale locale = locs.nextElement();
				    langs.add(locale.toString().replace("_", "-"));
				    log.info(locale.toString() + " / " + locale.getLanguage() + " + " + locale.getCountry() + " : " + locale.getDisplayCountry() + " | " + locale.getLanguage() + " : " + locale.getDisplayLanguage());
				}
				WebappDaoFactoryConfig config = new WebappDaoFactoryConfig();
				config.setDefaultNamespace(defaultNamespace);
				config.setPreferredLanguages(langs);
				
				//okay let's make a graph-backed model
				String endpointURI = ConfigurationProperties.getBean(
				        request).getProperty("VitroConnection.DataSource.endpointURI");
				
				Graph g = new SparqlGraphMultilingual(endpointURI, langs);
				//Graph g = new SparqlGraph(endpointURI);
				
				Model m = ModelFactory.createModelForGraph(g);
				OntModel om = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, m);
								
				dataset = DatasetFactory.create(new SparqlDatasetGraph(endpointURI));
				
				//DataSource datasource = DatasetFactory.create();
				//datasource.addNamedModel("fake:fake", m);
				//dataset = datasource;			
				
				vreq.setAssertionsWebappDaoFactory(wadf);

				ModelAccess.on(vreq).setOntModel(ModelID.UNION_ABOX, om);
				ModelAccess.on(vreq).setOntModel(ModelID.UNION_TBOX, om);
				ModelAccess.on(vreq).setOntModel(ModelID.UNION_FULL, om);
				OntModelSelector oms = ModelAccess.on(vreq).getOntModelSelector();
				
				wadf = new WebappDaoFactoryJena(oms, config);
				//wadf = new WebappDaoFactorySDB(oms, dataset, config);
				vreq.setFullWebappDaoFactory(wadf);
				vreq.setUnfilteredWebappDaoFactory(wadf);
				vreq.setWebappDaoFactory(wadf);
				vreq.setDataset(dataset);
			}
		} catch (Throwable t) {
			log.error("Unable to filter request to set up SDB connection", t);
		}
		
		request.setAttribute("WebappDaoFactorySDBPrep.setup", 1);
		
		try {
			filterChain.doFilter(request, response);
			return;
		} finally {
			if (conn != null) {
				conn.close();
			}
			if (dataset != null) {
			    dataset.close();
			}
			if (store != null) {
			    store.close();
			}
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
