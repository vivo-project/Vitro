/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * simple class that extends vclass to include a list of entites that are
 * members of that class.
 *
 * @author bdc34
 *
 */
public class VClassList extends VClass {

    List <Individual>entities = null;

    public VClassList( VClass vc, List<Individual> ents){
        this.entities = ents;
        this.setURI(vc.getURI());
        this.setNamespace(vc.getNamespace());
        this.setLocalName(vc.getLocalName());
        this.setDisplayLimit(vc.getDisplayLimit() );
        this.setDisplayRank(vc.getDisplayRank() );
        this.setName(vc.getName());
    }
    public List<Individual> getEntities() {
        return entities;
    }

    public void setEntities(List<Individual> entities) {
        this.entities = entities;
    }

    public void sort(){
        Collections.sort(getEntities(), getCompare() );
    }

    public int getSize(){
        if( entities != null )
            return entities.size();
        else
            return 0;
    }
    /**
     * override this if you want a different sorting.
     * @return
     */
    public Comparator<Individual> getCompare(){
        return new Comparator<Individual>(){
            public int compare(Individual o1, Individual o2) {
                if( o1 == null && o2 == null) return 0;
                if( o1 == null ) return 1;
                if( o2 == null ) return -1;

                if( o1.getName() == null && o2.getName() == null) return 0;
                if( o1.getName() == null) return 1;
                if( o2.getName() == null) return -1;
                return o1.getName().compareToIgnoreCase( o2.getName());


            }
        };
    }
}
