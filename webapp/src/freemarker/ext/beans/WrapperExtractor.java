/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package freemarker.ext.beans;

import java.util.Set;

/**
 * Class to extract wrapper used to wrap an object into a template model object. 
 * Used as workaround to gap in Freemarker template model API (can't get wrapper
 * for an arbitrary template model object).
 */
public class WrapperExtractor {
    
    public static BeansWrapper getWrapper(BeanModel model) {
        return model.wrapper;
    }
    
    public static int getWrapperExposureLevel(BeanModel model) {
        return model.wrapper.getExposureLevel();
    }

}
