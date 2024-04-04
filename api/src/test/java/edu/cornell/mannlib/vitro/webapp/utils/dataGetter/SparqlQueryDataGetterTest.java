/* $This file is distributed under the terms of the license in LICENSE$ */
package edu.cornell.mannlib.vitro.webapp.utils.dataGetter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequestStub;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SimpleOntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.VitroModelFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.RDFDefaultErrorHandler;
import org.apache.jena.vocabulary.RDF;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import stubs.javax.servlet.http.HttpServletRequestStub;

public class SparqlQueryDataGetterTest extends AbstractTestClass {

    private static final String TO_FILTER = "toFilter";
    private static final PropertyImpl HAS_ID = new PropertyImpl("test:has-id");
    private static final String VAR_PARAM = "param";
    private static final String PERSON_TYPE = "http://xmlns.com/foaf/0.1/Person";
    private static final String PREFIX = "http://vitro.mannlib.cornell.edu/ontologies/display/1.1#";
    private static final String BOB_URI = "http://example.com/p/bob";
    private static final Resource BOB = ResourceFactory.createResource(BOB_URI);
    private static final String ALICE_URI = "http://example.com/p/alice";
    private static final Resource ALICE = ResourceFactory.createResource(ALICE_URI);

    OntModel displayModel;
    String testDataGetterURI_1 = "query1data";
    WebappDaoFactory wdf;
    VitroRequestStub vreq;
    private Map<String, Object> params;
    private OntModel dataModel;

    @Before
    public void setUp() {
        // Suppress error logging.
        setLoggerLevel(RDFDefaultErrorHandler.class, Level.OFF);

        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        InputStream in = SparqlQueryDataGetterTest.class.getResourceAsStream("resources/dataGetterTest.n3");
        model.read(in, "", "N3");
        displayModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, model);

