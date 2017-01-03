/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.sparql;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Quad;

/**
 * Helper classes for producing N-Triples or N-Quads from ResultSets
 */
public class ResultSetIterators {
	/**
	 * If the ResultSet contains appropriate values for g, s, p, and o, return a
	 * Quad for each row.
	 */
	public static class ResultSetQuadsIterator implements Iterator<Quad> {
		private final ResultSet resultSet;

		public ResultSetQuadsIterator(ResultSet resultSet) {
			this.resultSet = resultSet;
		}

		@Override
		public boolean hasNext() {
			return resultSet.hasNext();
		}

		@Override
		public Quad next() {
			QuerySolution s = resultSet.next();
			return new Quad(NodeConverter.toNode(s.get("g")),
					NodeConverter.toNode(s.get("s")), NodeConverter.toNode(s
							.get("p")), NodeConverter.toNode(s.get("o")));
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * If the ResultSet contains appropriate values for s, p, and o, return a
	 * Triple for each row.
	 */
	public static class ResultSetTriplesIterator implements Iterator<Triple> {
		private final ResultSet resultSet;

		public ResultSetTriplesIterator(ResultSet resultSet) {
			this.resultSet = resultSet;
		}

		@Override
		public boolean hasNext() {
			return resultSet.hasNext();
		}

		@Override
		public Triple next() {
			QuerySolution s = resultSet.next();
			return new Triple(NodeConverter.toNode(s.get("s")),
					NodeConverter.toNode(s.get("p")), NodeConverter.toNode(s
							.get("o")));
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private static class NodeConverter {
		public static Node toNode(RDFNode rdfNode) {
			if (rdfNode.isAnon()) {
				Resource a = rdfNode.asResource();
				return NodeFactory.createAnon();
			}
			if (rdfNode.isLiteral()) {
				Literal l = rdfNode.asLiteral();
				return NodeFactory.createLiteral(l.getLexicalForm(),
						l.getLanguage(), l.getDatatype());
			}
			return NodeFactory.createURI(rdfNode.asResource().getURI());
		}
	}

}
