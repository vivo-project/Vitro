/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.appConfig.jena;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import static edu.cornell.mannlib.vitro.webapp.dao.appConfig.ApplicationConfiguration.*;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.ClassDisplayConfigDao;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans.ClassDisplayConfig;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;

public class ClassDisplayConfigDaoJena extends JenaAppConfigBase implements ClassDisplayConfigDao {        
         
    public ClassDisplayConfigDaoJena(OntModel appModel, String localAppNamespace) {
        super(appModel, localAppNamespace);
    }

    protected ClassDisplayConfig ontIndToClassDisplayConfig ( Individual ind ){
        if( ind == null )
            return null;
        ClassDisplayConfig cdc = new ClassDisplayConfig();
        cdc.setURI( ind.getURI() );
        appModel.enterCriticalSection(Lock.READ);
        try{            
            JenaAppConfigUtils.ontIndToApplicationConfig(ind, cdc);
            JenaAppConfigUtils.ontIndToDisplayConf(ind, cdc);

            //TODO: implement short views
            //cdc.setShortView( getPropertyResourceURIValues(ind, ApplicationConfiguration.SHORT_DISPLAY_VIEW));                        
        }finally{
            appModel.leaveCriticalSection();
        }
        return cdc;        
    }    

    @Override
    public ClassDisplayConfig getClassDisplayConfigByURI(String URI) {
        return ontIndToClassDisplayConfig( appModel.getIndividual(URI) );        
    }

    @Override
    public void insertNewClassDisplayConfig(ClassDisplayConfig cdc)
            throws InsertException {
        
        edu.cornell.mannlib.vitro.webapp.beans.Individual cdcInd =
            new IndividualImpl();        
        cdcInd.setNamespace( localAppNamespace );
        cdcInd.setLocalName( "classDiaplyconfig" );
        cdcInd.setVClassURI( ClassDisplayConfig.getURI() );

        WebappDaoFactoryJena wdf=new WebappDaoFactoryJena(appModel);
        try{
            wdf.getIndividualDao().insertNewIndividual( cdcInd );
        }catch(InsertException ex){
            throw new InsertException("Unable to insert ClassDisplayConfig " + cdcInd.getURI(), ex);            
        }finally{ 
            wdf.close(); 
        }
        
        if( cdcInd.getURI() != null ){
            cdc.setURI( cdcInd.getURI() );
            appModel.enterCriticalSection(Lock.WRITE);
            try{
                JenaAppConfigUtils.updateApplicationConfig(appModel, cdc);
                JenaAppConfigUtils.updateDisplayConf(appModel, cdc);
                                
                //TODO:save shortview
//                if( cdc.getShortView() != null ){
//                    if( cdc.getShortView().getURI() != null ){
//                        ind.addProperty(ApplicationConfiguration.HAS_SHORT_VIEW , 
//                                appModel.createIndividual(
//                                        cdc.getShortView().getURI(), ApplicationConfiguration.SHORT_DISPLAY_VIEW));
//
//                        //update short view or something?
//                    }else{
//                        
//                    }
            }catch( Exception ex ){
                log.error("could not save ClassDisplayConfig");                             
            }finally{
                appModel.leaveCriticalSection();
            }
        }else{
            log.error("could not insert new ClassDisplayConfig in to app model");
        }
    }   

    @Override
    public void deleteClassDisplayConfig(String URI) {
        appModel.enterCriticalSection(Lock.WRITE);
        try{
            super.smartRemove(appModel.getIndividual(URI), appModel);
        }finally{ 
            appModel.leaveCriticalSection();
        }
    }

    @Override
    public void deleteClassDisplayConfig(ClassDisplayConfig cdc) {        
        if( cdc != null ){
            appModel.enterCriticalSection(Lock.WRITE);
            try{        
                super.smartRemove(appModel.getIndividual(cdc.getURI()), appModel);
            }finally{
                appModel.leaveCriticalSection();
            }            
        }
    }

    @Override
    public void updateClassDisplayConfig(ClassDisplayConfig cdc) {
        JenaAppConfigUtils.updateApplicationConfig(appModel, cdc);

        //TODO: update shortviews        
    }

    
//  public void addAppConfigToVClass( VClass vclass, ConfigContext configContext ){
//      if( vclass != null && vclass.getURI() != null ){
//          String typeURI = vclass.getURI();
//          
//          String dn = getDisplayName(typeURI,configContext);
//          if( dn != null && ! dn.isEmpty() )
//              vclass.setName( getDisplayName(typeURI, configContext));
//
//          String pd = getPublicDescription(typeURI, configContext);
//          if( pd != null && pd.isEmpty() )
//              vclass.setDescription( pd );
//          
//          int rank = getDisplayRank(typeURI,configContext);
//          if( rank != -1 )
//              vclass.setDisplayRank( rank );
//          
//          int limit = getDisplayLimit(typeURI, configContext);
//          if( limit != -1 )
//              vclass.setDisplayLimit( limit );
//      }
//  }    

}
