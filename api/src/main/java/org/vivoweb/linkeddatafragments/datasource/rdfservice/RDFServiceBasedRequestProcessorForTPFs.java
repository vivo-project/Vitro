package org.vivoweb.linkeddatafragments.datasource.rdfservice;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.QueryUtils;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ResultSetConsumer;
import org.apache.jena.atlas.io.StringWriterI;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.apache.jena.tdb.TDBFactory;
import org.linkeddatafragments.datasource.AbstractRequestProcessorForTriplePatterns;
import org.linkeddatafragments.datasource.IFragmentRequestProcessor;
import org.linkeddatafragments.fragments.ILinkedDataFragment;
import org.linkeddatafragments.fragments.tpf.ITriplePatternElement;
import org.linkeddatafragments.fragments.tpf.ITriplePatternFragmentRequest;

import java.io.File;

public class RDFServiceBasedRequestProcessorForTPFs
    extends AbstractRequestProcessorForTriplePatterns<RDFNode,String,String>
{
    private static RDFService rdfService;

    public static void setRDFService(RDFService pRDFService) {
        rdfService = pRDFService;
    }

    @Override
    protected Worker getTPFSpecificWorker(
            final ITriplePatternFragmentRequest<RDFNode,String,String> request )
                                                throws IllegalArgumentException
    {
        return new Worker( request );
    }

    /**
     *
     */
    protected class Worker
       extends AbstractRequestProcessorForTriplePatterns.Worker<RDFNode,String,String>
    {
        public Worker(
                final ITriplePatternFragmentRequest<RDFNode,String,String> req )
        {
            super( req );
        }

        private void appendNode(StringBuilder builder, RDFNode node) {
            if (node.isLiteral()) {
                builder.append(literalToString(node.asLiteral()));
            } else if (node.isURIResource()) {
                builder.append('<' + node.asResource().getURI() + '>');
            }
        }

        private String literalToString(Literal l) {
            StringWriterI sw = new StringWriterI();
            NodeFormatter fmt = new NodeFormatterTTL(null, null);
            fmt.formatLiteral(sw, l.asNode());
            return sw.toString();
        }

        @Override
        protected ILinkedDataFragment createFragment(
                   final ITriplePatternElement<RDFNode,String,String> subject,
                   final ITriplePatternElement<RDFNode,String,String> predicate,
                   final ITriplePatternElement<RDFNode,String,String> object,
                   final long offset,
                   final long limit )
        {
//            CONSTRUCT { ?s ?p ?o . } WHERE { VALUES (?p) { (<http://www.w3.org/2002/07/owl#intersectionOf>) } ?s ?p ?o }  ORDER BY ?s ?p ?o LIMIT 100
//            CONSTRUCT WHERE { ?s (<http://www.w3.org/2002/07/owl#intersectionOf>) ?o }  ORDER BY ?s ?o LIMIT 100


            StringBuilder whereClause = new StringBuilder();
            StringBuilder orderBy = new StringBuilder();

            if ( ! subject.isVariable() ) {
                appendNode(whereClause.append(' '), subject.asConstantTerm());
            } else {
                whereClause.append(" ?s");
                orderBy.append(" ?s");
            }

            if ( ! predicate.isVariable() ) {
                appendNode(whereClause.append(' '), predicate.asConstantTerm());
            } else {
                whereClause.append(" ?p");
                orderBy.append(" ?p");
            }

            if ( ! object.isVariable() ) {
                appendNode(whereClause.append(' '), object.asConstantTerm());
            } else {
                whereClause.append(" ?o");
                orderBy.append(" ?o");
            }

            StringBuilder constructQuery = new StringBuilder();

            constructQuery.append("CONSTRUCT WHERE { ");
            constructQuery.append(whereClause.toString());
            constructQuery.append(" }");

            if (orderBy.length() > 0) {
                constructQuery.append(" ORDER BY").append(orderBy.toString());
            }

            if (limit > 0) {
                constructQuery.append(" LIMIT ").append(limit);
            }

            if (offset > 0) {
                constructQuery.append(" OFFSET ").append(offset);
            }

            Model triples = ModelFactory.createDefaultModel();

            try {
                rdfService.sparqlConstructQuery(constructQuery.toString(), triples);
            } catch (RDFServiceException e) {
                return createEmptyTriplePatternFragment();
            }

            if (triples.isEmpty()) {
                return createEmptyTriplePatternFragment();
            }

            // Try to get an estimate
            long size = triples.size();
            long estimate = -1;

            String boundCount = "SELECT (COUNT(*) AS ?count) WHERE { " + whereClause.toString() + " }";
            try {
                CountConsumer countConsumer = new CountConsumer();
                rdfService.sparqlSelectQuery(boundCount, countConsumer);
                estimate = countConsumer.estimate;
            } catch (RDFServiceException e) {
                return createEmptyTriplePatternFragment();
            }

            // No estimate or incorrect
            if (estimate < offset + size) {
                estimate = (size == limit) ? offset + size + 1 : offset + size;
            }

            // create the fragment
            final boolean isLastPage = ( estimate < offset + limit );
            return createTriplePatternFragment( triples, estimate, isLastPage );
        }

    } // end of class Worker


    /**
     * Constructor
     */
    public RDFServiceBasedRequestProcessorForTPFs() {
    }

    class CountConsumer extends ResultSetConsumer {
        public long estimate = -1;

        @Override
        protected void processQuerySolution(QuerySolution qs) {
            if (estimate == -1) {
                Literal literal = qs.getLiteral("count");
                estimate = literal.getLong();
            }
        }
    }
}
