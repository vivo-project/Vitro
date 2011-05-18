/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.jsptags;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditSubmission;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.Map;

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
public class ErrorMessage extends TagSupport {
    private String  name;

    public String getName() {
        return name;
    }
    public void setName(String n) {
        this.name = n;
    }


    public int doStartTag() {
        try {
            HttpSession session = pageContext.getSession();
            EditConfiguration editConfig = EditConfiguration.getConfigFromSession(session,(HttpServletRequest) pageContext.getRequest());
            EditSubmission editSub = EditSubmission.getEditSubmissionFromSession(session,editConfig);

            if( editSub == null )
                return SKIP_BODY;
            Map<String,String> errors = editSub.getValidationErrors();
            if( errors == null || errors.isEmpty())
                return SKIP_BODY;

            String val = errors.get(getName());
            if( val != null){
                JspWriter out = pageContext.getOut();
                out.print( val );
            }
        } catch (Exception ex) {
            throw new Error("Error in of ErrorMessage.doStartTag: " + ex.getMessage());
        }
        return SKIP_BODY;
    }

    public int doEndTag(){
	  return EVAL_PAGE;
	}
}