/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.lucene;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.joda.time.DateTime;

import com.hp.hpl.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.search.IndexingException;

import edu.cornell.mannlib.vitro.webapp.search.beans.IndividualProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.search.docbuilder.Obj2DocIface;

/**
 * This class expect that Entities passed to it will have
 * a VClass object and a list of keywords.  The entity should
 * be as full as possible.
 */
public class Entity2LuceneDoc  implements Obj2DocIface{
	
    /** These are the terms for the lucene index */
    public static class VitroLuceneTermNames{
        /** Id of entity, vclass or tab */
        public static String URI         = "URI";
        /** lucene document id */
        public static String DOCID      = "DocId";
        /** java class of the object that the Doc represents. */
        public static String JCLASS     = "JCLASS";
        /** rdf:type */
        public static String RDFTYPE    = "type";
        /** rdf:type */
        public static String CLASSGROUP_URI    = "classgroup";
        /** Modtime from db */
        public static String MODTIME    = "modTime";

        /** time of index in msec since epoc */
        public static String INDEXEDTIME= "indexedTime";
        /** text for 'full text' search, this is stemmed */
        public static String ALLTEXT    = "ALLTEXT";
        /** text for 'full text' search, this is unstemmed for
         * use with wildcards and prefix queries */
        public static String ALLTEXTUNSTEMMED = "ALLTEXTUNSTEMMED";
        /** class name for storing context nodes **/
        public static final String CONTEXTNODE = "contextNode";
        /** Does the individual have a thumbnail image? 1=yes 0=no */
        public static final String THUMBNAIL = "THUMBNAIL";        
        /** Should individual be included in full text search results? 1=yes 0=no */
        public static final String PROHIBITED_FROM_TEXT_RESULTS = "PROHIBITED_FROM_TEXT_RESULTS";
        /** class names in human readable form of an individual*/
        public static final String CLASSLOCALNAMELOWERCASE = "classLocalNameLowerCase";
        /** class names in human readable form of an individual*/
        public static final String CLASSLOCALNAME = "classLocalName";      

        // Fields derived from rdfs:label
        /** Raw rdfs:label: no lowercasing, no tokenizing, no stop words, no stemming.
         *  Used only in retrieval rather than search. **/
        public static String NAME_RAW = "nameRaw"; // was NAMERAW
        
        /** rdfs:label lowercased, no tokenizing, no stop words, no stemming **/
        public static String NAME_LOWERCASE = "nameLowercase"; // was NAMELOWERCASE
        
        /** Same as NAME_LOWERCASE, but single-valued so it's sortable. **/
        // RY Need to control how indexing selects which of multiple values to copy. 
        public static String NAME_LOWERCASE_SINGLE_VALUED = "nameLowercaseSingleValued";
        
        /** rdfs:label lowercased, tokenized, stop words, no stemming.
         *  Used for autocomplete matching on proper names. **/
        public static String NAME_UNSTEMMED = "nameUnstemmed"; // was NAMEUNSTEMMED        
        
        /** rdfs:label lowercased, tokenized, stop words, stemmed.
         *  Used for autocomplete matching where stemming is desired (e.g., book titles)  **/
        public static String NAME_STEMMED = "nameStemmed"; // was NAME
     
    }

    private static final Log log = LogFactory.getLog(Entity2LuceneDoc.class.getName());

    public static String earliestTime = "16000101";
    public static String latestTime = "30000101";

    /** a way to get to the term names for the lucene index */
    public static VitroLuceneTermNames term = new VitroLuceneTermNames();

    private static String entClassName = Individual.class.getName();
    
    private ProhibitedFromSearch classesProhibitedFromSearch;
    
    private IndividualProhibitedFromSearch individualProhibited;
    
    private static HashMap<String, String> IndividualURIToObjectProperties = new HashMap<String, String>();
    
    private static HashSet<String> objectProperties = new HashSet<String>();
    
    public Entity2LuceneDoc(
            ProhibitedFromSearch classesProhibitedFromSearch, 
            IndividualProhibitedFromSearch individualProhibited){
        this.classesProhibitedFromSearch = classesProhibitedFromSearch;
        this.individualProhibited = individualProhibited;
    }
    
    public boolean canTranslate(Object obj) {    	
        return (obj != null && obj instanceof Individual);        	
    }    

