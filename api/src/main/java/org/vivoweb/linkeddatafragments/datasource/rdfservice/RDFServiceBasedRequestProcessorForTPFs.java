package org.vivoweb.linkeddatafragments.datasource.rdfservice;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.QueryUtils;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ResultSetConsumer;
import org.apache.jena.atlas.io.StringWriterI;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
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
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
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

        private Node skolemize(Node node) {
            if (node != null && node.isBlank()) {
                return NodeFactory.createURI("bnode://" + node.getBlankNodeLabel());
            }

            return node;
        }

        private RDFNode deskolemize(RDFNode node) {
            if (node == null) {
                return null;
            }

            if (node.isResource()) {
                String uri = node.asResource().getURI();
                if (uri != null && uri.startsWith("bnode://")) {
                    String bnodeId = uri.substring(8);
                    return ModelFactory.createDefaultModel().asRDFNode(
                            NodeFactory.createBlankNode(bnodeId)
                    );
                }
            }

            return node;
        }

        @Override
        protected ILinkedDataFragment createFragment(
                   final ITriplePatternElement<RDFNode,String,String> subject,
                   final ITriplePatternElement<RDFNode,String,String> predicate,
                   final ITriplePatternElement<RDFNode,String,String> object,
                   final long offset,
                   final long limit )
        {
            try {
                RDFNode nSubject = subject.isVariable() ? null : deskolemize(subject.asConstantTerm());
                RDFNode nPredicate = predicate.isVariable() ? null : deskolemize(predicate.asConstantTerm());
                RDFNode nObject = object.isVariable() ? null : deskolemize(object.asConstantTerm());

                Model triples = rdfService.getTriples(nSubject, nPredicate, nObject, limit, offset);
                if (triples == null || triples.isEmpty()) {
                    return createEmptyTriplePatternFragment();
                }

                if (triples.size() > 0) {
                    Model replacedBlankNodes = ModelFactory.createDefaultModel();
                    StmtIterator iter = triples.listStatements();
                    while (iter.hasNext()) {
                        Statement oldStmt = iter.next();
                        Triple t = oldStmt.asTriple();
                        replacedBlankNodes.add(
                                replacedBlankNodes.asStatement(
                                        new Triple(
                                                skolemize(t.getSubject()),
                                                skolemize(t.getPredicate()),
                                                skolemize(t.getObject())
                                        )
                                )
                        );
                    }

                    triples = replacedBlankNodes;
                }

                long size = triples.size();
                long estimate = -1;
                estimate = rdfService.countTriples(nSubject, nPredicate, nObject);

                // No estimate or incorrect
                if (estimate < offset + size) {
                    estimate = (size == limit) ? offset + size + 1 : offset + size;
                }

                // create the fragment
                final boolean isLastPage = ( estimate < offset + limit );
                return createTriplePatternFragment( triples, estimate, isLastPage );
            } catch (RDFServiceException e) {
                return createEmptyTriplePatternFragment();
            }
        }

    } // end of class Worker


    /**
     * Constructor
     */
    public RDFServiceBasedRequestProcessorForTPFs() {
    }
}
