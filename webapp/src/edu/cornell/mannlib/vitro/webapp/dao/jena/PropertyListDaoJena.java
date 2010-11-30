/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyListDao;

public class PropertyListDaoJena extends JenaBaseDao implements PropertyListDao {

    static final protected String propertyQueryString = 
        "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>\n" +
        "SELECT ?group ?predicate WHERE {\n" +
        "   GRAPH ?g {\n" + 
        "       ?subject ?predicate ?object . \n" +
        "       OPTIONAL { ?predicate vitro:inPropertyGroupAnnot ?group . \n" +
        "       }\n" +
        "   }\n" +
        "}" +
        "ORDER BY DESC(?group) ?predicate\n";
    
    
    static protected Query propertyQuery;
    static {
        try {
            propertyQuery = QueryFactory.create(propertyQueryString);
        } catch(Throwable th){
            log.error("could not create SPARQL query for propertyQueryString " + th.getMessage());
            log.error(propertyQueryString);
        }  
    }
    public PropertyListDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }
    
    @Override
    protected OntModel getOntModel() {
        return getOntModelSelector().getABoxModel();
    }
    
    @Override
    public List<Property> getPropertyListForSubject(Individual subject) {
        return getPropertyListForSubject(subject.getURI());
    }
    
    @Override
    public List<Property> getPropertyListForSubject(String subjectUri) {
        
        // First get all the properties that occur in statements with this subject as subject. We must get these
        // from a db query because they may include properties that are not defined as "possible properties" 
        // for a subject of this class.
        
        // Bind the subject's uri to the ?subject query term
        QuerySolutionMap subjectBinding = new QuerySolutionMap();
        subjectBinding.add("subject", ResourceFactory.createResource(subjectUri));

        // Run the SPARQL query to get the properties        
        QueryExecution qexec = QueryExecutionFactory.create(propertyQuery, getOntModel());
        ResultSet results = qexec.execSelect();
        return null; 
    }

}
