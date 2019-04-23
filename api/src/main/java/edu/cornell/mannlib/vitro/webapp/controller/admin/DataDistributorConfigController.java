/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.admin;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor;
import edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.GraphBuilder;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.DataDistributorDao;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.InstanceWrapper;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.PropertyType;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.WrappedInstance;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.reflections.Reflections;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet(name = "DataDistConf", urlPatterns = {"/admin/datadistributor"} )
public class DataDistributorConfigController extends FreemarkerHttpServlet {
    private static final Log log = LogFactory.getLog(DataDistributorConfigController.class);

    private static final String SETUP_URI_BASE = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#";

    private static final String LIST_TEMPLATE_NAME = "admin-dataDistributor.ftl";
    private static final String EDIT_TEMPLATE_NAME = "admin-dataDistributor-edit-DataDistributor.ftl";

    private static final String SUBMIT_URL_BASE =  UrlBuilder.getUrl("admin/datadistributor");
    private static final String REDIRECT_PATH = "/admin/datadistributor";

    private static List<Class<? extends DataDistributor>> distributorTypes = new ArrayList<>();
    private static List<Class<? extends GraphBuilder>> graphbuilderTypes = new ArrayList<>();

    // Static initialiser uses reflection to find out what Java classes are present, as this only needs to be done
    // when the application is started
    static {
        // Find all classes that implement the DataDistributor interface
        Reflections ddReflections = new Reflections("org.vivoweb", "edu.cornell");
        for (Class<? extends DataDistributor> distributor : ddReflections.getSubTypesOf(DataDistributor.class)) {
            // As long as it is not an abstract class, add it to the list
            if (!Modifier.isAbstract(distributor.getModifiers())) {
                distributorTypes.add(distributor);
            }
        }
        distributorTypes.sort(new Comparator<Class<? extends DataDistributor>>() {
            @Override
            public int compare(Class<? extends DataDistributor> o1, Class<? extends DataDistributor> o2) {
                return o1.getSimpleName().compareToIgnoreCase(o2.getSimpleName());
            }
        });

        // Find all classes that implement the GraphBuilder interface
        Reflections gbReflections = new Reflections("org.vivoweb", "edu.cornell");
        for (Class<? extends GraphBuilder> builder : gbReflections.getSubTypesOf(GraphBuilder.class)) {
            // As long as it is not an abstract class, add it to the list
            if (!Modifier.isAbstract(builder.getModifiers())) {
                graphbuilderTypes.add(builder);
            }
        }
        graphbuilderTypes.sort(new Comparator<Class<? extends GraphBuilder>>() {
            @Override
            public int compare(Class<? extends GraphBuilder> o1, Class<? extends GraphBuilder> o2) {
                return o1.getSimpleName().compareToIgnoreCase(o2.getSimpleName());
            }
        });
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!isAuthorizedToDisplayPage(request, response, SimplePermission.MANAGE_DATA_DISTRIBUTORS.ACTION)) {
            return;
        }

        response.addHeader("X-XSS-Protection", "0");

