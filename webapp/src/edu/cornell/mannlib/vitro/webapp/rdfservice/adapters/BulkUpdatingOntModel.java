/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.GraphWithPerform;
import com.hp.hpl.jena.graph.impl.WrappedBulkUpdateHandler;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReaderF;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.RDFReaderFImpl;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.shared.WrappedIOException;
import com.hp.hpl.jena.util.iterator.Map1;

/**
 * A model that still handles bulk updates in the old-fashioned way: with a
 * BulkUpdateHandler.
 */
public class BulkUpdatingOntModel extends AbstractOntModelDecorator {
	private static final Log log = LogFactory
			.getLog(BulkUpdatingOntModel.class);

	private static final RDFReaderF readerFactory = new RDFReaderFImpl();

	private final BulkUpdateHandler buh;

	public BulkUpdatingOntModel(OntModel inner) {
		super(inner);
		this.buh = inner.getGraph().getBulkUpdateHandler();
	}

	@SuppressWarnings("deprecation")
	private static BulkUpdateHandler getWrappedBulkUpdateHandler(Graph graph) {
		if (graph instanceof GraphWithPerform) {
			return new WrappedBulkUpdateHandler((GraphWithPerform) graph,
					graph.getBulkUpdateHandler());
		} else {
			try {
				throw new IllegalStateException();
			} catch (IllegalStateException e) {
				log.warn("Graph is not an instance of GraphWithPerform", e);
			}
			return graph.getBulkUpdateHandler();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public Model add(StmtIterator iter) {
		try {
			buh.add(asTriples(iter));
		} finally {
			iter.close();
		}
		return this;
	}

	@Override
	public Model add(Model m) {
		return add(m, false);
	}

	@Deprecated
	@Override
	public Model add(Model m, boolean suppressReifications) {
		// suppressReifications is a no-op.
		buh.add(m.getGraph());
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Model read(String url) {
		Model m = ModelFactory.createDefaultModel();
		readerFactory.getReader().read(m, url);
		buh.add(m.getGraph());
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Model read(Reader reader, String base) {
		Model m = ModelFactory.createDefaultModel();
		readerFactory.getReader().read(m, reader, base);
		buh.add(m.getGraph());
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Model read(InputStream reader, String base) {
		Model m = ModelFactory.createDefaultModel();
		readerFactory.getReader().read(m, reader, base);
		buh.add(m.getGraph());
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Model read(String url, String lang) {
		Model m = ModelFactory.createDefaultModel();
		readerFactory.getReader(lang).read(m, url);
		buh.add(m.getGraph());
		return this;
	}

	@Override
	public Model read(String url, String base, String lang) {
		try {
			InputStream is = new URL(url).openStream();
			try {
				read(is, base, lang);
			} finally {
				if (null != is) {
					is.close();
				}
			}
		} catch (IOException e) {
			throw new WrappedIOException(e);
		}
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Model read(Reader reader, String base, String lang) {
		Model m = ModelFactory.createDefaultModel();
		readerFactory.getReader(lang).read(m, reader, base);
		buh.add(m.getGraph());
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Model read(InputStream reader, String base, String lang) {
		Model m = ModelFactory.createDefaultModel();
		readerFactory.getReader(lang).read(m, reader, base);
		buh.add(m.getGraph());
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Model remove(StmtIterator iter) {
		buh.delete(asTriples(iter));
		return this;
	}

	@Override
	public Model remove(Model m) {
		return remove(m, false);
	}

	@Override
	@Deprecated
	public Model remove(Model m, boolean suppressReifications) {
		buh.delete(m.getGraph());
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Model removeAll() {
		buh.removeAll();
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Model add(Statement[] statements) {
		buh.add(StatementImpl.asTriples(statements));
		return this;
	}

	@Override
	public Model add(List<Statement> statements) {
		add(statements.toArray(new Statement[statements.size()]));
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Model remove(Statement[] statements) {
		buh.delete(StatementImpl.asTriples(statements));
		return this;
	}

	@Override
	public Model remove(List<Statement> statements) {
		remove(statements.toArray(new Statement[statements.size()]));
		return this;
	}

	private Iterator<Triple> asTriples(StmtIterator it) {
		return it.mapWith(mapAsTriple);
	}

	private Map1<Statement, Triple> mapAsTriple = new Map1<Statement, Triple>() {
		@Override
		public Triple map1(Statement s) {
			return s.asTriple();
		}
	};

}
