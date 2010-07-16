/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.tags;

import java.util.HashMap;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspWriter;
import edu.cornell.mannlib.vedit.beans.FormObject;
import org.apache.commons.lang.StringEscapeUtils;
import edu.cornell.mannlib.vedit.tags.EditTag;

public class ValueTag extends EditTag {
    private String name = null;

    public void setName( String name ) {
        this.name = name;
    }

    public int doEndTag() throws JspException {
        try {
            JspWriter out = pageContext.getOut();

            HashMap values = null;
            try {
                // FormObject foo = (FormObject) pageContext.getSession().getAttribute("FormObject");
                // FormObject foo = TagUtils.getFormObject(pageContext);
                FormObject foo = getFormObject();
                values = foo.getValues();
            } catch (Exception e){
                System.out.println("Could not get the form object from which to build an option list");
            }

            if (values != null){
                String value = (String) values.get(name);
                if (value != null)
                    out.print(StringEscapeUtils.escapeHtml(value));
            } else {
                System.out.println("ValueTag unable to get HashMap of form values");
            }

        } catch(Exception ex) {
            throw new JspException(ex.getMessage());
        }
        return SKIP_BODY;
    }
}
