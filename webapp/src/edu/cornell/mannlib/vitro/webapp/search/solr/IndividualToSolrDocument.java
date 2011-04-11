package edu.cornell.mannlib.vitro.webapp.search.solr;

import org.apache.solr.common.SolrDocument;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.search.IndexingException;
import edu.cornell.mannlib.vitro.webapp.search.docbuilder.Obj2DocIface;
import edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc;

public class IndividualToSolrDocument implements Obj2DocIface {
    
    protected LuceneDocToSolrDoc luceneToSolr;
    protected Entity2LuceneDoc entityToLucene;
    
    public IndividualToSolrDocument(Entity2LuceneDoc e2d){
        entityToLucene = e2d;  
        luceneToSolr = new LuceneDocToSolrDoc();
    }
    
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

    @Override
    public Object translate(Object obj) throws IndexingException {
        return luceneToSolr.translate( entityToLucene.translate( obj ) );
    }

    @Override
    public Object unTranslate(Object result) {
        return luceneToSolr.unTranslate( result ); 
    }

}
