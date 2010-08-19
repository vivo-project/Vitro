/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;

/**
 * Class to find custom class views for individuals
 * @author rjy7
 *
 */
public class ViewFinder {
    
    private static final Log log = LogFactory.getLog(ViewFinder.class.getName());
    
    public enum ClassView { 
        DISPLAY("getCustomDisplayView", "/view-display"),
        // NB this is not the value currently used for custom forms - we use the value on the object property
        FORM("getCustomEntryForm", "/form"), 
        SEARCH("getCustomSearchView", "/view-search"),
        SHORT("getCustomShortView", "/view-short"); 
        
        private static String TEMPLATE_PATH = "/templates/freemarker";
        
        private Method method = null;
        private String path = null;
        
        ClassView(String methodName, String path) {
            Class<VClass> vc = VClass.class;
            this.path = path;
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
        
        protected String getPath() {
            return TEMPLATE_PATH + path;
        }

    }
    
    private ClassView view;
    
    public ViewFinder(ClassView view) {
        this.view = view;
    }
    
    public String findClassView(Individual individual, ServletContext context) {
        String viewName = "default.ftl"; 
        List<VClass> vclasses = individual.getVClasses();
        Method method = view.getMethod();
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
        for (VClass vc : vclasses) {
            try {
                String v = (String) method.invoke(vc);
                if (!StringUtils.isEmpty(v)) {
                    String pathToView = context.getRealPath(view.getPath() + "-" + v);
                    File viewFile = new File(pathToView);
                    if (viewFile.isFile() && viewFile.canRead()) {
                        viewName = v;
                        break;
                    }
                }
            } catch (IllegalArgumentException e) {
                log.error("Incorrect arguments passed to method " + method.getName() + " in findView().");
            } catch (IllegalAccessException e) {
                log.error("Method " + method.getName() + " cannot be accessed in findView().");
            } catch (InvocationTargetException e) {
                log.error("Exception thrown by method " + method.getName() + " in findView().");
            }

        }
        return viewName;
    }

}
