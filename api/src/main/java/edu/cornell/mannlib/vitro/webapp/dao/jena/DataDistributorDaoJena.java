/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor;
import edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.GraphBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.DataDistributorDao;
import edu.cornell.mannlib.vitro.webapp.rdfservice.filter.LangAwareOntModel;
import edu.cornell.mannlib.vitro.webapp.rdfservice.filter.LanguageFilteringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;

import java.util.ArrayList;
import java.util.List;

import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.createSelectQueryContext;

/**
 * Access object for interacting with DataDistributor and GraphBuilder configurations in the Display model
 */
public class DataDistributorDaoJena extends JenaBaseDao implements DataDistributorDao {
    private static final String DATA_DISTRIBUTOR_URI = "java:" + DataDistributor.class.getName();
    private static final String GRAPH_BUILDER_URI = "java:" + GraphBuilder.class.getName();
    private static final String ACTION_NAME_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#actionName";
    private static final String BUILDER_NAME_URI = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#builderName";

    public DataDistributorDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }

    private static final String ALL_DISTRIBUTORS = ""
            + "PREFIX : <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#> \n"
            + "SELECT ?distributor  \n" //
            + "WHERE { \n" //
            + "   ?distributor a <java:edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor> . \n" //
            + "} \n";

    private static final String ALL_GRAPHBUILDERS = ""
            + "PREFIX : <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#> \n"
            + "SELECT ?graphbuilder  \n" //
            + "WHERE { \n" //
            + "   ?graphbuilder a <java:edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.GraphBuilder> . \n" //
            + "} \n";


    /**
     * Retrieve the URIs of all objects declared as being of type DataDistributor
     */
    public List<String> getDistributorUris() {
        OntModel displayModel = getWebappDaoFactory().getOntModelSelector().getDisplayModel();
        return createSelectQueryContext(displayModel,
                ALL_DISTRIBUTORS).execute()
                .toStringFields("distributor").flatten();
    }

    /**
     * Retrieve the URIs of all objects declared as being of type GraphBuilder
     */
    public List<String> getGraphBuilderUris() {
        OntModel displayModel = getWebappDaoFactory().getOntModelSelector().getDisplayModel();
        return createSelectQueryContext(displayModel,
                ALL_GRAPHBUILDERS).execute()
                .toStringFields("graphbuilder").flatten();
    }

    /**
     * Get all DataDistributors
     */
    public List<Entry> getAllDistributors() {
        return getEntries(getDistributorUris());
    }

    /**
     * Get all GraphBuilders
     */
    public List<Entry> getAllGraphBuilders() {
        return getEntries(getGraphBuilderUris());
    }

    /**
     * Get a Jena model for all statements with the given Uri as a subject
     */
    public Model getModelByUri(String uri) {
        Model model = ModelFactory.createDefaultModel();

        OntModel displayModel = getWebappDaoFactory().getOntModelSelector().getDisplayModel();
        StmtIterator iterator = displayModel.listStatements(displayModel.getResource(uri), null, (RDFNode) null);
        if (iterator != null) {
            model.add(iterator);
        }

        return model;
    }

    /**
     * Update the statements for a given Uri subject with those in the passed model
     */
    public boolean updateModel(String uri, Model newModel) {
        // Retrieve the existing model for this uri
        Model existingModel = getModelByUri(uri);

        // If we haven't got a model (e.g. the uri doesn't exist), just use an empty model
        if (existingModel == null) {
            existingModel = ModelFactory.createDefaultModel();
        }

        // Calculate what statements need to be removed from the display model
        Model retractions = existingModel.difference(newModel);

        // Calculate what statements need to be added to the display model
        Model additions = newModel.difference(existingModel);

        OntModel displayModel = getWebappDaoFactory().getOntModelSelector().getDisplayModel();

        displayModel.enterCriticalSection(Lock.WRITE);
        try {
            // Remove any retractions
            if (!retractions.isEmpty()) {
                displayModel.remove(retractions);
            }

            // Add any additions
            if (!additions.isEmpty()) {
                displayModel.add(additions);
            }
        } finally {
            displayModel.leaveCriticalSection();
        }

        return true;
    }

    /**
     * Determine if the Uri is declared in the permanent store (i.e. it is not a file loaded from everytime)
     */
    public boolean isPersistent(String uri) {
        // Get the display model
        OntModel displayModel = getWebappDaoFactory().getOntModelSelector().getDisplayModel();

        // If we applied a Language filter to the OntModel, there will be an extended OntModel returned
        // And we can use this to check the underlying model to see if the uri is in a submodel
        if (displayModel instanceof LangAwareOntModel) {
            return !((LangAwareOntModel)displayModel).isDefinedInSubModel(uri);
        }

        // Not language filtered, so we need to check if the uri is declared in an attached submodel
        ExtendedIterator<OntModel> subModels = displayModel.listSubModels();
        while (subModels.hasNext()) {
            OntModel subModel = subModels.next();

            // If the uri is in a submodel, then we should treat it as not being persistent
            if (subModel.contains(subModel.getResource(uri), RDF.type)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get the action or builder name associated with the uri
     */
    public String getNameFromModel(Model model) {
        String nameUri = null;
        // Determine which Property we need to access depending on whether it is a DataDistributor or GraphBuilder
        if (model.contains(null, RDF.type, model.getResource(DATA_DISTRIBUTOR_URI))) {
            nameUri = ACTION_NAME_URI;
        } else {
            nameUri = BUILDER_NAME_URI;
        }

        // Retrieve the Property
        StmtIterator typeIterator = model.listStatements(null, model.getProperty(nameUri), (RDFNode)null);
        while (typeIterator.hasNext()) {
            Statement statement = typeIterator.nextStatement();
            if (statement.getObject().isLiteral()) {
                return statement.getObject().asLiteral().getString();
            }
        }

        // Could not find a name declared for the object
        return "[unknown name]";
    }

    /**
     * Get the class for this DataDistributor or GraphBuilder
     */
    public Class getClassFromModel(Model model) {
        Class objectClass = null;

        // Iterate through the types declared in the model
        StmtIterator typeIterator = model.listStatements(null, RDF.type, (RDFNode)null);
        while (typeIterator.hasNext()) {
            Statement statement = typeIterator.nextStatement();
            if (objectClass == null) {
                // Only use the class if it is the "most significant" (not the base DataDistributor or GraphBuilder interface)
                objectClass = getClassIfMostSignificant(statement);
            }
        }

        return objectClass;
    }

    /**
     * Retrieve all entries for all of the specificed uris
     */
    private List<Entry> getEntries(List<String> uris) {
        List<Entry> entries = new ArrayList<>();

        for (String uri : uris) {
            Model model = getModelByUri(uri);
            String name = getNameFromModel(model);
            Class clazz = getClassFromModel(model);
            boolean persistent = isPersistent(uri);

            entries.add(new Entry(uri, name, clazz, persistent));
        }

        return entries;
    }

    /**
     * Retrieve the significant (not DataDistributor or GraphBuilder interface) class of the object
     */
    private Class getClassIfMostSignificant(Statement statement) {
        if (statement.getObject().isURIResource()) {
            String classUri = statement.getObject().asResource().getURI();
            if (!DATA_DISTRIBUTOR_URI.equals(classUri) && !GRAPH_BUILDER_URI.equals(classUri)) {
                try {
                    return Class.forName(classUri.substring(5));
                } catch (ClassNotFoundException e) {
                }
            }
        }

        return null;
    }
}
