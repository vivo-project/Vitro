/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.joda.time.DateTime;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.search.IndexingException;
import edu.cornell.mannlib.vitro.webapp.search.VitroTermNames;
import edu.cornell.mannlib.vitro.webapp.search.beans.SearchQueryHandler;
import edu.cornell.mannlib.vitro.webapp.search.beans.IndividualProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.search.docbuilder.Obj2DocIface;

public class IndividualToSolrDocument implements Obj2DocIface {
    
    protected LuceneDocToSolrDoc luceneToSolr;
    
    public static final Log log = LogFactory.getLog(IndividualToSolrDocument.class.getName());
    
    public static VitroTermNames term = new VitroTermNames();
    
    private static String entClassName = Individual.class.getName();
    
    private ProhibitedFromSearch classesProhibitedFromSearch;
    
    private IndividualProhibitedFromSearch individualProhibitedFromSearch;
    
    private SearchQueryHandler searchQueryHandler;
    
    
        
    public IndividualToSolrDocument(ProhibitedFromSearch classesProhibitedFromSearch, 
    		IndividualProhibitedFromSearch individualProhibitedFromSearch,
    			SearchQueryHandler searchQueryHandler){
    	this.classesProhibitedFromSearch = classesProhibitedFromSearch;
    	this.individualProhibitedFromSearch = individualProhibitedFromSearch;
    	this.searchQueryHandler = searchQueryHandler;
    }
    
