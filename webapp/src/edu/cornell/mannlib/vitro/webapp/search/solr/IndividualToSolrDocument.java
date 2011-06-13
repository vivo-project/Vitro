/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.joda.time.DateTime;

import com.hp.hpl.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.search.IndexingException;
import edu.cornell.mannlib.vitro.webapp.search.VitroTermNames;
import edu.cornell.mannlib.vitro.webapp.search.beans.ClassProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.search.beans.IndividualProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.search.docbuilder.Obj2DocIface;

public class IndividualToSolrDocument {
        
    public static final Log log = LogFactory.getLog(IndividualToSolrDocument.class.getName());
    
    public static VitroTermNames term = new VitroTermNames();
    
    private static String entClassName = Individual.class.getName();
    
    private ClassProhibitedFromSearch classesProhibitedFromSearch;
    
    private IndividualProhibitedFromSearch individualProhibitedFromSearch;
    
    public static ArrayList<String> superClassNames = null;
    
    public static StringBuffer addUri = null;

    private List<DocumentModifier> documentModifiers = new ArrayList<DocumentModifier>();
    
    private static List<String> contextNodeClassNames = new ArrayList<String>();
    
    public IndividualToSolrDocument(
            ClassProhibitedFromSearch classesProhibitedFromSearch, 
    		IndividualProhibitedFromSearch individualProhibitedFromSearch){
    	
        this(   classesProhibitedFromSearch,
    	        individualProhibitedFromSearch,
    	        Collections.EMPTY_LIST);
    }
    
    public IndividualToSolrDocument(
            ClassProhibitedFromSearch classesProhibitedFromSearch, 
            IndividualProhibitedFromSearch individualProhibitedFromSearch,
            List<DocumentModifier> docModifiers){
        this.classesProhibitedFromSearch = classesProhibitedFromSearch;
        this.individualProhibitedFromSearch = individualProhibitedFromSearch;
        this.documentModifiers = docModifiers;
        fillContextNodes(); 
    }
    
