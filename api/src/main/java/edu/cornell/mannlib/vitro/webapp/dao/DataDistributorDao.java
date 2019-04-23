/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import org.apache.jena.rdf.model.Model;

import java.util.List;

/**
 * Access object for interacting with DataDistributor and GraphBuilder configurations in the Display model
 */
public interface DataDistributorDao {
    public class Entry {
        private String uri;
        private String name;
        private Class clazz;
        private boolean persistent;

        public Entry(String uri, String name, Class clazz, boolean persistent) {
            this.uri = uri;
            this.name = name;
            this.clazz = clazz;
            this.persistent = persistent;
        }

        public String getUri() {
            return uri;
        }

        public String getName() {
            return name;
        }

        public String getClassName() {
            return clazz.getName();
        }

        public boolean isPersistent() {
            return persistent;
        }
    }

    /**
     * Retrieve the URIs of all objects declared as being of type DataDistributor
     */
    List<String> getDistributorUris();

    /**
     * Retrieve the URIs of all objects declared as being of type GraphBuilder
     */
    List<String> getGraphBuilderUris();

    /**
     * Get all DataDistributors
     */
    List<Entry> getAllDistributors();

    /**
     * Get all GraphBuilders
     */
    List<Entry> getAllGraphBuilders();

    /**
     * Get a Jena model for all statements with the given Uri as a subject
     */
    Model getModelByUri(String uri);

    /**
     * Update the statements for a given Uri subject with those in the passed model
     */
    boolean updateModel(String uri, Model newModel);

    /**
     * Determine if the Uri is declared in the permanent store (i.e. it is not a file loaded from everytime)
     */
    boolean isPersistent(String uri);

    /**
     * Get the action or builder name associated with the uri
     */
    String getNameFromModel(Model model);

    /**
     * Get the class for this DataDistributor or GraphBuilder
     */
    Class getClassFromModel(Model model);
}
