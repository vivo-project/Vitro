/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.ResourceGenerator;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import org.apache.jena.ontology.OntModel;

public class Initializer implements ServletContextListener {

    public Initializer() {

    }

    private void initializeResourcePool() {
        ResourceAPIPool.getInstance().init();
    }

    private void initializeProcedurePool() {
        ProcedurePool.getInstance().init();
    }

    private void initializeRPCPool() {
        RPCPool.getInstance().init();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        initializeStorage();
        initializeNewResourceGenerator();
        initializeProcedurePool();
        initializeRPCPool();
        initializeResourcePool();
    }

    private void initializeStorage() {
        DynapiModelProvider storage = DynapiModelProvider.getInstance();
        OntModel abox = ModelAccess.getInstance().getOntModel(ModelNames.DYNAMIC_API_ABOX);
        OntModel tbox = ModelAccess.getInstance().getOntModel(ModelNames.DYNAMIC_API_TBOX);
        storage.init(abox, tbox);
    }

    private void initializeNewResourceGenerator() {
        ResourceGenerator resourceGenerator = ResourceGenerator.getInstance();
        resourceGenerator.init(ModelAccess.getInstance().getWebappDaoFactory());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // TODO Auto-generated method stub
    }

}