    @SuppressWarnings("static-access")
    public SolrInputDocument translate(Individual ind) throws IndexingException{
    	long tProhibited = System.currentTimeMillis();    	    	
    	String value;
    	StringBuffer classPublicNames = new StringBuffer();
    	classPublicNames.append("");
    	SolrInputDocument doc = new SolrInputDocument();
    	
    	//DocId
    	String id = ind.getURI();
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
    	List<VClass> vclasses = ind.getVClasses(false);
    	superClassNames = new ArrayList<String>();
    	String superLclName = null;
    	long tClassgroup = System.currentTimeMillis();
    	for(VClass clz : vclasses){
    		superLclName = clz.getLocalName();
    		superClassNames.add(superLclName);
    		if(clz.getURI() == null){
    			continue;
    		}else if(OWL.Thing.getURI().equals(clz.getURI())){
    			//index individuals of type owl:Thing, just don't add owl:Thing as the type field in the index
    			continue;
    		} else if(clz.getURI().startsWith(OWL.NS)){
    			log.debug("not indexing " + id + " because of type " + clz.getURI());
    			return null;
    		} else if(contextNodeClassNames.contains(superLclName)) { // check to see if context node is being indexed.
    			return null;
    		}
    		else {
    			if( !prohibited && classesProhibitedFromSearch.isClassProhibitedFromSearch(clz.getURI()))
    				prohibited = true;
    			if( clz.getSearchBoost() != null)
    				doc.setDocumentBoost(doc.getDocumentBoost() + clz.getSearchBoost());
    			
    			doc.addField(term.RDFTYPE, clz.getURI());
    			
    			if(clz.getLocalName() != null){
    				doc.addField(term.CLASSLOCALNAME, clz.getLocalName());
    				doc.addField(term.CLASSLOCALNAMELOWERCASE, clz.getLocalName().toLowerCase());
    			}
    			
    			if(clz.getName() != null){
    				classPublicNames.append(" ");
    			    classPublicNames.append(clz.getName());
    			}
    			
    			//Classgroup URI
    			if(clz.getGroupURI() != null){
    				doc.addField(term.CLASSGROUP_URI,clz.getGroupURI());
    			}
    			
    		}
    	}
    	
    	if(superClassNames.isEmpty()){
    		return null;
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
    	if(ind.getRdfsLabel() != null)
    		value = ind.getRdfsLabel();
    	else{
    		log.debug("Using local name for individual with rdfs:label " + ind.getURI());
    		value = ind.getLocalName();
    	}
    	
    	// collecting object property statements 
    	
    	String uri = ind.getURI();
    	StringBuffer objectNames = new StringBuffer();
    	objectNames.append("");
    	String t=null;
    	addUri = new StringBuffer();
    	addUri.append("");
    	 List<ObjectPropertyStatement> objectPropertyStatements = ind.getObjectPropertyStatements();
         if (objectPropertyStatements != null) {
             Iterator<ObjectPropertyStatement> objectPropertyStmtIter = objectPropertyStatements.iterator();
             while (objectPropertyStmtIter.hasNext()) {
                 ObjectPropertyStatement objectPropertyStmt = objectPropertyStmtIter.next();
                 if( "http://www.w3.org/2002/07/owl#differentFrom".equals(objectPropertyStmt.getPropertyURI()) )
                     continue;
                 try {
                	 objectNames.append(" ");
                     objectNames.append(((t=objectPropertyStmt.getObject().getName()) == null)?"":t);   
                     addUri.append(" ");
                     addUri.append(((t=objectPropertyStmt.getObject().getURI()) == null)?"":t);
                 } catch (Exception e) { 
                     log.debug("could not index name of related object: " + e.getMessage());
                 }
             }
         }         
    	
         if(documentModifiers == null){
        	 doc.addField(term.NAME_RAW, value, NAME_BOOST);
        	 doc.addField(term.NAME_LOWERCASE, value.toLowerCase(),NAME_BOOST);
        	 doc.addField(term.NAME_UNSTEMMED, value,NAME_BOOST);
        	 doc.addField(term.NAME_STEMMED, value, NAME_BOOST);
        	 doc.addField(term.NAME_PHONETIC, value, PHONETIC_BOOST);
         }else{
        	 doc.addField(term.NAME_RAW, value);
        	 doc.addField(term.NAME_LOWERCASE, value.toLowerCase());
        	 doc.addField(term.NAME_UNSTEMMED, value);
        	 doc.addField(term.NAME_STEMMED, value);
        	 doc.addField(term.NAME_PHONETIC, value, PHONETIC_BOOST);
         }
    	
        
        long tMoniker = System.currentTimeMillis();
    	
        if(documentModifiers == null){
        //boost for entity
        if(ind.getSearchBoost() != null && ind.getSearchBoost() != 0)
        doc.setDocumentBoost(ind.getSearchBoost());
        }
        
        //thumbnail
        try{
        	value = null;
        	if(ind.hasThumb())
        		doc.addField(term.THUMBNAIL, "1");
        	else
        		doc.addField(term.THUMBNAIL, "0");
        }catch(Exception ex){
        	log.debug("could not index thumbnail: " + ex);
        }
        
        
        //time of index in millis past epoc
        Object anon[] =  { new Long((new DateTime() ).getMillis())  };
        doc.addField(term.INDEXEDTIME, String.format("%019d", anon));
        
    	log.debug("time to include thumbnail and indexedtime in the index: " + Long.toString(System.currentTimeMillis() - tMoniker));

        long tPropertyStatements = System.currentTimeMillis();
        
        //collecting data property statements 
        
        if(!prohibited){
            //ALLTEXT, all of the 'full text'
            StringBuffer allTextValue = new StringBuffer();
            allTextValue.append("");
            allTextValue.append(" ");
            allTextValue.append(((t=ind.getName()) == null)?"":t);  
            allTextValue.append(" ");
            allTextValue.append(((t=ind.getAnchor()) == null)?"":t); 
            allTextValue.append(" ");
            allTextValue.append(classPublicNames.toString()); 
    
            List<DataPropertyStatement> dataPropertyStatements = ind.getDataPropertyStatements();
            if (dataPropertyStatements != null) {
                Iterator<DataPropertyStatement> dataPropertyStmtIter = dataPropertyStatements.iterator();
                while (dataPropertyStmtIter.hasNext()) {
                    DataPropertyStatement dataPropertyStmt =  dataPropertyStmtIter.next();
                    allTextValue.append(" ");
                    allTextValue.append(((t=dataPropertyStmt.getData()) == null)?"":t);
                }
            }
             
            allTextValue.append(objectNames.toString());
            
        	log.debug("time to include data property statements, object property statements in the index: " + Long.toString(System.currentTimeMillis() - tPropertyStatements));
            
        	String alltext = allTextValue.toString();
            doc.addField(term.ALLTEXT, alltext);
            doc.addField(term.ALLTEXTUNSTEMMED, alltext);
            doc.addField(term.ALLTEXT_PHONETIC, alltext,PHONETIC_BOOST);
            
            //run the document modifiers
            if( documentModifiers != null ){
            	doc.addField(term.targetInfo,"");
                for(DocumentModifier modifier: documentModifiers){
                    modifier.modifyDocument(ind, doc);
                }
            }
        }
        
        return doc;
    }
    
           
    public Object getIndexId(Object obj) {
        throw new Error("IndiviudalToSolrDocument.getIndexId() is unimplemented");        
    }

    public Individual unTranslate(Object result) {
        Individual ent = null;
        if( result != null && result instanceof Document){
            Document hit = (Document) result;
            String id = hit.get(term.URI);
            ent = new IndividualImpl();
            ent.setURI(id);
        }
        return ent;
    }
    
    private void fillContextNodes(){
    	this.contextNodeClassNames.add("Role");
        this.contextNodeClassNames.add("AttendeeRole");
        this.contextNodeClassNames.add("ClinicalRole");
        this.contextNodeClassNames.add("LeaderRole");
        this.contextNodeClassNames.add("MemberRole");
        this.contextNodeClassNames.add("OutreachProviderRole");
        this.contextNodeClassNames.add("PresenterRole");
        this.contextNodeClassNames.add("ResearcherRole");
        this.contextNodeClassNames.add("InvestigatorRole");
        this.contextNodeClassNames.add("CoPrincipalInvestigatorRole");
        this.contextNodeClassNames.add("PrincipalInvestigatorRole");
        this.contextNodeClassNames.add("ServiceProviderRole");
        this.contextNodeClassNames.add("TeacherRole");
        this.contextNodeClassNames.add("Position");
        this.contextNodeClassNames.add("FacultyAdministrativePosition");
        this.contextNodeClassNames.add("FacultyPosition");
        this.contextNodeClassNames.add("LibrarianPosition");
        this.contextNodeClassNames.add("Non-AcademicPosition");
        this.contextNodeClassNames.add("Non-FacultyAcademicPosition");
        this.contextNodeClassNames.add("PostdoctoralPosition");
        this.contextNodeClassNames.add("AdvisingRelationship");
        this.contextNodeClassNames.add("Authorship");
        this.contextNodeClassNames.add("AcademicDegree");
    }
    

    public static float NAME_BOOST = 2.0F;
    public static float ALL_TEXT_BOOST = 2.5F;
    public static float PHONETIC_BOOST = 0.1F;
    
    
}
