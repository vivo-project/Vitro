/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.Map;

import org.apache.jena.atlas.lib.Pair;

import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.dao.FauxPropertyDao;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

/**
 * TODO
 */
public class FauxPropertyDaoJena implements FauxPropertyDao {

	/**
	 * @param rdfService
	 * @param dwf
	 * @param customListViewConfigFileMap
	 * @param webappDaoFactoryJena
	 */
	public FauxPropertyDaoJena(
			RDFService rdfService,
			DatasetWrapperFactory dwf,
			Map<Pair<String, Pair<ObjectProperty, String>>, String> customListViewConfigFileMap,
			WebappDaoFactoryJena webappDaoFactoryJena) {
		// TODO Auto-generated constructor stub
		throw new RuntimeException("FauxPropertyDaoJena Constructor not implemented.");
	}

}
