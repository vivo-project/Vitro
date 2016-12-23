/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.filter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelChangedListener;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService.ModelSerializationFormat;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ResultSetConsumer;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceImpl;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

public class SameAsFilteringRDFServiceFactory implements RDFServiceFactory {

    private final static Log log = LogFactory.getLog(
            SameAsFilteringRDFServiceFactory.class);
    private RDFServiceFactory f;
    private Model sameAsModel;
    
    public SameAsFilteringRDFServiceFactory(RDFServiceFactory rdfServiceFactory) {
        this.f = rdfServiceFactory;
        try {
            InputStream in = f.getRDFService().sparqlConstructQuery("CONSTRUCT { ?s <" + OWL.sameAs.getURI() + "> ?o } WHERE { ?s <" + OWL.sameAs.getURI() + "> ?o } ", ModelSerializationFormat.N3);
            sameAsModel = RDFServiceUtils.parseModel(in, ModelSerializationFormat.N3);
        } catch (RDFServiceException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public RDFService getRDFService() {
        return new SameAsFilteringRDFService(f.getRDFService());
    }
    
    @Override
    public RDFService getShortTermRDFService() {
        return new SameAsFilteringRDFService(f.getShortTermRDFService());
    }
    
    @Override
    public void registerListener(ChangeListener changeListener) throws RDFServiceException {
        f.registerListener(changeListener);
    }
    
    @Override
    public void unregisterListener(ChangeListener changeListener) throws RDFServiceException {
        f.registerListener(changeListener);
    }
    
    @Override
    public void registerJenaModelChangedListener(ModelChangedListener changeListener) throws RDFServiceException {
        f.registerJenaModelChangedListener(changeListener);
    }
    
    @Override
    public void unregisterJenaModelChangedListener(ModelChangedListener changeListener) throws RDFServiceException {
        f.registerJenaModelChangedListener(changeListener);
    }
    
    public class SameAsFilteringRDFService extends RDFServiceImpl implements RDFService {
        
        private final Log log = LogFactory.getLog(SameAsFilteringRDFService.class);
        
        private RDFService s;
        
        public SameAsFilteringRDFService(RDFService rdfService) {
            this.s = rdfService;
        }
        
        @Override
        public InputStream sparqlConstructQuery(String query, 
                RDFService.ModelSerializationFormat resultFormat) 
                        throws RDFServiceException {
            Model m = RDFServiceUtils.parseModel(
                    s.sparqlConstructQuery(query, resultFormat), resultFormat);
            Model filtered = ModelFactory.createDefaultModel();
            StmtIterator stmtIt = m.listStatements();
            while (stmtIt.hasNext()) {
                Statement stmt = stmtIt.nextStatement();
                if (!isRedundant(stmt)) {
                    filtered.add(stmt);
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            filtered.write(out, RDFServiceUtils.getSerializationFormatString(
                    resultFormat));
            return new ByteArrayInputStream(out.toByteArray());
        }

        @Override
        public void sparqlConstructQuery(String query, Model model)
                throws RDFServiceException {
            Model m = ModelFactory.createDefaultModel();
            s.sparqlConstructQuery(query, m);

            StmtIterator stmtIt = m.listStatements();
            while (stmtIt.hasNext()) {
                Statement stmt = stmtIt.nextStatement();
                if (!isRedundant(stmt)) {
                    model.add(stmt);
                }
            }
        }

        @Override
        public InputStream sparqlSelectQuery(String query, ResultFormat resultFormat) 
                throws RDFServiceException {
            ResultSet rs = ResultSetFactory.load(
                    s.sparqlSelectQuery(query, resultFormat), 
                            RDFServiceUtils.getJenaResultSetFormat(resultFormat));            
            List<QuerySolution> solutions = new ArrayList<QuerySolution>();
            while (rs.hasNext()) {
                QuerySolution solution = rs.nextSolution();
                if (!isRedundant(solution)) {
                    solutions.add(solution);
                }
            }
            ResultSet resultSet = new FilteredResultSet(solutions, rs);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); 
            switch (resultFormat) {
               case CSV:
                  ResultSetFormatter.outputAsCSV(outputStream,resultSet);
                  break;
               case TEXT:
                  ResultSetFormatter.out(outputStream,resultSet);
                  break;
               case JSON:
                  ResultSetFormatter.outputAsJSON(outputStream, resultSet);
                  break;
               case XML:
                  ResultSetFormatter.outputAsXML(outputStream, resultSet);
                  break;
               default: 
                  throw new RDFServiceException("unrecognized result format");
            }              
            return new ByteArrayInputStream(outputStream.toByteArray());        
        }

        @Override
        public void sparqlSelectQuery(String query, ResultSetConsumer consumer)
                throws RDFServiceException {

            s.sparqlSelectQuery(query, new ResultSetConsumer.Chaining(consumer) {
                @Override
                public void processQuerySolution(QuerySolution qs) {
                    if (!isRedundant(qs)) {
                        chainProcessQuerySolution(qs);
                    }
                }
            });
        }

        private boolean isRedundant(Statement s) {
            List<Resource> sameAsResources = getSameAsResources(s.getSubject());
            if (sameAsResources.size() > 0 && !sameAsResources.get(0).equals(s.getSubject())) {
                return true;
            } 
            if (s.getObject().isLiteral() || s.getObject().isAnon()) {
                return false;
            }
            sameAsResources = getSameAsResources(s.getObject().asResource());
            if (sameAsResources.size() > 0 && !sameAsResources.get(0).equals(s.getObject().asResource())) {
                return true;
            }
            return false;
        }
        
        private List<Resource> getSameAsResources(Resource resource) {
            List<Resource> sameAsResources = new ArrayList<Resource>();
            if (resource.isAnon()) {
                return sameAsResources;
            }
            String queryStr = "SELECT DISTINCT ?s WHERE { <" + resource.getURI() + "> <" + OWL.sameAs.getURI() + "> ?s } ORDER BY ?s";
            try  {
                Query query = QueryFactory.create(queryStr);
                QueryExecution qe = QueryExecutionFactory.create(query, sameAsModel);
                try {
                    ResultSet rs = qe.execSelect();
                    //ResultSet rs = JSONInput.fromJSON(s.sparqlSelectQuery(queryStr, ResultFormat.JSON));
                    while (rs.hasNext()) {
                        QuerySolution q = rs.next();
                        Resource res = q.getResource("s");
                        if (s != null) {
                            log.info("adding same as " + res.getURI());
                            sameAsResources.add(res);
                        }
                    }
                } finally {
                    qe.close();
                }
                return sameAsResources;
            } catch (/*RDFService*/Exception e) {
                throw new RuntimeException(e);
            }
        }

        private boolean isRedundant(QuerySolution q) {
            Iterator<String> varIt = q.varNames();
            while(varIt.hasNext()) {
                String varName = varIt.next();
                RDFNode n = q.get(varName);
                if (n.isResource()) {
                    Resource r = n.asResource();
                    List<Resource> sames = getSameAsResources(r);
                    if (sames.size() > 0 && !sames.get(0).equals(r)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean changeSetUpdate(ChangeSet changeSet)
                throws RDFServiceException {
            return s.changeSetUpdate(changeSet);
        }

        @Override
        public InputStream sparqlDescribeQuery(String query,
                ModelSerializationFormat resultFormat)
                throws RDFServiceException {
            Model m = RDFServiceUtils.parseModel(
                    s.sparqlConstructQuery(query, resultFormat), resultFormat);
            Model filtered = ModelFactory.createDefaultModel();
            StmtIterator stmtIt = m.listStatements();
            while (stmtIt.hasNext()) {
                Statement stmt = stmtIt.nextStatement();
                if (!isRedundant(stmt)) {
                    filtered.add(stmt);
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            filtered.write(out, RDFServiceUtils.getSerializationFormatString(
                    resultFormat));
            return new ByteArrayInputStream(out.toByteArray()); 
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
    	public void serializeAll(OutputStream outputStream)
    			throws RDFServiceException {
        	s.serializeAll(outputStream);
    	}

    	@Override
    	public void serializeGraph(String graphURI, OutputStream outputStream)
    			throws RDFServiceException {
    		s.serializeGraph(graphURI, outputStream);
    	}

    	@Override
    	public boolean isEquivalentGraph(String graphURI,
    			InputStream serializedGraph,
    			ModelSerializationFormat serializationFormat) throws RDFServiceException {
    		return s.isEquivalentGraph(graphURI, serializedGraph, serializationFormat);
    	}

        @Override
        public boolean isEquivalentGraph(String graphURI,
                                         Model graph) throws RDFServiceException {
            return s.isEquivalentGraph(graphURI, graph);
        }

        @Override
        public void close() {
            s.close();
        }
        
        
    }
    
    
}
