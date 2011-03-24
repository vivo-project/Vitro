package edu.cornell.mannlib.vitro.webapp.web.jsptags;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration;
import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.List;

/**
 *
 * Build the options list using info in EditConfiguration.  If there are
 * parameters in the request that match the name attribute then mark that
 * option as selected.
 *
 * User: bdc34
 * Date: Jan 26, 2008
 * Time: 3:00:22 PM
 */
public class Options extends TagSupport {
    private String  name;

    public String getName() {
        return name;
    }
    public void setName(String n) {
        this.name = n;
    }


    public int doStartTag() {
        throw new Error("Value tag is no longer supported, Use InputElementFormating ");
    }

    public int doEndTag(){
	  return EVAL_PAGE;
	}
}
