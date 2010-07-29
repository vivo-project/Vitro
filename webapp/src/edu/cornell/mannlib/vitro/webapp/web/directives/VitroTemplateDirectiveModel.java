/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.directives;

import freemarker.template.Configuration;
import freemarker.template.TemplateDirectiveModel;

public interface VitroTemplateDirectiveModel extends TemplateDirectiveModel {
    
    public String help(Configuration config);

}
