/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.sql.Connection;

public interface SQLGraphGenerator extends GraphGenerator {

	public Connection getConnection();

}
