/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import stubs.javax.servlet.http.HttpServletRequestStub;

import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditSubmission;

public class EditSubmissionTest extends AbstractTestClass {
	HttpServletRequestStub request;
	EditConfiguration editConfig;

	@Before
	public void createEditConfig() throws IOException {
		InputStream is = this.getClass().getResourceAsStream(
				"testEditConfig.json");
		editConfig = new EditConfiguration(readAll(is));
	}

	@Before
	public void createRequest() {
		request = new HttpServletRequestStub();

		request.addParameter("yearfield222", "2001");
		request.addParameter("monthfield222", "10");
		request.addParameter("dayfield222", "13");
		request.addParameter("hourfield222", "11");
		request.addParameter("minutefield222", "00");

		request.addParameter("talkName", "this is the name of a talk");

		request.addParameter("room", "http://someBogusUri/#individual2323");
		request.addParameter("editKey", "fakeEditKey");
	}

	public void testSetup() {
		Assert.assertNotNull("EditConfiguration is null", editConfig);
		Assert.assertNotNull("request must not be null", request);
	}

	@Test
	public void testDateTimeParameter() {
		EditSubmission editSub = new EditSubmission(request.getParameterMap(),
				editConfig);
		Literal field222 = editSub.getLiteralsFromForm().get("field222");
		Assert.assertNotNull(field222);
	}

	@Test
	public void testCanHandleMissingParameter() {
		/*
		 * test if the EditSubmission can be passed a request which is missing a
		 * parameter. This will be the case when a checkbox type input is not
		 * selected.
		 */
		request.removeParameter("talkName");
		request.removeParameter("room");

		// just trying to make this without an exception
		EditSubmission editSub = new EditSubmission(request.getParameterMap(),
				editConfig);
		Assert.assertNotNull(editSub);
	}
}
