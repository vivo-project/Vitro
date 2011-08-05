/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package freemarker.ext.beans;

import java.lang.reflect.Member;

/**
 * Class to expose protected information about template models and their data
 * and wrappers to dump methods. Used as workaround to some problems and gaps
 * in the Freemarker template model API.
 */
public class WrapperUtils {
    
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