        SimpleOntModelSelector sos =
                new SimpleOntModelSelector(ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM));
        sos.setDisplayModel(displayModel);
        wdf = new WebappDaoFactoryJena(sos);
        vreq = new VitroRequestStub(new HttpServletRequestStub());
        params = new HashMap<>();
        dataModel = VitroModelFactory.createOntologyModel();
    }

    @Test
    public void testBasicGetData() throws Exception {
        SparqlQueryDataGetter sdg = getDataGetter(testDataGetterURI_1);
        dataModel.add(BOB, RDF.type, ResourceFactory.createResource(PERSON_TYPE));
        Map<String, Object> data = sdg.doQueryOnModel(sdg.queryText, dataModel);
        checkData(data);
    }

    @Test
    public void testDataGetterWithUriParam() throws Exception {
        SparqlQueryDataGetter sdg = getDataGetter("dataGetterUriParam");
        dataModel.add(BOB, RDF.type, ResourceFactory.createResource(PERSON_TYPE));
        dataModel.add(ALICE, RDF.type, ResourceFactory.createResource("http://xmlns.com/foaf/0.1/Agent"));
        vreq.setRDFService(new RDFServiceModel(dataModel));
        params.put(VAR_PARAM, PERSON_TYPE);
        Map<String, Object> data = sdg.getData(params);
        checkData(data);
    }

    @Test
    public void testDataGetterWithStringParam() throws Exception {
        SparqlQueryDataGetter sdg = getDataGetter("dataGetterStringParam");
        dataModel.add(BOB, HAS_ID, dataModel.createTypedLiteral("profile"));
        dataModel.add(ALICE, HAS_ID, dataModel.createTypedLiteral("car"));
        vreq.setRDFService(new RDFServiceModel(dataModel));
        params.put(VAR_PARAM, "profile");
        Map<String, Object> data = sdg.getData(params);
        checkData(data);
    }

    @Test
    public void testDataGetterWithIntParam() throws Exception {
        SparqlQueryDataGetter sdg = getDataGetter("dataGetterIntParam");
        dataModel.add(BOB, HAS_ID, dataModel.createTypedLiteral(1));
        dataModel.add(ALICE, HAS_ID, dataModel.createTypedLiteral(2));
        vreq.setRDFService(new RDFServiceModel(dataModel));
        params.put(VAR_PARAM, "1");
        Map<String, Object> data = sdg.getData(params);
        checkData(data);
    }

    @Test
    public void testDataGetterWithLongParam() throws Exception {
        SparqlQueryDataGetter sdg = getDataGetter("dataGetterLongParam");
        dataModel.add(BOB, HAS_ID, dataModel.createTypedLiteral(1L));
        dataModel.add(ALICE, HAS_ID, dataModel.createTypedLiteral(2L));
        vreq.setRDFService(new RDFServiceModel(dataModel));
        params.put(VAR_PARAM, "1");
        Map<String, Object> data = sdg.getData(params);
        checkData(data);
    }

    @Test
    public void testDataGetterWithFloatParam() throws Exception {
        SparqlQueryDataGetter sdg = getDataGetter("dataGetterFloatParam");
        Float value = 1.1f;
        dataModel.add(BOB, HAS_ID, dataModel.createTypedLiteral(value));
        dataModel.add(ALICE, HAS_ID, dataModel.createTypedLiteral(1.2f));
        vreq.setRDFService(new RDFServiceModel(dataModel));
        params.put(VAR_PARAM, value.toString());
        Map<String, Object> data = sdg.getData(params);
        checkData(data);
    }

    @Test
    public void testDataGetterWithDoubleParam() throws Exception {
        SparqlQueryDataGetter sdg = getDataGetter("dataGetterDoubleParam");
        Double value = 1.1d;
        dataModel.add(BOB, HAS_ID, dataModel.createTypedLiteral(value));
        dataModel.add(ALICE, HAS_ID, dataModel.createTypedLiteral(1.2d));
        vreq.setRDFService(new RDFServiceModel(dataModel));
        params.put(VAR_PARAM, value.toString());
        Map<String, Object> data = sdg.getData(params);
        checkData(data);
    }

    @Test
    public void testDataGetterWithBooleanParam() throws Exception {
        SparqlQueryDataGetter sdg = getDataGetter("dataGetterBooleanParam");
        Boolean value = true;
        dataModel.add(BOB, HAS_ID, dataModel.createTypedLiteral(value));
        dataModel.add(ALICE, HAS_ID, dataModel.createTypedLiteral(!value));
        vreq.setRDFService(new RDFServiceModel(dataModel));
        params.put(VAR_PARAM, value.toString());
        Map<String, Object> data = sdg.getData(params);
        checkData(data);
    }

    @Test
    public void testFilterUnavailableParameters() throws Exception {
        SparqlQueryDataGetter sdg = getDataGetter("dataGetterStringParam");
        Map<String, String> unfilteredParameters = new HashMap<String, String>();
        unfilteredParameters.put(VAR_PARAM, "");
        unfilteredParameters.put(TO_FILTER, "");
        Map<String, String> filteredParameters = sdg.filterUnavailableParameters(unfilteredParameters);
        assertFalse(filteredParameters.containsKey(TO_FILTER));
        assertTrue(filteredParameters.containsKey(VAR_PARAM));
    }

    private SparqlQueryDataGetter getDataGetter(String dataGetterName)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, InvocationTargetException {
        DataGetter dg = DataGetterUtils.dataGetterForURI(vreq, displayModel, PREFIX + dataGetterName);
        Assert.assertNotNull(dg);
        Assert.assertTrue("DataGetter should be of type " + SparqlQueryDataGetter.class.getName(),
                dg instanceof SparqlQueryDataGetter);
        SparqlQueryDataGetter sdg = (SparqlQueryDataGetter) dg;
        return sdg;
    }

    private void checkData(Map<String, Object> data) {
        Assert.assertNotNull(data);
        Assert.assertTrue("should contain key people", data.containsKey("people"));
        Object obj = data.get("people");
        Assert.assertTrue("people should be a List, it is " + obj.getClass().getName(), obj instanceof List);
        @SuppressWarnings("rawtypes")
        List people = (List) obj;
        Assert.assertEquals(1, people.size());
        @SuppressWarnings("unchecked")
        Map<String, String> first = (Map<String, String>) people.get(0);
        Assert.assertEquals(BOB_URI, first.get("uri"));
    }
}
