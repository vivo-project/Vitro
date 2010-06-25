/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.lucene;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.joda.time.DateTime;
import org.openrdf.model.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
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
        /** Name of entity, unstemmed */
        public static String NAMEUNSTEMMED       = "nameunstemmed";
        /** Name of portal */
        public static String PORTAL     = "portal";
        /** time of index in msec since epoc */
        public static String INDEXEDTIME= "indexedTime";
        /** time of sunset/end of entity */
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
        if( id == null )
            throw new IndexingException("Not indexing bnodes");
        
        doc.add( new Field(term.DOCID, entClassName + id,
                            Field.Store.YES, Field.Index.NOT_ANALYZED));

        //vitro Id        
        doc.add(  new Field(term.URI, id, Field.Store.YES, Field.Index.NOT_ANALYZED));

        //java class
        doc.add( new  Field(term.JCLASS, entClassName, Field.Store.YES, Field.Index.NOT_ANALYZED));

        //Entity Name
        if( ent.getName() != null )
            value=ent.getName();
        else
            value="";
        Field name =new Field(term.NAME, value, 
                               Field.Store.YES, Field.Index.ANALYZED);
        name.setBoost( NAME_BOOST );
        doc.add( name );
        
        Field nameUn = new Field(term.NAMEUNSTEMMED, value, 
        						Field.Store.YES, Field.Index.ANALYZED);        
        nameUn.setBoost( NAME_BOOST );
        doc.add( nameUn );

        //boost for entity
        if( ent.getSearchBoost() != null && ent.getSearchBoost() != 0 )
            doc.setBoost(ent.getSearchBoost());

        //rdf:type and ClassGroup
        List<VClass> vclasses = ent.getVClasses(false);
        for( VClass clz : vclasses){
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

        //Keywords
        for( String word : ent.getKeywords()){
            if( word != null ){
                Field kwf = new Field(term.KEYWORDS, word, 
                                    Field.Store.YES, Field.Index.NOT_ANALYZED);
                kwf.setBoost( KEYWORD_BOOST );
                doc.add( kwf );
            }
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

        //time of index in millis past epoc
        Object anon[] =  { new Long((new DateTime() ).getMillis())  };
        doc.add(  new Field(term.INDEXEDTIME, String.format( "%019d", anon ),
                            Field.Store.YES, Field.Index.NOT_ANALYZED));

        //portal Flags
        doPortalFlags(ent, doc);


        //ALLTEXT, all of the 'full text'
        String t=null;
        value ="";
        value+= " "+( ((t=ent.getName()) == null)?"":t );
        value+= " "+( ((t=ent.getAnchor()) == null)?"":t);
        value+= " "+ ( ((t=ent.getMoniker()) == null)?"":t );
        value+= " "+ ( ((t=ent.getDescription()) == null)?"":t );
        value+= " "+ ( ((t=ent.getBlurb()) == null)?"":t );
        value+= " "+ ( ((t=ent.getCitation()) == null)?"":t );
        value+= " "+ getKeyterms(ent);


        List dataPropertyStatements = ent.getDataPropertyStatements();
        if (dataPropertyStatements != null) {
            Iterator dataPropertyStmtIter = dataPropertyStatements.iterator();
            while (dataPropertyStmtIter.hasNext()) {
                DataPropertyStatement dataPropertyStmt = (DataPropertyStatement) dataPropertyStmtIter.next();
                value+= " "+ ( ((t=dataPropertyStmt.getData()) == null)?"":t );
            }
        }

        List objectPropertyStatements = ent.getObjectPropertyStatements();
        if (objectPropertyStatements != null) {
            Iterator objectPropertyStmtIter = objectPropertyStatements.iterator();
            while (objectPropertyStmtIter.hasNext()) {
                ObjectPropertyStatement objectPropertyStmt = (ObjectPropertyStatement) objectPropertyStmtIter.next();
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

//      System.out.print('\n'+"numeric: " + ent.getFlag1Numeric()
//              + " " + Arrays.toString(portalIds) +" = ");
//
        long id = -1;
        for( Long idLong : portalIds){
            id = idLong.longValue();
            String numericPortal = Long.toString(id);
            doc.add( new Field(term.PORTAL,numericPortal,
                    Field.Store.NO, Field.Index.NOT_ANALYZED));
//          System.out.print(numericPortal+" ");
        }/* end of portal id code */
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
