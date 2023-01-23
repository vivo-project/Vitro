package edu.cornell.mannlib.vitro.webapp.dynapi;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.DynapiModelFactory;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.ResourceGenerator;

public class Initializer implements ServletContextListener {

    public Initializer() {

    }

    private void initializeDynamicAPIDocumentation(ServletContext ctx) {
        DynamicAPIDocumentation dynamicAPIDocumentation = DynamicAPIDocumentation.getInstance();
        dynamicAPIDocumentation.init(ctx);
    }

    private void initializeResourcePool(ServletContext ctx) {
        ResourceAPIPool resourceAPIPool = ResourceAPIPool.getInstance();
        resourceAPIPool.init(ctx);
    }

    private void initializeProcedurePool(ServletContext ctx) {
        ProcedurePool actionPool = ProcedurePool.getInstance();
        actionPool.init(ctx);
    }
    
    private void initializeRPCPool(ServletContext ctx) {
        RPCPool rpcPool = RPCPool.getInstance();
        rpcPool.init(ctx);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        initializeStorage(ctx);
        initializeNewResourceGenerator(ctx);
        initializeProcedurePool(ctx);
        initializeRPCPool(ctx);
        initializeResourcePool(ctx);
        initializeDynamicAPIDocumentation(ctx);
        initializeDynamicAPIModelFactory(ctx);
    }

    private void initializeStorage(ServletContext ctx) {
		DynapiModelProvider storage = DynapiModelProvider.getInstance();
		OntModel abox = ModelAccess.on(ctx).getOntModel(ModelNames.DYNAMIC_API_ABOX);
		OntModel tbox = ModelAccess.on(ctx).getOntModel(ModelNames.DYNAMIC_API_TBOX);
		storage.init(abox, tbox);
    }

    private void initializeNewResourceGenerator(ServletContext ctx) {
    	ResourceGenerator resourceGenerator = ResourceGenerator.getInstance();
    	resourceGenerator.init(ModelAccess.on(ctx).getWebappDaoFactory());
    }

    private void initializeDynamicAPIModelFactory(ServletContext ctx) {
	DynapiModelFactory factory = DynapiModelFactory.getInstance();
	factory.init(ctx);
    }

	@Override
    public void contextDestroyed(ServletContextEvent sce) {
        // TODO Auto-generated method stub
    }

}
