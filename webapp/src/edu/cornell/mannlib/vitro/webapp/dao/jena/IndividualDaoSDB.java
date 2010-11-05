/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class IndividualDaoSDB extends IndividualDaoJena {

	private Dataset dataset;
	private WebappDaoFactoryJena wadf;
	
    public IndividualDaoSDB(Dataset dataset, WebappDaoFactoryJena wadf) {
        super(wadf);
        this.dataset = dataset;
    }
    
    protected Dataset getDataset() {
    	return this.dataset;
    }
    
    protected Individual makeIndividual(String individualURI) {
    	//return new IndividualSDB2(individualURI, getDataset(), getWebappDaoFactory());
    	return new IndividualSDB(individualURI, getDataset(), getWebappDaoFactory());
    }

    private static final Log log = LogFactory.getLog(IndividualDaoSDB.class.getName());

    @Override
    public List getIndividualsByVClassURI(String vclassURI, int offset, int quantity ) {

    	if (vclassURI==null) {
            return null;
        }
        
        List ents = new ArrayList();
        
        Resource theClass = (vclassURI.indexOf(PSEUDO_BNODE_NS) == 0) 
            ? getOntModel().createResource(new AnonId(vclassURI.split("#")[1]))
            : ResourceFactory.createResource(vclassURI);
    
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            if (theClass.isAnon() && theClass.canAs(UnionClass.class)) {
            	UnionClass u = (UnionClass) theClass.as(UnionClass.class);
            	for (OntClass operand : u.listOperands().toList()) {
            		VClass vc = new VClassJena(operand, getWebappDaoFactory());
            		ents.addAll(getIndividualsByVClass(vc));
            	}
            } else {
	            StmtIterator stmtIt = getOntModel().listStatements((Resource) null, RDF.type, theClass);
	            try {
	                while (stmtIt.hasNext()) {
	                    Statement stmt = stmtIt.nextStatement();
	                    OntResource ind = (OntResource) stmt.getSubject().as(OntResource.class);
	                    ents.add(makeIndividual(ind.getURI()));
	                }
	            } finally {
	                stmtIt.close();
	            }
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }

        java.util.Collections.sort(ents);

        return ents;

    }
	
    @Override
    public Individual getIndividualByURI(String entityURI) {
        if( entityURI == null || entityURI.length() == 0 ) {
            return null;
        } else {
        	return makeIndividual(entityURI);	
        }
    }  
    
    /**
     * fills in the Individual objects needed for any ObjectPropertyStatements attached to the specified individual.
     * @param entity
     */
    private void fillIndividualsForObjectPropertyStatements(Individual entity){
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            Iterator e2eIt = entity.getObjectPropertyStatements().iterator();
            while (e2eIt.hasNext()) {
                ObjectPropertyStatement e2e = (ObjectPropertyStatement) e2eIt.next();
                e2e.setSubject(makeIndividual(e2e.getSubjectURI()));
                e2e.setObject(makeIndividual(e2e.getObjectURI()));       
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }
    
    /**
     * In Jena it can be difficult to get an object with a given dataproperty if
     * you do not care about the datatype or lang of the literal.  Use this
     * method if you would like to ignore the lang and datatype.  
     */
    @Override
    public List<Individual> getIndividualsByDataProperty(String dataPropertyUri, String value){        
        Property prop = null;
        if( RDFS.label.getURI().equals( dataPropertyUri )){
            prop = RDFS.label;
        }else{
            prop = getOntModel().getDatatypeProperty(dataPropertyUri);
        }

        if( prop == null ) {            
            log.debug("Could not getIndividualsByDataProperty() " +
                    "because " + dataPropertyUri + "was not found in model.");
            return Collections.emptyList();
        }

        if( value == null ){
            log.debug("Could not getIndividualsByDataProperty() " +
                    "because value was null");
            return Collections.emptyList();
        }
        
        Literal litv1 = getOntModel().createLiteral(value);        
        Literal litv2 = getOntModel().createTypedLiteral(value);   
        
        //warning: this assumes that any language tags will be EN
        Literal litv3 = getOntModel().createLiteral(value,"EN");        
        
        HashMap<String,Individual> individualsMap = new HashMap<String, Individual>();
                
        getOntModel().enterCriticalSection(Lock.READ);
        int count = 0;
        try{
            StmtIterator stmts
                = getOntModel().listStatements((Resource)null, prop, litv1);                                           
            while(stmts.hasNext()){
                count++;
                Statement stmt = stmts.nextStatement();
                
                RDFNode sub = stmt.getSubject();
                if( sub == null || sub.isAnon() || sub.isLiteral() )                    
                    continue;                
                
                RDFNode obj = stmt.getObject();
                if( obj == null || !obj.isLiteral() )
                    continue;
                
                Literal literal = (Literal)obj;
                Object v = literal.getValue();
                if( v == null )                     
                    continue;                
                
                String subUri = ((Resource)sub).getURI();
                if( ! individualsMap.containsKey(subUri)){
                    individualsMap.put(subUri,makeIndividual(subUri));
                }
            }
            
            stmts = getOntModel().listStatements((Resource)null, prop, litv2);                                           
            while(stmts.hasNext()){
                count++;
                Statement stmt = stmts.nextStatement();
                
                RDFNode sub = stmt.getSubject();
                if( sub == null || sub.isAnon() || sub.isLiteral() )                    
                    continue;                
                
                RDFNode obj = stmt.getObject();
                if( obj == null || !obj.isLiteral() )
                    continue;
                
                Literal literal = (Literal)obj;
                Object v = literal.getValue();
                if( v == null )                     
                    continue;                
                
                String subUri = ((Resource)sub).getURI();
                if( ! individualsMap.containsKey(subUri)){
                    individualsMap.put(subUri, makeIndividual(subUri));
                }                
            }
            
            stmts = getOntModel().listStatements((Resource)null, prop, litv3);                                           
            while(stmts.hasNext()){
                count++;
                Statement stmt = stmts.nextStatement();
                
                RDFNode sub = stmt.getSubject();
                if( sub == null || sub.isAnon() || sub.isLiteral() )                    
                    continue;                
                
                RDFNode obj = stmt.getObject();
                if( obj == null || !obj.isLiteral() )
                    continue;
                
                Literal literal = (Literal)obj;
                Object v = literal.getValue();
                if( v == null )                     
                    continue;                
                
                String subUri = ((Resource)sub).getURI();
                if( ! individualsMap.containsKey(subUri)){
                    individualsMap.put(subUri, makeIndividual(subUri));
                }                
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        
        List<Individual> rv = new ArrayList(individualsMap.size());
        rv.addAll(individualsMap.values());
        return rv;
    }

    @Override
    public Iterator getAllOfThisTypeIterator() {
        final List<com.hp.hpl.jena.ontology.Individual> list = 
            new LinkedList<com.hp.hpl.jena.ontology.Individual>();
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            ClosableIterator allIndIt = getOntModel().listIndividuals();
            try {
                while (allIndIt.hasNext()) {
                    com.hp.hpl.jena.ontology.Individual ind = (com.hp.hpl.jena.ontology.Individual) allIndIt.next();
                    
                    //don't include anything that lacks a label, issue VIVO-119.
                    if( getLabel(ind) == null )
                        continue;
                    
                    boolean userVisible = true;
                    //Check for non-user visible types, maybe this should be an annotation?
                    ClosableIterator typeIt = ind.listRDFTypes(false);
                    try {
                        while (typeIt.hasNext()) {
                            Resource typeRes = (Resource) typeIt.next();
                            String type = typeRes.getURI();
                            // brute forcing this until we implement a better strategy
                            if (VitroVocabulary.PORTAL.equals(type) || 
                            	VitroVocabulary.TAB.equals(type) ||
                            	VitroVocabulary.TAB_INDIVIDUALRELATION.equals(type) ||
                            	VitroVocabulary.LINK.equals(type) ||
                            	VitroVocabulary.KEYWORD.equals(type) ||
                            	VitroVocabulary.KEYWORD_INDIVIDUALRELATION.equals(type) ||
                            	VitroVocabulary.CLASSGROUP.equals(type) ||
                            	VitroVocabulary.PROPERTYGROUP.equals(type) ||
                            	VitroVocabulary.APPLICATION.equals(type)) {    	
                                userVisible = false;
                                break;
                            }
                            if( OWL.ObjectProperty.getURI().equals(type) ||
                            	OWL.DatatypeProperty.getURI().equals(type) ||
                            	OWL.AnnotationProperty.getURI().equals(type) ||
                            	RDF.type.getURI().equals(type) ){
                            	userVisible = false;
                            	break;
                        	} 
                        }
                    } finally {
                        typeIt.close();
                    }
                    if (userVisible) {
                        list.add(ind);
                    }
                    
                }
            } finally {
                allIndIt.close();
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        if (list.size() >0){
            return new Iterator(){
                Iterator<com.hp.hpl.jena.ontology.Individual> innerIt = list.iterator();
                public boolean hasNext() { 
                    return innerIt.hasNext();                    
                }
                public Object next() {
                    return makeIndividual(innerIt.next().getURI());
                }
                public void remove() {
                    //not used
                }            
            };
        }
        else
            return null;
    }  

    @Override
    public Iterator getAllOfThisVClassIterator(String vClassURI) {
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            List ents = new LinkedList();
            OntClass cls = getOntModel().getOntClass(vClassURI);
            Iterator indIt = cls.listInstances();
            while (indIt.hasNext()) {
                com.hp.hpl.jena.ontology.Individual ind = (com.hp.hpl.jena.ontology.Individual) indIt.next();
                ents.add(makeIndividual(ind.getURI()));
            }
            return ents.iterator();
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

    @Override
    public Iterator getUpdatedSinceIterator(long updatedSince){
        List ents = new ArrayList();
        Date since = new DateTime(updatedSince).toDate();
        String sinceStr = xsdDateTimeFormat.format(since);
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            Property modTimeProp = MODTIME;
            if (modTimeProp == null)
                modTimeProp = getOntModel().getProperty(VitroVocabulary.MODTIME);
            if (modTimeProp == null)
                return null; // throw an exception?
            String queryStr = "PREFIX vitro: <"+ VitroVocabulary.vitroURI+"> " +
                              "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                              "SELECT ?ent " +
                              "WHERE { " +
                              "     ?ent vitro:modTime ?modTime ." +
                              "     FILTER (xsd:dateTime(?modTime) >= \""+sinceStr+"\"^^xsd:dateTime) " +
                              "}";
            Query query = QueryFactory.create(queryStr);
            QueryExecution qe = QueryExecutionFactory.create(query,getOntModel());
            ResultSet results = qe.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = (QuerySolution) results.next();
                Resource res = (Resource) qs.get("?ent");
                com.hp.hpl.jena.ontology.Individual ent = getOntModel().getIndividual(res.getURI());
                if (ent != null) {
                    boolean userVisible = false;
                    ClosableIterator typeIt = ent.listRDFTypes(true);
                    try {
                        while (typeIt.hasNext()) {
                            Resource typeRes = (Resource) typeIt.next();
                            if (typeRes.getNameSpace() == null || (!NONUSER_NAMESPACES.contains(typeRes.getNameSpace()))) {
                                userVisible = true;
                                break;
                            }
                        }
                    } finally {
                        typeIt.close();
                    }
                    if (userVisible) {
                        ents.add(makeIndividual(ent.getURI()));
                    }
                }
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        return ents.iterator();
    }

    
}
