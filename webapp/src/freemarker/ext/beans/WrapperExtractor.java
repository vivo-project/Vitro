/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package freemarker.ext.beans;

/**
 * Class to extract information about the wrapper used to wrap an object in 
 * the template model. This is something of a hack: the class belongs to
 * package freemarker.ext.beans so we can get at protected members of
 * BeanModel and BeansWrapper. The Freemarker API unfortunately provides 
 * no way to get  the wrapper that is used to wrap an object in the 
 * template data model.
 */
public class WrapperExtractor {
    
    public static BeansWrapper getWrapper(BeanModel model) {
        return model.wrapper;
    }
    
    public static int getWrapperExposureLevel(BeanModel model) {
        return model.wrapper.getExposureLevel();
    }
    
    public static int getWrapperExposureLevel(BeansWrapper wrapper) {
        return wrapper.getExposureLevel();
    }

}
