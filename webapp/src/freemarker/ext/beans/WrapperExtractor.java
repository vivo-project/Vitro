/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package freemarker.ext.beans;

import java.lang.reflect.Member;


/**
 * Class to extract information about the wrapper used to wrap an object in 
 * the template model.
 */
public class WrapperExtractor {
    
    public static BeansWrapper getWrapper(BeanModel model) {
        return model.wrapper;
    }
    
    public static int getWrapperExposureLevel(BeanModel model) {
        return model.wrapper.getExposureLevel();
    }
    
    public static Member getMember(SimpleMethodModel model) {
        return model.getMember();
    }
}
