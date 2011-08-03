/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package freemarker.ext.beans;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** BeansWrapper that is more restrictive than EXPOSE_SAFE, by
 * exposing getters but not setters. A setter is defined for this
 * purpose as a method that returns void, or whose name
 * starts with "set". 
 * 
 * @author rjy7
 *
 */
public class ReadOnlyBeansWrapper extends BeansWrapper {

    private static final Log log = LogFactory.getLog(ReadOnlyBeansWrapper.class);
    
    public ReadOnlyBeansWrapper() {
        // Start by exposing all safe methods.
        setExposureLevel(EXPOSE_SAFE);
    }
    
    @Override
    protected void finetuneMethodAppearance(Class cls, Method method, MethodAppearanceDecision decision) {
        
        // How to define a setter? This is an approximation: a method whose name
        // starts with "set" or returns void.
        if ( method.getName().startsWith("set") ) {
            decision.setExposeMethodAs(null);
        } else if ( method.getReturnType().getName().equals("void") ) {
            decision.setExposeMethodAs(null);
        }
    }
    
// For exposing a method as a property (when it's not named getX or isX). Note that this is not
// just a syntactic change in the template from X() to X, but also makes the value get precomputed.
//    private void exposeAsProperty(Method method, MethodAppearanceDecision decision)  {
//        try {
//            PropertyDescriptor pd = new PropertyDescriptor(method.getName(), method, null);
//            decision.setExposeAsProperty(pd);
//            decision.setMethodShadowsProperty(false);
//        } catch (IntrospectionException e) {
//            log.error(e, e);
//        }
//    }
    
}
