/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;

public class ApplicationConfigurationOntologyUtils {

    private static final Log log = LogFactory.getLog(ApplicationConfigurationOntologyUtils.class);
    
    public static List<ObjectProperty> getAdditionalFauxSubpropertiesForList(List<ObjectProperty> propList, Individual subject, VitroRequest vreq) {
		Model displayModel = vreq.getDisplayModel();
        Model tboxModel = vreq.getOntModelSelector().getTBoxModel();
        return getAdditionalFauxSubpropertiesForList(propList, subject, displayModel, tboxModel);
    }
    
    public static List<ObjectProperty> getAdditionalFauxSubproperties(ObjectProperty prop, 
                                                                         Individual subject,
                                                                         Model tboxModel,
                                                                         Model union) {

        List<ObjectProperty> additionalProps = new ArrayList<ObjectProperty>();
        String queryStr = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "PREFIX config: <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#> \n" +
                "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n" +
                "SELECT DISTINCT ?range ?domain ?property WHERE { \n" +
                "    ?context config:configContextFor ?property . \n" +
                "    ?context config:qualifiedBy ?range . \n" +
                "    ?context config:hasConfiguration ?configuration . \n" +
                "    ?configuration a config:ObjectPropertyDisplayConfig . \n" +
                "    OPTIONAL { ?context config:qualifiedByDomain ?domain } \n" +
                "}"; 
      
        if(prop != null) {
            log.debug("Checking " + prop.getURI() + " for additional properties");
            queryStr = queryStr.replaceAll("For \\?property", "For <" + prop.getURI() + ">");
        }
        log.debug(queryStr);
        Query q = QueryFactory.create(queryStr);
        QueryExecution qe = QueryExecutionFactory.create(q, union);
        WebappDaoFactory wadf = new WebappDaoFactoryJena(
                ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, union));
        ObjectPropertyDao opDao = wadf.getObjectPropertyDao();
        try {
            ResultSet rs = qe.execSelect();
            while (rs.hasNext()) {
                QuerySolution qsoln = rs.nextSolution();
                log.debug(qsoln);
                String opURI = (prop != null) ? prop.getURI() : qsoln.getResource(
                        "property").getURI();
                Resource domainRes = qsoln.getResource("domain");
                String domainURI = (domainRes != null) ? domainRes.getURI() : null;
                String rangeURI = qsoln.getResource("range").getURI();
                if (appropriateDomain(domainRes, subject, tboxModel)) {
                    ObjectProperty faux = opDao.getObjectPropertyByURIs(
                            opURI, domainURI, rangeURI, (prop != null) ? prop.clone() : null);
                    if (faux != null) {
                        additionalProps.add(faux);
                    } else {
                        log.error("Could not retrieve " + opURI + " qualified by " +
                                " domain " + domainURI + " and range " + rangeURI); 
                    }
                }
            }  
        } finally {
            qe.close();
        }
        return additionalProps;
    }   
    
    
    
    public static List<ObjectProperty> getAdditionalFauxSubpropertiesForList(List<ObjectProperty> propList, 
                                                                         Individual subject, 
                                                                         Model displayModel, 
                                                                         Model tboxModel) {
        
        List<ObjectProperty> additionalProps = new ArrayList<ObjectProperty>();
        Model union = ModelFactory.createUnion(displayModel, tboxModel);
        
        for (ObjectProperty op : propList) {
            additionalProps.addAll(getAdditionalFauxSubproperties(op, subject, tboxModel, union));
        }    

        return additionalProps;
    }
    
    private static boolean appropriateDomain(Resource domainRes, Individual subject, Model tboxModel) {
        if (subject == null || domainRes == null) {
            return true;
        }
        for (VClass vclass : subject.getVClasses()) {
            if ((vclass.getURI() != null) &&
                    ((vclass.getURI().equals(domainRes.getURI()) ||
                    (tboxModel.contains(
                            ResourceFactory.createResource(
                                    vclass.getURI()), RDFS.subClassOf, domainRes))))) {
                return true;
            }
        }
        return false;
    }
    
}
