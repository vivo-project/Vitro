/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaModelMaker;

public class VitroJenaModelMakerSetup implements ServletContextListener {
	private static final Log log = LogFactory
			.getLog(VitroJenaModelMakerSetup.class);

	protected final static String DB_TYPE = "MySQL";
	
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void contextInitialized(ServletContextEvent arg0) {
		try {		
			String jdbcUrl = ConfigurationProperties.getProperty("VitroConnection.DataSource.url")
					+ "?useUnicode=yes&characterEncoding=utf8";
			String username = ConfigurationProperties.getProperty("VitroConnection.DataSource.username");
			String password = ConfigurationProperties.getProperty("VitroConnection.DataSource.password");
				
			DBConnection dbConn = new DBConnection(jdbcUrl, username, password, DB_TYPE);
			ModelMaker mMaker = ModelFactory.createModelRDBMaker(dbConn);
			VitroJenaModelMaker vjmm = new VitroJenaModelMaker(mMaker);
			arg0.getServletContext().setAttribute("vitroJenaModelMaker", vjmm);
			log.debug("VitroJenaModelMaker set up");
		} catch (Throwable t) {
			log.error("Unable to set up default VitroJenaModelMaker", t);
		}

	}

}
