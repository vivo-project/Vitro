/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena.tabFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.sf.jga.algorithms.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.dao.TabEntityFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.IndividualJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;

public class TabEntityFactoryManualJena extends TabEntityFactoryJena implements TabEntityFactory {
    List<Individual> _allents = null;
    
    private static final Log log = LogFactory.getLog(TabEntityFactoryManualJena.class.getName());

    public TabEntityFactoryManualJena(Tab tab, int auth_level, ApplicationBean appBean, WebappDaoFactoryJena wadf) {
        super(tab, auth_level, appBean, wadf);
    }

    public List getRelatedEntites(String alpha) {
        com.hp.hpl.jena.ontology.Individual tabInd = getTabIndividual(tab);
        if( tabInd == null ) return Collections.EMPTY_LIST;

        if( _allents == null )
            _allents = getAllRelatedEnts();

        if( alpha == null || "all".equals(alpha) )
            return _allents;

        List<Individual> out = new LinkedList<Individual>();
        Filter.filter(_allents, new FirstLetterFilter( alpha ), out);
        return out;
    }

    private List<Individual> getAllRelatedEnts(){
    	getOntModel().enterCriticalSection(Lock.READ);
    	try {
	        com.hp.hpl.jena.ontology.Individual tabInd = getTabIndividual(tab);
	        if( tabInd == null ) return Collections.emptyList();
	
	        List<Individual> ents = new LinkedList<Individual>();
	        if( TAB_INDIVIDUALRELATION == null )
	            log.debug(" TAB_INDIVIDUALRELATION is " + TAB_INDIVIDUALRELATION );
	        if( TAB_INDIVIDUALRELATION_INVOLVESTAB == null )
	            log.debug(" TAB_INDIVIDUALRELATION_INVOLVESTAB is " + TAB_INDIVIDUALRELATION_INVOLVESTAB );
	        if( TAB_INDIVIDUALRELATION_INVOLVESINDIVIDUAL == null )
	            log.debug(" TAB_INDIVIDUALRELATION_INVOLVESINDIVIDUAL is " + TAB_INDIVIDUALRELATION_INVOLVESINDIVIDUAL );
	
	        ClosableIterator stmtIt = getOntModel().listStatements(null , TAB_INDIVIDUALRELATION_INVOLVESTAB, tabInd);
	        try{
	            while(stmtIt.hasNext() ){
	                Statement relationStmt = (Statement)stmtIt.next();
	                Resource relation =  relationStmt.getSubject();
	                ClosableIterator manualLinkStmtIt = getOntModel().listStatements(relation, TAB_INDIVIDUALRELATION_INVOLVESINDIVIDUAL, (Resource)null);
	                try{
	                    while(manualLinkStmtIt.hasNext()){
	                        Statement stmt = (Statement)manualLinkStmtIt.next();
	                        if( stmt.getObject().canAs(com.hp.hpl.jena.ontology.Individual.class) ) {
	                        	com.hp.hpl.jena.ontology.Individual ind = (com.hp.hpl.jena.ontology.Individual) stmt.getObject().as(com.hp.hpl.jena.ontology.Individual.class);                        
	                            Individual relatedInd = new IndividualJena(ind, (WebappDaoFactoryJena)webappDaoFactory);
	                            ents.add(relatedInd);
	                        }
	                    }
	                }finally{
	                    manualLinkStmtIt.close();
	                }
	            }
	        }finally{
	            stmtIt.close();
	        }
	        removeDuplicates(ents);
	        return ents;
    	} finally {
    		getOntModel().leaveCriticalSection();
    	}
    }
}
