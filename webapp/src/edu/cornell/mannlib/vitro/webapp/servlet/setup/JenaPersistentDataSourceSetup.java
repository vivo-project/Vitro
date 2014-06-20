/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.beans.PropertyVetoException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;

/**
 * Create connection to DB and DataSource, put them in the context.
 */
public class JenaPersistentDataSourceSetup implements ServletContextListener {
	private static final Log log = LogFactory
			.getLog(JenaPersistentDataSourceSetup.class.getName());

	private static final String PROPERTY_DBTYPE = "VitroConnection.DataSource.dbtype";
	private static final String PROPERTY_JDBC_URL = "VitroConnection.DataSource.url";
	private static final String PROPERTY_DRIVER_CLASS = "VitroConnection.DataSource.driver";
	private static final String PROPERTY_PASSWORD = "VitroConnection.DataSource.password";
	private static final String PROPERTY_USERNAME = "VitroConnection.DataSource.username";
	private static final String PROPERTY_MAX_ACTIVE = "VitroConnection.DataSource.pool.maxActive";
	private static final String PROPERTY_MAX_IDLE = "VitroConnection.DataSource.pool.maxIdle";
	private static final String PROPERTY_VALIDATION_QUERY = "VitroConnection.DataSource.validationQuery";

	private static final String DEFAULT_DBTYPE = "MySQL";
	private static final String DEFAULT_DRIVER_CLASS = "com.mysql.jdbc.Driver";
	private static final String DEFAULT_VALIDATION_QUERY = "SELECT 1";

	private static final int DEFAULT_MAXACTIVE = 40; //ms
	private static final int MINIMUM_MAXACTIVE = 20; //ms
	private static final int DEFAULT_MAXIDLE = 10; //ms

	private static final boolean DEFAULT_TESTONBORROW = true;
	private static final boolean DEFAULT_TESTONRETURN = true;

	private ConfigurationProperties configProps;
	
	private static ComboPooledDataSource ds;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		configProps = ConfigurationProperties.getBean(ctx);

		// we do not want to fetch imports when we wrap Models in OntModels
		OntDocumentManager.getInstance().setProcessImports(false);

		JenaPersistentDataSourceSetup.ds = makeC3poDataSource();
	}

	private ComboPooledDataSource makeC3poDataSource() {
		try {
			ComboPooledDataSource cpds = new ComboPooledDataSource();
			cpds.setDriverClass(getDbDriverClassName());
			cpds.setJdbcUrl(getJdbcUrl());
			cpds.setUser(configProps.getProperty(PROPERTY_USERNAME));
			cpds.setPassword(configProps.getProperty(PROPERTY_PASSWORD));
			cpds.setMaxPoolSize(getMaxActive());
			cpds.setMinPoolSize(getMaxIdle());
			cpds.setMaxIdleTime(43200); // s
			cpds.setMaxIdleTimeExcessConnections(300);
			cpds.setAcquireIncrement(5);
			cpds.setNumHelperThreads(6);
			cpds.setTestConnectionOnCheckout(DEFAULT_TESTONBORROW);
			cpds.setTestConnectionOnCheckin(DEFAULT_TESTONRETURN);
			cpds.setPreferredTestQuery(getValidationQuery());
			return cpds;
		} catch (PropertyVetoException pve) {
			throw new RuntimeException(pve);
		}
	}

	private String getDbDriverClassName() {
		return configProps.getProperty(PROPERTY_DRIVER_CLASS,
				DEFAULT_DRIVER_CLASS);
	}

	private String getDbType() {
		return configProps.getProperty(PROPERTY_DBTYPE, DEFAULT_DBTYPE);
	}

	private String getJdbcUrl() {
		String url = configProps.getProperty(PROPERTY_JDBC_URL);

		// Ensure that MySQL handles unicode properly, else all kinds of
		// horrible nastiness ensues.
		if (DEFAULT_DBTYPE.equals(getDbType()) && !url.contains("?")) {
			url += "?useUnicode=yes&characterEncoding=utf8";
		}

		return url;
	}

	private String getValidationQuery() {
		return configProps.getProperty(PROPERTY_VALIDATION_QUERY,
				DEFAULT_VALIDATION_QUERY);
	}

	private int getMaxActive() {
		String maxActiveStr = configProps.getProperty(PROPERTY_MAX_ACTIVE);
		if (StringUtils.isEmpty(maxActiveStr)) {
			return DEFAULT_MAXACTIVE;
		}

		int maxActive = DEFAULT_MAXACTIVE;
		try {
			maxActive = Integer.parseInt(maxActiveStr);
		} catch (NumberFormatException nfe) {
			log.error("Unable to parse connection pool maxActive setting "
					+ maxActiveStr + " as an integer");
			return DEFAULT_MAXACTIVE;
		}

		if (maxActive >= MINIMUM_MAXACTIVE) {
			return maxActive;
		}

		log.warn("Specified value for " + PROPERTY_MAX_ACTIVE
				+ " is too low. Using minimum value of " + MINIMUM_MAXACTIVE);
		return MINIMUM_MAXACTIVE;
	}

	private int getMaxIdle() {
		int maxIdleInt = Math.max(getMaxActive() / 4, DEFAULT_MAXIDLE);
		String maxIdleStr = configProps.getProperty(PROPERTY_MAX_IDLE);

		if (StringUtils.isEmpty(maxIdleStr)) {
			return maxIdleInt;
		}

		try {
			return Integer.parseInt(maxIdleStr);
		} catch (NumberFormatException nfe) {
			log.error("Unable to parse connection pool maxIdle setting "
					+ maxIdleStr + " as an integer");
			return maxIdleInt;
		}
	}

	public static DataSource getApplicationDataSource() {
		return JenaPersistentDataSourceSetup.ds;
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if (JenaPersistentDataSourceSetup.ds != null) {
			JenaPersistentDataSourceSetup.ds.close();
		}
	}

}
