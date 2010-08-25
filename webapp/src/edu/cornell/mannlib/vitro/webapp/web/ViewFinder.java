/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;

/**
 * Class to find custom class views for individuals
 * @author rjy7
 *
 */
public class ViewFinder {
    
    private static final Log log = LogFactory.getLog(ViewFinder.class);
    
    public enum ClassView { 
        DISPLAY("getCustomDisplayView", "view-display-default.ftl"),
        // NB this is not the value currently used for custom forms - we use the value on the object property.
        // This value is specifiable from the backend editor, however.
        FORM("getCustomEntryForm", "form-default.ftl"), 
        SEARCH("getCustomSearchView", "view-search-default.ftl"),
        SHORT("getCustomShortView", "view-short-default.ftl"); 
        
        private Method method = null;
        private String defaultTemplate = null;
        
        ClassView(String methodName, String defaultTemplate) {
            Class<VClass> vc = VClass.class;
            this.defaultTemplate = defaultTemplate;
            try {
                method = vc.getMethod(methodName);
            } catch (SecurityException e) {
                log.error("Access denied to method " + methodName + " or class " + vc.getName());   
            } catch (NoSuchMethodException e) {
                log.error("Method " + methodName + " not defined for class " + vc.getName());
            }
        }
        
        protected Method getMethod() {
            return method;
        }
        
        protected String getDefaultTemplate() {
            return defaultTemplate;
        }        

    }
    
    private ClassView view;
    
    public ViewFinder(ClassView view) {
        this.view = view;
    }
    
    public String findClassView(Individual individual, ServletContext context, VitroRequest vreq) {
        String templateName = view.getDefaultTemplate();
        Method method = view.getMethod();
        TemplateLoader templateLoader = ((Configuration) vreq.getAttribute("freemarkerConfig")).getTemplateLoader();

        /* RY The logic here is incorrect. The vclasses are
         * returned in a random order, whereas we need to
         * traverse the class hierarchy and find the most
         * specific custom view applicable to the individual.
         * The logic is complex because individuals can belong
         * to multiple classes, and classes can subclass multiple
         * classes. If there are two competing custom views at the 
         * same level of specificity, what should we do? Also, if we
         * are displaying a list of individuals belonging to a certain
         * class, we may want to use only a custom view defined for that 
         * class and NOT a more specific one. See NIHVIVO-568. Similarly 
         * when we're displaying an object property: if we are displaying
         * #hasPrincipalInvestigatorRole, the object should be displayed
         * as a PrincipalInvestigatorRole object rather than some other type.
         * 
         * RY 7/19/10 Use distinction between asserted and inferred vclasses
         * as a starting point: see MiscWebUtils.getCustomShortView().
         */
        List<VClass> vclasses = individual.getVClasses();
        String customTemplate = null;
        for (VClass vc : vclasses) {
            customTemplate = findCustomTemplateForVClass(vc, method, templateLoader);
            if (customTemplate != null) {
                templateName = customTemplate;
                break;
            }
        }
        log.debug("Using template " + templateName + " for individual " + individual.getName());
        return templateName;
    }
    
    private String findCustomTemplateForVClass(VClass vclass, Method method, TemplateLoader templateLoader) {
        String customTemplate = null;
        String vClassCustomTemplate = null;
        
        try {
            vClassCustomTemplate = (String) method.invoke(vclass);
        } catch (IllegalArgumentException e) {
            log.error("Incorrect arguments passed to method " + method.getName() + " in findCustomTemplateForVClass().");
        } catch (IllegalAccessException e) {
            log.error("Method " + method.getName() + " cannot be accessed in findCustomTemplateForVClass().");
        } catch (InvocationTargetException e) {
            log.error("Exception thrown by method " + method.getName() + " in findCustomTemplateForVClass().");
        }
        
        if (!StringUtils.isEmpty(vClassCustomTemplate)) {
            log.debug("Custom template " + vClassCustomTemplate + " defined for class " + vclass.getName());
            try {
                // Make sure the template exists
                if (templateLoader.findTemplateSource(vClassCustomTemplate) != null) {
                    log.debug("Found defined custom template " + vClassCustomTemplate + " for class " + vclass.getName());
                    customTemplate = vClassCustomTemplate;
                } else {
                    log.warn("Custom template " + vClassCustomTemplate + " for class " + vclass.getName() + " defined but does not exist.");
                }
            } catch (IOException e) {
                log.error("IOException looking for source for template " + vClassCustomTemplate);
            }                   
        }
        
        if (log.isDebugEnabled()) {
            if (customTemplate != null) {
                log.debug("Using custom template " + customTemplate + " for class " + vclass.getName());
            } else {
                log.debug("No custom template found for class " + vclass.getName());
            }
        }
        return customTemplate;
    }

}
