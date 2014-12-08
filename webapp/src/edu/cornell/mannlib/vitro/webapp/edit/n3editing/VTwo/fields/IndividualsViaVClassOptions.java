/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.modules.tboxreasoner.TBoxReasonerStatus;

public class IndividualsViaVClassOptions implements FieldOptions {

    public static final String LEFT_BLANK = "";
    protected List<String> vclassURIs;    
    protected String defaultOptionLabel;    

    public IndividualsViaVClassOptions(String ... vclassURIs) throws Exception {
        super();
        
        if (vclassURIs==null )
            throw new Exception("vclassURIs must not be null or empty ");
                
        this.vclassURIs = new ArrayList<String>(vclassURIs.length);
        for(int i=0;i<vclassURIs.length;i++){
            if( vclassURIs[i] != null && !vclassURIs[i].trim().isEmpty() )
                this.vclassURIs.add(vclassURIs[i]);
        }                              
    }

    public FieldOptions setDefaultOptionLabel(String label){
        this.defaultOptionLabel = label;
        return this;
    }
    
    @Override
    public Map<String, String> getOptions(
            EditConfigurationVTwo editConfig, 
            String fieldName, 
            WebappDaoFactory wDaoFact) throws Exception {              
        
        Map<String, Individual> individualMap = new HashMap<String, Individual>();

        for( String vclassURI : this.vclassURIs){
            individualMap.putAll(  getIndividualsForClass( vclassURI, wDaoFact) );
        }

        //sort the individuals 
        List<Individual> individuals = new ArrayList<Individual>();
        individuals.addAll(individualMap.values());
        Collections.sort(individuals);

        Map<String, String> optionsMap = new HashMap<String,String>();
        
        if (defaultOptionLabel != null) {
            optionsMap.put(LEFT_BLANK, defaultOptionLabel);
        }

        if (individuals.size() == 0) {                        
            optionsMap.putAll( notFoundMsg() );
        } else {
            for (Individual ind : individuals) {                
                if (ind.getURI() != null) {
                    optionsMap.put(ind.getURI(), ind.getName().trim());
                }
            }
        }
        return optionsMap;
    }
    
    
    private Map<? extends String, ? extends String> notFoundMsg() {
        String msg = "No individuals found for "+ (vclassURIs.size() > 1?"types":"type");
        for( String uri : vclassURIs ){
            msg += " " + uri;
        }
        return Collections.singletonMap("", msg);
    }

    protected Map<String,Individual> getIndividualsForClass(String vclassURI, WebappDaoFactory wDaoFact ){
        Map<String, Individual> individualMap = new HashMap<String, Individual>();
        IndividualDao indDao = wDaoFact.getIndividualDao();
        
        List<Individual> indsForClass= indDao.getIndividualsByVClassURI(vclassURI, -1, -1);                          
        for (Individual ind : indsForClass) {
            if (ind.getURI() != null) {
                individualMap.put(ind.getURI(), ind);
            }
        }
        
        // if reasoning isn't available, we will also need to add
        // individuals asserted in subclasses
        individualMap.putAll( addWhenMissingInference(vclassURI, wDaoFact));        

        return individualMap;        
    }
    
    protected boolean isReasoningAvailable(){
    	TBoxReasonerStatus status = ApplicationUtils.instance().getTBoxReasonerModule().getStatus();
    	return status.isConsistent() && !status.isInErrorState();
    }
    
    protected Map<String, Individual> addWhenMissingInference( String classUri , WebappDaoFactory wDaoFact ){
        boolean inferenceAvailable = isReasoningAvailable();    
        Map<String,Individual> individualMap = new HashMap<String,Individual>();
        if ( !inferenceAvailable ) {
            for (String subclassURI : wDaoFact.getVClassDao().getAllSubClassURIs(classUri)) {
                for (Individual ind : wDaoFact.getIndividualDao().getIndividualsByVClassURI(subclassURI, -1, -1)) {
                    if (ind.getURI() != null) {                       
                        individualMap.put(ind.getURI(), ind);
                    }
                }
            }
        }
        return individualMap;   
    }    
    
    public Comparator<String[]> getCustomComparator() {
    	return null;
    }
}


