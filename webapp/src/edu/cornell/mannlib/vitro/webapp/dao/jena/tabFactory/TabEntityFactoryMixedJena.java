/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena.tabFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.dao.TabEntityFactory;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;

public class TabEntityFactoryMixedJena extends TabEntityFactoryJena implements
        TabEntityFactory {
    TabEntityFactory autoPart;
    TabEntityFactory manualPart;

    public TabEntityFactoryMixedJena(Tab tab, int auth_level, ApplicationBean appBean, WebappDaoFactoryJena wadf) {
        super(tab, auth_level, appBean, wadf);
        autoPart = new TabEntityFactoryAutoJena(tab, auth_level, appBean, wadf);
        manualPart = new TabEntityFactoryManualJena(tab, auth_level, appBean, wadf);
    }

    @SuppressWarnings("unchecked")
    public List getRelatedEntites(String alpha) {
        List relEntsList = manualPart.getRelatedEntites(alpha);
        relEntsList.addAll(autoPart.getRelatedEntites(alpha));
        
        Collections.sort(relEntsList, comp);
        removeDuplicates(relEntsList);                

        return relEntsList;
    }
    
    private static Comparator comp = new Comparator() {
        public int compare(Object obj1, Object obj2){
            Individual first = (Individual) obj1;
            Individual second = (Individual) obj2;
            return (first.getName().compareTo(second.getName()));
        }
    };

}
