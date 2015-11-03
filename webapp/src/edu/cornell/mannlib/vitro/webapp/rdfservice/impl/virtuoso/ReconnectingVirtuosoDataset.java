/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.virtuoso;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.LabelExistsException;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.util.Context;
import virtuoso.jena.driver.VirtDataset;

import javax.sql.ConnectionPoolDataSource;
import java.sql.SQLException;
import java.util.Iterator;

public class ReconnectingVirtuosoDataset implements Dataset {
    private final ConnectionPoolDataSource dataSource;

    private VirtDataset dataset = null;

    public ReconnectingVirtuosoDataset(ConnectionPoolDataSource ds) {
        this.dataSource = ds;
    }

    private synchronized VirtDataset getDataset() {
        if (dataset != null) {
            try {
                if (dataset.getConnection().isValid(100)) {
                    return dataset;
                }
            } catch (SQLException e) {
            }
        }

        releaseDataset();

        dataset = new VirtDataset(dataSource);
//				dataset.setReadFromAllGraphs(true);
        return dataset;
    }

    private synchronized void releaseDataset() {
        if (dataset != null) {
            dataset.end();
            dataset.close();
            dataset = null;
        }
    }

    @Override
    public Model getDefaultModel() {
        return getDataset().getDefaultModel();
    }

    @Override
    public void setDefaultModel(Model model) {
        getDataset().setDefaultModel(model);
    }

    @Override
    public Model getNamedModel(String s) {
        return getDataset().getNamedModel(s);
    }

    @Override
    public boolean containsNamedModel(String s) {
        return getDataset().containsNamedModel(s);
    }

    @Override
    public void addNamedModel(String s, Model model) throws LabelExistsException {
        getDataset().addNamedModel(s, model);
    }

    @Override
    public void removeNamedModel(String s) {
        getDataset().removeNamedModel(s);
    }

    @Override
    public void replaceNamedModel(String s, Model model) {
        getDataset().replaceNamedModel(s, model);
    }

    @Override
    public Iterator<String> listNames() {
        return getDataset().listNames();
    }

    @Override
    public Lock getLock() {
        return getDataset().getLock();
    }

    @Override
    public Context getContext() {
        return getDataset().getContext();
    }

    @Override
    public boolean supportsTransactions() {
        return getDataset().supportsTransactions();
    }

    @Override
    public void begin(ReadWrite readWrite) {
        getDataset().begin(readWrite);
    }

    @Override
    public void commit() {
        getDataset().commit();
        releaseDataset();
    }

    @Override
    public void abort() {
        getDataset().abort();
        releaseDataset();
    }

    @Override
    public boolean isInTransaction() {
        return getDataset().isInTransaction();
    }

    @Override
    public void end() {
        releaseDataset();
    }

    @Override
    public DatasetGraph asDatasetGraph() {
        return getDataset().asDatasetGraph();
    }

    @Override
    public void close() {
        releaseDataset();
    }
}
