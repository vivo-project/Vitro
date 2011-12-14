/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.IndividualCreationEvent;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.IndividualDeletionEvent;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.IndividualUpdateEvent;
import edu.cornell.mannlib.vitro.webapp.edit.EditLiteral;

public class IndividualDaoJena extends JenaBaseDao implements IndividualDao {

    public IndividualDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }

    private static final Log log = LogFactory.getLog(IndividualDaoJena.class.getName());

    public Collection<DataPropertyStatement> getExternalIds(String individualURI) {
        return this.getExternalIds(individualURI, null);
    }

    public Collection<DataPropertyStatement> getExternalIds(String individualURI, String dataPropertyURI) {
        Collection<DataPropertyStatement> externalIdStatements = new ArrayList<DataPropertyStatement>();
        Individual ind = getIndividualByURI(individualURI);
        HashSet<String> externalIdPropURISet = new HashSet<String>();
        HashSet<String> nonExternalIdPropURISet = new HashSet<String>();
        if (ind != null) {
            Collection<DataPropertyStatement> dpsColl = getWebappDaoFactory().getDataPropertyStatementDao().getDataPropertyStatementsForIndividualByDataPropertyURI(ind, dataPropertyURI);
            Iterator<DataPropertyStatement> dpsIt = dpsColl.iterator();
            while (dpsIt.hasNext()) {
                DataPropertyStatement dps = dpsIt.next();
                if (externalIdPropURISet.contains(dps.getDatapropURI())) {
                    externalIdStatements.add(dps);
                } else if (!nonExternalIdPropURISet.contains(dps.getDatapropURI())) {
                	OntModel tboxOntModel = getOntModelSelector().getTBoxModel();
                    tboxOntModel.enterCriticalSection(Lock.READ);
                    try {
                        Resource dataprop = tboxOntModel.getResource(dps.getDatapropURI());
                        if (dataprop != null && (tboxOntModel.contains(dataprop, DATAPROPERTY_ISEXTERNALID, ResourceFactory.createTypedLiteral(true)) || tboxOntModel.contains(dataprop, DATAPROPERTY_ISEXTERNALID, "TRUE") )) {
                            externalIdPropURISet.add(dps.getDatapropURI());
                            externalIdStatements.add(dps);
                        } else {
                            nonExternalIdPropURISet.add(dps.getDatapropURI());
                        }
                    } finally {
                        tboxOntModel.leaveCriticalSection();
                    }
                }
            }
        }
        return externalIdStatements;
    }

    public void addVClass(String individualURI, String vclassURI) {
    	OntModel ontModel = getOntModelSelector().getABoxModel();
        ontModel.enterCriticalSection(Lock.WRITE);
        ontModel.getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),true,individualURI));
        try {
            Resource indRes = ontModel.getResource(individualURI);
            ontModel.add(indRes, RDF.type, ontModel.getResource(vclassURI));
            updatePropertyDateTimeValue(indRes, MODTIME, Calendar.getInstance().getTime(),ontModel);
        } finally {
            ontModel.getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),false,individualURI));
            ontModel.leaveCriticalSection();
        }
    }

    public void removeVClass(String individualURI, String vclassURI) {
    	OntModel ontModel = getOntModelSelector().getABoxModel();
        ontModel.enterCriticalSection(Lock.WRITE);
        ontModel.getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),true,individualURI));
        try {
            Resource indRes = ontModel.getResource(individualURI);
            getOntModel().remove(indRes, RDF.type, ontModel.getResource(vclassURI));
            updatePropertyDateTimeValue(indRes, MODTIME, Calendar.getInstance().getTime(), ontModel);
        } finally {
            ontModel.getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),false,individualURI));
            ontModel.leaveCriticalSection();
        }
    }

    public List<Individual> getIndividualsByVClass(VClass vclass ) {
        return getIndividualsByVClassURI(vclass.getURI(),-1,-1);
    }
    
    public List<Individual> getIndividualsByVClassURI(String vclassURI) {
        return getIndividualsByVClassURI(vclassURI,-1,-1);
    }

    public List<Individual> getIndividualsByVClassURI(String vclassURI, int offset, int quantity ) {

    	if (vclassURI==null) {
            return null;
        }
        
        List<Individual> ents = new ArrayList<Individual>();
        
        Resource theClass = (vclassURI.indexOf(PSEUDO_BNODE_NS) == 0) 
            ? getOntModel().createResource(new AnonId(vclassURI.split("#")[1]))
            : ResourceFactory.createResource(vclassURI);
    
        
        
        if (theClass.isAnon() && theClass.canAs(UnionClass.class)) {
        	UnionClass u = (UnionClass) theClass.as(UnionClass.class);
        	for (OntClass operand : u.listOperands().toList()) {
        		VClass vc = new VClassJena(operand, getWebappDaoFactory());
        		ents.addAll(getIndividualsByVClass(vc));
        	}
        } else {
        	OntModel ontModel = getOntModelSelector().getABoxModel();
        	try {
        		ontModel.enterCriticalSection(Lock.READ);
	            StmtIterator stmtIt = ontModel.listStatements((Resource) null, RDF.type, theClass);
	            try {
	                while (stmtIt.hasNext()) {
	                    Statement stmt = stmtIt.nextStatement();
	                    OntResource ind = (OntResource) stmt.getSubject().as(OntResource.class);
	                    ents.add(new IndividualJena(ind, (WebappDaoFactoryJena) getWebappDaoFactory()));
	                }
	            } finally {
	                stmtIt.close();
	            }
        	} finally {
        		ontModel.leaveCriticalSection();
        	}
        }
        

        java.util.Collections.sort(ents);

        return ents;

    }

    public int getCountOfIndividualsInVClass(String vclassURI ) {
        int count = 0;
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            OntClass cls = getOntModel().getOntClass(vclassURI);
            Iterator<? extends OntResource> indIt = cls.listInstances();
            while (indIt.hasNext()) {
                count++;
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        return count;
    }

    public String insertNewIndividual(Individual ent) throws InsertException {
        return insertNewIndividualWebapp(ent);
    }

    public String insertNewIndividualWebapp(Individual ent) throws InsertException {
        initInd(ent);
        return insertNewIndividual(ent, getOntModelSelector().getABoxModel());
    }

    /**
     * Inserts a new Individual into the knowledge base.
     * Note that a number of magic things happen in here.
     */
    public String insertNewIndividual(Individual ent, OntModel ontModel) throws InsertException {
    	
        String preferredURI = getUnusedURI(ent);          
           
        String entURI = null;
        
    	Resource cls = (ent.getVClassURI() != null) 
            ? ontModel.getResource(ent.getVClassURI())
            : OWL.Thing; // This assumes we want OWL-DL compatibility.
                         // Individuals cannot be untyped.
        
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
         
            entURI = new String(preferredURI);          
            com.hp.hpl.jena.ontology.Individual test = ontModel.getIndividual(entURI);
            int count = 0;
            while (test != null) {
                ++count;
                entURI = new String(preferredURI) + count;
                test = ontModel.getIndividual(entURI);
            }
            
            try {
                ontModel.getBaseModel().notifyEvent(new IndividualCreationEvent(getWebappDaoFactory().getUserURI(),true,entURI));
                com.hp.hpl.jena.ontology.Individual ind = ontModel.createIndividual(entURI,cls);
                if (ent.getName() != null) {
                    ind.setLabel(ent.getName(), (String) getDefaultLanguage());
                }
                List<VClass> vclasses = ent.getVClasses(false);
                if (vclasses != null) {
                    for (Iterator<VClass> typeIt = vclasses.iterator(); typeIt.hasNext(); ) {
                        VClass vc = typeIt.next();
                        ind.addRDFType(ResourceFactory.createResource(vc.getURI()));
                    }
                }
                addPropertyDateTimeValue(ind,MODTIME,Calendar.getInstance().getTime(),ontModel);
                if (ent.getMainImageUri() != null) {
                	addPropertyResourceURIValue(ind, IND_MAIN_IMAGE, ent.getMainImageUri());
                }                
                if( ent.getSearchBoost() != null ) {
                    addPropertyFloatValue(ind,SEARCH_BOOST_ANNOT, ent.getSearchBoost(), ontModel);
                }
            } catch (Exception e) {
                log.error("Exception inserting individual: ",e);
            }
        } finally {
            ontModel.getBaseModel().notifyEvent(new IndividualCreationEvent(getWebappDaoFactory().getUserURI(),false,entURI));
            ontModel.leaveCriticalSection();
        }
        return entURI;
    }

    public int updateIndividual(Individual ent) {
        return updateIndividualWebapp(ent);
    }

    public int updateIndividualWebapp(Individual ent) {
        initInd(ent);
        return updateIndividual(ent, getOntModelSelector().getABoxModel());
    }

    private void initInd(Individual ent) {
        ent.getClass();
        ent.getVClasses(false);
        ent.getDataPropertyList();
        ent.getDataPropertyStatements();
        ent.getExternalIds();
        ent.getMainImageUri();
        ent.getModTime();
        ent.getName();
        ent.getNamespace();
        ent.getObjectPropertyList();
        ent.getVClassURI();
        ent.getVClass();
        ent.getVClassURI();
    }

    public int updateIndividual(Individual ent, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            ontModel.getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),true,ent.getURI()));
            com.hp.hpl.jena.ontology.Individual ind = ontModel.getIndividual(ent.getURI());
            if (ind != null) {
                if (ent.getName() != null && ( (ind.getLabel(getDefaultLanguage())==null) || (ind.getLabel(getDefaultLanguage())!=null && ent.getName()!=null && !ent.getName().equals(ind.getLabel(getDefaultLanguage())) ) ) ) {
                	
                	// removal of existing values done this odd way to trigger
                	// the change listeners
                	Model temp = ModelFactory.createDefaultModel();
                	temp.add(ontModel.listStatements(ind, RDFS.label, (RDFNode) null));
                	ontModel.remove(temp);

                    ind.setLabel(ent.getName(), getDefaultLanguage());
                }
                Set<String> oldTypeURIsSet = new HashSet<String>();
                for (Iterator<Resource> typeIt = ind.listRDFTypes(true); typeIt.hasNext();) {
                    Resource t = typeIt.next();
                    if (t.getURI() != null) {
                        oldTypeURIsSet.add(t.getURI());
                    }
                }
                Set<String> newTypeURIsSet = new HashSet<String>();
                if (ent.getVClassURI() != null) {
                	newTypeURIsSet.add(ent.getVClassURI());
                }
                boolean conservativeTypeDeletion = false;
                try {
                    List<VClass> vcl = ent.getVClasses(false);
                    if (vcl == null) {
                        conservativeTypeDeletion = true; // if the bean has null here instead of an empty list, we don't want to trust it and just start deleting any existing types.  So we'll just update the Vitro flag-related types and leave the rest alone.
                    } else {
                        for (Iterator<VClass> typeIt = vcl.iterator(); typeIt.hasNext(); ) {
                            VClass vc = typeIt.next();
                            newTypeURIsSet.add(vc.getURI());
                        }
                    }
                } catch (Exception e) {
                    log.error(e, e);
                }
                for (Iterator<String> oldIt = oldTypeURIsSet.iterator(); oldIt.hasNext();) {
                    String uri = oldIt.next();
                    if (!newTypeURIsSet.contains(uri)) {
                        if ( (!conservativeTypeDeletion) || (uri.indexOf(VitroVocabulary.vitroURI) == 0) ) {
                            ind.removeRDFType(ResourceFactory.createResource(uri));
                        }
                    }
                }
                for (Iterator<String> newIt = newTypeURIsSet.iterator(); newIt.hasNext();) {
                    String uri = newIt.next();
                    if (!oldTypeURIsSet.contains(uri)) {
                        ind.addRDFType(ResourceFactory.createResource(uri));
                    }
                }
                updatePropertyDateTimeValue(ind,MODTIME,Calendar.getInstance().getTime(),ontModel);
                updatePropertyResourceURIValue(ind, IND_MAIN_IMAGE, ent.getMainImageUri(), ontModel);
                if( ent.getSearchBoost() != null ) {
                    updatePropertyFloatValue(ind, SEARCH_BOOST_ANNOT, ent.getSearchBoost(), ontModel);
                }
            return 0;
            } else {
                return 1;
            }
        } finally {
            ontModel.getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),false,ent.getURI()));
            ontModel.leaveCriticalSection();
        }
    }

    public void markModified(Individual ind) {
        markModified(ind,getOntModel());
    }

    public void markModified(Individual ind, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            com.hp.hpl.jena.ontology.Individual jind = ontModel.getIndividual(ind.getURI());
            if (jind != null) {
                updatePropertyDateTimeValue(jind,MODTIME,Calendar.getInstance().getTime(),ontModel);
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    public int deleteIndividual(String URI) {
        return deleteIndividual(URI, getOntModelSelector().getABoxModel());
    }

    public int deleteIndividual(String URI, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            if (URI == null) {
                log.debug("Cannot remove individual with null URI");
                return 1;
            }
            ontModel.getBaseModel().notifyEvent(new IndividualDeletionEvent(getWebappDaoFactory().getUserURI(),true,URI));
            Resource res = ontModel.getResource(URI);
            if (res == null) {
                // This should never happen, but just in case
                log.error("Null resource returned from model");
                return 1;
            }
            if (res.canAs(OntResource.class)) {
                OntResource ontRes = (OntResource) res.as(OntResource.class);
                smartRemove(ontRes, ontModel);
            } else {
                ontModel.removeAll(res,null,null);
                ontModel.removeAll(null,null,res);
            }
        } finally {
            ontModel.getBaseModel().notifyEvent(new IndividualDeletionEvent(getWebappDaoFactory().getUserURI(),false,URI));
            ontModel.leaveCriticalSection();
        }
        return 0;
    }

    public int deleteIndividual(Individual ent) {
        return deleteIndividual(ent.getURI());
    }

    public int deleteIndividualWebapp(Individual ent) {
        return deleteIndividual(ent.getURI());
    }

    public Individual getIndividualByURI(String entityURI) {
        if( entityURI == null || entityURI.length() == 0 )
            return null;

        OntModel ontModel = getOntModelSelector().getABoxModel();
        
        ontModel.enterCriticalSection(Lock.READ);
        try {
        	OntResource ontRes = (entityURI.startsWith(VitroVocabulary.PSEUDO_BNODE_NS)) 
        		? (OntResource) ontModel.createResource(new AnonId(entityURI.substring(VitroVocabulary.PSEUDO_BNODE_NS.length()))).as(OntResource.class)
        		: ontModel.getOntResource(entityURI);
            Individual ent = new IndividualJena(ontRes, (WebappDaoFactoryJena) getWebappDaoFactory());
            return ent;
        } catch (Exception ex) {
            return null;
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    public void fillVClassForIndividual(Individual entity) {
        entity.setVClass(getWebappDaoFactory().getVClassDao().getVClassByURI(entity.getVClassURI()));
    }

    /**
     * In Jena it can be difficult to get an object with a given dataproperty if
     * you do not care about the datatype or lang of the literal.  Use this
     * method if you would like to ignore the lang and datatype.  
     * 
     * Note: this method doesn't require that a property be declared in the 
     * ontology as a data property -- only that it behaves as one.
     */
    public List<Individual> getIndividualsByDataProperty(String dataPropertyUri, String value){        
        Property prop = null;
        if( RDFS.label.getURI().equals( dataPropertyUri )){
            prop = RDFS.label;
        }else{
            prop = getOntModel().getProperty(dataPropertyUri);
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
                    com.hp.hpl.jena.ontology.Individual ind = getOntModel().getIndividual(subUri);
                    individualsMap.put(subUri,new IndividualJena(ind, (WebappDaoFactoryJena) getWebappDaoFactory()));
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
                    com.hp.hpl.jena.ontology.Individual ind = getOntModel().getIndividual(subUri);
                    individualsMap.put(subUri,new IndividualJena(ind, (WebappDaoFactoryJena) getWebappDaoFactory()));
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
                    com.hp.hpl.jena.ontology.Individual ind = getOntModel().getIndividual(subUri);
                    individualsMap.put(subUri,new IndividualJena(ind, (WebappDaoFactoryJena) getWebappDaoFactory()));
                }                
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        
        List<Individual> rv = new ArrayList<Individual>(individualsMap.size());
        rv.addAll(individualsMap.values());
        return rv;
    }

    public List<Individual> getIndividualsByDataProperty(String dataPropertyUri, String value, String datatypeUri, String lang){        
        if( datatypeUri == null && lang == null )
            return getIndividualsByDataProperty(dataPropertyUri,value);
        
        Property prop = null;
        
        if( RDFS.label.getURI().equals( dataPropertyUri )){
            prop = RDFS.label;
        }else{
            prop = getOntModel().getDatatypeProperty(dataPropertyUri);
        }
        
        if( prop == null || value == null){
            log.debug("Could not getIndividualsByDataProperty() " +
                    "because " + dataPropertyUri + "was not found in model.");
            return Collections.emptyList();
        }

        ResIterator stmts = null;
        List<Individual> inds = new ArrayList<Individual>();

        Literal literal = null;
        if( datatypeUri != null && datatypeUri.length() > 0 )
            literal = getOntModel().createTypedLiteral(value, datatypeUri);
        else if( lang != null && lang.length() > 0 )
            literal =  getOntModel().createLiteral(value,lang);
        else
            literal = getOntModel().createLiteral(value);

        getOntModel().enterCriticalSection(Lock.READ);
        try{
            stmts = getOntModel().listSubjectsWithProperty(prop, literal);
            while(stmts.hasNext()){
                Resource st = stmts.nextResource();
                if ( st.getURI() == null ) { // check to make sure this node isn't null
                    continue;
                }
                com.hp.hpl.jena.ontology.Individual ind = getOntModel().getIndividual(st.getURI());
                inds.add(new IndividualJena(ind, (WebappDaoFactoryJena) getWebappDaoFactory()));
            }
        } finally {
            if( stmts != null ) stmts.close();
            getOntModel().leaveCriticalSection();
        }
        return inds;
    }    

    public Iterator<String> getAllOfThisTypeIterator() {
        //this is implemented in IndivdiualSDB
        throw new NotImplementedException();
    }  

    public Iterator<String> getUpdatedSinceIterator(long updatedSince){
        //this is implemented in IndivdiualSDB
        throw new NotImplementedException();
    }

    public boolean isIndividualOfClass(String vclassURI, String indURI) {
        if( vclassURI == null || indURI == null 
            || "".equals(vclassURI) || "".equals(indURI)) 
            return false;
        return getOntModel().contains(getOntModel().getResource(indURI), 
                    RDF.type ,
                    getOntModel().getResource(vclassURI));        
    }
    
	public String getUnusedURI(Individual individual) throws InsertException {
		String errMsg = null;		
		String namespace = null;
		String uri = null;
		boolean uriIsGood = false;
		
		if ( individual == null || 
			(individual.getURI() != null && individual.getURI().startsWith( DEFAULT_NAMESPACE ) ) 
			|| individual.getNamespace() == null 
			|| individual.getNamespace().length() == 0 
		    || DEFAULT_NAMESPACE.equals(individual.getNamespace()) ){
			//we always want local names like n23423 for the default namespace
			namespace = DEFAULT_NAMESPACE;
			uri = null;			
		}else if( individual.getURI() != null ){
			errMsg = getWebappDaoFactory().checkURI(individual.getURI());
			if( errMsg == null){
				uriIsGood = true;
				uri = individual.getURI();
			}else{
				throw new InsertException(errMsg);
			}
		}else{
			namespace = individual.getNamespace();
			if( namespace == null || namespace.length() == 0 )
				namespace = DEFAULT_NAMESPACE;
			String localName = individual.getName();
			
			/* try to use the specified namespace and local name */
			if (localName != null) {							
				localName = localName.replaceAll("\\W", "");
				localName = localName.replaceAll(":", "");
				if (localName.length() > 2) {
					if (Character.isDigit(localName.charAt(0))) {
						localName = "n" + localName;
					}
					uri = namespace + localName;
					errMsg = getWebappDaoFactory().checkURI(uri);
					if( errMsg == null)
						uriIsGood = true;
					else
						throw new InsertException(errMsg);					
				}
			}
			/* else try namespace + n2343 */ 			
		}
		
		Random random = new Random(System.currentTimeMillis());
		int attempts = 0;
		
		while( uriIsGood == false && attempts < 30 ){			
			String localName = "n" + random.nextInt( Math.min(Integer.MAX_VALUE,(int)Math.pow(2,attempts + 13)) );
			uri = namespace + localName;			
			errMsg = getWebappDaoFactory().checkURI(uri);
			if(  errMsg != null)
				uri = null;
			else
				uriIsGood = true;				
			attempts++;
		}
		
		if( uri == null )
			throw new InsertException("Could not create URI for individual: " + errMsg);
								
		return uri;
	}

    @Override
    // This method returns an EditLiteral rather than a Jena Literal, since IndividualDao
    // should not reference Jena objects. (However, the problem isn't really solved 
    // because EditLiteral currently references the Jena API.)
    public EditLiteral getLabelEditLiteral(String individualUri) {
        Literal literal = getLabelLiteral(individualUri);
        if (literal == null) {
            return null;
        }
        String value = literal.getLexicalForm();
        String datatype = literal.getDatatypeURI();
        String lang = literal.getLanguage();
        return new EditLiteral(value, datatype, lang);       
    }
	
}
