/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.audit.storage;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.audit.AuditChangeSet;
import edu.cornell.mannlib.vitro.webapp.audit.AuditResults;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * AuditDAO using Jena triple store as the backend.
 * This is a base class for a either TDB or SDB implementations
 */
public abstract class AuditDAOJena implements AuditDAO {
    // The graph that records the metadata for changesets
    private final static String auditGraph = "http://vivoweb.org/audit";

    // The base URI for creating change objects
    private final static String changesBaseURI = "http://vivoweb.org/audit/changes/";

    // The current request
    private HttpServletRequest request = null;

    /**
     * Get a dataset from the Jena store that be written to / read from
     *
     * @return
     */
    protected abstract Dataset getDataset();

    /**
     * Create an AuditDAO for the current request
     * @param req
     */
    public AuditDAOJena(ServletRequest req) {
        if (req instanceof HttpServletRequest) {
            request = (HttpServletRequest)req;
        }
    }

    @Override
    public void write(AuditChangeSet changes) {
        // Only record changes if there is a current request
        if (request == null) {
            return;
        }

        // Ensure we have something to write
        if (changes.getAddedDataset().asDatasetGraph().isEmpty() &&
            changes.getRemovedDataset().asDatasetGraph().isEmpty()) {
            return;
        }

        // Get the audit dataset that we will write into
        Dataset auditStore = getDataset();
        if (auditStore == null) {
            return;
        }

        // Lock the dataset
        auditStore.begin(ReadWrite.WRITE);
        try {
            // Get the main graph that we will write the metadata into
            Model auditModel = auditStore.getNamedModel(auditGraph);
            if (auditModel == null) {
                return;
            }

            // Create a URI for this request
            String changeUri = changesBaseURI + changes.getUUID().toString();
            Resource changeResource = auditModel.createResource(changeUri);

            // Define the type of this resource
            changeResource.addProperty(RDF.type, auditModel.createResource(AuditVocabulary.TYPE_CHANGESET));

            // Add the UUID
            changeResource.addProperty(auditModel.createProperty(AuditVocabulary.PROP_UUID), changes.getUUID().toString());

            // Add the user information
            UserAccount acc = LoginStatusBean.getCurrentUser(request);
            if (acc != null) {
                changeResource.addProperty(auditModel.createProperty(AuditVocabulary.PROP_USER), auditModel.createResource(acc.getUri()));
            } else {
                changeResource.addProperty(auditModel.createProperty(AuditVocabulary.PROP_USER), auditModel.createResource(AuditVocabulary.RESOURCE_UNKNOWN));
            }

            // Add the time of the change
            changeResource.addProperty(auditModel.createProperty(AuditVocabulary.PROP_DATE), Long.toString(changes.getRequestTime().getTime(),10));

            // Get the names of modified graphs
            Set<String> names = changes.getGraphUris();

            // Counter for generate graph URIs
            int graphCount = 1;

            // Loop through all of the graphs in the changeset
            for (String graphName: names) {
                // Get any additions / removals in the named graph
                Model addedModel = changes.getAddedModel(graphName);
                Model removedModel = changes.getRemovedModel(graphName);

                // If we have some changes to write
                if (!isEmpty(addedModel) || !isEmpty(removedModel)) {
                    // Create a URI for the set of changes made to a specific graph
                    String graphUri = changeUri + "/graph/" + Integer.toString(graphCount, 10);
                    Resource graphResource = auditModel.createResource(graphUri);

                    // Record the URI in the overall metadata
                    changeResource.addProperty(auditModel.createProperty(AuditVocabulary.PROP_HASGRAPH), graphResource);

                    // Define the type of this resource
                    graphResource.addProperty(RDF.type, auditModel.createResource(AuditVocabulary.TYPE_CHANGESETGRAPH));

                    // If we are recording changes for a named (rather than default) graph, record the graph that the changes apply to
                    if (!StringUtils.isEmpty(graphName)) {
                        graphResource.addProperty(auditModel.createProperty(AuditVocabulary.PROP_GRAPH), auditModel.createResource(graphName));
                    }

                    // Record graph of added statements
                    if (!isEmpty(addedModel)) {
                        String addedName = graphUri + "/added";
                        Model addedAuditModel = auditStore.getNamedModel(addedName);
                        if (addedAuditModel != null) {
                            addedAuditModel.add(addedModel);
                            graphResource.addProperty(auditModel.createProperty(AuditVocabulary.PROP_GRAPH_ADDED), auditModel.createResource(addedName));
                        }
                    }

                    // Record graph of removed statements
                    if (!isEmpty((removedModel))) {
                        String removedName = graphUri + "/removed";
                        Model removedAuditModel = auditStore.getNamedModel(removedName);
                        if (removedAuditModel != null) {
                            removedAuditModel.add(removedModel);
                            graphResource.addProperty(auditModel.createProperty(AuditVocabulary.PROP_GRAPH_REMOVED), auditModel.createResource(removedName));
                        }
                    }

                    // Increment to create a new URI for any additional graphs
                    graphCount++;
                }
            }

            // Commit the changes to the audit store
            auditStore.commit();
        } finally {
            auditStore.end();
        }
    }

