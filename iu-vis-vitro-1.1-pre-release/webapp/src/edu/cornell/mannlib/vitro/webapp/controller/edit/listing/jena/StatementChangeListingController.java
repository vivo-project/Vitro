/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit.listing.jena;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.UserDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class StatementChangeListingController extends BaseEditController {

    private class ResultRow implements Comparable {
        String stmt = "";
        String action = "";
        String time = "";
        public int compareTo(Object o) {
            return ((ResultRow)o).time.compareTo(this.time);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        VitroRequest vrequest = new VitroRequest(request);
        Portal portal = vrequest.getPortal();

        if(!checkLoginStatus(request,response))
            return;

        try {
            super.doGet(request, response);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        
        try {
        	
        LoginFormBean loginBean = (LoginFormBean) request.getSession().getAttribute("loginHandler");
        
        // TODO: need to make this more restrictive
        String userURI = (request.getParameter("userURI") != null) ? request.getParameter("userURI") : loginBean.getUserURI();

        OntModel ontModel = (OntModel) getServletContext().getAttribute("jenaOntModel");
        Model auditModel = (Model) request.getSession().getServletContext().getAttribute("jenaAuditModel");

        UserDao dao = getWebappDaoFactory().getUserDao();

        User user = dao.getUserByURI(userURI);

        ArrayList results = new ArrayList();
        request.setAttribute("results",results);
        results.add("XX");
        results.add("individual");
        results.add("action");
        results.add("date/time");

        String EMPTY = "";

        List<ResultRow> resultRows = new LinkedList<ResultRow>();

        /*
        String queryStr = "PREFIX vitro: <"+ VitroVocabulary.vitroURI+"> " +
        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
        "SELECT ?subj ?pred ?obj ?time " +
        "WHERE { " +
        "     ?x rdf:type rdf:Statement ." +
        "     ?x rdf:subject ?subj ." +
        "     ?x rdf:predicate ?pred ." +
        "     ?x rdf:object ?obj ." +
        "     ?x vitro:addDate ?time . " +
        "}" +
        "     ORDER BY DESC( ?time ) LIMIT 32 " ;
        */
        
        String queryStr = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
        		" PREFIX vitro: <"+VitroVocabulary.vitroURI+"> " +
        		" SELECT ?individual ?dateTime " +
        		" WHERE " + 
        		" { " +
        		" ?editEvent vitro:editEventAgent <"+userURI+"> ." +
        		" ?editEvent rdf:type vitro:IndividualEditEvent . " +
        		" ?editEvent vitro:editedIndividual ?individual . " +
        		" ?editEvent vitro:editEventDateTime ?dateTime . " +
        		" } " +
        		" ORDER BY DESC( ?dateTime ) LIMIT 100 ";
        
		Query query = QueryFactory.create(queryStr);
		QueryExecution qe = QueryExecutionFactory.create(query,auditModel);
		ResultSet resultSet = qe.execSelect();
		
		while (resultSet.hasNext()) {
			QuerySolution qs = (QuerySolution) resultSet.next();
			Resource ind = (Resource) qs.get("?individual");
		    Literal time = (Literal) qs.get("?dateTime");
		    ResultRow resultRow = new ResultRow();
		    String stmtStr = "";
		    try {
		    	stmtStr = "<a href=\"entity?home="+vrequest.getPortal().getPortalId()+"&amp;uri="+URLEncoder.encode(ind.getURI(),"UTF-8")+"\">"+ontModel.getIndividual(ind.getURI()).getLabel(null)+"</a>";
		    } catch (Throwable t) {
		    	t.printStackTrace();
		    	stmtStr = ind.getLocalName();
		    }
		    resultRow.stmt = stmtStr;
		    resultRow.action = "";
		    resultRow.time = time.getString();
		    resultRows.add(resultRow);
		}

            Collections.sort(resultRows);

            for (Iterator<ResultRow> rrIt = resultRows.iterator(); rrIt.hasNext();) {
                 ResultRow rr = rrIt.next();
                 results.add("XX");
                 results.add(rr.stmt);
                 results.add(rr.action);
                 results.add(rr.time);
            }


        request.setAttribute("columncount",new Integer(4));
        request.setAttribute("suppressquery","true");
        request.setAttribute("title","Individuals edited recently by "+user.getFirstName()+" "+user.getLastName());
        request.setAttribute("portalBean",portal);
        request.setAttribute("bodyJsp", Controllers.HORIZONTAL_JSP);
        request.setAttribute("home", portal.getPortalId());
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        
        try {
            rd.forward(request,response);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        } catch (Throwable t) {
        	t.printStackTrace();
        }
        
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        doGet(request,response);
    }

}
