/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyListDao;

public class ObjectPropertyListDaoJena extends PropertyListDaoJena implements
        ObjectPropertyListDao {

    protected static final Log log = LogFactory.getLog(ObjectPropertyListDaoJena.class);
    
    protected static final String objectPropertyQueryString = 
        PREFIXES + "\n" +
        "SELECT DISTINCT ?predicate WHERE { \n" +
        //"   GRAPH ?g {\n" + 
        "       ?subject ?predicate ?object . \n" +
        "       ?predicate rdf:type owl:ObjectProperty . \n" +
        //"   }\n" +
        "}" +
        "ORDER BY ?predicate\n";

    protected static Query objectPropertyQuery;
    static {
        try {
            objectPropertyQuery = QueryFactory.create(objectPropertyQueryString);
        } catch(Throwable th){
            log.error("could not create SPARQL query for objectPropertyQueryString " + th.getMessage());
            log.error(objectPropertyQueryString);
        }           
    }

    public ObjectPropertyListDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }
    
    @Override
    public List<ObjectProperty> getObjectPropertyList(Individual subject) {
        return getObjectPropertyList(subject.getURI());
    }
    
    @Override
    public List<ObjectProperty> getObjectPropertyList(String subjectUri) {
        log.debug("objectPropertyQuery:\n" + objectPropertyQuery);
        ResultSet results = getPropertyQueryResults(subjectUri, objectPropertyQuery);
        List<ObjectProperty> properties = new ArrayList<ObjectProperty>();
        while (results.hasNext()) {
            QuerySolution sol = results.next();
            Resource resource = sol.getResource("predicate");
            // This is a hack to throw out properties in the vitro, rdf, rdfs, and owl namespaces.
            // It will be implemented in a better way in v1.3 (Editing and Display Configuration).
            // It must be done here rather than in PropertyList or PropertyListBuilder, because
            // those properties must be removed for the IndividualFiltering object.
            if ( ! EXCLUDED_NAMESPACES.contains(resource.getNameSpace())) {
                String uri = resource.getURI();
                ObjectPropertyDao opDao = getWebappDaoFactory().getObjectPropertyDao();
                ObjectProperty property = opDao.getObjectPropertyByURI(uri);
                properties.add(property);
            }
        }
        return properties; 
    }
    
}
