/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;

public class IndividualsViaObjectPropetyOptions implements FieldOptions {
    private static final String LEFT_BLANK = "";
    private String subjectUri;
    private String predicateUri;    
    private String objectUri;
    
    private String defaultOptionLabel;
    
    public IndividualsViaObjectPropetyOptions(String subjectUri,
            String predicateUri, String objectUri) throws Exception {
        super();
        
        if (subjectUri == null || subjectUri.equals("")) {
            throw new Exception("no subjectUri found for field ");
        }
        if (predicateUri == null || predicateUri.equals("")) {
            throw new Exception("no predicateUri found for field ");                    
        } 

        this.subjectUri = subjectUri;
        this.predicateUri = predicateUri;
        this.objectUri = objectUri;
    }

    public IndividualsViaObjectPropetyOptions setDefaultOptionLabel(String label){
        this.defaultOptionLabel = label;
        return this;
    }
    
    @Override
    public Map<String, String> getOptions(
            EditConfigurationVTwo editConfig, 
            String fieldName, 
            WebappDaoFactory wDaoFact) {
        HashMap<String, String> optionsMap = new LinkedHashMap<String, String>();
        int optionsCount = 0;
 
        // first test to see whether there's a default "leave blank"
        // value specified with the literal options                
        if ((defaultOptionLabel ) != null) {
            optionsMap.put(LEFT_BLANK, defaultOptionLabel);
        }
        
        Individual subject = wDaoFact.getIndividualDao().getIndividualByURI(subjectUri);                    
        ObjectProperty objProp = wDaoFact.getObjectPropertyDao().getObjectPropertyByURI(predicateUri);
        List<VClass> vclasses = wDaoFact.getVClassDao().getVClassesForProperty(subject.getVClassURI(), predicateUri);
                
        if (vclasses == null || vclasses.size() == 0) {           
            return optionsMap;
        }
        
        List<Individual> individuals = new ArrayList<Individual>();
        HashSet<String> uriSet = new HashSet<String>();        
        for (VClass vclass : vclasses) {
            List<Individual> inds = wDaoFact.getIndividualDao().getIndividualsByVClassURI(vclass.getURI(), -1, -1);
            for (Individual ind : inds) {
                if (!uriSet.contains(ind.getURI())) {
                    uriSet.add(ind.getURI());
                    individuals.add(ind);
                }
            }
        }

        List<ObjectPropertyStatement> stmts = subject.getObjectPropertyStatements();

        individuals = removeIndividualsAlreadyInRange(
                individuals, stmts, predicateUri, objectUri);
        // Collections.sort(individuals,new compareIndividualsByName());

        for (Individual ind : individuals) {
            String uri = ind.getURI();
            if (uri != null) {
                optionsMap.put(uri, ind.getName().trim());
                ++optionsCount;
            }
        }

        return optionsMap;
    }

    
    // copied from OptionsForPropertyTag.java in the thought that class may be deprecated
    private static List<Individual> removeIndividualsAlreadyInRange(List<Individual> individuals,
            List<ObjectPropertyStatement> stmts, String predicateUri, String objectUriBeingEdited){        
        HashSet<String>  range = new HashSet<String>();

        for(ObjectPropertyStatement ops : stmts){
            if( ops.getPropertyURI().equals(predicateUri))
                range.add( ops.getObjectURI() );
        }

        int removeCount=0;
        ListIterator<Individual> it = individuals.listIterator();
        while(it.hasNext()){
            Individual ind = it.next();
            if( range.contains( ind.getURI()) && !(ind.getURI().equals(objectUriBeingEdited)) ) {
                it.remove();
                ++removeCount;
            }
        }
        
        return individuals;
    }
    
    public Comparator<String[]> getCustomComparator() {
    	return null;
    }
    
    
}