    @Override
    public AuditResults findForUser(String userUri, long offset, int limit) {
        long total = 0;
        List<AuditChangeSet> datasets = new ArrayList<AuditChangeSet>();

        // Must have a user uri
        if (StringUtils.isEmpty(userUri)) {
            return null;
        }

        // Get the audit dataset
        Dataset auditStore = getDataset();
        if (auditStore == null) {
            return null;
        }

        // Indicate that we are reading from the audit store
        auditStore.begin(ReadWrite.READ);
        try {
            StringBuilder queryString;
            Query query;
            QueryExecution qexec;

            // SPARQL query to retrieve overall change sets authored by the user, in reverse chronological order, with pagination
            queryString = new StringBuilder();
            queryString.append("SELECT ?dataset ");
            queryString.append(" WHERE {");
            queryString.append("   ?dataset a <").append(AuditVocabulary.TYPE_CHANGESET).append("> . ");
            queryString.append("   ?dataset <").append(AuditVocabulary.PROP_USER).append("> <").append(userUri).append("> . ");
            queryString.append("   ?dataset <").append(AuditVocabulary.PROP_DATE).append("> ?date . ");
            queryString.append(" } ");
            queryString.append(" ORDER BY DESC(?date) ");
            queryString.append(" LIMIT ").append(limit);
            queryString.append(" OFFSET ").append(offset);

            query = QueryFactory.create(queryString.toString());
            qexec = QueryExecutionFactory.create(query, auditStore);

            try {
                ResultSet rs = qexec.execSelect();

                // For each result
                while (rs.hasNext()) {
                    QuerySolution qs = rs.next();
                    String uri = qs.getResource("dataset").getURI();

                    // Read the change set from the audit store
                    datasets.add(getChangeSet(auditStore, uri));
                }
            } finally {
                qexec.close();
                query.clone();
            }

            // SPARQL Query to obtain a count of all change sets for this user
            queryString = new StringBuilder();
            queryString.append("SELECT (COUNT(?dataset) AS ?datasetCount) ");
            queryString.append(" WHERE {");
            queryString.append("   ?dataset a <").append(AuditVocabulary.TYPE_CHANGESET).append("> . ");
            queryString.append("   ?dataset <").append(AuditVocabulary.PROP_USER).append("> <").append(userUri).append("> . ");
            queryString.append("   ?dataset <").append(AuditVocabulary.PROP_DATE).append("> ?date . ");
            queryString.append(" } ");

            query = QueryFactory.create(queryString.toString());
            qexec = QueryExecutionFactory.create(query, auditStore);

            try {
                ResultSet rs = qexec.execSelect();
                while (rs.hasNext()) {
                    QuerySolution qs = rs.next();
                    total = qs.getLiteral("datasetCount").getLong();
                }
            } finally {
                qexec.close();
                query.clone();
            }
        } finally {
            auditStore.end();
        }

        // Create a new results object
        return new AuditResults(total, offset, limit, datasets);
    }

