/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.jsptags;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

/**
 * This tag will build an option list for individuals of a VClass.
 *
 * User: bdc34
 * Date: Jan 4, 2008
 * Time: 12:16:29 PM
 */
public class OptionsForClassTag extends TagSupport {
    private String  selectedUri;
    private String  classUri;

    public String getClassUri() {
        return classUri;
    }
    public void setClassUri(String classUri) {
        this.classUri = classUri;
    }

    public String getSelectedUri() {
        return selectedUri;
    }
    public void setSelectedUri(String selectedUri) {
        this.selectedUri = selectedUri;
    }

    public int doStartTag() {
        try {
            VitroRequest vreq = new VitroRequest( (HttpServletRequest) pageContext.getRequest() );
            WebappDaoFactory wdf = vreq.getWebappDaoFactory();
            if( wdf == null ) throw new Exception("could not get WebappDaoFactory from request.");

            VClass vclass = wdf.getVClassDao().getVClassByURI( getClassUri());
            if( vclass == null ) throw new Exception ("could not get class for " + getClassUri());

            List<Individual> individuals = wdf.getIndividualDao().getIndividualsByVClassURI(vclass.getURI(),-1,-1);

            JspWriter out = pageContext.getOut();

            for( Individual ind : individuals ){
                String uri = ind.getURI()  ;
                if( uri != null ){
                    out.print("<option value=\"" + StringEscapeUtils.escapeHtml( uri ) + '"');
                    if( uri.equals(getSelectedUri()))
                        out.print(" selected=\"selected\"");
                    out.print('>');
                    out.print(StringEscapeUtils.escapeHtml( ind.getName() ));
                    out.println("</option>");
                }

            }
        } catch (Exception ex) {
            throw new Error("Error in doStartTag: " + ex.getMessage());
        }
        return SKIP_BODY;
    }

    public int doEndTag(){
	  return EVAL_PAGE;
	}
}