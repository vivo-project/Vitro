/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.util.Objects;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.SearchEngineUtil;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Start up the appropriate search engine smoke test based on the configured URL property.
 */
public class SearchEngineSmokeTest implements ServletContextListener {

    private static final Log log = LogFactory.getLog(SearchEngineSmokeTest.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        final StartupStatus ss = StartupStatus.getBean(sce.getServletContext());

        String searchEngineUrlString = SearchEngineUtil.getSearchEngineURLProperty();

        if (Objects.isNull(searchEngineUrlString) || searchEngineUrlString.isEmpty()) {
            ss.fatal(this, "Search engine URL is not set.");
        }

        try {
        	ApplicationUtils.instance().getSearchEngine().test(this, sce);
        } catch (Exception e) {
        	log.error(e, e);
        	ss.fatal(this, e.getMessage());
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nothing to tear down.
    }

    

}