    @SuppressWarnings("static-access")
    public Object translate(Object obj) throws IndexingException {
        if(!( obj instanceof Individual))
            return null;
        Individual ent = (Individual)obj;
        String value;
        Document doc = new Document();
        String classPublicNames = "";
        
        //DocId
        String id = ent.getURI();
        log.debug("translating " + id);
        
        if( id == null ){
        	log.debug("cannot add individuals without URIs to lucene index");
            return null;
        }else if( id.startsWith( VitroVocabulary.vitroURI ) 
                || id.startsWith( VitroVocabulary.VITRO_PUBLIC )
                || id.startsWith( VitroVocabulary.PSEUDO_BNODE_NS)
                || id.startsWith( OWL.NS ) ){
            log.debug("not indxing because of namespace:" + id );
            return null;
        }
        
        //filter out class groups, owl:ObjectProperties etc.
        if( individualProhibited.isIndividualProhibited( id ) ){
            return null;
        }
        
        /* Types and ClassGroup */
        boolean prohibited = false;
        List<VClass> vclasses = ent.getVClasses(false);
        for( VClass clz : vclasses){
            if( clz.getURI() == null ){
                continue;
            }else if( OWL.Thing.getURI().equals( clz.getURI()) ){
                //index individuals of type owl:Thing, just don't add owl:Thing the type field in the index
                continue;
            } else if ( clz.getURI().startsWith( OWL.NS ) ){
                log.debug("not indexing " + id + " because of type " + clz.getURI());
                return null;
             }else{                
                if( !prohibited && classesProhibitedFromSearch.isClassProhibitedFromSearch(clz.getURI()) )
                    prohibited = true;                                                   
                
                if( clz.getSearchBoost() != null )
                    doc.setBoost( doc.getBoost() + clz.getSearchBoost() );
                
                Field typeField = new Field (term.RDFTYPE, clz.getURI(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
                doc.add( typeField);
                
                if(clz.getLocalName() != null){
                	Field classLocalName = new Field(term.CLASSLOCALNAME, clz.getLocalName(), Field.Store.YES, Field.Index.ANALYZED);
                	Field classLocalNameLowerCase = new Field(term.CLASSLOCALNAMELOWERCASE, clz.getLocalName().toLowerCase(), Field.Store.YES, Field.Index.ANALYZED);
                	doc.add(classLocalName);
                	doc.add(classLocalNameLowerCase);
                }
                
                if( clz.getName() != null )
                    classPublicNames = classPublicNames + " " + clz.getName();
                
                //Classgroup URI
                if( clz.getGroupURI() != null ){
                	Field classGroupField = new Field(term.CLASSGROUP_URI, clz.getGroupURI(), 
                            Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
                //	classGroupField.setBoost(FIELD_BOOST);
                    doc.add(classGroupField);
                }
            }
        }        
        doc.add( new Field(term.PROHIBITED_FROM_TEXT_RESULTS, prohibited?"1":"0", 
                Field.Store.NO,Field.Index.NOT_ANALYZED_NO_NORMS) );
        
        /* lucene DOCID */
        doc.add( new Field(term.DOCID, entClassName + id,
                            Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
        
        
        //vitro Id        
        doc.add(  new Field(term.URI, id, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));        
        
        
        //java class
        doc.add( new  Field(term.JCLASS, entClassName, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
        
        // Individual label      
        if( ent.getRdfsLabel() != null )
            value=ent.getRdfsLabel();
        else{
            //log.debug("Skipping individual without rdfs:label " + ent.getURI());
            //return null;
            log.debug("Using local name for individual with rdfs:label " + ent.getURI());
            value = ent.getLocalName();
        }
        Field nameRaw = new Field(term.NAME_RAW, value, Field.Store.YES, Field.Index.NOT_ANALYZED);
        nameRaw.setBoost(NAME_BOOST);
        doc.add(nameRaw);
        
        // RY Not sure if we need to store this. For Solr, see schema.xml field definition.
        Field nameLowerCase = new Field(term.NAME_LOWERCASE, value.toLowerCase(), Field.Store.YES, Field.Index.NOT_ANALYZED);
        nameLowerCase.setBoost(NAME_BOOST);
        doc.add(nameLowerCase);
        
        Field nameUnstemmed = new Field(term.NAME_UNSTEMMED, value, Field.Store.NO, Field.Index.ANALYZED);
        nameUnstemmed.setBoost(NAME_BOOST);
        doc.add(nameUnstemmed);
        
        Field nameStemmed = new Field(term.NAME_STEMMED, value, Field.Store.NO, Field.Index.ANALYZED);
        nameStemmed.setBoost(NAME_BOOST);
        doc.add(nameStemmed);
        
        String contextNodePropertyValues;
        
//        if(ent.isVClass("http://xmlns.com/foaf/0.1/Person")){
        /*contextNodePropertyValues = searchQueryHandler.getPropertiesAssociatedWithEducationalTraining(ent.getURI());
        contextNodePropertyValues += searchQueryHandler.getPropertiesAssociatedWithRole(ent.getURI());
        contextNodePropertyValues += searchQueryHandler.getPropertiesAssociatedWithPosition(ent.getURI());
        contextNodePropertyValues += searchQueryHandler.getPropertiesAssociatedWithRelationship(ent.getURI());
        contextNodePropertyValues += searchQueryHandler.getPropertiesAssociatedWithAwardReceipt(ent.getURI());
        contextNodePropertyValues += searchQueryHandler.getPropertiesAssociatedWithInformationResource(ent.getURI());      */  

//        }
        
       /* Field contextNodeInformation = new Field(term.CONTEXTNODE, contextNodePropertyValues, Field.Store.YES, Field.Index.ANALYZED );
        doc.add(contextNodeInformation);*/
        
        //boost for entity
        if( ent.getSearchBoost() != null && ent.getSearchBoost() != 0 )
            doc.setBoost(ent.getSearchBoost());
       
        //Modification time
//        if( ent.getModTime() != null){
//            value = (new DateTime(ent.getModTime().getTime()))
//                .toString(LuceneIndexer.MODTIME_DATE_FORMAT) ;
//        } else {
//            value=  (new DateTime()).toString(LuceneIndexer.MODTIME_DATE_FORMAT) ;
//        }
//        doc.add(  new Field(term.MODTIME, value , Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));      
        
        /* thumbnail */
        try{
            value = null;
            if( ent.hasThumb() )
                doc.add(new Field(term.THUMBNAIL, "1", Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
            else
                doc.add(new Field(term.THUMBNAIL, "0", Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
        }catch(Exception ex){
            log.debug("could not index thumbnail: " + ex);
        }
                        
        
        //time of index in millis past epoc
        Object anon[] =  { new Long((new DateTime() ).getMillis())  };
        doc.add(  new Field(term.INDEXEDTIME, String.format( "%019d", anon ),
                            Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));                 
        
        
        if( ! prohibited ){
            //ALLTEXT, all of the 'full text'
            String t=null;
            value =""; 
            value+= " "+( ((t=ent.getName()) == null)?"":t );  
            value+= " "+ getKeyterms(ent); 
    
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
                        
                        if(ent.isVClass("http://xmlns.com/foaf/0.1/Person")){
                        	//IndividualURIToObjectProperties.put(ent.getURI(), ( ((t=objectPropertyStmt.getProperty().getURI()) == null)?"":t ) );
                        	objectProperties.add(( ((t=objectPropertyStmt.getProperty().getURI()) == null)?"":t ));
                        }
                        
                    } catch (Exception e) { 
                        log.debug("could not index name of related object: " + e.getMessage());
                    }
                }
            }
            //stemmed terms
            doc.add( new  Field(term.ALLTEXT, value , Field.Store.NO, Field.Index.ANALYZED));            
            //unstemmed terms
            doc.add( new Field(term.ALLTEXTUNSTEMMED, value, Field.Store.NO, Field.Index.ANALYZED));
        }
        
        
       // log.info("\n IndividualURItoObjectProperties " + IndividualURIToObjectProperties.toString() + " \n\n");
        log.info(" \n Object Properties " + objectProperties.toString() + "\n\n");
        
        return doc;
    }           

    @SuppressWarnings("static-access")
    public boolean canUnTranslate(Object result) {
        if( result != null && result instanceof Document){
            Document hit = (Document) result;
            if( entClassName.equalsIgnoreCase(hit.get(term.JCLASS)) ){
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("static-access")
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

    @SuppressWarnings("static-access")
    public Object getIndexId(Object obj) {
        return new Term(term.DOCID, entClassName + ((Individual)obj).getURI() ) ;
    }

    private String getKeyterms(Individual ent){
        /* bdc34: vitro:keywords are no longer being indexed */
        return "";
    }

    public ProhibitedFromSearch getClassesProhibitedFromSearch() {
        return classesProhibitedFromSearch;
    }

    public void setClassesProhibitedFromSearch(
            ProhibitedFromSearch classesProhibitedFromSearch) {
        this.classesProhibitedFromSearch = classesProhibitedFromSearch;
    }
    
    public static float NAME_BOOST = 3.0F;
    public static float MONIKER_BOOST = 2.0F;
    public static float FIELD_BOOST = 1.0F;
}
