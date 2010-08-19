/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.tags;

import java.util.HashMap;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspWriter;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import org.apache.commons.lang.StringEscapeUtils;

public class EditTag extends TagSupport {
    private String name = null;

    public void setName( String name ) {
        this.name = name;
    }

    public int doEndTag() throws JspException {
        return SKIP_BODY;
    }

    public EditProcessObject getEpo() {
        EditProcessObject epo = null;
        String epoKey = null;
        String epoKeyAttr = (String) pageContext.getRequest().getAttribute("epoKey");
        if (epoKeyAttr != null) {
            epoKey = epoKeyAttr;
        }
        else {
            String epoKeyParam = (String) pageContext.getRequest().getParameter("epoKey");
            if (epoKeyParam != null) {
                epoKey = epoKeyParam;
            }
        }
        HashMap epoHash = (HashMap) pageContext.getSession().getAttribute("epoHash");
        try {
            epo = (EditProcessObject) epoHash.get(epoKey);
        } catch (NullPointerException npe) {
            System.out.println("Null epoHash in edu.cornell.mannlib.vitro.edu.tags.utils.TagUtils.getEpo()");
        }
        return epo;
    }

    public FormObject getFormObject() {
        FormObject foo=null;
        try {
            foo=getEpo().getFormObject();
        } catch (NullPointerException npe) {
            System.out.println("Null epo in edu.cornell.mannlib.vitro.edit.tags.utils.TagUtils.getFormObject()");
        }
        return foo;
    }
}
