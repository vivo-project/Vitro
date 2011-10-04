/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.jga.algorithms.Filter;
import net.sf.jga.algorithms.Transform;
import net.sf.jga.fn.UnaryFunctor;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;
import edu.cornell.mannlib.vitro.webapp.edit.EditLiteral;


class IndividualDaoFiltering extends BaseFiltering implements IndividualDao{
    IndividualDao innerIndividualDao;
    VitroFilters filters;

    public IndividualDaoFiltering(IndividualDao individualDao,
            VitroFilters filters2) {
        this.innerIndividualDao = individualDao;
        this.filters = filters2;
    }


    protected List<Individual> filterAndWrap(Collection<Individual> cin, VitroFilters filters){
        if( cin == null || cin.size() == 0) return Collections.EMPTY_LIST;

        ArrayList<Individual>  filteredList = new ArrayList<Individual>();
        Filter.filter(cin,filters.getIndividualFilter(),filteredList);
        
        ArrayList<Individual> cout  = new ArrayList<Individual>();
        for(Individual ind : filteredList){
            cout.add( new IndividualFiltering(ind,filters));
        }        
        return cout;
    }

    protected Iterator<Individual> filterAndWrap(Iterator<Individual> it, VitroFilters filters){
        if( it == null ) return null;
        if( filters == null ) return it;
        return Transform.transform(    
                Filter.filter(it,filters.getIndividualFilter()),
                new ToFilteredIndividual(filters));        
    }
    
    /* **************** methods that filter ****************** */
    public Individual getIndividualByURI(String individualURI) {
        Individual ind = innerIndividualDao.getIndividualByURI(individualURI);
        if( ind != null && filters.getIndividualFilter().fn(ind))           
            return new IndividualFiltering(ind, filters);
        else
            return null;        
    }
    
    public void fillVClassForIndividual(Individual individual) {
        innerIndividualDao.fillVClassForIndividual(individual);
    }

    public List<Individual> getIndividualsByDataProperty(String dataPropertyUri, String value) {
        return filterAndWrap(innerIndividualDao.getIndividualsByDataProperty(dataPropertyUri,value),
                     filters);
    }

    public List<Individual> getIndividualsByDataProperty(
            String dataPropertyUri, String value, String datatypeUri,
            String lang) {
        return filterAndWrap(innerIndividualDao.getIndividualsByDataProperty(dataPropertyUri,value,datatypeUri,lang),
                filters);
    }
        
    public List getIndividualsByVClass(VClass vclass) {
        List<Individual> list = innerIndividualDao.getIndividualsByVClass(vclass);
        if( list == null )
            return Collections.EMPTY_LIST;
        else
            return filterAndWrap(list, filters);
    }
    
    public List getIndividualsByVClassURI(String vclassURI) {    
        List<Individual> list = innerIndividualDao.getIndividualsByVClassURI(vclassURI);
        if( list == null ) 
            return Collections.EMPTY_LIST;
        else
            return filterAndWrap(list,filters);
    }
    
    public List getIndividualsByVClassURI(String vclassURI, int offset, int quantity) {    
        List<Individual> list = innerIndividualDao.getIndividualsByVClassURI(vclassURI,offset,quantity);
        if( list == null ) 
            return Collections.EMPTY_LIST;
        else
            return filterAndWrap(list,filters);
    }  
    
    /* All of the methods that return iterator don't wrap the Individual in
     * a IndividualFiltering so they might cause problems */
    

    
    private class ToFilteredIndividual extends UnaryFunctor<Individual, Individual>{
        private final VitroFilters filters;
        public ToFilteredIndividual(VitroFilters vf){
            this.filters = vf;
        }
        @Override
        public Individual fn(Individual arg) {
            return new IndividualFiltering(arg,filters);
        }        
    }              

    
    /* ******************* unfiltered methods ****************** */
    
    public Iterator<String> getAllOfThisTypeIterator() {        
        return innerIndividualDao.getAllOfThisTypeIterator(); 
                       
    }
    
    public Iterator<String> getUpdatedSinceIterator(long updatedSince) {
        return  innerIndividualDao.getUpdatedSinceIterator(updatedSince);
                       
    }
    
    public Collection<DataPropertyStatement> getExternalIds(String individualURI) {
        return innerIndividualDao.getExternalIds(individualURI);
    }

    public Collection<DataPropertyStatement> getExternalIds(String individualURI, String dataPropertyURI) {
        return innerIndividualDao.getExternalIds(individualURI, dataPropertyURI);
    }

    public int deleteIndividual(String individualURI) {
        return innerIndividualDao.deleteIndividual(individualURI);
    }


    public int deleteIndividual(Individual individual) {
        return innerIndividualDao.deleteIndividual(individual);
    }

    public int getCountOfIndividualsInVClass(int vclassId) {
        throw new Error("IndividualDaoFiltering.getCountOfIndividualsInVClass is not supported");
    }
    
    public void addVClass(String individualURI, String vclassURI) {
        innerIndividualDao.addVClass(individualURI, vclassURI);
    }
    
    public void removeVClass(String individualURI, String vclassURI) {
        innerIndividualDao.removeVClass(individualURI, vclassURI);
    }

    public String insertNewIndividual(Individual individual) throws InsertException {
        return innerIndividualDao.insertNewIndividual(individual);
    }

    public int updateIndividual(Individual individual) {
        return innerIndividualDao.updateIndividual(individual);
    }

    public void markModified(Individual individual) {
        innerIndividualDao.markModified(individual);
    }

    public String toString(){
        return "IndividualDaoFiltering filter: " + filters.getIndividualFilter().toString() + "\n" +
                "InnerDao: " + innerIndividualDao.toString();
    }

    public boolean isIndividualOfClass(String vclassURI, String indURI) {
        return innerIndividualDao.isIndividualOfClass(vclassURI, indURI);
    }

	public String getUnusedURI(Individual individual) throws InsertException {
		return innerIndividualDao.getUnusedURI(individual);
	}

    @Override
    public EditLiteral getLabelEditLiteral(String individualUri) {
        return innerIndividualDao.getLabelEditLiteral(individualUri);
    }

}