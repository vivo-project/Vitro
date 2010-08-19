/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspWriter;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.tags.EditTag;
import org.apache.commons.lang.StringEscapeUtils;

/** This tag allows validation error messages to be displayed on a form JSP **/
public class ErrorTag extends EditTag {
    private String name = null;

    public void setName( String name ) {
        this.name = name;
    }

    public int doEndTag() throws JspException {
        try {
            JspWriter out = pageContext.getOut();

            String errors = null;
            try {
                errors = (String) getFormObject().getErrorMap().get(name);
            } catch (Exception e){
                System.out.println("Could not get the form object from which to extract validation error message.");
            }

            if (errors != null){
                out.print(StringEscapeUtils.escapeHtml((String) errors));
            }

        } catch(Exception ex) {
            throw new JspException(ex.getMessage());
        }
        return SKIP_BODY;
    }
}
