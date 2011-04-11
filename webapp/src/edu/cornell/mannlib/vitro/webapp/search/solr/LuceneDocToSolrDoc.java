package edu.cornell.mannlib.vitro.webapp.search.solr;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.search.IndexingException;
import edu.cornell.mannlib.vitro.webapp.search.docbuilder.Obj2DocIface;
import edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc;

/**
 * Translate a lucene Document into a SolrDocument.
 */
public class LuceneDocToSolrDoc implements Obj2DocIface {

    
    @Override
    public boolean canTranslate(Object obj) {        
        return obj != null && obj instanceof Document;
    }

    @Override
    public boolean canUnTranslate(Object result) {
        return result != null && result instanceof SolrDocument; 
    }

    @Override
    public Object getIndexId(Object obj) {        
        //"this method isn't useful for solr"
        return null;
    }

    @Override
    public Object translate(Object obj) throws IndexingException {
        Document luceneDoc = (Document)obj;
        SolrInputDocument solrDoc = new SolrInputDocument();
        
        for( Object f : luceneDoc.getFields()){
            Field field = (Field)f;
            solrDoc.addField( new String(field.name()), field.stringValue()  );
        }
        return solrDoc;
    }

    @Override
    public Object unTranslate(Object result) {
        Individual ind = null;
        if( result != null && result instanceof SolrDocument){
            SolrDocument hit = (SolrDocument)result;
            String id = (String) hit.getFieldValue(Entity2LuceneDoc.term.URI);
            ind = new IndividualImpl();
            ind.setURI(id);            
        }
        return ind;
    }

}
