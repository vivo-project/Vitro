/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.dao.ApplicationDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class ApplicationDaoJena extends JenaBaseDao implements ApplicationDao {

	Integer portalCount = null;
	
    public ApplicationDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }
	   
	
	public boolean isFlag1Active() {
		if (portalCount == null) {
			boolean active = false;
			for (Portal p : getWebappDaoFactory().getPortalDao().getAllPortals()) {
				if (p.isFlag1Filtering()) {
					return true;
				}
			}
		}
		return false;
	}

	
	public boolean isFlag2Active() {
		return (getFlag2ValueMap().isEmpty()) ? false : true;
	}

}
