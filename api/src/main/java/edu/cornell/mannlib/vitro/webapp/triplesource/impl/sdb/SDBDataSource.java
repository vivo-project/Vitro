/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb;

import java.beans.PropertyVetoException;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;

import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.DEFAULT_DRIVER_CLASS;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.DEFAULT_MAXACTIVE;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.DEFAULT_MAXIDLE;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.DEFAULT_MAX_IDLE_TIME;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.DEFAULT_MAX_IDLE_TIME_EXCESS;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.DEFAULT_TESTONBORROW;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.DEFAULT_TESTONRETURN;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.DEFAULT_TYPE;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.DEFAULT_VALIDATION_QUERY;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.MINIMUM_MAXACTIVE;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.PROPERTY_DB_DRIVER_CLASS_NAME;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.PROPERTY_DB_MAX_ACTIVE;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.PROPERTY_DB_MAX_IDLE;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.PROPERTY_DB_MAX_IDLE_TIME;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.PROPERTY_DB_MAX_IDLE_TIME_EXCESS;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.PROPERTY_DB_PASSWORD;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.PROPERTY_DB_TYPE;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.PROPERTY_DB_URL;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.PROPERTY_DB_USERNAME;
import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.PROPERTY_DB_VALIDATION_QUERY;

/**
 * Create a DataSource on the SDB database.
 */
public class SDBDataSource {
	private static final Log log = LogFactory.getLog(SDBDataSource.class);
	
	private final ConfigurationProperties configProps;
	

	public SDBDataSource(ServletContext ctx) {
		this.configProps = ConfigurationProperties.getBean(ctx);
	}

	public ComboPooledDataSource getDataSource() {
		try {
			ComboPooledDataSource cpds = new ComboPooledDataSource();
			cpds.setDriverClass(getDbDriverClassName());
			cpds.setJdbcUrl(getJdbcUrl());
			cpds.setUser(configProps.getProperty(PROPERTY_DB_USERNAME));
			cpds.setPassword(configProps.getProperty(PROPERTY_DB_PASSWORD));
			cpds.setMaxPoolSize(getMaxActive());
			cpds.setMinPoolSize(getMaxIdle());
			cpds.setMaxIdleTime(getMaxIdleTime());
			cpds.setMaxIdleTimeExcessConnections(getMaxIdleTimeExcess());
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
		return configProps.getProperty(PROPERTY_DB_DRIVER_CLASS_NAME,
				DEFAULT_DRIVER_CLASS);
	}

	private String getDbType() {
		return configProps.getProperty(PROPERTY_DB_TYPE, DEFAULT_TYPE);
	}

	private String getJdbcUrl() {
		String url = configProps.getProperty(PROPERTY_DB_URL);

		// Ensure that MySQL handles unicode properly, else all kinds of
		// horrible nastiness ensues.
		if (DEFAULT_TYPE.equals(getDbType()) && !url.contains("?")) {
			url += "?useUnicode=yes&characterEncoding=utf8";
		}

		return url;
	}

	private String getValidationQuery() {
		return configProps.getProperty(PROPERTY_DB_VALIDATION_QUERY,
				DEFAULT_VALIDATION_QUERY);
	}

	private int getMaxActive() {
		String maxActiveStr = configProps.getProperty(PROPERTY_DB_MAX_ACTIVE);
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

		log.warn("Specified value for " + PROPERTY_DB_MAX_ACTIVE
				+ " is too low. Using minimum value of " + MINIMUM_MAXACTIVE);
		return MINIMUM_MAXACTIVE;
	}

	private int getMaxIdle() {
		int maxIdleInt = Math.max(getMaxActive() / 4, DEFAULT_MAXIDLE);
		return getPropertyAsInt(PROPERTY_DB_MAX_IDLE, maxIdleInt);
	}

	private int getMaxIdleTime() {
		return getPropertyAsInt(PROPERTY_DB_MAX_IDLE_TIME, DEFAULT_MAX_IDLE_TIME);
	}

	private int getMaxIdleTimeExcess() {
		return getPropertyAsInt(PROPERTY_DB_MAX_IDLE_TIME_EXCESS, DEFAULT_MAX_IDLE_TIME_EXCESS);
	}

	private int getPropertyAsInt(String prop, int defaultValue) {
		String propStr = configProps.getProperty(prop);

		if (StringUtils.isEmpty(propStr)) {
			return defaultValue;
		}

		try {
			return Integer.parseInt(propStr);
		} catch (NumberFormatException nfe) {
			log.error("Unable to parse connection pool maxIdle setting "
					+ propStr + " as an integer");
			return defaultValue;
		}
	}
}
