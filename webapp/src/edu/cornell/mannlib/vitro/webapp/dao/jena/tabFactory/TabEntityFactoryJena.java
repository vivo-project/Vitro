/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena.tabFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import net.sf.jga.algorithms.Sort;
import net.sf.jga.algorithms.Transform;
import net.sf.jga.algorithms.Unique;
import net.sf.jga.fn.UnaryFunctor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.dao.TabEntityFactory;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDao;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;

public abstract class TabEntityFactoryJena extends JenaBaseDao implements
        TabEntityFactory {

    Tab tab = null;
    ApplicationBean appBean;
    WebappDaoFactory webappDaoFactory;
    String current_str = "";
    int auth_level = 0;
    
    transient final UnaryFunctor<Individual,String> firstLetterOfName = new FirstLetterOfEnt();
    private static final Log log = LogFactory.getLog(TabEntityFactoryJena.class.getName());

    /**
     * @param tab
     * @param auth_level -
     *            int: if <0, don't check entity statusId; otherwise filter to
     *            entities whose statusId<=auth_level
     * @param appBean
     *            is where we check to see if flag2 and flag3 are active.
     */
public TabEntityFactoryJena(Tab tab, int auth_level,
        ApplicationBean appBean, WebappDaoFactoryJena wadf) {
    super(wadf);
        this.tab = tab;

        this.auth_level = auth_level;

        this.appBean = appBean;

        this.webappDaoFactory = wadf;
    }

    public List getLettersOfEnts() {
       return getLettersOfEnts( getRelatedEntites(null));
    }

    public abstract List getRelatedEntites(String alpha);

    public int getRelatedEntityCount() {
        List<Individual> ents = getRelatedEntites(null);
        if( ents != null )
            return ents.size();
        else
            return 0;
    }

    protected String getTabUri(Tab tab){
        return DEFAULT_NAMESPACE+"tab"+tab.getTabId();
    }


    protected com.hp.hpl.jena.ontology.Individual getTabIndividual(Tab tab){
        String taburi = getTabUri(tab);
        com.hp.hpl.jena.ontology.Individual tabInd = getOntModel().getIndividual(taburi);
        if( tabInd == null ){
            log.error("could not find tab: " + taburi);
            return null;
        }
        return tabInd;
    }

    @SuppressWarnings("unchecked")
    public List getLettersOfEnts(List<Individual> ents) {
        Comparator<String> comp = new Comparator<String>(){
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            };
        };

            Iterable<String>i =
                Unique.unique(
                        Sort.sort(
                                Transform.transform( ents ,firstLetterOfName ),
                                comp
                        )
                );

            ArrayList<String> out = new ArrayList<String>(26);
            for( String str : i){
                out.add(str);
            }
            return out;
     }

    @SuppressWarnings("serial")
    class FirstLetterFilter extends UnaryFunctor<Individual,Boolean>{
        String firstLetter;
        public FirstLetterFilter(String alpha){
           firstLetter = alpha;
        }
        @Override
        public Boolean fn(Individual arg){
            if( arg.getName() == null )
                return Boolean.FALSE;
            else
                return new Boolean( firstLetter.equalsIgnoreCase( arg.getName().substring(0,1) ) );
        }
    }

    @SuppressWarnings("serial")
    private class FirstLetterOfEnt extends UnaryFunctor<Individual,String>{
        @Override
        public String fn(Individual arg) {
            return arg.getName().substring(0,1).toUpperCase();
        }
    }
    
    protected void removeDuplicates(List<Individual> ents ){
        Collections.sort(ents, indComp);
        ListIterator<Individual> li = ents.listIterator();
        Individual prev = null;
        while(li.hasNext() ){
            Individual current = li.next();
            if( prev != null && prev.getURI().equals(current.getURI()) )
                li.remove();
            else
                prev = current;
        }
    }
    
    protected static Comparator<Individual> indComp = 
        new Comparator<Individual>(){
            public int compare (Individual a, Individual b){
                if( a == null && b == null ) return 0;
                if( a == null ) return -1;
                if( b == null ) return 1;
                return a.getURI().compareTo(b.getURI());
            }            
    };
}
