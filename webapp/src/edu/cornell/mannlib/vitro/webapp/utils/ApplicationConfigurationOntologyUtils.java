/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelID;

public class ApplicationConfigurationOntologyUtils {

    private static final Log log = LogFactory.getLog(ApplicationConfigurationOntologyUtils.class);
    
    public static List<ObjectProperty> getAdditionalFauxSubpropertiesForList(List<ObjectProperty> propList, VitroRequest vreq) {
        ServletContext ctx = vreq.getSession().getServletContext();
		Model displayModel = ModelAccess.on(ctx).getDisplayModel();
        Model tboxModel = ModelAccess.on(ctx).getOntModel(ModelID.UNION_TBOX);
        return getAdditionalFauxSubpropertiesForList(propList, displayModel, tboxModel);
    }
    
    public static List<ObjectProperty> getAdditionalFauxSubpropertiesForList(List<ObjectProperty> propList, 
                                                                         Model displayModel, 
                                                                         Model tboxModel) {
        List<ObjectProperty> additionalProps = new ArrayList<ObjectProperty>();
        Model union = ModelFactory.createUnion(displayModel, tboxModel);
        String propQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "PREFIX config: <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#> \n" +
                "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n" +
                "SELECT ?range ?label ?group ?customForm WHERE { \n" +
        		"    ?p rdfs:subPropertyOf ?property . \n" +
                "    ?context config:configContextFor ?p . \n" +
        		"    ?context config:qualifiedBy ?range . \n" +
                "    ?context config:hasConfiguration ?configuration . \n" +
        		"    OPTIONAL { ?configuration config:propertyGroup ?group } \n" +
                "    OPTIONAL { ?configuration config:displayName ?label } \n" +
                "    OPTIONAL { ?configuration vitro:customEntryFormAnnot ?customForm } \n" +
        		"}"; 
      
        for (ObjectProperty op : propList) {
            log.debug("Checking " + op.getURI() + " for additional properties");
            String queryStr = propQuery.replaceAll("\\?property", "<" + op.getURI() + ">");
            log.debug(queryStr);
            Query q = QueryFactory.create(queryStr);
            QueryExecution qe = QueryExecutionFactory.create(q, union);
            try {
                ResultSet rs = qe.execSelect();
                while (rs.hasNext()) {
                    ObjectProperty newProp = new ObjectProperty();
                    newProp.setURI(op.getURI());
                    QuerySolution qsoln = rs.nextSolution();
                    log.debug(qsoln);
                    Resource rangeRes = qsoln.getResource("range");
                    if (rangeRes != null) {
                        newProp.setRangeVClassURI(rangeRes.getURI());
                    } else {
                        newProp.setRangeVClassURI(op.getRangeVClassURI());
                    }
                    Resource groupRes = qsoln.getResource("group");
                    if (groupRes != null) {
                        newProp.setGroupURI(groupRes.getURI());
                    } else {
                        newProp.setGroupURI(op.getURI());
                    }
                    Literal labelLit = qsoln.getLiteral("label");
                    if (labelLit != null) {
                        newProp.setDomainPublic(labelLit.getLexicalForm());
                    } else {
                        newProp.setDomainPublic(op.getDomainPublic());
                    }
                    Literal customFormLit = qsoln.getLiteral("customForm");
                    if (customFormLit != null) {
                        newProp.setCustomEntryForm(customFormLit.getLexicalForm());
                    } else {
                        newProp.setCustomEntryForm(op.getCustomEntryForm());
                    }
                    additionalProps.add(newProp);
                }  
            } finally {
                qe.close();
            }
        }
        
        return additionalProps;
    }
    
    
    
}
