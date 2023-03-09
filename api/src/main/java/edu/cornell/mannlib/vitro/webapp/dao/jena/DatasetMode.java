package edu.cornell.mannlib.vitro.webapp.dao.jena;

/**
 * Modes for optimizing database queries.
 */
public enum DatasetMode {

    /**
     * Only perform assertions.
     */
    ASSERTIONS_ONLY,

    /**
     * Only perform inferences.
     */
    INFERENCES_ONLY,

    /**
     * Perform both assertions and inferences.
     */
    ASSERTIONS_AND_INFERENCES,
}
