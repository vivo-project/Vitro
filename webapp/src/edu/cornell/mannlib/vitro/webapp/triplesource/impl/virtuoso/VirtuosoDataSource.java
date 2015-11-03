/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.triplesource.impl.virtuoso;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import virtuoso.jdbc4.VirtuosoConnectionPoolDataSource;

import javax.servlet.ServletContext;
import javax.sql.ConnectionPoolDataSource;
import java.beans.PropertyVetoException;
import java.sql.SQLException;

import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb.ContentTripleSourceSDB.*;

/**
 * Create a DataSource on the SDB database.
 */
public class VirtuosoDataSource {
	private static final Log log = LogFactory.getLog(VirtuosoDataSource.class);

	private final ConfigurationProperties configProps;


	public VirtuosoDataSource(ServletContext ctx) {
		this.configProps = ConfigurationProperties.getBean(ctx);
	}

	public ConnectionPoolDataSource getDataSource(ContentTripleSourceVirtuosoJena.Configuration config) throws SQLException {
		VirtuosoConnectionPoolDataSource ds = new VirtuosoConnectionPoolDataSource();
		ds.setDataSourceName("VIVO");
//		ds.setDatabaseName("VIVO");
		ds.setUser(config.username);
		ds.setPassword(config.password);
		ds.setPortNumber(1111);
		ds.setServerName("localhost");
		ds.setMaxPoolSize(10);
		ds.setMaxIdleTime(60);

		return ds;
	}
}
