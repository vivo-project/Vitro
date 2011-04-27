/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.sparql;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseMiscellaneousPages;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;

/**
 * This servlet gets all the properties for a given subject.
 * 
 * @param vClassURI
 * @author yuysun
 */

public class GetClazzAllProperties extends BaseEditController {

	private static final Log log = LogFactory.getLog(GetClazzAllProperties.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!isAuthorizedToDisplayPage(request, response, new Actions(new UseMiscellaneousPages()))) {
        	return;
		}

		VitroRequest vreq = new VitroRequest(request);

		String vClassURI = vreq.getParameter("vClassURI");
		if (vClassURI == null || vClassURI.trim().equals("")) {
			return;
		}

		Map<String, String> hm = new HashMap();

		// Get Data Properties
		// Add rdfs:label to the list
		hm.put("label", "http://www.w3.org/2000/01/rdf-schema#label0");
		/*
		 * respo += "<option>" + "<key>" + "label" + "</key>" + "<value>" +
		 * "http://www.w3.org/2000/01/rdf-schema#label" + "</value>" +
		 * "<type>0</type>" + "</option>";
		 */
		DataPropertyDao ddao = vreq.getFullWebappDaoFactory()
				.getDataPropertyDao();

		Collection<DataProperty> dataProps = ddao
				.getDataPropertiesForVClass(vClassURI);
		Iterator<DataProperty> dataPropIt = dataProps.iterator();
		HashSet<String> dpropURIs = new HashSet<String>();
		while (dataPropIt.hasNext()) {
			DataProperty dp = dataPropIt.next();
			if (!(dpropURIs.contains(dp.getURI()))) {
				dpropURIs.add(dp.getURI());
				DataProperty dprop = (DataProperty) ddao
						.getDataPropertyByURI(dp.getURI());
				if (dprop != null) {
					if (dprop.getLocalName() != null
							|| !dprop.getLocalName().equals("")) {
						hm.put(dprop.getLocalName(), dprop.getURI() + "0");
					}
					/*
					 * respo += "<option>" + "<key>" + dprop.getLocalName() +
					 * "</key>" + "<value>" + dprop.getURI() + "</value>" +
					 * "<type>0</type>" + "</option>";
					 */
				}
			}
		}

		// Get Object Properties

		ObjectPropertyDao odao = vreq.getFullWebappDaoFactory()
				.getObjectPropertyDao();
		PropertyInstanceDao piDao = vreq.getFullWebappDaoFactory()
				.getPropertyInstanceDao();
		VClassDao vcDao = vreq.getFullWebappDaoFactory().getVClassDao();

		// incomplete list of classes to check, but better than before
		List<String> superclassURIs = vcDao.getAllSuperClassURIs(vClassURI);
		superclassURIs.add(vClassURI);
		superclassURIs.addAll(vcDao.getEquivalentClassURIs(vClassURI));

		Map<String, PropertyInstance> propInstMap = new HashMap<String, PropertyInstance>();
		for (String classURI : superclassURIs) {
			Collection<PropertyInstance> propInsts = piDao
					.getAllPropInstByVClass(classURI);
			try {
				for (PropertyInstance propInst : propInsts) {
					propInstMap.put(propInst.getPropertyURI(), propInst);
				}
			} catch (NullPointerException ex) {
				continue;
			}
		}
		List<PropertyInstance> propInsts = new ArrayList<PropertyInstance>();
		propInsts.addAll(propInstMap.values());
		Collections.sort(propInsts);

		Iterator propInstIt = propInsts.iterator();
		HashSet opropURIs = new HashSet();
		while (propInstIt.hasNext()) {
			PropertyInstance pi = (PropertyInstance) propInstIt.next();
			if (!(opropURIs.contains(pi.getPropertyURI()))) {
				opropURIs.add(pi.getPropertyURI());
				ObjectProperty oprop = (ObjectProperty) odao
						.getObjectPropertyByURI(pi.getPropertyURI());
				if (oprop != null) {
					/*
					 * respo += "<option>" + "<key>" + oprop.getLocalName() +
					 * "</key>" + "<value>" + oprop.getURI() + "</value>" +
					 * "<type>1</type>" + "</option>";
					 */
					if (oprop.getLocalName() != null
							|| !oprop.getLocalName().equals("")) {
						hm.put(oprop.getLocalName(), oprop.getURI() + "1");
					}

				}
			}
		}
		String respo = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		respo += "<options>";
		Object[] keys = hm.keySet().toArray();
		Arrays.sort(keys);
		for (int i = 0; i < keys.length; i++) {
			String key = (String) keys[i];
			String value = hm.get(key);

			respo += "<option>" + "<key>" + key + "</key>" + "<value>"
					+ value.substring(0, value.length() - 1) + "</value>"
					+ "<type>" + value.charAt(value.length() - 1) + "</type>"
					+ "</option>";
		}
		respo += "</options>";
		response.setContentType("text/xml");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		out.println(respo);
		out.flush();
		out.close();
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
