/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFReaderF;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.RDFReaderFImpl;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.shared.WrappedIOException;
import org.apache.jena.util.iterator.Map1;

/**
 * A model that still handles bulk updates in the old-fashioned way: with a
 * BulkUpdateHandler.
 */
public class BulkUpdatingModel extends AbstractModelDecorator {
	private static final RDFReaderF readerFactory = new RDFReaderFImpl();

	public BulkUpdatingModel(Model inner) {
		super(inner);
	}

	@SuppressWarnings("deprecation")
	@Override
	public Model add(StmtIterator iter) {
		try {
			this.getGraph().getTransactionHandler().begin();
			GraphUtil.add(this.getGraph(), asTriples(iter));
			this.getGraph().getTransactionHandler().commit();
		} finally {
			iter.close();
		}
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Model read(String url) {
		Model m = ModelFactory.createDefaultModel();
		readerFactory.getReader().read(m, url);
		this.getGraph().getTransactionHandler().begin();
		GraphUtil.addInto(this.getGraph(), m.getGraph());
		this.getGraph().getTransactionHandler().commit();
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Model read(Reader reader, String base) {
		Model m = ModelFactory.createDefaultModel();
		readerFactory.getReader().read(m, reader, base);
		this.getGraph().getTransactionHandler().begin();
		GraphUtil.addInto(this.getGraph(), m.getGraph());
		this.getGraph().getTransactionHandler().commit();
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Model read(InputStream reader, String base) {
		Model m = ModelFactory.createDefaultModel();
		readerFactory.getReader().read(m, reader, base);
		this.getGraph().getTransactionHandler().begin();
		GraphUtil.addInto(this.getGraph(), m.getGraph());
		this.getGraph().getTransactionHandler().commit();
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Model read(String url, String lang) {
		Model m = ModelFactory.createDefaultModel();
		readerFactory.getReader(lang).read(m, url);
		this.getGraph().getTransactionHandler().begin();
		GraphUtil.addInto(this.getGraph(), m.getGraph());
		this.getGraph().getTransactionHandler().commit();
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
		this.getGraph().getTransactionHandler().begin();
		GraphUtil.addInto(this.getGraph(), m.getGraph());
		this.getGraph().getTransactionHandler().commit();
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Model read(InputStream reader, String base, String lang) {
		Model m = ModelFactory.createDefaultModel();
		readerFactory.getReader(lang).read(m, reader, base);
		this.getGraph().getTransactionHandler().begin();
		GraphUtil.addInto(this.getGraph(), m.getGraph());
		this.getGraph().getTransactionHandler().commit();
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Model remove(StmtIterator iter) {
		this.getGraph().getTransactionHandler().begin();
		GraphUtil.delete(this.getGraph(), asTriples(iter));
		this.getGraph().getTransactionHandler().commit();
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Model add(Statement[] statements) {
		this.getGraph().getTransactionHandler().begin();
		GraphUtil.add(this.getGraph(), StatementImpl.asTriples(statements));
		this.getGraph().getTransactionHandler().commit();
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
		this.getGraph().getTransactionHandler().begin();
		GraphUtil.delete(this.getGraph(), StatementImpl.asTriples(statements));
		this.getGraph().getTransactionHandler().commit();
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
		public Triple apply(Statement statement) {
			return statement.asTriple();
		}
	};
}
