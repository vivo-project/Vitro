/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateException;

public class FreemarkerConfigurationLoader {    
    private static final Log log = LogFactory.getLog(FreemarkerConfigurationLoader.class);

    private ServletContext context;
    
    public FreemarkerConfigurationLoader(ServletContext context){
        this.context = context;
        context.setAttribute("FreemarkerConfigurationLoader", this);
    }
    
    /**
     * @return Under serious error conditions this will return null.
     */
    public static FreemarkerConfigurationLoader getFreemarkerConfigurationLoader(ServletContext context){
        if( context!=null){
            FreemarkerConfigurationLoader fcl = (FreemarkerConfigurationLoader)
                context.getAttribute("FreemarkerConfigurationLoader");
            if( fcl == null ){
                log.error("Must be constructed before calling " +
                        "getFreemarkerConfigurationLoader(), usually this is done by FreemarkerSetup");
                return null;
            }
            return fcl;
        }else{
            log.error("getFreemarkerConfigurationLoader() must not be called with a null context");        
            return null; 
        }
    }
    
    public Configuration getConfig(VitroRequest vreq) {       
        String themeDir = getThemeDir(vreq.getPortal());
        return getConfigForTheme(themeDir);
    }

    protected String getThemeDir(Portal portal) {
        return portal.getThemeDir().replaceAll("/$", "");
    }

    
    protected Configuration getConfigForTheme(String themeDir) {
        
        // The template loader is theme-specific because it specifies the theme template directory as a location to
        // load templates from. Thus configurations are associated with themes rather than portals.
        @SuppressWarnings("unchecked")
        Map<String, Configuration> themeToConfigMap = (Map<String, Configuration>) context.getAttribute("themeToConfigMap");
        
        if( themeToConfigMap == null ) {
            log.error("The templating system is not configured correctly. Make sure that you have the FreemarkerSetup context listener in your web.xml.");
            // We'll end up with a blank page as well as errors in the log, which is probably fine. 
            // Doesn't seem like we should throw a checked exception in this case.
            return null;    
        } else if (themeToConfigMap.containsKey(themeDir)) {
            return themeToConfigMap.get(themeDir);
        } else {
            Configuration config = getNewConfig(themeDir);
            themeToConfigMap.put(themeDir, config);
            return config;
        }
    }
    
    private Configuration getNewConfig(String themeDir) {
        
        Configuration config = new Configuration();
        
        String buildEnv = ConfigurationProperties.getProperty("Environment.build");
        log.debug("Current build environment: " + buildEnv);
        if ("development".equals(buildEnv)) { // Set Environment.build = development in deploy.properties
            log.debug("Disabling Freemarker template caching in development build.");
            config.setTemplateUpdateDelay(0); // no template caching in development 
        } else {
            int delay = 60;
            log.debug("Setting Freemarker template cache update delay to " + delay + ".");            
            config.setTemplateUpdateDelay(delay); // in seconds; Freemarker default is 5
        }

        // Specify how templates will see the data model. 
        // The default wrapper exposes set methods unless exposure level is set.
        // By default we want to block exposure of set methods. 
        BeansWrapper wrapper = new DefaultObjectWrapper();
        wrapper.setExposureLevel(BeansWrapper.EXPOSE_PROPERTIES_ONLY);
        config.setObjectWrapper(wrapper);

        // Set some formatting defaults. These can be overridden at the template
        // or environment (template-processing) level, or for an individual
        // token by using built-ins.
        config.setLocale(java.util.Locale.US);
        
        String dateFormat = "M/d/yyyy";
        config.setDateFormat(dateFormat);
        String timeFormat = "hh:mm a";
        config.setTimeFormat(timeFormat);
        config.setDateTimeFormat(dateFormat + " " + timeFormat);
        
        //config.setNumberFormat("#,##0.##");
        
        try {
            config.setSetting("url_escaping_charset", "ISO-8859-1");
        } catch (TemplateException e) {
            log.error("Error setting value for url_escaping_charset.");
        }
        
        config.setTemplateLoader(getTemplateLoader(config, themeDir));        
        
        return config;
    }

    // Define template locations. Template loader will look first in the theme-specific
    // location, then in the vitro location.
    protected final TemplateLoader getTemplateLoader(Configuration config, String themeDir) {

        List<TemplateLoader> loaders = new ArrayList<TemplateLoader>();
        MultiTemplateLoader mtl = null;
        try {
            // Theme template loader
            String themeTemplatePath = context.getRealPath(themeDir) + "/templates";
            File themeTemplateDir = new File(themeTemplatePath);    
            // Handle the case where there's no theme template directory gracefully
            if (themeTemplateDir.exists()) {
                FileTemplateLoader themeFtl = new FileTemplateLoader(themeTemplateDir);
                loaders.add(themeFtl);
            } 
            
            // Vitro template loader
            String vitroTemplatePath = context.getRealPath("/templates/freemarker");
            loaders.add(new FlatteningTemplateLoader(new File(vitroTemplatePath)));
            
            loaders.add(new ClassTemplateLoader(getClass(), ""));
            
            TemplateLoader[] loaderArray = loaders.toArray(new TemplateLoader[loaders.size()]);
            mtl = new MultiTemplateLoader(loaderArray);
            
        } catch (IOException e) {
            log.error("Error creating template loaders");
        }
        return mtl;
        
    }
    
}
