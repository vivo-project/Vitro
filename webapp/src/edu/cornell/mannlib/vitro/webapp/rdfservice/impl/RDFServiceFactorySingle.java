/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;

/**
 * An RDFServiceFactory that always returns the same RDFService object
 * @author bjl23
 *
 */
public class RDFServiceFactorySingle implements RDFServiceFactory {

    private RDFService rdfService;
    
    public RDFServiceFactorySingle(RDFService rdfService) {
        this.rdfService = new UnclosableRDFService(rdfService);
    }
    
    @Override
    public RDFService getRDFService() {
        return this.rdfService;
    }
    
    @Override 
    public RDFService getShortTermRDFService() {
        return this.rdfService;
    }
    
    @Override
    public void registerListener(ChangeListener listener) throws RDFServiceException {
        this.rdfService.registerListener(listener);
    }
    
    @Override
    public void unregisterListener(ChangeListener listener) throws RDFServiceException {
        this.rdfService.unregisterListener(listener);
    }
    
    public class UnclosableRDFService implements RDFService {
        
        private RDFService s;
        
        public UnclosableRDFService(RDFService rdfService) {
            this.s = rdfService;
        }

        @Override
        public boolean changeSetUpdate(ChangeSet changeSet)
                throws RDFServiceException {
            return s.changeSetUpdate(changeSet);
        }

        @Override
        public void newIndividual(String individualURI, String individualTypeURI)
                throws RDFServiceException {
            s.newIndividual(individualURI, individualTypeURI);
        }

        @Override
        public void newIndividual(String individualURI,
                String individualTypeURI, String graphURI)
                throws RDFServiceException {
            s.newIndividual(individualURI, individualTypeURI, graphURI);
        }

        @Override
        public InputStream sparqlConstructQuery(String query,
                ModelSerializationFormat resultFormat)
                throws RDFServiceException {
            return s.sparqlConstructQuery(query, resultFormat);
        }

        @Override
        public InputStream sparqlDescribeQuery(String query,
                ModelSerializationFormat resultFormat)
                throws RDFServiceException {
            return s.sparqlDescribeQuery(query, resultFormat);
        }

        @Override
		public void sparqlSelectQuery(String query, ResultFormat resultFormat,
				OutputStream outputStream) throws RDFServiceException {
        	s.sparqlSelectQuery(query, resultFormat, outputStream);
		}

        @Override
        public InputStream sparqlSelectQuery(String query,
                ResultFormat resultFormat) throws RDFServiceException {
            return s.sparqlSelectQuery(query, resultFormat);
        }

        @Override
        public boolean sparqlAskQuery(String query) throws RDFServiceException {
            return s.sparqlAskQuery(query);
        }

        @Override
        public List<String> getGraphURIs() throws RDFServiceException {
            return s.getGraphURIs();
        }

        @Override
        public void getGraphMetadata() throws RDFServiceException {
            s.getGraphMetadata();
        }

        @Override
        public String getDefaultWriteGraphURI() throws RDFServiceException {
            return s.getDefaultWriteGraphURI();
        }

        @Override
        public void registerListener(ChangeListener changeListener)
                throws RDFServiceException {
            s.registerListener(changeListener);
        }

        @Override
        public void unregisterListener(ChangeListener changeListener)
                throws RDFServiceException {
            s.unregisterListener(changeListener);
        }

        @Override
        public ChangeSet manufactureChangeSet() {
            return s.manufactureChangeSet();
        }

        @Override
        public void close() {
            // Don't close s.  It's being used by everybody.
        }
        
    }
    

}
