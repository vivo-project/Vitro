package edu.cornell.mannlib.vitro.webapp.rdfservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.listeners.StatementListener;
import org.apache.jena.rdf.model.ModelChangedListener;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dao.jena.event.BulkUpdateEvent;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;

public class RDFServiceNotifictaionTest {

	private String TEST_TRIPLE = "<test:uri1> <test:pred1> \"test value\" .";
	private List<ModelChange> modelChanges = null;

	@Before
	public void reset() {
		modelChanges = new ArrayList<>();
	}

	@Test
	public void testModelChangeUserIdNotificationAdd() throws RDFServiceException {
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		RDFServiceModel rdfServiceModel = new RDFServiceModel(model);
		String editorUri = "test:user-id";
		rdfServiceModel.registerListener(new TestListener());
		ChangeSet cs = createChangeSet(rdfServiceModel, editorUri, TEST_TRIPLE, null);
		rdfServiceModel.changeSetUpdate(cs);
		assertTrue(modelChanges.size() > 0);
		assertTrue(editorUri.equals(modelChanges.get(0).getUserId()));
	}

	@Test
	public void testModelChangeUserIdNotificationRetract() throws RDFServiceException {
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		RDFServiceModel rdfServiceModel = new RDFServiceModel(model);
		String editorUri = "test:user-id";
		rdfServiceModel.registerListener(new TestListener());
		ChangeSet cs = createChangeSet(rdfServiceModel, editorUri, null, TEST_TRIPLE);
		rdfServiceModel.changeSetUpdate(cs);
		assertTrue(modelChanges.size() > 0);
		assertTrue(editorUri.equals(modelChanges.get(0).getUserId()));
	}
	
	@Test
	public void testMultipleNotifications() throws RDFServiceException {
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		RDFServiceModel rdfServiceModel = new RDFServiceModel(model);
		String editorUri = "test:user-id";
		int n = 5;
		for (int i = 0; i < n;i++) {
			rdfServiceModel.registerListener(new TestListener());	
		}
		ChangeSet cs = createChangeSet(rdfServiceModel, editorUri, null, TEST_TRIPLE);
		rdfServiceModel.changeSetUpdate(cs);
		assertTrue(modelChanges.size() == n);
	}
	
	@Test
	public void testMultipleNotificationsReceived() throws RDFServiceException {
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		RDFServiceModel rdfServiceModel = new RDFServiceModel(model);
		String editorUri = "test:user-id";
		int n = 5;
		for (int i = 0; i < n;i++) {
			rdfServiceModel.registerListener(new TestListener(TEST_TRIPLE));	
		}
		ChangeSet cs = createChangeSet(rdfServiceModel, editorUri, null, TEST_TRIPLE);
		rdfServiceModel.changeSetUpdate(cs);
	}

	private ChangeSet createChangeSet(RDFServiceModel rdfServiceModel, String editorUri, String additions,
			String retractions) {
		ChangeSet cs = rdfServiceModel.manufactureChangeSet();
		cs.addPreChangeEvent(new BulkUpdateEvent(null, true));
		cs.addPostChangeEvent(new BulkUpdateEvent(null, false));
		if (additions != null) {
			InputStream additionsInputStream = new ByteArrayInputStream(additions.getBytes(StandardCharsets.UTF_8));
			cs.addAddition(additionsInputStream, RDFServiceUtils.getSerializationFormatFromJenaString("N3"), null,
					editorUri);
		}
		if (retractions != null) {
			InputStream retractionsInputStream = new ByteArrayInputStream(retractions.getBytes(StandardCharsets.UTF_8));
			cs.addRemoval(retractionsInputStream, RDFServiceUtils.getSerializationFormatFromJenaString("N3"), null,
					editorUri);
		}
		return cs;
	}

	private class TestListener extends StatementListener implements ModelChangedListener, ChangeListener {

		private String serializedChange;

		public TestListener() {}

		public TestListener(String serializedChange) {
			this.serializedChange = serializedChange;
		}
		@Override
		public void notifyModelChange(ModelChange modelChange) {
			if (serializedChange != null) {
				String receivedTriple = null;
				try {
					receivedTriple = IOUtils.toString(modelChange.getSerializedModel(), StandardCharsets.UTF_8);
				} catch (IOException e) {
					e.printStackTrace();
				}
				assertEquals(serializedChange, receivedTriple);
			} else {
				modelChanges.add(modelChange);	
			}
		}

		@Override
		public void notifyEvent(String graphURI, Object event) {}
	}
}