    @SuppressWarnings("static-access")
	@Override
    public Object translate(Object obj) throws IndexingException{
    	long tProhibited = System.currentTimeMillis();
    	
    	if(!(obj instanceof Individual))
    		return null;
    	
    	Individual ent = (Individual)obj;
    	String value;
    	String classPublicNames = "";
    	SolrInputDocument doc = new SolrInputDocument();
    	
    	float beta = searchQueryHandler.calculateBeta(ent.getURI());
    	doc.addField(term.BETA,beta);
    	
    	//DocId
    	String id = ent.getURI();
    	log.debug("translating " + id);
    	
    	if(id == null){
    		log.debug("cannot add individuals without URIs to lucene Index");
    		return null;
    	}else if( id.startsWith(VitroVocabulary.vitroURI) ||
    			id.startsWith(VitroVocabulary.VITRO_PUBLIC) ||
    			id.startsWith(VitroVocabulary.PSEUDO_BNODE_NS) ||
    			id.startsWith(OWL.NS)){
    		log.debug("not indexing because of namespace:" + id);
    		return null;
    	}
    	
    	//filter out class groups, owl:ObjectProperties etc..
    	if(individualProhibitedFromSearch.isIndividualProhibited(id)){
    		return null;
    	}
    	
    	log.debug("time to check if individual is prohibited:" + Long.toString(System.currentTimeMillis() - tProhibited));
    	
    	// Types and classgroups
    	boolean prohibited = false;
    	List<VClass> vclasses = ent.getVClasses(false);
    	long tClassgroup = System.currentTimeMillis();
    	for(VClass clz : vclasses){
    		if(clz.getURI() == null){
    			continue;
    		}else if(OWL.Thing.getURI().equals(clz.getURI())){
    			//index individuals of type owl:Thing, just don't add owl:Thing as the type field in the index
    			continue;
    		} else if(clz.getURI().startsWith(OWL.NS)){
    			log.debug("not indexing " + id + " because of type " + clz.getURI());
    			return null;
    		} else {
    			if( !prohibited && classesProhibitedFromSearch.isClassProhibited(clz.getURI()))
    				prohibited = true;
    			if( clz.getSearchBoost() != null)
    				doc.setDocumentBoost(doc.getDocumentBoost() + clz.getSearchBoost());
    			
    			doc.addField(term.RDFTYPE, clz.getURI());
    			
    			if(clz.getLocalName() != null){
    				doc.addField(term.CLASSLOCALNAME, clz.getLocalName());
    				doc.addField(term.CLASSLOCALNAMELOWERCASE, clz.getLocalName().toLowerCase());
    			}
    			
    			if(clz.getName() != null)
    				classPublicNames += clz.getName();
    			
    			//Classgroup URI
    			if(clz.getGroupURI() != null){
    				doc.addField(term.CLASSGROUP_URI,clz.getGroupURI());
    			}
    			
    		}
    	}
    	
    	log.debug("time to check if class is prohibited and adding classes, classgroups and type to the index: " + Long.toString(System.currentTimeMillis() - tClassgroup));

    	
    	doc.addField(term.PROHIBITED_FROM_TEXT_RESULTS, prohibited?"1":"0");
    	
    	//lucene DocID
    	doc.addField(term.DOCID, entClassName + id);
    	
    	//vitro id
    	doc.addField(term.URI, id);
    	
    	//java class
    	doc.addField(term.JCLASS, entClassName);
    	
    	//Individual Label
    	if(ent.getRdfsLabel() != null)
    		value = ent.getRdfsLabel();
    	else{
    		log.debug("Using local name for individual with rdfs:label " + ent.getURI());
    		value = ent.getLocalName();
    	}
    	
    	doc.addField(term.NAME_RAW, value, (NAME_BOOST*beta));
    	doc.addField(term.NAME_LOWERCASE, value.toLowerCase(),(NAME_BOOST*beta));
    	doc.addField(term.NAME_UNSTEMMED, value,(NAME_BOOST*beta));
    	doc.addField(term.NAME_STEMMED, value, (NAME_BOOST*beta));
    	
    	long tContextNodes = System.currentTimeMillis();
    	
    	String contextNodePropertyValues = "";
    	contextNodePropertyValues += searchQueryHandler.getPropertiesAssociatedWithEducationalTraining(ent.getURI());
        contextNodePropertyValues += searchQueryHandler.getPropertiesAssociatedWithRole(ent.getURI());
        contextNodePropertyValues += searchQueryHandler.getPropertiesAssociatedWithPosition(ent.getURI());
        contextNodePropertyValues += searchQueryHandler.getPropertiesAssociatedWithRelationship(ent.getURI());
        contextNodePropertyValues += searchQueryHandler.getPropertiesAssociatedWithAwardReceipt(ent.getURI());
        contextNodePropertyValues += searchQueryHandler.getPropertiesAssociatedWithInformationResource(ent.getURI()); 
        
        
        doc.addField(term.CONTEXTNODE, contextNodePropertyValues);

    	log.debug("time to fire contextnode queries and include them in the index: " + Long.toString(System.currentTimeMillis() - tContextNodes));

        
        long tMoniker = System.currentTimeMillis();
    	
        //Moniker 
        if(ent.getMoniker() != null){
        	doc.addField(term.MONIKER, ent.getMoniker());
        }
        
        //boost for entity
        if(ent.getSearchBoost() != null && ent.getSearchBoost() != 0)
        	doc.setDocumentBoost(ent.getSearchBoost());
        
        //thumbnail
        try{
        	value = null;
        	if(ent.hasThumb())
        		doc.addField(term.THUMBNAIL, "1");
        	else
        		doc.addField(term.THUMBNAIL, "0");
        }catch(Exception ex){
        	log.debug("could not index thumbnail: " + ex);
        }
        
        
        //time of index in millis past epoc
        Object anon[] =  { new Long((new DateTime() ).getMillis())  };
        doc.addField(term.INDEXEDTIME, String.format("%019d", anon));
        
    	log.debug("time to include moniker , thumbnail and indexedtime in the index: " + Long.toString(System.currentTimeMillis() - tMoniker));

        long tPropertyStatements = System.currentTimeMillis();
        if(!prohibited){
            //ALLTEXT, all of the 'full text'
            String t=null;
            value =""; 
            value+= " "+( ((t=ent.getName()) == null)?"":t );  
            value+= " "+( ((t=ent.getAnchor()) == null)?"":t); 
            value+= " "+ ( ((t=ent.getMoniker()) == null)?"":t ); 
            value+= " "+ ( ((t=ent.getDescription()) == null)?"":t ); 
            value+= " "+ ( ((t=ent.getBlurb()) == null)?"":t ); 
    
            value+= " " + classPublicNames; 
    
            List<DataPropertyStatement> dataPropertyStatements = ent.getDataPropertyStatements();
            if (dataPropertyStatements != null) {
                Iterator<DataPropertyStatement> dataPropertyStmtIter = dataPropertyStatements.iterator();
                while (dataPropertyStmtIter.hasNext()) {
                    DataPropertyStatement dataPropertyStmt =  dataPropertyStmtIter.next();
                    value+= " "+ ( ((t=dataPropertyStmt.getData()) == null)?"":t );
                }
            }
    
            List<ObjectPropertyStatement> objectPropertyStatements = ent.getObjectPropertyStatements();
            if (objectPropertyStatements != null) {
                Iterator<ObjectPropertyStatement> objectPropertyStmtIter = objectPropertyStatements.iterator();
                while (objectPropertyStmtIter.hasNext()) {
                    ObjectPropertyStatement objectPropertyStmt = objectPropertyStmtIter.next();
                    if( "http://www.w3.org/2002/07/owl#differentFrom".equals(objectPropertyStmt.getPropertyURI()) )
                        continue;
                    try {
                        value+= " "+ ( ((t=objectPropertyStmt.getObject().getName()) == null)?"":t );                        
                    } catch (Exception e) { 
                        log.debug("could not index name of related object: " + e.getMessage());
                    }
                }
            }
            
        	log.debug("time to include data property statements, object property statements in the index: " + Long.toString(System.currentTimeMillis() - tPropertyStatements));
            
            doc.addField(term.ALLTEXT, value,(ALL_TEXT_BOOST*beta));
            doc.addField(term.ALLTEXTUNSTEMMED, value,(ALL_TEXT_BOOST*beta));
        }
        
        return doc;
    }
    
//    public IndividualToSolrDocument(Entity2LuceneDoc e2d){
////        entityToLucene = e2d;  
//        luceneToSolr = new LuceneDocToSolrDoc();
//    }
    
    @Override
    public boolean canTranslate(Object obj) {
        return obj != null && obj instanceof Individual;
    }

    @Override
    public boolean canUnTranslate(Object result) {
        return result != null && result instanceof SolrDocument;
    }

    @Override
    public Object getIndexId(Object obj) {
        throw new Error("IndiviudalToSolrDocument.getIndexId() is unimplemented");        
    }

//    @Override
//    public Object translate(Object obj) throws IndexingException {
//        return luceneToSolr.translate( entityToLucene.translate( obj ) );
//    }

    @Override
    public Object unTranslate(Object result) {
        Individual ent = null;
        if( result != null && result instanceof Document){
            Document hit = (Document) result;
            String id = hit.get(term.URI);
            ent = new IndividualImpl();
            ent.setURI(id);
        }
        return ent;
    }

    public static float NAME_BOOST = 3.0F;
    public static float ALL_TEXT_BOOST = 2.0F;
    
}
