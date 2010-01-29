package edu.cornell.mannlib.vitro.webapp.dao.jena;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.SQLException;

public class RDBModelGenerator implements ModelGenerator {

    private BasicDataSource ds = null;
    private String dbTypeStr = null;
    private String modelNameStr = null;
    private OntModelSpec ontModelSpec = null;

    public RDBModelGenerator(BasicDataSource bds, String dbTypeStr, String modelNameStr, OntModelSpec ontModelSpec) {
        this.ds = bds;
        this.dbTypeStr = dbTypeStr;
        this.modelNameStr = modelNameStr;
        this.ontModelSpec = ontModelSpec;
    }

    public OntModel generateModel() {
        try {
            IDBConnection conn = new DBConnection(ds.getConnection(), dbTypeStr);
            ModelMaker maker = ModelFactory.createModelRDBMaker(conn);
            Model model = maker.openModel(modelNameStr);
            OntModel oModel = ModelFactory.createOntologyModel(ontModelSpec, model);
            oModel.prepare();
            return oModel;
        } catch (SQLException e) {
            return null;
        }
    }

}
