/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena.tabFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.sf.jga.algorithms.Filter;
import net.sf.jga.fn.UnaryFunctor;

import org.joda.time.DateTime;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.dao.TabEntityFactory;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;

/**
 * A TabEntityFactoryGallery does the same thing as any
 * TabEntityFactory it just needs to return entities that have
 * non-null thumbnails. They also need to return a random selection
 * of entities when doing a non-alpha filtered request.
 *
 * @author bdc34
 *
 */
public class TabEntityFactoryGalleryJena extends TabEntityFactoryJena
        implements TabEntityFactory {

    private TabEntityFactory _innerFactory = null;
    public final UnaryFunctor<Individual,Boolean>onlyWithThumbs = new OnlyWithThumbs();

    public TabEntityFactoryGalleryJena(TabEntityFactory innerEntFactory, Tab tab, int auth_level, ApplicationBean appBean, WebappDaoFactoryJena wadf) {
        super(tab, auth_level, appBean, wadf);
        this._innerFactory = innerEntFactory;
    }

    public List getRelatedEntites(String alpha){
        if( alpha != null )
            return doAlphaFilteredGallery(alpha);
        else
            return doGallery();
    }

    @Override
    public int getRelatedEntityCount() {
        return _innerFactory.getRelatedEntityCount();
    }

    @Override
    public List getLettersOfEnts() {
       return _innerFactory.getLettersOfEnts();
    }

    private List <Individual> doAlphaFilteredGallery(String alpha){
        List ents = _innerFactory.getRelatedEntites(alpha);
        if( ents == null )
            return Collections.EMPTY_LIST;
        return ents;
    }

    /**
     * Return a random selection of ents with non-null thumbnail images
     * of the size specified by the tab.
     * @return
     */
    private List <Individual> doGallery(){
        int numberOfrequestedEnts = tab.getGalleryCols() * tab.getGalleryRows() ;
        List ents = _innerFactory.getRelatedEntites(null);
        if( ents == null )
            return Collections.EMPTY_LIST;

        List filteredEnts = new LinkedList( );
        Filter.filter(ents,onlyWithThumbs,filteredEnts);

        if( filteredEnts.size() <= numberOfrequestedEnts)
            return filteredEnts;

        Random r = new Random( (new DateTime()).getMillis() );
        List entsOut = new ArrayList( numberOfrequestedEnts );
        while( entsOut.size() < numberOfrequestedEnts && filteredEnts.size() > 0){
            int randIndex =Math.abs(r.nextInt()) % filteredEnts.size() ;
            entsOut.add( filteredEnts.get(randIndex) );
            filteredEnts.remove(randIndex);
        }
        return entsOut;
    }

    private class OnlyWithThumbs extends UnaryFunctor<Individual,Boolean>{
        @Override
        public Boolean fn(Individual arg) {
            return  arg.getImageThumb() != null;
        }
    }
}

