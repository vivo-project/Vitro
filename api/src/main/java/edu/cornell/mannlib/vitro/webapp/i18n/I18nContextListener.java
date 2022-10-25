package edu.cornell.mannlib.vitro.webapp.i18n;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class I18nContextListener implements ServletContextListener{

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		initializeTranslationProvider(sce);
		initializeTranslationConverter(sce);
	}

	private void initializeTranslationConverter(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		TranslationConverter.getInstance().initialize(ctx);

	}

	private void initializeTranslationProvider(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		TranslationProvider.getInstance().initialize(ctx);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}

}
