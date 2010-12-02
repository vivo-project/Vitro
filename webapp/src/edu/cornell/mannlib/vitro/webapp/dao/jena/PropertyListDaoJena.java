/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyListDao;

public class PropertyListDaoJena extends JenaBaseDao implements PropertyListDao {

    protected static final Log log = LogFactory.getLog(PropertyListDaoJena.class);
    
    protected static final String PREFIXES = 
        "PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
        "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n" + 
        "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
        "PREFIX afn: <http://jena.hp1.hp.com/ARQ/function#>";

    /* This may be the intent behind JenaBaseDao.NONUSER_NAMESPACES, but that
     * value does not contain all of these namespaces.
     */
    protected static final List<String> EXCLUDED_NAMESPACES = Arrays.asList(
            "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#",
            "http://vitro.mannlib.cornell.edu/ns/vitro/public#",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
            "http://www.w3.org/2000/01/rdf-schema#",
            "http://www.w3.org/2002/07/owl#"            
        );

    public PropertyListDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }
    
    @Override
    protected OntModel getOntModel() {
        return getOntModelSelector().getFullModel();
    }    
    
    protected ResultSet getPropertyQueryResults(String subjectUri, Query query) {        
        log.debug("SPARQL query:\n" + query.toString());
        // Bind the subject's uri to the ?subject query term
        QuerySolutionMap subjectBinding = new QuerySolutionMap();
        subjectBinding.add("subject", ResourceFactory.createResource(subjectUri));

        // Run the SPARQL query to get the properties        
        QueryExecution qexec = QueryExecutionFactory.create(query, getOntModelSelector().getFullModel(), subjectBinding);
        return qexec.execSelect();        
    }
}
