package edu.cornell.mannlib.vitro.webapp.dynapi;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

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

    private void initializeActionPool(ServletContext ctx) {
        ActionPool actionPool = ActionPool.getInstance();
        actionPool.init(ctx);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        initializeActionPool(ctx);
        initializeResourcePool(ctx);
        initializeDynamicAPIDocumentation(ctx);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // TODO Auto-generated method stub
    }

}
