/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import java.util.List;

/**
 * Base class for creating consumers of ResultSets
 *
 * processQuerySolution MUST be overridden - it takes each QuerySolution in turn until the ResultSet is complete
 *
 * startProcessing and endProcessing may be overridden if the implementation needs to know when the processing starts,
 * or when there are no more solutions left to process.
 */
public abstract class ResultSetConsumer {
    private ResultSet resultSet;

    public ResultSetConsumer() {
    }

    /**
     * Method for processing each QuerySolution - must be overridden in each implementation
     *
     * @param qs - the current query solution
     */
    protected abstract void processQuerySolution(QuerySolution qs);

    /**
     * Method to notify the consumer that a ResultSet is valid and is about to be processed
     */
    protected void startProcessing() {
    }

    /**
     * Method to notify the consumer that all QuerySolutions have been processed
     */
    protected void endProcessing() {
    }

    /**
     * Helper method that ensures the start / end processing is done correctly
     *
     * @param rs - the ResultSet to process
     */
    public void processResultSet(ResultSet rs) {
        if (rs != null) {
            resultSet = rs;
            startProcessing();
            while (rs.hasNext()) {
                processQuerySolution(rs.next());
            }
            endProcessing();
        }
    }

    /**
     * Helper method to allow an implementation to get the var names from the resultset
     *
     * @return list of result set var names
     */
    final protected List<String> getResultVars() {
        return resultSet.getResultVars();
    }

    /**
     * Helper implemenation of ResutSetConsumer that can be used to wrap another ResultSetConsumer
     * - useful for filtering implementations
     */
    public static abstract class Chaining extends ResultSetConsumer {
        private ResultSetConsumer innerConsumer;

        protected Chaining(ResultSetConsumer innerConsumer) {
            this.innerConsumer = innerConsumer;
        }

        protected void startProcessing() {
            chainStartProcessing();
        }

        protected void endProcessing() {
            chainEndProcessing();
        }

        /**
         * Helper method that calls the processQuerySolution on an embedded ResultSetConsumer
         * @param qs Query solution
         */
        protected void chainProcessQuerySolution(QuerySolution qs) {
            if (innerConsumer != null) {
                innerConsumer.processQuerySolution(qs);
            }
        }

        /**
         * Helper method that calls startProcessing on an embedded ResultSetConsumer
         */
        protected void chainStartProcessing() {
            if (innerConsumer != null)
                innerConsumer.startProcessing();
        }

        /**
         * Helper method that calls endProcessing on an embedded ResultSetConsumer
         */
        protected void chainEndProcessing() {
            if (innerConsumer != null) {
                innerConsumer.endProcessing();
            }

        }
    }

    /**
     * Helper implementation that allows you to find out simply if there were any results in the ResultSet
     */
    public static class HasResult extends ResultSetConsumer {
        private boolean hasResult = false;

        /**
         * Override the helper method for processing results to quickly check if there are any results
         * @param rs - the ResultSet to process
         */
        @Override
        public void processResultSet(ResultSet rs) {
            hasResult = (rs != null && rs.hasNext());
        }

        @Override
        protected void processQuerySolution(QuerySolution qs) {
            hasResult = true;
        }

        /**
         * Were any results found
         */
        public boolean hasResult() {
            return hasResult;
        }
    }
}
