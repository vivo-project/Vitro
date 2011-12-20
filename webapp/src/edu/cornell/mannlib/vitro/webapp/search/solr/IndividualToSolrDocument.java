/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.solr;

import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;
import edu.cornell.mannlib.vitro.webapp.search.beans.ClassProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.search.beans.IndividualProhibitedFromSearch;

public class IndividualToSolrDocument {
        
    public static final Log log = LogFactory.getLog(IndividualToSolrDocument.class.getName());
    
    public static VitroSearchTermNames term = new VitroSearchTermNames();
    
    protected static String entClassName = Individual.class.getName();
    
    protected ClassProhibitedFromSearch classesProhibitedFromSearch;
    
    protected IndividualProhibitedFromSearch individualProhibitedFromSearch;
    
    protected final String label = "http://www.w3.org/2000/01/rdf-schema#label";
    
    protected List<DocumentModifier> documentModifiers = new ArrayList<DocumentModifier>();
    
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

    }
    

	@SuppressWarnings("static-access")
    public SolrInputDocument translate(Individual ind) throws IndexingException{
        try{    	            	      	        	
        	log.debug("translating " + ind.getURI());
        	checkForSkipBasedOnNS( ind );
        	        	            
        	SolrInputDocument doc = new SolrInputDocument();                    	
        	
            //DocID
            doc.addField(term.DOCID, getIdForUri( ind.getURI() ) );
            
            //vitro id
            doc.addField(term.URI, ind.getURI());
            
            //java class
            doc.addField(term.JCLASS, entClassName);

            //Individual Label
            addLabel( ind, doc );
            
        	//add classes, classgroups get if prohibied becasue of its class
            StringBuffer classPublicNames = new StringBuffer("");
        	boolean prohibited = addClasses(ind, doc, classPublicNames);
        	
        	//filter out class groups, owl:ObjectProperties etc..
        	if(individualProhibitedFromSearch.isIndividualProhibited( ind.getURI() )){
        		return null;
        	}        	   
        	        	        	
        	// collecting URIs and rdfs:labels of objects of statements         	
        	StringBuffer objectNames = new StringBuffer("");        	
        	StringBuffer addUri = new StringBuffer("");           	
        	addObjectPropertyText(ind, doc, objectNames, addUri);        	                 	                                   
        	
        	//add if the individual has a thumbnail or not.
        	addThumbnailExistance(ind, doc);           
                        
            //time of index in millis past epoc
            doc.addField(term.INDEXEDTIME, new Long( (new DateTime()).getMillis() ) ); 
            
            if(!prohibited){
               addAllText( ind, doc, classPublicNames, objectNames );
               
               runAdditionalDocModifers(ind,doc,addUri);                        

               //boost for entity
                if(documentModifiers == null || documentModifiers.isEmpty() &&
                   (ind.getSearchBoost() != null && ind.getSearchBoost() != 0)) {
                        doc.setDocumentBoost(ind.getSearchBoost());                    
                }
            }
            
            return doc;
        }catch(SkipIndividualException ex){
            //indicates that this individual should not be indexed by returning null
            log.debug(ex);
            return null;
        }catch(Throwable th){
            //Odd exceptions from jena get thrown on shutdown
            if( log != null )
                log.debug(th);
            return null;
        }
    }
    
           
	protected void runAdditionalDocModifers( Individual ind, SolrInputDocument doc, StringBuffer addUri ) 
    throws SkipIndividualException{
        //run the document modifiers
        if( documentModifiers != null && !documentModifiers.isEmpty()){
            for(DocumentModifier modifier: documentModifiers){
                modifier.modifyDocument(ind, doc, addUri);
            }
        }
    }
    
    protected void checkForSkipBasedOnNS(Individual ind) throws SkipIndividualException {
        String id = ind.getURI();                  
        if(id == null){            
            throw new SkipIndividualException("cannot add individuals without URIs to search index");
        }else if( id.startsWith(VitroVocabulary.vitroURI) ||
                id.startsWith(VitroVocabulary.VITRO_PUBLIC) ||
                id.startsWith(VitroVocabulary.PSEUDO_BNODE_NS) ||
                id.startsWith(OWL.NS)){
            throw new SkipIndividualException("not indexing because of namespace:" + id);            
        }        
    }

    protected void addAllText(Individual ind, SolrInputDocument doc, StringBuffer classPublicNames, StringBuffer objectNames) {
        String t=null;
        //ALLTEXT, all of the 'full text'
        StringBuffer allTextValue = new StringBuffer();

        //collecting data property statements
        List<DataPropertyStatement> dataPropertyStatements = ind.getDataPropertyStatements();
        if (dataPropertyStatements != null) {
            Iterator<DataPropertyStatement> dataPropertyStmtIter = dataPropertyStatements.iterator();
            while (dataPropertyStmtIter.hasNext()) {
                DataPropertyStatement dataPropertyStmt =  dataPropertyStmtIter.next();
                if(dataPropertyStmt.getDatapropURI().equals(label)){ // we don't want label to be added to alltext
                    continue;
                } else if(dataPropertyStmt.getDatapropURI().equals("http://vivoweb.org/ontology/core#preferredTitle")){
                	//add the preferredTitle field
                	String preferredTitle = null;
                	doc.addField(term.PREFERRED_TITLE, ((preferredTitle=dataPropertyStmt.getData()) == null)?"":preferredTitle);
                	log.debug("Preferred Title: " + dataPropertyStmt.getData());
                }
                allTextValue.append(" ");
                allTextValue.append(((t=dataPropertyStmt.getData()) == null)?"":t);
            }
        }
         
        allTextValue.append(objectNames.toString());
        
        allTextValue.append(' ');                      
        allTextValue.append(classPublicNames);
        
        try {
            String stripped = Jsoup.parse(allTextValue.toString()).text();
            allTextValue.setLength(0);
            allTextValue.append(stripped);
        } catch(Exception e) {
            log.debug("Could not strip HTML during search indexing. " + e);
        }
                
        String alltext = allTextValue.toString();
        
        doc.addField(term.ALLTEXT, alltext);
        doc.addField(term.ALLTEXTUNSTEMMED, alltext);
        doc.addField(term.ALLTEXT_PHONETIC, alltext);
    }

    protected void addLabel(Individual ind, SolrInputDocument doc) {
        String value = "";
        String label = ind.getRdfsLabel();
        if (label != null) {
            value = label;
        } else {
            value = ind.getLocalName();
        }            

        doc.addField(term.NAME_RAW, value);
        // NAME_RAW will be copied by solr into the following fields:
        // NAME_LOWERCASE, NAME_UNSTEMMED, NAME_STEMMED, NAME_PHONETIC, AC_NAME_UNTOKENIZED, AC_NAME_STEMMED
    }

    /**
     * Adds if the individual has a thumbnail image or not.
     */
    protected void addThumbnailExistance(Individual ind, SolrInputDocument doc) {
        try{
            if(ind.hasThumb())
                doc.addField(term.THUMBNAIL, "1");
            else
                doc.addField(term.THUMBNAIL, "0");
        }catch(Exception ex){
            log.debug("could not index thumbnail: " + ex);
        }        
    }

    /**
     * Get the rdfs:labes for objects of statements and put in objectNames.
     *  Get the URIs for objects of statements and put in addUri.
     */
    protected void addObjectPropertyText(Individual ind, SolrInputDocument doc,
            StringBuffer objectNames, StringBuffer addUri) {
        List<ObjectPropertyStatement> objectPropertyStatements = ind.getObjectPropertyStatements();
        if (objectPropertyStatements != null) {
            Iterator<ObjectPropertyStatement> objectPropertyStmtIter = objectPropertyStatements.iterator();
            while (objectPropertyStmtIter.hasNext()) {
                ObjectPropertyStatement objectPropertyStmt = objectPropertyStmtIter.next();
                if( "http://www.w3.org/2002/07/owl#differentFrom".equals(objectPropertyStmt.getPropertyURI()) ){
                    continue;
                }
                try {
                    objectNames.append(" ");
                    String t=null;
                    objectNames.append(((t=objectPropertyStmt.getObject().getRdfsLabel()) == null)?"":t);   
                    addUri.append(" ");
                    addUri.append(((t=objectPropertyStmt.getObject().getURI()) == null)?"":t);
                } catch (Exception e) { 
                     log.debug("could not index name of related object: " + e.getMessage());
                }
            }
        }        
    }

    /**
     * Adds the info about the classes that the individual is a member
     * of, classgroups and checks if prohibited.
     * @param classPublicNames 
     * @returns true if prohibited from search
     * @throws SkipIndividualException 
     */
    protected boolean addClasses(Individual ind, SolrInputDocument doc, StringBuffer classPublicNames) throws SkipIndividualException{
        ArrayList<String> superClassNames = null;        
        
        // Types and classgroups
        boolean prohibited = false;
        List<VClass> vclasses = ind.getVClasses(false);
        superClassNames = new ArrayList<String>();         
        for(VClass clz : vclasses){
            String superLclName = clz.getLocalName();
            superClassNames.add(superLclName);
            if(clz.getURI() == null){
                continue;
            }else if(OWL.Thing.getURI().equals(clz.getURI())){
                //index individuals of type owl:Thing, just don't add owl:Thing as the type field in the index
                continue;
            } else if(clz.getURI().startsWith(OWL.NS)){
                throw new SkipIndividualException("not indexing " + ind.getURI() + " because of type " + clz.getURI() );    
            } 
            // do not index individuals of type Role, AdvisingRelationShip, Authorship, etc.(see search.n3 for more information)
            else if(classesProhibitedFromSearch.isClassProhibitedFromSearch(clz.getURI())){
            	 throw new SkipIndividualException("not indexing " + ind.getURI() + " because of prohibited type " + clz.getURI() );
            } else {
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
                
                //Add the Classgroup URI to a field
                if(clz.getGroupURI() != null){
                    doc.addField(term.CLASSGROUP_URI,clz.getGroupURI());
                }               
            }
        }            
        
        if(superClassNames.isEmpty()){
            throw new SkipIndividualException("Not indexing because individual has no super classes");
        }               
        
        doc.addField(term.PROHIBITED_FROM_TEXT_RESULTS, prohibited?"1":"0");
        return prohibited;
    }
    
    public Object getIndexId(Object obj) {
        throw new Error("IndiviudalToSolrDocument.getIndexId() is unimplemented");        
    }
    
    public String getIdForUri(String uri){
        if( uri != null ){
            return  entClassName + uri;
        }else{
            return null;
        }
    }
    
    public String getQueryForId(String uri ){
        return term.DOCID + ':' + getIdForUri(uri);
    }
    
    public Individual unTranslate(Object result) {
        Individual ent = null;

        if( result != null && result instanceof SolrDocument){
            SolrDocument hit = (SolrDocument) result;
            String uri= (String) hit.getFirstValue(term.URI);

            ent = new IndividualImpl();
            ent.setURI(uri);
        }
        return ent;
    }
    
    public void shutdown(){
        for(DocumentModifier dm: documentModifiers){
            try{
                dm.shutdown();
            }catch(Exception e){
                if( log != null)
                    log.debug(e,e);
            }
        }
    }

    public static float NAME_BOOST = 1.2F;
    
}
