/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SDBGraphConnectionGenerator {

	private final static Log log = LogFactory.getLog(
			SDBGraphConnectionGenerator.class);

	private DataSource ds = null;
	private Connection connection = null;

	public SDBGraphConnectionGenerator(DataSource dataSource) {
		this.ds = dataSource;
	}

	public Connection generateConnection() throws SQLException {
        if ( this.connection == null ) {
            this.connection = ds.getConnection();
        } else if ( this.connection.isClosed() ) {
            try {
                this.connection.close();
            } catch (SQLException e) {
                // The connection will throw an "Already closed"
                // SQLException that we need to catch.  We need to
                // make this extra call to .close() in order to make
                // sure that the connection is returned to the pool.
                // This depends on the particular behavior of version
                // 1.4 of the Apache Commons connection pool library.
                // Earlier versions threw the exception right away,
                // making this impossible. Future versions may do the
                // same.
            }
            this.connection = ds.getConnection();
        }
        return connection;
	}

}
