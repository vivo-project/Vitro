/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.lucene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.joda.time.DateTime;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.search.IndexingException;
import edu.cornell.mannlib.vitro.webapp.search.docbuilder.Obj2DocIface;
import edu.cornell.mannlib.vitro.webapp.utils.FlagMathUtils;

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
        /** Name of entity, tab or vclass */
        public static String NAME       = "name";
        /** rdfs:label unanalyzed */
        public static String NAMEUNANALYZED = "nameunanalyzed" ;
        /** Name of entity, unstemmed */
        public static String NAMEUNSTEMMED       = "nameunstemmed";
        /** portal ( 2 ^ portalId ) */
        public static String PORTAL     = "portal";
        /** Flag 2 (legacy, only used at Cornell) */
        public static String FLAG2 = "FLAG2";
        /** time of index in msec since epoc */
        public static String INDEXEDTIME= "indexedTime";
        /** timekey of entity in yyyymmddhhmm  */
        public static String TIMEKEY="TIMEKEY";
        /** time of sunset/end of entity in yyyymmddhhmm  */
        public static String SUNSET="SUNSET";
        /** time of sunrise/start of entity in yyyymmddhhmm  */
        public static String SUNRISE="SUNRISE";
        /** text for 'full text' search, this is stemmed */
        public static String ALLTEXT    = "ALLTEXT";
        /** text for 'full text' search, this is unstemmed for
         * use with wildcards and prefix queries */
        public static String ALLTEXTUNSTEMMED = "ALLTEXTUNSTEMMED";
        /** keywords */
        public static final String KEYWORDS = "KEYWORDS";
        /** Does the individual have a thumbnail image? 1=yes 0=no */
        public static final String THUMBNAIL = "THUMBNAIL";        
    }

    private static final Log log = LogFactory.getLog(Entity2LuceneDoc.class.getName());

    public static String earliestTime = "16000101";
    public static String latestTime = "30000101";

    /** a way to get to the term names for the lucene index */
    public static VitroLuceneTermNames term = new VitroLuceneTermNames();

    private static String entClassName = Individual.class.getName();

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

        //DocId
        String id = ent.getURI();
        if( id == null ){
        	log.debug("cannot translate bnodes");
            throw new IndexingException("Not indexing bnodes");
        }
        
        doc.add( new Field(term.DOCID, entClassName + id,
                            Field.Store.YES, Field.Index.NOT_ANALYZED));

        //vitro Id        
        doc.add(  new Field(term.URI, id, Field.Store.YES, Field.Index.NOT_ANALYZED));
        log.debug( id );
        
        //java class
        doc.add( new  Field(term.JCLASS, entClassName, Field.Store.YES, Field.Index.NOT_ANALYZED));

        //Entity Name        
        if( ent.getRdfsLabel() != null )
            value=ent.getRdfsLabel();
        else{
            //log.debug("Skipping individual without rdfs:label " + ent.getURI());
            //return null;
            log.debug("Using local name for individual with rdfs:label " + ent.getURI());
            value = ent.getLocalName();
        }
        Field name =new Field(term.NAME, value, 
                               Field.Store.YES, Field.Index.ANALYZED);
        name.setBoost( NAME_BOOST );
        doc.add( name );
        
        Field nameUn = new Field(term.NAMEUNSTEMMED, value, 
        						Field.Store.NO, Field.Index.ANALYZED);        
        nameUn.setBoost( NAME_BOOST );
        doc.add( nameUn );

        Field nameUnanalyzed = new Field(term.NAMEUNANALYZED, value.toLowerCase(), 
				Field.Store.YES, Field.Index.NOT_ANALYZED);        
        doc.add( nameUnanalyzed );

        //boost for entity
        if( ent.getSearchBoost() != null && ent.getSearchBoost() != 0 )
            doc.setBoost(ent.getSearchBoost());

        //rdf:type and ClassGroup
        List<VClass> vclasses = ent.getVClasses(false);
        for( VClass clz : vclasses){
        	log.debug( id + " as type " + clz.getURI() );
        	
            //document boost for given classes
            if( clz.getSearchBoost() != null )
                doc.setBoost( doc.getBoost() + clz.getSearchBoost() );            
            doc.add( new Field(term.RDFTYPE, clz.getURI(), 
                                Field.Store.YES, Field.Index.NOT_ANALYZED));                                               
            //Classgroup URI
            if( clz.getGroupURI() != null )
                doc.add( new Field(term.CLASSGROUP_URI, clz.getGroupURI(), 
                                    Field.Store.YES, Field.Index.NOT_ANALYZED));
        }

        //Modification time
        if( ent.getModTime() != null){
            value = (new DateTime(ent.getModTime().getTime()))
                .toString(LuceneIndexer.MODTIME_DATE_FORMAT) ;
        } else {
            value=  (new DateTime()).toString(LuceneIndexer.MODTIME_DATE_FORMAT) ;
        }
        doc.add(  new Field(term.MODTIME, value , Field.Store.YES, Field.Index.NOT_ANALYZED));

        //do sunrise and sunset. set to 'null' if not found
        // which would indicate that it was sunrised at the beginning of
        // time or sunset at the end of time.
        try{
            value = null;
            if( ent.getSunrise() != null ){
                value = (new DateTime(ent.getSunrise().getTime()))
                    .toString(LuceneIndexer.DATE_FORMAT);
            }
        }catch (Exception ex){
            value = null;
        }

        if( value != null ){
            doc.add( new Field(term.SUNRISE, value, Field.Store.YES, Field.Index.NOT_ANALYZED));
        }else{
            doc.add(new Field(term.SUNRISE, earliestTime, Field.Store.YES, Field.Index.NOT_ANALYZED));
        }

        try{
            value = null;
            if( ent.getSunset() != null ){
                value = (new DateTime(ent.getSunset().getTime()))
                    .toString(LuceneIndexer.DATE_FORMAT);
            }
        }catch (Exception ex){
            value = null;
        }

        if( value != null ){
            doc.add( new Field(term.SUNSET, value, Field.Store.YES, Field.Index.NOT_ANALYZED));
        }else{
            doc.add(new Field(term.SUNSET, latestTime, Field.Store.YES, Field.Index.NOT_ANALYZED));
        }

        try{
            value = null;
            if( ent.getTimekey() != null ){
                value = (new DateTime(ent.getTimekey().getTime())).toString(LuceneIndexer.DATE_FORMAT);
                doc.add(new Field(term.TIMEKEY, value, Field.Store.YES, Field.Index.NOT_ANALYZED));
            }
        }catch(Exception ex){            
            log.error("could not save timekey " + ex);            
        }        
        
        try{
            value = null;
            if( ent.hasThumb() )
                doc.add(new Field(term.THUMBNAIL, "1", Field.Store.YES, Field.Index.NOT_ANALYZED));
            else
                doc.add(new Field(term.THUMBNAIL, "0", Field.Store.YES, Field.Index.NOT_ANALYZED));
        }catch(Exception ex){
            log.debug("could not index thumbnail: " + ex);
        }
        
        //time of index in millis past epoc
        Object anon[] =  { new Long((new DateTime() ).getMillis())  };
        doc.add(  new Field(term.INDEXEDTIME, String.format( "%019d", anon ),
                            Field.Store.YES, Field.Index.NOT_ANALYZED));

        //portal Flags
        doPortalFlags(ent, doc);

        //do flag 2 legacy, only used at Cornell
        //doFlag2( ent, doc );
        
        //ALLTEXT, all of the 'full text'
        String t=null;
        value ="";
        value+= " "+( ((t=ent.getName()) == null)?"":t );
        value+= " "+( ((t=ent.getAnchor()) == null)?"":t);
        value+= " "+ ( ((t=ent.getMoniker()) == null)?"":t );
        value+= " "+ ( ((t=ent.getDescription()) == null)?"":t );
        value+= " "+ ( ((t=ent.getBlurb()) == null)?"":t );
        value+= " "+ getKeyterms(ent);


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
                } catch (Exception e) { }
            }
        }

        //what else? linkAnchors? externalIds?

        //stemmed terms
        doc.add( new  Field(term.ALLTEXT, value , Field.Store.NO, Field.Index.ANALYZED));
        //unstemmed terms
        doc.add( new Field(term.ALLTEXTUNSTEMMED, value, Field.Store.NO, Field.Index.ANALYZED));

        return doc;
    }
       
    /**
     * Flag two is a legacy field that is used only by Cornell.
     * It is related to the old portal filtering.
     */
    private void doFlag2(Individual ent, Document doc) {
        String flag2Set = ent.getFlag2Set();
        if( flag2Set != null && ! "".equals(flag2Set)){
            for( String flag2Value : flag2Set.split(",")){
                if( flag2Value != null ){
                    String value = flag2Value.replace(",", "");
                    if(!value.isEmpty())
                        doc.add( new Field(term.FLAG2, value, Field.Store.NO, Field.Index.ANALYZED));
                }                
            }
        }
    }

    /**
     * Splits up the entity's flag1 value into portal id and then
     * adds the id to the doc.
     *
     * This should work fine with blank portal id and entities with
     * the portal set to NULL.
     *
     * @param ent
     * @param doc
     */
    @SuppressWarnings("static-access")
    private void doPortalFlags(Individual ent, Document doc){
        /* this is the code to add the portal names, we don't use this
         * now but since there is no consistant way to store flags you
         * might want this in the future.
        String portalIdsInCommaSeperatedList = ent.getFlag1Set();

        if(portalIdsInCommaSeperatedList == null) return;
        String[] portalNames = portalIdsInCommaSeperatedList.split(",");
        for( String name : portalNames){
            doc.add( new Field(term.PORTAL,name,Field.Store.NO,Field.Index.NOT_ANALYZED));
        }
        */

        /* this is the code to store portal ids to the lucene index */
        if( ent.getFlag1Numeric() == 0 )
            return;
        Long[] portalIds = FlagMathUtils.numeric2numerics( ent.getFlag1Numeric() );
        if( portalIds == null || portalIds.length == 0)
            return;
                
        log.debug("Flag 1 numeric: " + ent.getFlag1Numeric() + " for " + ent.getURI());

        long id = -1;
        for( Long idLong : portalIds){
            if( idLong != null ){
                id = idLong.longValue();
                String numericPortal = Long.toString(id);
                if( numericPortal != null ){
                    doc.add( new Field(term.PORTAL,numericPortal,                    
                            Field.Store.NO, Field.Index.NOT_ANALYZED));
                    log.debug("adding portal " + numericPortal + " to " + ent.getURI());  
                }
            }
        }                
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
        //List<String> terms = entityWADao.getKeywords(ent.getId());
        List<String> terms = ent.getKeywords();
        String rv = "";
        if( terms != null ){
            for( String term : terms){
                rv += term + " ";
            }
        }
        return rv;
    }

    public static float NAME_BOOST = 10;
    public static float KEYWORD_BOOST = 2;
}
