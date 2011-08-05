/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package freemarker.ext.beans;


/**
 * Class to expose template model wrapper. Used as workaround to gaps
 * in the Freemarker template model API (can't get wrapper for an
 * arbitrary template model object).
 */
public class WrapperExtractor {
    
    public static BeansWrapper getWrapper(BeanModel model) {
        return model.wrapper;
    }
    
    public static int getWrapperExposureLevel(BeanModel model) {
        return model.wrapper.getExposureLevel();
    }
    
}
