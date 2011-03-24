package edu.cornell.mannlib.vitro.webapp.dao.jena;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.HashSet;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.dao.FlagDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class FlagDaoJena extends JenaBaseDao implements FlagDao {

    public FlagDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }

    public String convertNumericFlagToInsertString(int numeric, String column,
            String table) {
        // TODO Auto-generated method stub
        return null;
    }

    public String convertNumericFlagToInsertString(int numeric,
            String flagColumns) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getFlagNames(String table, String field) {
        // TODO Auto-generated method stub
        return null;
    }

}
