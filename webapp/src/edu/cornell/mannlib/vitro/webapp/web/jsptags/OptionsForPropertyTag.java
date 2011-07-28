/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.jsptags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * This tag will build an option list using the subjectUri
 * and the predicateUri.
 *
 * User: bdc34
 * Date: Jan 4, 2008
 * Time: 12:16:29 PM
 */
public class OptionsForPropertyTag extends TagSupport {
	
	private static final Log log = LogFactory.getLog(OptionsForPropertyTag.class.getName());
    private String subjectUri, predicateUri, selectedUri;

    public String getSubjectUri() {
        return subjectUri;
    }
    public void setSubjectUri(String subjectUri) {
        this.subjectUri = subjectUri;
    }

    public String getPredicateUri() {
        return predicateUri;
    }
    public void setPredicateUri(String predicateUri) {
        this.predicateUri = predicateUri;
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

            Individual subject = wdf.getIndividualDao().getIndividualByURI(getSubjectUri());
            if( subject == null ) throw new Exception("could not get individual for subject uri " + getSubjectUri());

            ObjectProperty objProp = wdf.getObjectPropertyDao().getObjectPropertyByURI(getPredicateUri());
            if( objProp == null ) throw new Exception ("could not get object property for predicate " + getPredicateUri());

            List <VClass> vclasses = new ArrayList<VClass>();
            vclasses = wdf.getVClassDao().getVClassesForProperty(getPredicateUri(), true);

            HashMap<String,Individual> indMap = new HashMap<String,Individual>();
            for ( VClass vclass :  vclasses){
                for( Individual ind : wdf.getIndividualDao().getIndividualsByVClassURI(vclass.getURI(),-1,-1))
                  if( !indMap.containsKey(ind.getURI()))
                    indMap.put(ind.getURI(),ind);
            }

            List<Individual> individuals = new ArrayList(indMap.values());

            List<ObjectPropertyStatement> stmts = subject.getObjectPropertyStatements();
            if( stmts == null ) throw new Exception("object properties for subject were null");

            individuals = removeIndividualsAlreadyInRange(individuals,stmts);
            Collections.sort(individuals,new  compareEnts());

            JspWriter out = pageContext.getOut();

            int optionsCount=0;
            for( Individual ind : individuals ){
                String uri = ind.getURI()  ;
                if( uri != null ){
                    out.print("<option value=\"" + StringEscapeUtils.escapeHtml( uri ) + '"');
                    if( uri.equals(getSelectedUri()))
                        out.print(" selected=\"selected\"");
                    out.print('>');
                    out.print(StringEscapeUtils.escapeHtml( ind.getName() ));
                    out.println("</option>");
                    ++optionsCount;
                }

            }
            log.trace("added "+optionsCount+" options for object property \""+getPredicateUri()+"\" in OptionsForPropertyTag.doStartTag()");
        } catch (Exception ex) {
            throw new Error("Error in doStartTag: " + ex.getMessage());
        }
        return SKIP_BODY;
    }

    public int doEndTag(){
	  return EVAL_PAGE;
	}

    private List<Individual> removeIndividualsAlreadyInRange(List<Individual> indiviuals,
                                                             List<ObjectPropertyStatement> stmts){
        log.trace("starting to check for duplicate range individuals in OptionsForPropertyTag.removeIndividualsAlreadyInRange() ...");
        HashSet<String>  range = new HashSet<String>();

        for(ObjectPropertyStatement ops : stmts){
            if( ops.getPropertyURI().equals(getPredicateUri()))
                range.add( ops.getObjectURI() );
        }

        int removeCount=0;
        ListIterator<Individual> it = indiviuals.listIterator();
        while(it.hasNext()){
            Individual ind = it.next();
            if( range.contains( ind.getURI() ) ) {
                it.remove();
                ++removeCount;
            }
        }
        log.trace("removed "+removeCount+" duplicate range individuals");
        return indiviuals;
    }


    private class compareEnts  implements Comparator<Individual>{
        public int compare(Individual o1, Individual o2) {
            return ((o1.getName()==null)?"":o1.getName()).compareToIgnoreCase((o2.getName()==null)?"": o2.getName());
        }
    }
}
