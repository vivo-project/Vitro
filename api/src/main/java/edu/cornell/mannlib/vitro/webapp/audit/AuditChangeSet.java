/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.audit;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import java.io.StringWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

/**
 * The current set of changes for a triple store, as tracked against a single request
 */
public class AuditChangeSet {
    // Unique identifier for the changes
    private final UUID id;

    // The time the request was made
    private final Date requestTime;

    // The statements added to the triple store
    private Dataset addedDataset   = null;

    // The statemetns removed from the triple store
    private Dataset removedDataset = null;

    /**
     * Initialize a change set
     */
    public AuditChangeSet() {
        this.id = UUID.randomUUID();
        this.requestTime = new Date();
    }

    /**
     * Create a change set for a given UUID / request time (e.g. for reading from the audit store)
     * @param id
     * @param requestTime
     */
    public AuditChangeSet(UUID id, Date requestTime) {
        this.id = id;
        this.requestTime = requestTime;
    }

    /**
     * Get the unique identifier
     *
     * @return
     */
    public UUID getUUID() {
        return id;
    }

    /**
     * Get the request time
     *
     * @return
     */
    public Date getRequestTime() {
        return requestTime;
    }

    /**
     * Get the dataset of additions
     *
     * @return
     */
    public Dataset getAddedDataset() {
        return addedDataset;
    }

    /**
     * Get the dataset of removals
     *
     * @return
     */
    public Dataset getRemovedDataset() {
        return removedDataset;
    }

    /**
     * Get the model of added statements for a named graph
     *
     * @param graphURI
     * @return
     */
    public Model getAddedModel(String graphURI) {
        if (addedDataset == null) {
            addedDataset = DatasetFactory.createGeneral();
        }

        if (StringUtils.isEmpty(graphURI)) {
            return addedDataset.getDefaultModel();
        }

        return addedDataset.getNamedModel(graphURI);
    }

    /**
     * Get the added statements for a named graph
     *
     * @param graphUri
     * @return
     */
    public String getAddedStatements(String graphUri) {
        return getStatements(getAddedModel(graphUri));
    }

    /**
     * Get the model of removed statements for a named graph
     *
     * @param graphURI
     * @return
     */
    public Model getRemovedModel(String graphURI) {
        if (removedDataset == null) {
            removedDataset = DatasetFactory.createGeneral();
        }

        if (StringUtils.isEmpty(graphURI)) {
            return removedDataset.getDefaultModel();
        }

        return removedDataset.getNamedModel(graphURI);
    }

    /**
     * Get the removed statements for a named graph
     *
     * @param graphUri
     * @return
     */
    public String getRemovedStatements(String graphUri) {
        return getStatements(getRemovedModel(graphUri));
    }

    /**
     * Check if the added and removed datasets are empty
     *
     * @return
     */
    public boolean isEmpty() {
        if (addedDataset != null && !addedDataset.asDatasetGraph().isEmpty()) {
            return false;
        }

        if (removedDataset != null && !removedDataset.asDatasetGraph().isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * Get a set of all the named graphs in the added / removed datasets
     *
     * @return
     */
    public Set<String> getGraphUris() {
        Set<String> graphUris = new HashSet<>();

        populateGraphUriSet(graphUris, addedDataset);
        populateGraphUriSet(graphUris, removedDataset);

        return graphUris;
    }

    private void populateGraphUriSet(Set<String> graphUris, Dataset dataset) {
        if (dataset == null) {
            return;
        }

        Iterator<String> iterator = dataset.listNames();
        while (iterator.hasNext()) {
            graphUris.add(iterator.next());
        }
    }

    private String getStatements(Model model) {
        if (model != null) {
            StringWriter sw = new StringWriter();
            RDFDataMgr.write(sw, model, RDFFormat.NTRIPLES);
            return sw.toString();
        }

        // No model, so return an empty string
        return "";
    }
}
