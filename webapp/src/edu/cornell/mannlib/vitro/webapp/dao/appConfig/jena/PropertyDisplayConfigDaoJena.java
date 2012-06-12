/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.appConfig.jena;

import static edu.cornell.mannlib.vitro.webapp.dao.appConfig.ApplicationConfiguration.*;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.PropertyDisplayConfigDao;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans.DatatypePropertyDisplayConfig;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans.ObjectPropertyDisplayConfig;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans.PropertyDisplayConfig;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;

public class PropertyDisplayConfigDaoJena extends JenaAppConfigBase implements PropertyDisplayConfigDao{

    public PropertyDisplayConfigDaoJena(OntModel appModel,
            String localAppNamespace) {
        super(appModel, localAppNamespace);
    }

    @Override
    public ObjectPropertyDisplayConfig getObjectPropertyDisplayConfigByURI(
            String URI) {
        return ontIndToObjPropDisplayConfig ( appModel.getIndividual( URI ) );
    }

    @Override
    public DatatypePropertyDisplayConfig getDataPropertyDisplayConfigByURI(
            String URI) {
        return ontIndToDataPropDisplayConfig( appModel.getIndividual( URI));
    }

    @Override
    public void insertNewPropertyDisplayConfig(PropertyDisplayConfig conf)
            throws InsertException {
        edu.cornell.mannlib.vitro.webapp.beans.Individual cdcInd =
            new IndividualImpl();        
        cdcInd.setNamespace( localAppNamespace );

        String type = "propertyDisplayConfig";
        if( conf instanceof DatatypePropertyDisplayConfig )
            type = "datatypePropertyDisplayConfig";
        if( conf instanceof ObjectPropertyDisplayConfig )
            type = "objectPropertyDisplayConfig";
        
        cdcInd.setLocalName( type );
        cdcInd.setVClassURI( ClassDisplayConfig.getURI() );

        WebappDaoFactoryJena wdf=new WebappDaoFactoryJena(appModel);
        try{
            wdf.getIndividualDao().insertNewIndividual( cdcInd );
        }catch(InsertException ex){
            throw new InsertException("Unable to insert ClassDisplayConfig " + cdcInd.getURI(), ex);            
        }finally{ 
            wdf.close(); 
        }
        conf.setURI( cdcInd.getURI() );
        
        JenaAppConfigUtils.updateApplicationConfig(appModel, conf);
        JenaAppConfigUtils.updateDisplayConf(appModel, conf);
        
        updatePropertyDisplayConfig(conf);              
    }

    @Override
    public void updatePropertyDisplayConfig(PropertyDisplayConfig conf) {
        appModel.enterCriticalSection( Lock.WRITE);
        try{
            Individual ind = appModel.getIndividual( conf.getURI());
            
            updateHasListView( ind, conf );
            
            if( conf instanceof DatatypePropertyDisplayConfig)
                updateDatatypePropertyDisplayConfig(ind, (DatatypePropertyDisplayConfig) conf);
            if( conf instanceof ObjectPropertyDisplayConfig)
                updateObjectPropertyDisplayConf( ind, (ObjectPropertyDisplayConfig) conf);         
        }finally{
            appModel.leaveCriticalSection();        
        }
    }

    private void updateHasListView(Individual ind, PropertyDisplayConfig conf) {                
        JenaAppConfigUtils.updateResProp(ind, hasListView, conf.getListViewURIs() );                   
    }

    private void updateObjectPropertyDisplayConf(Individual ind, ObjectPropertyDisplayConfig conf) {        
        JenaAppConfigUtils.updateProp(ind, collateBySubclass, 
                conf.getCollateBySubclassBoolean(), XSDDatatype.XSDboolean);
    }

    private void updateDatatypePropertyDisplayConfig(Individual ind, DatatypePropertyDisplayConfig conf) {
        JenaAppConfigUtils.updateProp(ind, mediaType, 
                conf.getMediaType(), XSDDatatype.XSDstring);
        
    }

    @Override
    public void deletePropertyDisplayConfig(String URI) {
        appModel.enterCriticalSection(Lock.WRITE);
        try{
            super.smartRemove(appModel.getIndividual(URI), appModel);
        }finally{ 
            appModel.leaveCriticalSection();
        }
    }

    @Override
    public void deletePropertyDisplayConfig(PropertyDisplayConfig conf) {
        if( conf != null && conf.getURI() != null )
            deletePropertyDisplayConfig( conf.getURI() );        
    }


    private void ontIndToProperyDisplayConfig( Individual individual, PropertyDisplayConfig pdc){
        JenaAppConfigUtils.ontIndToApplicationConfig(individual, pdc);
        JenaAppConfigUtils.ontIndToDisplayConf(individual, pdc);
        
        pdc.setListViewURIs( getPropertyResourceURIValues(individual, hasListView));
    }
    
    private ObjectPropertyDisplayConfig ontIndToObjPropDisplayConfig(
            Individual individual) {               
        ObjectPropertyDisplayConfig opdc = new ObjectPropertyDisplayConfig();        
        ontIndToProperyDisplayConfig(individual, opdc);        
        
        opdc.setCollateBySubclass( getPropertyBooleanValue(individual, collateBySubclass));        
        return opdc;
    }


    private DatatypePropertyDisplayConfig ontIndToDataPropDisplayConfig(
            Individual individual) {
        
        DatatypePropertyDisplayConfig dpdc = new DatatypePropertyDisplayConfig();
        ontIndToProperyDisplayConfig( individual, dpdc);
        
        dpdc.setMediaType( getPropertyStringValue(individual, mediaType) );        
        return dpdc;
    }

}
