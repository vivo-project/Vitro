package org.vivoweb.linkeddatafragments.datasource.rdfservice;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import org.apache.jena.atlas.io.StringWriterI;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.linkeddatafragments.datasource.AbstractRequestProcessorForTriplePatterns;
import org.linkeddatafragments.fragments.ILinkedDataFragment;
import org.linkeddatafragments.fragments.tpf.ITriplePatternElement;
import org.linkeddatafragments.fragments.tpf.ITriplePatternFragmentRequest;

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
                            NodeFactory.createAnon(new AnonId(bnodeId))
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
