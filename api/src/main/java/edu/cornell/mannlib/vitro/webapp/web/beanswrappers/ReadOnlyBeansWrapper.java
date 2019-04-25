/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.web.beanswrappers;

import java.lang.reflect.Method;

import freemarker.ext.beans.MethodAppearanceFineTuner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapper.MethodAppearanceDecision;

/** A BeansWrapper that is more restrictive than EXPOSE_SAFE, by
 * exposing getters but not setters. A setter is defined for this
 * purpose as a method that returns void, or whose name
 * starts with "set". It also hides built-in methods of Java
 * utility classes like Map.put(), etc.
 *
 * @author rjy7
 *
 */
public class ReadOnlyBeansWrapper extends BeansWrapper {

    private static final Log log = LogFactory.getLog(ReadOnlyBeansWrapper.class);

    public ReadOnlyBeansWrapper() {
        // Start by exposing all safe methods.
        setExposureLevel(EXPOSE_SAFE);
        setMethodAppearanceFineTuner(new MethodAppearanceFineTuner() {
            @Override
            public void process(MethodAppearanceDecisionInput methodAppearanceDecisionInput, MethodAppearanceDecision methodAppearanceDecision) {
                Method method = methodAppearanceDecisionInput.getMethod();
                // How to define a setter? This is a weak approximation: a method whose name
                // starts with "set" or returns void.
                if ( method.getName().startsWith("set") ) {
                    methodAppearanceDecision.setExposeMethodAs(null);

                } else if ( method.getReturnType().getName().equals("void") ) {
                    methodAppearanceDecision.setExposeMethodAs(null);

                } else {

                    Class<?> declaringClass = method.getDeclaringClass();
                    if (declaringClass.equals(java.lang.Object.class)) {
                        methodAppearanceDecision.setExposeMethodAs(null);

                    } else {
                        Package pkg = declaringClass.getPackage();
                        if (pkg.getName().equals("java.util")) {
                            methodAppearanceDecision.setExposeMethodAs(null);
                        }
                    }
                }
            }
        });
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