        super.doGet(request, response);
    }

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) throws Exception {
        ResponseValues response = null;

        // Determine whether we are adding, editing or deleting an object
        if (!StringUtils.isEmpty(vreq.getParameter("addType"))) {
            response = processAdd(vreq);
        } else if (!StringUtils.isEmpty(vreq.getParameter("editUri"))) {
            response = processEdit(vreq);
        } else if (!StringUtils.isEmpty(vreq.getParameter("deleteUri"))) {
            response = processDelete(vreq);
        }

        // If we haven't determined a response, show a list of distributors and graphbuilders
        return response != null ? response : processList(vreq);
    }

    private ResponseValues processList(VitroRequest vreq) {
        Map<String, Object> bodyMap = new HashMap<>();

        bodyMap.put("title", I18n.text(vreq, "page_datadistributor_config"));
        bodyMap.put("submitUrlBase", SUBMIT_URL_BASE);

        DataDistributorDao ddDao = vreq.getWebappDaoFactory().getDataDistributorDao();

        bodyMap.put("distributors", ddDao.getAllDistributors());
        bodyMap.put("distributorTypes", distributorTypes);
        bodyMap.put("distributorTypeBase", DataDistributor.class);

        bodyMap.put("graphbuilders", ddDao.getAllGraphBuilders());
        bodyMap.put("graphbuilderTypes", graphbuilderTypes);
        bodyMap.put("graphbuilderTypeBase", GraphBuilder.class);

        return new TemplateResponseValues(LIST_TEMPLATE_NAME, bodyMap);
    }

    private ResponseValues processDelete(VitroRequest vreq) {
        String uri = vreq.getParameter("deleteUri");

        DataDistributorDao ddDao = vreq.getWebappDaoFactory().getDataDistributorDao();

        // A delete is simply an update with an empty model
        ddDao.updateModel(uri, ModelFactory.createDefaultModel());

        return new RedirectResponseValues(REDIRECT_PATH);
    }

    private ResponseValues processEdit(VitroRequest vreq) {
        String uri = vreq.getParameter("editUri");

        DataDistributorDao ddDao = vreq.getWebappDaoFactory().getDataDistributorDao();

        // Retrieve the model from the triple store
        Model model = ddDao.getModelByUri(uri);

        if (model != null && !model.isEmpty()) {
            // Get the class of the object
            Class objectClass = ddDao.getClassFromModel(model);

            // If we are processing a submitted form
            if (!StringUtils.isEmpty(vreq.getParameter("submitted"))) {
                // Generate a model from the submitted form
                Model requestModel = getModelFromRequest(vreq, uri, objectClass);

                // Update the model in the triple store with the submitted form
                ddDao.updateModel(uri, requestModel);

                // Redirect to the list
                return new RedirectResponseValues(REDIRECT_PATH);
            }

            Map<String, Object> bodyMap = new HashMap<>();
            Map<String, Object> fieldMap = new HashMap<>();

            // Convert the statements of the model into a field map for the UI
            StmtIterator iterator = model.listStatements();
            while (iterator.hasNext()) {
                Statement statement = iterator.nextStatement();
                addFieldToMap(fieldMap, statement.getPredicate(), statement.getObject());
            }

            // Pass the field map to the template
            bodyMap.put("fields", fieldMap);
            bodyMap.put("editUri", uri);

            // If the uri is not for a "persistent" object (it is in a temporary submodel)
            if (!ddDao.isPersistent(uri)) {
                // Tell the UI to display in readonly form
                bodyMap.put("readOnly", true);
            }

            // Create the response
            return makeResponseValues(vreq, objectClass, bodyMap);
        }

        return null;
    }

    private ResponseValues processAdd(VitroRequest vreq) {
        // Get the class from the parameter
        Class objectClass = findClass(vreq.getParameter("addType"));

        if (objectClass != null) {
            // If we are processing a submitted form
            if (!StringUtils.isEmpty(vreq.getParameter("submitted"))) {
                DataDistributorDao ddDao = vreq.getWebappDaoFactory().getDataDistributorDao();

                // Generate a unique ID for the object
                String uri = SETUP_URI_BASE + UUID.randomUUID().toString();

                // Generate a model from the submitted form
                Model requestModel = getModelFromRequest(vreq, uri, objectClass);

                // Update the model in the triple store with the submitted form
                ddDao.updateModel(uri, requestModel);

                // Redirect to the list
                return new RedirectResponseValues(REDIRECT_PATH);
            }


            Map<String, Object> bodyMap = new HashMap<>();
            Map<String, Object> fieldMap = new HashMap<>();
            bodyMap.put("addType", vreq.getParameter("addType"));

            // Adding a new object, so pass an empty field map to the UI for a blank form
            bodyMap.put("fields", fieldMap);

            // Create the response
            return makeResponseValues(vreq, objectClass, bodyMap);
        }

        return null;

    }

    /**
     * Field map is of the form:
     *
     * key = property uri
     * value = list of values
     */
    private void addFieldToMap(Map<String, Object> map, Property property, RDFNode object) {
        String propUri = property.getURI();

        List<String> values;
        if (map.containsKey(propUri)) {
            values = (List<String>)map.get(propUri);
        } else {
            values = new ArrayList<>();
            map.put(propUri, values);
        }

        if (object.isLiteral()) {
            values.add(object.asLiteral().getString());
        } else {
            values.add(object.asResource().getURI());
        }
    }

    private ResponseValues makeResponseValues(VitroRequest vreq, Class objectClass, Map<String, Object> bodyMap) {
        bodyMap.put("properties", getPropertyMethodsFor(objectClass));
        bodyMap.put("objectClass", objectClass);

        DataDistributorDao ddDao = vreq.getWebappDaoFactory().getDataDistributorDao();

        // Pass existing distributors to the UI for any drop downs to select a child distributor
        bodyMap.put("datadistributors", ddDao.getAllDistributors());

        // Pass existing graphbuilders to the UI for any drop downs to select a child graphbuilder
        bodyMap.put("graphbuilders", ddDao.getAllGraphBuilders());

        bodyMap.put("submitUrlBase", SUBMIT_URL_BASE);

        return new TemplateResponseValues(EDIT_TEMPLATE_NAME, bodyMap);
    }

    private Class findClass(String name) {
        Class objectClass = null;

        if (!StringUtils.isEmpty(name)) {
            if (name.contains(".")) {
                try {
                    objectClass = Class.forName(name);
                } catch (ClassCastException | ClassNotFoundException ce) {
                }
            }
        }

        if (objectClass != null) {
            if (DataDistributor.class.isAssignableFrom(objectClass) || GraphBuilder.class.isAssignableFrom(objectClass)) {
                return objectClass;
            }
        }

        return null;
    }

    /**
     * Convert the submitted values into a Jena model
     */
    private Model getModelFromRequest(VitroRequest vreq, String subjectUri, Class objectClass) {
        Model model = ModelFactory.createDefaultModel();

        // The subject uri passed will be the subject of all statements in this model
        Resource subject = model.createResource(subjectUri);

        // Add the interface and object class types to the model
        model.add(subject, RDF.type, model.getResource("java:" + objectClass.getName()));
        if (DataDistributor.class.isAssignableFrom(objectClass)) {
            model.add(subject, RDF.type, model.getResource("java:" + DataDistributor.class.getName()));
        } else {
            model.add(subject, RDF.type, model.getResource("java:" + GraphBuilder.class.getName()));
        }

        // Get all the property methods for this object class
        Collection<PropertyType.PropertyMethod> propertyMethods = getPropertyMethodsFor(objectClass);
        for (PropertyType.PropertyMethod method : propertyMethods) {
            // Get any values for this property URI from the submitted parameters
            String[] values = vreq.getParameterValues(method.getPropertyUri());

            // If we have values
            if (values != null) {
                for (String value : values) {
                    if (value != null) {
                        value = value.trim();
                        if (!StringUtils.isEmpty(value)) {
                            // If the value is a string
                            if (String.class.equals(method.getParameterType())) {
                                // Add a statement for this property with the value as a literal object
                                model.add(subject, model.getProperty(method.getPropertyUri()), value);
                            } else {
                                // Not a String, so add a statement for this property with the value as a resource
                                model.add(subject, model.getProperty(method.getPropertyUri()), model.getResource(value));
                            }
                        }
                    }
                }
            }
        }

        return model;
    }

    private final Map<Class, Collection<PropertyType.PropertyMethod>> propertyMethodsMap = new HashMap<>();

    private Collection<PropertyType.PropertyMethod> getPropertyMethodsFor(Class objectClass) {
        if (!propertyMethodsMap.containsKey(objectClass)) {
            addPropetyMethodsFor(objectClass);
        }

        return propertyMethodsMap.get(objectClass);
    }

    private synchronized void addPropetyMethodsFor(Class objectClass) {
        if (!propertyMethodsMap.containsKey(objectClass)) {
            try {
                WrappedInstance<DataDistributor> wrapped = InstanceWrapper.wrap(objectClass);
                Map<String, PropertyType.PropertyMethod> propertyMethods = wrapped.getPropertyMethods();
                propertyMethodsMap.put(objectClass, propertyMethods.values());
            } catch (InstanceWrapper.InstanceWrapperException e) {
            }
        }
    }
}
