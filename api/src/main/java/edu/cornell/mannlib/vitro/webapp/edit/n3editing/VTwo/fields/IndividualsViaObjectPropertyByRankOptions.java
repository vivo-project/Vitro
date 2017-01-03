/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields;

import java.text.Collator;
import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class IndividualsViaObjectPropertyByRankOptions extends IndividualsViaObjectPropetyOptions {
    final static Log log = LogFactory.getLog(IndividualsViaObjectPropertyByRankOptions.class);

    private WebappDaoFactory wdf = null;
    private Model queryModel = null;
    public IndividualsViaObjectPropertyByRankOptions(String subjectUri,
            String predicateUri, String objectUri, WebappDaoFactory wdf, Model model)  throws Exception {
       super(subjectUri, predicateUri, objectUri);
       this.wdf = wdf;
       this.queryModel = model;
    }
    
    public Comparator<String[]> getCustomComparator() {
    	return new DisplayRankComparator(wdf, queryModel);
    }
    
    private static class DisplayRankComparator implements Comparator<String[]> {
    	private WebappDaoFactory wdf = null;
        private Model queryModel = null;

    	public DisplayRankComparator(WebappDaoFactory wdf, Model model) {
    		this.wdf = wdf;
    		this.queryModel = model;
    	}
        public int compare (String[] s1, String[] s2) {
            if (s2 == null) {
                return 1;
            } else if (s1 == null) {
                return -1;
            } else {
            	if ("".equals(s1[0])) {
            		return -1;
            	} else if ("".equals(s2[0])) {
            		return 1;
            	}
                if (s2[1]==null) {
                    return 1;
                } else if (s1[1] == null){
                    return -1;
                } else {
                    return compareRanks(s1, s2); 
                }
            }
        }
        
        private  int compareRanks(String[] s1, String[] s2) {
        	String uri1 = s1[0];
        	String uri2 = s2[0];
        	Individual ind1 = this.wdf.getIndividualDao().getIndividualByURI(uri1);
        	Individual ind2 = this.wdf.getIndividualDao().getIndividualByURI(uri2);
        	int displayRank1 = getDisplayRank(ind1);
        	int displayRank2 = getDisplayRank(ind2);
        	//Get display ranks 
        	return (displayRank1 > displayRank2 ? 1: (displayRank1 == displayRank2? 0: -1));
        	//TODO: Incorporate sparql query here to retrieve the ranks
        	//This qualifies as neither a data property or an object property so will need to access
        	//using sparql query
        }
        
        //Run sparql query to get display rank for individual - uses vitro annotation property
        private Integer getDisplayRank(Individual ind) {
        	Integer rankResult = new Integer(0);
        	String query = getRankQuery();
        	//Set up bindings: individualURI in query should be individual
        	QuerySolutionMap initBindings = new QuerySolutionMap();
            initBindings.add("individualURI", ResourceFactory.createResource(ind.getURI()));
        	//Create query
            Query rankQuery = QueryFactory.create(query);
            this.queryModel.enterCriticalSection(Lock.READ);
            try {
            	QueryExecution qe = QueryExecutionFactory.create(rankQuery, this.queryModel, initBindings);
                ResultSet res = qe.execSelect();
                try {
                	while(res.hasNext()) {
                		QuerySolution qs = res.nextSolution();
                		//Check for rank
                		if(qs.get("rank") != null && qs.get("rank").isLiteral()) {
                			Literal rankLiteral = qs.getLiteral("rank");
                			rankResult = new Integer(rankLiteral.getInt());
                		} else {
                			log.debug("Rank was not returned in query or was not literal");
                		}
                		
                	}
                }finally{ qe.close(); }

            } catch(Exception ex) {
            	log.error("Error occurred in executing query " + query, ex);
            } finally {
            	this.queryModel.leaveCriticalSection();
            }
        	
        	return rankResult;
        }
        
        private String getRankQuery() {
        	return "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> " + 
        			"SELECT ?rank WHERE {?individualURI vitro:displayRankAnnot ?rank .} ";
        }
    }    
}
