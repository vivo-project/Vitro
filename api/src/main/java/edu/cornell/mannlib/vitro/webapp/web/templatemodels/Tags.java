/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class Tags extends BaseTemplateModel {
    
    private static final Log log = LogFactory.getLog(Tags.class);
    
    protected final LinkedHashSet<String> tags;

    public Tags() {
        this.tags = new LinkedHashSet<String>();
    }
    
    public Tags(LinkedHashSet<String> tags) {
        this.tags = tags;
    }
    
    public TemplateModel wrap() {
        try {
            return new TagsWrapper().wrap(this);    	
        } catch (TemplateModelException e) {
            log.error("Error creating Tags template model");
            return null;
        }
    }
    
    /** Script and stylesheet lists are wrapped with a specialized BeansWrapper
     * that exposes certain write methods, instead of the configuration's object wrapper,
     * which doesn't. The templates can then add stylesheets and scripts to the lists
     * by calling their add() methods.
     */
    static public class TagsWrapper extends BeansWrapper {
        
        public TagsWrapper() {
            // Start by exposing all safe methods.
            setExposureLevel(EXPOSE_SAFE);
        }
        
        @SuppressWarnings("rawtypes")
        @Override
        protected void finetuneMethodAppearance(Class cls, Method method, MethodAppearanceDecision decision) {
            
            try {
                String methodName = method.getName();
                if ( ! ( methodName.equals("add") || methodName.equals("list")) ) {
                    decision.setExposeMethodAs(null);
                }
            } catch (Exception e) {
                log.error(e, e);
            }
        }
    }
    
    
    /* Template methods */

    public void add(String... tags) {
        for (String tag : tags) {
            add(tag);
        }
    }
    
    public void add(String tag) {
        tags.add(tag);
    }
 
    public String list() {
        return StringUtils.join(tags, "\n");
    }
    

}
