/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.sparql;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mindswap.pellet.jena.vocabulary.SWRL;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;

import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseMiscellaneousPages;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * This servlet gets all the prefix for initizing the sparql query builder.
 * 
 * @author yuysun
 */

public class GetAllPrefix extends BaseEditController {

	private static final Log log = LogFactory.getLog(GetAllPrefix.class);

	/**
	 * The doGet method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!isAuthorizedToDisplayPage(request, response, new Actions(new UseMiscellaneousPages()))) {
        	return;
		}

		VitroRequest vreq = new VitroRequest(request);

		// EditProcessObject epo = super.createEpo(request);
		OntologyDao daoObj = vreq.getFullWebappDaoFactory().getOntologyDao();
		List ontologiesObj = daoObj.getAllOntologies();
		ArrayList prefixList = new ArrayList();

		response.setContentType("text/xml");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		String respo = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		respo += "<options>";
		if (ontologiesObj != null && ontologiesObj.size() > 0) {

			Iterator ontItr = ontologiesObj.iterator();
			while (ontItr.hasNext()) {
				Ontology ont = (Ontology) ontItr.next();
				if (ont.getPrefix() != null) {
					respo += makeOption(ont.getPrefix(), ont.getURI());
				}
			}

		}
		;
		respo += makeOption("owl", OWL.NAMESPACE);
		respo += makeOption("rdf", RDF.NAMESPACE);
		respo += makeOption("rdfs", RDFS.getURI());
		respo += makeOption("swrl", "http://www.w3.org/2003/11/swrl#");
		respo += makeOption("swrlb", "http://www.w3.org/2003/11/swrlb#");
		respo += makeOption("xsd", XSD.getURI());
		respo += makeOption("vitro", VitroVocabulary.vitroURI);
		respo += "</options>";
		out.println(respo);
		out.flush();
		out.close();
	}
	
	/**
	 * Makes the markup for a prefix option
	 * @param prefix
	 * @param URI
	 * @return option string
	 */
	private String makeOption(String prefix, String URI) {
	    return "<option>" + "<key>" + prefix + "</key>"
                + "<value>" + URI + "</value>"
                + "</option>";
	}
	

	/**
	 * The doPost method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to
	 * post.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doGet(request, response);
	}

}
