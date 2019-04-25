/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.dbcp2.BasicDataSource;

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

	public BasicDataSource getDataSource() {
		BasicDataSource cpds = new BasicDataSource();
		cpds.setDriverClassName(getDbDriverClassName());
		cpds.setUrl(getJdbcUrl(configProps));
		cpds.setUsername(configProps.getProperty(PROPERTY_DB_USERNAME));
		cpds.setPassword(configProps.getProperty(PROPERTY_DB_PASSWORD));
		cpds.setMaxTotal(getMaxActive());
		cpds.setMaxIdle(getMaxIdle());
		cpds.setMinEvictableIdleTimeMillis(getMaxIdleTime());
		cpds.setTestOnBorrow(DEFAULT_TESTONBORROW);
		cpds.setTestOnReturn(DEFAULT_TESTONRETURN);
		cpds.setValidationQuery(getValidationQuery());
		return cpds;
//		try {
//			cpds.setMaxIdleTimeExcessConnections(getMaxIdleTimeExcess());
//			cpds.setAcquireIncrement(5);
//			cpds.setNumHelperThreads(6);
//		} catch (PropertyVetoException pve) {
//			throw new RuntimeException(pve);
//		}
	}

	private String getDbDriverClassName() {
		return configProps.getProperty(PROPERTY_DB_DRIVER_CLASS_NAME,
				DEFAULT_DRIVER_CLASS);
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

	/**
	 * Get the JDBC URL, perhaps with special MySQL options.
	 *
	 * This must be static and package-accessible so SDBConnectionSmokeTests can
	 * use the same options.
	 */
	static String getJdbcUrl(ConfigurationProperties props) {
		String url = props.getProperty(PROPERTY_DB_URL);

		// Ensure that MySQL handles unicode properly, else all kinds of
		// horrible nastiness ensues. Also, set some other handy options.
		if (DEFAULT_TYPE.equals(getDbType(props))) {
			if (!url.contains("?")) {
				url += "?useUnicode=yes&characterEncoding=utf8&nullNamePatternMatchesAll=true&cachePrepStmts=true&useServerPrepStmts=true&serverTimezone=UTC";
			} else {
				String urlLwr = url.toLowerCase();
				if (!urlLwr.contains("useunicode")) {
					url += "&useUnicode=yes";
				}
				if (!urlLwr.contains("characterencoding")) {
					url += "&characterEncoding=utf8";
				}
				if (!urlLwr.contains("nullnamepatternmatchesall")) {
					url += "&nullNamePatternMatchesAll=true";
				}
				if (!urlLwr.contains("cacheprepstmts")) {
					url += "&cachePrepStmts=true";
				}
				if (!urlLwr.contains("useserverprepstmts")) {
					url += "&useServerPrepStmts=true";
				}
				if (!urlLwr.contains("servertimezone")) {
					url += "&serverTimezone=UTC";
				}
			}
		}
		return url;
	}

	private static String getDbType(ConfigurationProperties props) {
		return props.getProperty(PROPERTY_DB_TYPE, DEFAULT_TYPE);
	}


}
