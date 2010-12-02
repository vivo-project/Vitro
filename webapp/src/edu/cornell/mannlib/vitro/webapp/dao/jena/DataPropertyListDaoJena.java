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

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyListDao;

public class DataPropertyListDaoJena extends PropertyListDaoJena implements
        DataPropertyListDao {

    protected static final Log log = LogFactory.getLog(DataPropertyListDaoJena.class);

    protected static final String dataPropertyQueryString = 
        PREFIXES + "\n" +
        "SELECT DISTINCT ?predicate WHERE { \n" +
        //"   GRAPH ?g {\n" + 
        "       ?subject ?predicate ?object . \n" +
        "       ?predicate rdf:type owl:DatatypeProperty . \n" +
        //"   }\n" +
        "}" +
        "ORDER BY ?predicate\n";
    
    static protected Query dataPropertyQuery;
    static {
        try {
            dataPropertyQuery = QueryFactory.create(dataPropertyQueryString);
        } catch(Throwable th){
            log.error("could not create SPARQL query for dataPropertyQueryString " + th.getMessage());
            log.error(dataPropertyQueryString);
        }             
    }

    public DataPropertyListDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }
    
    @Override
    public List<DataProperty> getDataPropertyList(Individual subject) {
        return getDataPropertyList(subject.getURI());
    }
    
    @Override
    public List<DataProperty> getDataPropertyList(String subjectUri) {
        log.debug("dataPropertyQuery:\n" + dataPropertyQuery);        
        ResultSet results = getPropertyQueryResults(subjectUri, dataPropertyQuery);
        List<DataProperty> properties = new ArrayList<DataProperty>();
        while (results.hasNext()) {
            QuerySolution sol = results.next();
            Resource resource = sol.getResource("predicate");
            // This is a hack to throw out properties in the vitro, rdf, rdfs, and owl namespaces.
            // It will be implemented in a better way in v1.3 (Editing and Display Configuration).
            // It must be done here rather than in PropertyList or PropertyListBuilder, because
            // those properties must be removed for the IndividualFiltering object.
            if ( ! EXCLUDED_NAMESPACES.contains(resource.getNameSpace())) {
                String uri = resource.getURI();
                DataPropertyDao dpDao = getWebappDaoFactory().getDataPropertyDao();
                DataProperty property = dpDao.getDataPropertyByURI(uri);
                properties.add(property);
            }
        }
        return properties; 
    }
    
}
