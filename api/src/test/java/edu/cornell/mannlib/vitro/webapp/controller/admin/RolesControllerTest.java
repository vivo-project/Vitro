package edu.cornell.mannlib.vitro.webapp.controller.admin;

import static edu.cornell.mannlib.vitro.webapp.controller.admin.RolesController.ACTION_PARAM;
import static edu.cornell.mannlib.vitro.webapp.controller.admin.RolesController.Action.ADD;
import static edu.cornell.mannlib.vitro.webapp.controller.admin.RolesController.Action.LIST;
import static edu.cornell.mannlib.vitro.webapp.controller.admin.RolesController.LABEL_PARAM;
import static edu.cornell.mannlib.vitro.webapp.controller.admin.RolesController.URI_PARAM;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.LABEL;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.PERMISSIONSET;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.RDF_TYPE;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.USER_ACCOUNTS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueSetRegistry;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyLoader;
import edu.cornell.mannlib.vitro.webapp.beans.PermissionSet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.admin.RolesController.Action;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.VitroModelFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class RolesControllerTest {

    private MockedStatic<ModelAccess> modelAccess;
    private MockedStatic<I18n> i18n;

    private OntModel ua;
    private OntModel tmpModel;
    private RequestModelAccess rma;
    private ContextModelAccess cma;
    private UserAccountsDao uadao;
    private WebappDaoFactory wadf;
    private VitroRequest request;
    private RolesController controller;
    private Collection<PermissionSet> expectedRoles;
    private Model accessControl;

    @Before
    public void setUp() {
        modelAccess = Mockito.mockStatic(ModelAccess.class);
        i18n = Mockito.mockStatic(I18n.class);

        accessControl = VitroModelFactory.createOntologyModel();
        Dataset configurationDataSet = DatasetFactory.createTxnMem();
        configurationDataSet.addNamedModel(ModelNames.ACCESS_CONTROL, accessControl);
        RDFServiceModel rdfService = new RDFServiceModel(configurationDataSet);
        AttributeValueSetRegistry.getInstance().clear();
        PolicyLoader.initialize(rdfService);

        ua = VitroModelFactory.createOntologyModel();
        controller = new RolesController();
        cma = Mockito.mock(ContextModelAccess.class);
        request = Mockito.mock(VitroRequest.class);
        wadf = Mockito.mock(WebappDaoFactory.class);
        uadao = Mockito.mock(UserAccountsDao.class);
        rma = Mockito.mock(RequestModelAccess.class);
        expectedRoles = getRoleSet();
        when(uadao.getAllPermissionSets()).thenReturn(expectedRoles);
        when(wadf.getUserAccountsDao()).thenReturn(uadao);
        when(rma.getWebappDaoFactory()).thenReturn(wadf);
        when(rma.getOntModel(USER_ACCOUNTS)).thenReturn(ua);
        when(cma.getOntModel(USER_ACCOUNTS)).thenReturn(ua);
        modelAccess.when(() -> ModelAccess.on(request)).thenReturn(rma);
        modelAccess.when(() -> ModelAccess.getInstance()).thenReturn(cma);
        i18n.when(() -> I18n.text( any(), anyString(), any())).thenReturn("translation");
        tmpModel = VitroModelFactory.createOntologyModel();
        tmpModel.add(ua);
    }

    @After
    public void tearDown() {
        modelAccess.close();
        i18n.close();
    }

    @Test
    public void testEmptyRequest() throws Exception {
        ResponseValues response = controller.processRequest(request);
        assertEquals(expectedRoles, response.getMap().get("roles"));
        assertTrue(tmpModel.isIsomorphicWith(ua));
    }

    @Test
    public void testListRequest() throws Exception {
        when(request.getParameter(ACTION_PARAM)).thenReturn(LIST.toString());
        ResponseValues response = controller.processRequest(request);
        assertEquals(expectedRoles, response.getMap().get("roles"));
        assertTrue(tmpModel.isIsomorphicWith(ua));
    }

    @Test
    public void testAddRequestWithoutLabel() throws Exception {
        when(request.getParameter(ACTION_PARAM)).thenReturn(ADD.toString());
        ResponseValues response = controller.processRequest(request);
        assertEquals(expectedRoles, response.getMap().get("roles"));
        assertTrue(tmpModel.isIsomorphicWith(ua));
    }

    @Test
    public void testAddRequestWithLabel() throws Exception {
        when(request.getParameter(ACTION_PARAM)).thenReturn(ADD.toString());
        when(request.getParameter(LABEL_PARAM)).thenReturn("SomeNewRoleLabel");

        ResponseValues response = controller.processRequest(request);
        assertEquals(expectedRoles, response.getMap().get("roles"));
        assertFalse(tmpModel.isIsomorphicWith(ua));
        ua.remove(tmpModel);
        assertEquals(2, ua.size());
        ResIterator it = ua.listSubjects();
        Resource resource = it.next();
        assertEquals("SomeNewRoleLabel", resource.getProperty(new PropertyImpl(LABEL)).getLiteral().toString());
        assertEquals(PERMISSIONSET, resource.getProperty(new PropertyImpl(RDF_TYPE)).getResource().toString());
        it.close();
    }

    @Test
    public void testAddRequestWithLocalizedLabel() throws Exception {
        when(request.getParameter(ACTION_PARAM)).thenReturn(ADD.toString());
        when(request.getLocale()).thenReturn(Locale.US);
        when(request.getParameter(LABEL_PARAM)).thenReturn("SomeNewRoleLabel");

        ResponseValues response = controller.processRequest(request);
        assertEquals(expectedRoles, response.getMap().get("roles"));
        assertFalse(tmpModel.isIsomorphicWith(ua));
        ua.remove(tmpModel);
        assertEquals(2, ua.size());
        ResIterator it = ua.listSubjects();
        Resource resource = it.next();
        assertEquals("SomeNewRoleLabel@en-US", resource.getProperty(new PropertyImpl(LABEL)).getLiteral().toString());
        assertEquals(PERMISSIONSET, resource.getProperty(new PropertyImpl(RDF_TYPE)).getResource().toString());
        it.close();
    }

    @Test
    public void testEditRequestWithLabel() throws Exception {
        when(request.getParameter(ACTION_PARAM)).thenReturn(Action.EDIT.toString());
        when(request.getParameter(URI_PARAM)).thenReturn("test:curator");
        when(request.getParameter(LABEL_PARAM)).thenReturn("Manager");

        ResponseValues response = controller.processRequest(request);
        assertEquals(expectedRoles, response.getMap().get("roles"));
        assertFalse(tmpModel.isIsomorphicWith(ua));
        ua.remove(tmpModel);
        assertEquals(1, ua.size());
        ResIterator it = ua.listSubjects();
        Resource resource = it.next();
        assertEquals("Manager", resource.getProperty(new PropertyImpl(LABEL)).getLiteral().toString());
        it.close();
    }

    @Test
    public void testRemoveRequestWithLabel() throws Exception {
        when(request.getParameter(ACTION_PARAM)).thenReturn(Action.REMOVE.toString());
        when(request.getParameter(URI_PARAM)).thenReturn("test:curator");

        ResponseValues response = controller.processRequest(request);
        assertEquals(expectedRoles, response.getMap().get("roles"));
        assertFalse(tmpModel.isIsomorphicWith(ua));
        tmpModel.remove(ua);
        assertEquals(2, tmpModel.size());
        ResIterator it = tmpModel.listSubjects();
        Resource resource = it.next();
        assertEquals("curator", resource.getProperty(new PropertyImpl(LABEL)).getLiteral().toString());
        assertEquals(PERMISSIONSET, resource.getProperty(new PropertyImpl(RDF_TYPE)).getResource().toString());
    }

    private Collection<PermissionSet> getRoleSet() {
        ArrayList<PermissionSet> list = new ArrayList<>();
        list.add(createPermissionSet("admin"));
        list.add(createPermissionSet("curator"));
        list.add(createPermissionSet("editor"));
        list.add(createPermissionSet("self-editor"));
        list.add(createPermissionSet("public"));
        return list;
    }

    private PermissionSet createPermissionSet(String name) {
        PermissionSet ps = new PermissionSet();
        String uri = "test:" + name;
        ps.setUri(uri);
        ps.setLabel(name);
        ua.add(ua.createResource(uri), new PropertyImpl(RDF_TYPE) ,ua.createResource(PERMISSIONSET));
        ua.add(ua.createResource(uri), new PropertyImpl(LABEL) ,ua.createLiteral(name));
        return ps;
    }
}
