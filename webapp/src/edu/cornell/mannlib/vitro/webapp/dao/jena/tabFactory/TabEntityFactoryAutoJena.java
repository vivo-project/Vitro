/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena.tabFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import net.sf.jga.algorithms.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.dao.TabEntityFactory;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.IndividualJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;

public class TabEntityFactoryAutoJena extends TabEntityFactoryJena implements TabEntityFactory {
    List<Individual> _relatedEnts;
    private static final Log log = LogFactory.getLog(TabEntityFactoryAutoJena.class.getName());
    
    public TabEntityFactoryAutoJena(Tab tab, int auth_level, ApplicationBean appBean, WebappDaoFactoryJena wadf) {
        super(tab, auth_level, appBean, wadf);
    }

    @SuppressWarnings("unchecked")
    public List getRelatedEntites(String alpha) {
        if( _relatedEnts == null )
            _relatedEnts =  getAllRelatedEnts();

        if( alpha == null || "all".equals(alpha) )
            return _relatedEnts;

        List<Individual> out = new LinkedList<Individual>();
        Filter.filter(_relatedEnts, new FirstLetterFilter( alpha ), out);
        return out;
    }

    private List getAllRelatedEnts(){
        LinkedList<Individual> ents = new LinkedList<Individual>();
        getOntModel().enterCriticalSection(Lock.READ);
        try{
            com.hp.hpl.jena.ontology.Individual tabInd = getTabIndividual(tab);
            if( tabInd == null ) return Collections.EMPTY_LIST;

            if( TAB_AUTOLINKEDTOTAB == null ){
                log.error("could not find annotation property " + VitroVocabulary.TAB_AUTOLINKEDTOTAB);
                return Collections.EMPTY_LIST;
            }

            //get the classes that are linked to this tab
            ClosableIterator classIt = getOntModel().listStatements(null, TAB_AUTOLINKEDTOTAB, tabInd);

            try{
                while(classIt.hasNext()){
                    Statement linkedToTab = (Statement)classIt.next();
                    OntClass linkedClass = getOntModel().getOntClass( linkedToTab.getSubject().getURI() );

                    ClosableIterator entIt = getOntModel().listStatements(null, RDF.type, linkedClass);
                    try{
                        while(entIt.hasNext()){
                            Statement entIsOfClass = (Statement)entIt.next();
                            if( entIsOfClass.getSubject().canAs(com.hp.hpl.jena.ontology.Individual.class) ) {
                            	com.hp.hpl.jena.ontology.Individual ind = (com.hp.hpl.jena.ontology.Individual) entIsOfClass.getSubject().as(com.hp.hpl.jena.ontology.Individual.class);
                                Individual ent = new IndividualJena(ind, (WebappDaoFactoryJena)webappDaoFactory);
                                ents.add(ent);
                            }
                        }
                    }finally{
                        entIt.close();
                    }
                }
            }finally{
                classIt.close();
            }
        }finally{
            getOntModel().leaveCriticalSection();
        }

        removeDuplicates( ents );        
        return ents;
    }
}
