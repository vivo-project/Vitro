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
            StringBuilder whereClause = new StringBuilder();
            StringBuilder filter = new StringBuilder();
            StringBuilder orderBy = new StringBuilder();

            if ( ! subject.isVariable() ) {
                appendNode(whereClause.append(' '), subject.asConstantTerm());
            } else {
                whereClause.append(" ?s");
                if (filter.length() > 0) { filter.append(" && "); }
                filter.append("!isBlank(?s)");
                orderBy.append(" ?s");
            }

            if ( ! predicate.isVariable() ) {
                appendNode(whereClause.append(' '), predicate.asConstantTerm());
            } else {
                whereClause.append(" ?p");
                if (filter.length() > 0) { filter.append(" && "); }
                filter.append("!isBlank(?p)");
                orderBy.append(" ?p");
            }

            if ( ! object.isVariable() ) {
                appendNode(whereClause.append(' '), object.asConstantTerm());
            } else {
                whereClause.append(" ?o");
                if (filter.length() > 0) { filter.append(" && "); }
                filter.append("!isBlank(?o)");
                orderBy.append(" ?o");
            }

            StringBuilder constructQuery = new StringBuilder();

            constructQuery.append("CONSTRUCT { ");
            constructQuery.append(whereClause.toString());
            constructQuery.append(" } WHERE { ");
            constructQuery.append(whereClause.toString()).append(" . ");
            if (filter.length() > 0) {
                constructQuery.append(" FILTER(").append(filter.toString()).append(")");
            }
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

            StringBuilder count = new StringBuilder();
            count.append("SELECT (COUNT(*) AS ?count) WHERE { ");
            count.append(whereClause.toString());
            count.append(" . ");
            if (filter.length() > 0) {
                count.append(" FILTER(").append(filter.toString()).append(") ");
            }
            count.append(" }");
            try {
                CountConsumer countConsumer = new CountConsumer();
                rdfService.sparqlSelectQuery(count.toString(), countConsumer);
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
