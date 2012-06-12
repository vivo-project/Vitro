/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.appConfig.jena;

import static edu.cornell.mannlib.vitro.webapp.dao.appConfig.ApplicationConfiguration.*;

import java.util.Collection;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans.ApplicationConfig;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans.DisplayConfig;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans.EditConfig;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDaoUtils;

public class JenaAppConfigUtils extends JenaBaseDaoUtils {

    protected static void updateProp( Individual ind, Property prop, Object value, XSDDatatype datatype){
        boolean hasReplacementValue = false;
        if( datatype == XSDDatatype.XSDstring ){
            hasReplacementValue = 
                value != null && value.toString().trim().length() > 0;
        }else{
            hasReplacementValue = value != null;
        }
        
        try{
            ind.removeAll( prop);
            if( hasReplacementValue ){
                ind.addProperty( prop, datatype.unparse(value), datatype);
            }            
        }catch(Exception e){
            log.error("Exception updating <" + prop.getURI() + "> for indivdiual <" + ind.getURI() + ">", e);
        }
    }
        
    
    
    protected static void updateResProp(Individual ind, ObjectProperty prop,
            Collection<String> uris) {
        try{
            ind.removeAll( prop );
            if( uris!= null ){
                for( String uri: uris){
                    if( uri != null && uri.trim().length() > 0 ){
                        ind.addProperty(prop,  ind.getModel().getResource(uri) );
                    }
                }
            }
        }catch(Exception e){
            log.error("Error updating object property <" + prop.getURI() + "> for indivdiual <" + ind.getURI() + ">", e);
        }               
    }
    
    /**
     * Sets properties on appConf from ind. ind Must be from the model that
     * you want this data written to.
     */
    public static void ontIndToApplicationConfig( Individual ind, ApplicationConfig appConf ){
        
        ind.getModel().enterCriticalSection(Lock.READ);
        try{
            appConf.setURI( ind.getURI());
            appConf.setPublicDescription( getPropertyStringValue(ind, publicDescription));
            appConf.setInheritingConfigurationFor( getPropertyResourceURIValues(ind, inheritingConfigurationFor));
            appConf.setNoninheritingConfigurationFor( getPropertyResourceURIValues(ind, nonInheritingConfigurationFor));
        }finally{
            ind.getModel().leaveCriticalSection();
        }
    }
    
    /**
     * update ApplicationConfig properties to model.  There must be an Individual in appModel for 
     * the appConf already.
     */
    public static void updateApplicationConfig( OntModel appModel, ApplicationConfig appConf){
        updateApplicationConfig(appConf, appModel.getIndividual(appConf.getURI()));
    }
    
    /**
     * update ApplicationConfig properties to model.  There must be an Individual in appModel for 
     * the appConf already.
     */
    public static void updateApplicationConfig(  ApplicationConfig appConf, Individual appInd){
        appInd.getModel().enterCriticalSection(Lock.WRITE);
        try {
            updateProp(appInd, publicDescription, appConf.getPublicDescription(), 
                    XSDDatatype.XSDstring);            
            updateResProp( appInd, inheritingConfigurationFor, appConf.getInheritingConfigurationFor());
            updateResProp( appInd, nonInheritingConfigurationFor, appConf.getNoninheritingConfigurationFor());                        
        }finally{
            appInd.getModel().leaveCriticalSection();
        }
    }
        


    public static void ontIndToDisplayConf(Individual ind, DisplayConfig conf){
        ind.getModel().enterCriticalSection(Lock.READ);
        try{
            conf.setDisplayName( getPropertyStringValue(ind, displayName));
            conf.setDisplayRank( getPropertyNonNegativeIntegerValue(ind, displayRank));
            conf.setDisplayRank( getPropertyNonNegativeIntegerValue(ind, displayRank));
            conf.setDisplayLimit( getPropertyNonNegativeIntegerValue(ind, displayLimit));
            conf.setSupressDisplay( getPropertyBooleanValue( ind, suppressDisplay ));
        }finally{
            ind.getModel().leaveCriticalSection();
        }
        
    }
    
    /**
     * update DisplayConfig properties to model.  There must be an Individual in appModel for 
     * the displayConf already.
     * This method does not lock the model, locking needs to be taken care of by calling method.
     */
    public static void updateDisplayConf( OntModel appModel, DisplayConfig displayConf){
        //consider optimizing this
        updateDisplayConf( displayConf, appModel.getIndividual(displayConf.getURI()));
    }
    
    /**
     * update DisplayConfig properties to model.  Ind must already be in
     * and associated with a OntModel.
     */
    public static void updateDisplayConf(  DisplayConfig displayConf, Individual ind){
        ind.getModel().enterCriticalSection(Lock.WRITE);
        try{
            updateProp(ind, displayRank, 
                    displayConf.getDisplayRankInteger(), XSDDatatype.XSDint);
            updateProp(ind, displayLimit, 
                    displayConf.getDisplayLimitInteger(), XSDDatatype.XSDint);            
            updateProp(ind, displayName, 
                    displayConf.getDisplayName(), XSDDatatype.XSDstring);                   
            updateProp(ind, suppressDisplay, 
                    displayConf.getSupressDisplayBoolean(), XSDDatatype.XSDboolean);
        }finally{ 
            ind.getModel().leaveCriticalSection(); 
        }
    }
    
    
    
    public static void ontIndToEditConf(Individual ind, EditConfig conf){
        conf.setEntryLimit( getPropertyNonNegativeIntegerValue( ind, entryLimit));
    }
    
    public static void updateEditConf( OntModel appModel, EditConfig conf){
        updateEditConf(conf, appModel.getIndividual(conf.getURI()));        
    }
    
    public static void updateEditConf( EditConfig conf, Individual ind){
        ind.getModel().enterCriticalSection(Lock.WRITE);
        try{
            updateProp(ind, entryLimit, conf.getEntryLimitInteger(), XSDDatatype.XSDint);
        }finally{ 
            ind.getModel().leaveCriticalSection(); 
        }
    }
    
}
