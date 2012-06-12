/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.appConfig.jena;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.ClassEditConfigDao;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans.ClassEditConfig;

/**
 * 
 * bdc34: there don't seem to be any properties associated with 
 * ClassEditConfig individuals so this isn't implemented yet.
 *
 */
public class ClassEditConfigDaoJena extends JenaAppConfigBase implements ClassEditConfigDao {

    public ClassEditConfigDaoJena(OntModel appModel, String localAppNamespace) {
        super(appModel, localAppNamespace);
    }

    protected ClassEditConfig ontIndToClassEditConfig ( Individual ind ){
        throw new NotImplementedException();
//        if( ind == null )
//            return null;
//        ClassEditConfig cec = new ClassEditConfig();                
//        appModel.enterCriticalSection(Lock.READ);
//        try{            
//            cec.setURI( ind.getURI());            
//
//        }finally{
//            appModel.leaveCriticalSection();
//        }
//        return cec;        
    }    

    
    @Override
    public ClassEditConfig getByURI(String URI) {        
        return ontIndToClassEditConfig( appModel.getIndividual( URI ));
    }

    @Override
    public void insertNewConfig(ClassEditConfig cdc) throws InsertException {
        throw new NotImplementedException();
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateConfig(ClassEditConfig cdc) {
        throw new NotImplementedException();
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deleteConfig(String URI) {
        throw new NotImplementedException();
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deleteConfig(ClassEditConfig cdc) {
        throw new NotImplementedException();
        // TODO Auto-generated method stub
        
    }
    
}
