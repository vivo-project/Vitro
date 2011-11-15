/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.config.RevisionInfoBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.Tags;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.Tags.TagsWrapper;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class FreemarkerConfiguration extends Configuration {

    private static final Log log = LogFactory.getLog(FreemarkerConfiguration.class);

    private final String themeDir;
    private final ServletContext context;
    private final ApplicationBean appBean;
    private final String appName;
    private final Tags stylesheets;
    private final Tags scripts;
    private final Tags headScripts;
    
    FreemarkerConfiguration(String themeDir, VitroRequest vreq, ServletContext context) {
        
        this.themeDir = themeDir;
        this.context = context;
        this.appBean = vreq.getAppBean();
        this.appName = appBean.getApplicationName();
        
        this.stylesheets = new Tags();
        this.scripts = new Tags();
        this.headScripts = new Tags();
    
        String buildEnv = ConfigurationProperties.getBean(context).getProperty("Environment.build");
        log.debug("Current build environment: " + buildEnv);
        if ("development".equals(buildEnv)) { // Set Environment.build = development in deploy.properties
            log.debug("Disabling Freemarker template caching in development build.");
            setTemplateUpdateDelay(0); // no template caching in development 
        } else {
            int delay = 60;
            log.debug("Setting Freemarker template cache update delay to " + delay + ".");            
            setTemplateUpdateDelay(delay); // in seconds; Freemarker default is 5
        }
    
        // Specify how templates will see the data model. 
        // The Freemarker default wrapper exposes set methods and get methods that take
        // arguments. We block exposure to these methods by default. 
        BeansWrapper wrapper = new DefaultObjectWrapper();
        wrapper.setExposureLevel(BeansWrapper.EXPOSE_PROPERTIES_ONLY);
        setObjectWrapper(wrapper);
        
        // Set some formatting defaults. These can be overridden at the template
        // or environment (template-processing) level, or for an individual
        // token by using built-ins.
        setLocale(java.util.Locale.US);
        
        String dateFormat = "M/d/yyyy";
        setDateFormat(dateFormat);
        String timeFormat = "h:mm a";
        setTimeFormat(timeFormat);
        setDateTimeFormat(dateFormat + " " + timeFormat);
        
        //config.setNumberFormat("#,##0.##");
        
        try {
            setSetting("url_escaping_charset", "ISO-8859-1");
        } catch (TemplateException e) {
            log.error("Error setting value for url_escaping_charset.");
        }
        
        setTemplateLoader(createTemplateLoader());   

        setSharedVariables(vreq);

    }

    /** Some template variables are shared so that they are accessible to
     * all templates, but they are request-specific and so need to be
     * reset at the beginning of a new request.
     * 
     * This is public for now because it's accessed by propDelete.jsp. 
     * Once the property deletion is integrated into Freemarker and generated
     * with a Freemarker page, the visibility can be reduced to package.
     */
    public void resetRequestSpecificSharedVariables() {      
        stylesheets.reset();
        scripts.reset();
        headScripts.reset();
    }
    
    /**
     * These are values that are accessible to all
     * templates loaded by the Configuration's TemplateLoader. They
     * should be application- rather than request-specific, or else get
     * reset with a new request.
     * @param VitroRequest vreq
     */
    private void setSharedVariables(VitroRequest vreq) {

        Map<String, Object> sharedVariables = new HashMap<String, Object>();
        
        sharedVariables.put("siteName", appName);        
        sharedVariables.put("version", getRevisionInfo());
        sharedVariables.put("urls", getSiteUrls());
        sharedVariables.put("themeDir", themeDir);
        sharedVariables.put("currentTheme", themeDir.substring(themeDir.lastIndexOf('/')+1));
        
        sharedVariables.put("stylesheets", wrapTagList(stylesheets));
        sharedVariables.put("scripts", wrapTagList(scripts));
        sharedVariables.put("headScripts", wrapTagList(headScripts));
        
        sharedVariables.putAll(getDirectives());
        sharedVariables.putAll(getMethods());
        sharedVariables.put("siteTagline", appBean.getShortHand()); 
        
        for ( Map.Entry<String, Object> variable : sharedVariables.entrySet() ) {
            try {
                setSharedVariable(variable.getKey(), variable.getValue());
            } catch (TemplateModelException e) {
                log.error("Could not set shared variable '" + variable.getKey() + "' in Freemarker configuration");
            }
        }      
    }
    
    private final Map<String, Object> getRevisionInfo() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("label", RevisionInfoBean.getBean(context)
                .getReleaseLabel());
        map.put("moreInfoUrl", UrlBuilder.getUrl("/revisionInfo"));
        return map;
    }
    
    private final Map<String, String> getSiteUrls() {
        Map<String, String> urls = new HashMap<String, String>();

        // Templates use this to construct urls.
        urls.put("base", context.getContextPath());
        
        urls.put("home", UrlBuilder.getHomeUrl());
        urls.put("about", UrlBuilder.getUrl(Route.ABOUT));
        urls.put("search", UrlBuilder.getUrl(Route.SEARCH));  
        urls.put("termsOfUse", UrlBuilder.getUrl(Route.TERMS_OF_USE));  
        urls.put("login", UrlBuilder.getLoginUrl());          
        urls.put("logout", UrlBuilder.getLogoutUrl());       
        urls.put("siteAdmin", UrlBuilder.getUrl(Route.SITE_ADMIN));  
        urls.put("themeImages", UrlBuilder.getUrl(themeDir + "/images"));
        urls.put("images", UrlBuilder.getUrl("/images"));
        urls.put("theme", UrlBuilder.getUrl(themeDir));
        urls.put("index", UrlBuilder.getUrl("/browse"));
        
        return urls;
    }
 
    /** Script and stylesheet lists are wrapped with a specialized BeansWrapper
     * that exposes certain write methods, instead of the configuration's object wrapper,
     * which doesn't. The templates can then add stylesheets and scripts to the lists
     * by calling their add() methods.
     * @param Tags tags
     * @return TemplateModel
     */
    private TemplateModel wrapTagList(Tags tags) {        
        try {
            BeansWrapper wrapper = new TagsWrapper();
            return wrapper.wrap(tags); // this is a StringModel
        } catch (TemplateModelException e) {
            log.error("Error creating Tags template model");
            return null;
        }
    }

    public static Map<String, Object> getDirectives() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("dump", new freemarker.ext.dump.DumpDirective());
        map.put("dumpAll", new freemarker.ext.dump.DumpAllDirective());  
        map.put("help", new freemarker.ext.dump.HelpDirective());    
        return map;
    }
    
    public static Map<String, Object> getMethods() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("profileUrl", new edu.cornell.mannlib.vitro.webapp.web.methods.IndividualProfileUrlMethod());
        map.put("localName", new edu.cornell.mannlib.vitro.webapp.web.methods.IndividualLocalNameMethod());
        return map;
    }
    
    // Define template locations. Template loader will look first in the theme-specific
    // location, then in the vitro location.
    protected final TemplateLoader createTemplateLoader() {
    
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
