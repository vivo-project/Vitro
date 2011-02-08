/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import java.io.IOException;
import java.sql.SQLException;
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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;

public class WebappDaoFactorySDBPrep implements Filter {
	
	private final static Log log = LogFactory.getLog(WebappDaoFactorySDBPrep.class);
	
	BasicDataSource _bds;
	StoreDesc _storeDesc;
	SDBConnection _conn;
	OntModelSelector _oms;
	String _defaultNamespace;

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
	
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		
		if ( (!(JenaDataSourceSetupBase.isSDBActive())) || 
		        (request.getAttribute(
		                "WebappDaoFactorySDBPrep.setup") != null) ) {
			// don't run multiple times or if SDB is not active
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
		
		SDBConnection conn = null;
		Store store = null;
		Dataset dataset = null;
		
		try {
			if (
					request instanceof HttpServletRequest &&
					_bds != null && _storeDesc != null && _oms != null) {
				try {
					conn = new SDBConnection(_bds.getConnection()) ;
				} catch (SQLException sqe) {
					throw new RuntimeException("Unable to connect to database", sqe);
				}
				if (conn != null) {
					store = SDBFactory.connectStore(conn, _storeDesc);
					dataset = SDBFactory.connectDataset(store);
					VitroRequest vreq = new VitroRequest((HttpServletRequest) request);
					WebappDaoFactory wadf = 
						new WebappDaoFactorySDB(_oms, dataset, _defaultNamespace, null, null);
					vreq.setWebappDaoFactory(wadf);
					vreq.setFullWebappDaoFactory(wadf);
					vreq.setDataset(dataset);
				}
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
				conn = null;
			}
			if (dataset != null) {
			    dataset.close();
			    dataset = null;
			}
			if (store != null) {
			    store.close();
			    store = null;
			}
			_bds = null;
			_storeDesc = null;
			_conn = null;
			_oms = null;
			_defaultNamespace = null;
		}
		
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		try {
			ServletContext ctx = filterConfig.getServletContext();
			_bds = JenaDataSourceSetupBase.getApplicationDataSource(ctx);
		    _storeDesc = (StoreDesc) ctx.getAttribute("storeDesc");
		    _oms = (OntModelSelector) ctx.getAttribute("unionOntModelSelector");
		    _defaultNamespace = (String) ctx.getAttribute("defaultNamespace");
		} catch (Throwable t) {
			log.error("Unable to set up SDB WebappDaoFactory for request", t);
		}		
	}
	
	public void destroy() {
		// no destroy actions
	}

}
