/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues;

import java.util.Map;

public class ExceptionResponseValues extends TemplateResponseValues {
    private final static String DEFAULT_TEMPLATE_NAME = "error-standard.ftl";
    private final Throwable cause;

    public ExceptionResponseValues(Throwable cause) {
        super(DEFAULT_TEMPLATE_NAME);
        this.cause = cause;
    }

    public ExceptionResponseValues(Throwable cause, int statusCode) {
        super(DEFAULT_TEMPLATE_NAME, statusCode);
        this.cause = cause;
    }
    
    public ExceptionResponseValues(String templateName, Throwable cause) {
        super(templateName);
        this.cause = cause;
    }

    public ExceptionResponseValues(String templateName, Throwable cause, int statusCode) {
        super(templateName, statusCode);
        this.cause = cause;
    }
    
    public ExceptionResponseValues(String templateName, Map<String, Object> map, Throwable cause) {
        super(templateName, map);
        this.cause = cause;
    }

    public ExceptionResponseValues(String templateName, Map<String, Object> map, Throwable cause, int statusCode) {
        super(templateName, map, statusCode);
        this.cause = cause;
    }

    @Override
    public Throwable getException() {
        return cause;
    }       
}