    /**
     * Retrieve a changeset from the audit store
     *
     * You should "lock" the data store for read operations before calling this method, and release after.
     * This method is provided so that it can be called from other methods that have already done this,
     * such as in the loop for reading a set of changes for a given user.
     *
     * @param auditStore
     * @param changesetUri
     * @return
     */
    private AuditChangeSet getChangeSet(Dataset auditStore, String changesetUri) {
        // Must have a changeset URI to retrieve
        if (StringUtils.isEmpty(changesetUri)) {
            return null;
        }

        UUID id = null;
        Date time = null;
        List<String> graphUris = new ArrayList<>();

        // NOTE We do NOT "lock" the data store for read - see the method comment for more information

        // Get the main metadata graph from the audit store
        Model auditModel = auditStore.getNamedModel(auditGraph);
        if (auditModel == null) {
            return null;
        }

        // Get the resource that represents metadata about the requested changeset
        Resource datasetResource = auditModel.getResource(changesetUri);
        if (datasetResource != null) {
            // Get the UUID, date, and change graph URIs from the metadata
            StmtIterator iter = datasetResource.listProperties();
            try {
                while (iter.hasNext()) {
                    Statement stmt = iter.next();
                    switch (stmt.getPredicate().getURI()) {
                        case AuditVocabulary.PROP_UUID:
                            id = UUID.fromString(stmt.getObject().asLiteral().toString());
                            break;

                        case AuditVocabulary.PROP_DATE:
                            time = new Date(stmt.getObject().asLiteral().getLong());
                            break;

                        case AuditVocabulary.PROP_HASGRAPH:
                            graphUris.add(stmt.getObject().asResource().getURI());
                            break;
                    }
                    stmt.getObject();
                }
            } finally {
                iter.close();
            }
        }

        // Create a changeset object
        AuditChangeSet auditChangeSet = new AuditChangeSet(id, time);

        // Loop through all of the change graphs
        for (String graphUri : graphUris) {
            String graphName = null;
            String addedGraph = null;
            String removedGraph = null;

            Resource graphResource = auditModel.getResource(graphUri);
            StmtIterator iter = graphResource.listProperties();
            try {
                while (iter.hasNext()) {
                    Statement stmt = iter.next();
                    switch (stmt.getPredicate().getURI()) {
                        // Get the graph name (e.g. the graph in the content store) that was affected by this change
                        case AuditVocabulary.PROP_GRAPH:
                            graphName = stmt.getObject().asResource().getURI();
                            break;

                         // Get the URI of the graph that contains the statements that were added
                        case AuditVocabulary.PROP_GRAPH_ADDED:
                            addedGraph = stmt.getObject().asResource().getURI();
                            break;

                        // Get the URI of the graph that contains the statements that were removed
                        case AuditVocabulary.PROP_GRAPH_REMOVED:
                            removedGraph = stmt.getObject().asResource().getURI();
                            break;
                    }
                }
            } finally {
                iter.close();
            }

            // If there were statements added
            if (!StringUtils.isEmpty(addedGraph)) {
                // Retrieve the statements from the graph in the audit store
                Model added = auditStore.getNamedModel(addedGraph);

                // Get a model from the changeset for the current graphname
                Model writeTo = auditChangeSet.getAddedModel(graphName);

                // Copy the statements from the audit store to the (in-memory) changeset
                if (writeTo != null && added != null) {
                    writeTo.add(added);
                }
            }

            if (!StringUtils.isEmpty(removedGraph)) {
                // Retrieve the statements from the graph in the audit store
                Model removed = auditStore.getNamedModel(removedGraph);

                // Get a model from the changeset for the current graphname
                Model writeTo = auditChangeSet.getRemovedModel(graphName);

                // Copy the statements from the audit store to the (in-memory) changeset
                if (writeTo != null && removed != null) {
                    writeTo.add(removed);
                }
            }
        }

        return auditChangeSet;
    }

    /**
     * Helper method to determine if a model has not been retrieved, or is empty
     *
     * @param model
     * @return
     */
    private boolean isEmpty(Model model) {
        return model == null || model.isEmpty();
    }
}

