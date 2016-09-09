/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceGraph;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SparqlGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFReaderF;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.RDFReaderFImpl;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.shared.WrappedIOException;
import org.apache.jena.sparql.graph.GraphFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.List;

public class BulkUpdatingOntModel extends AbstractOntModelDecorator {
    private static final RDFReaderF readerFactory = new RDFReaderFImpl();
    private AbstractBulkUpdater updater;

    protected BulkUpdatingOntModel(OntModel m) {
        super(m);
        Graph graph = m.getGraph();
        if (graph instanceof RDFServiceGraph) {
            updater = new RDFServiceBulkUpdater((RDFServiceGraph)graph);
        } else if (graph instanceof SparqlGraph) {
            updater = new SparqlBulkUpdater((SparqlGraph)graph);
        } else {
            updater  = null;
        }
    }

    protected BulkUpdatingOntModel(OntModel m, Graph graph) {
        super(m);
        if (graph instanceof RDFServiceGraph) {
            updater = new RDFServiceBulkUpdater((RDFServiceGraph)graph);
        } else if (graph instanceof SparqlGraph) {
            updater = new SparqlBulkUpdater((SparqlGraph)graph);
        } else {
            updater  = null;
        }
    }

    @Override
    public Model add(StmtIterator iter) {
        if (updater != null && iter != null) {
            Graph g = GraphFactory.createPlainGraph();
            while (iter.hasNext()) {
                g.add(iter.nextStatement().asTriple());
            }
            updater.add(g);
        } else {
            super.add(iter);
        }
        return this;
    }

    @Override
    public Model add(Model m) {
        if (updater != null && m != null) {
            updater.add(m.getGraph());
        } else {
            super.add(m);
        }
        return this;
    }

    @Override
    public Model add(Statement[] statements) {
        if (updater != null && statements != null) {
            Graph g = GraphFactory.createPlainGraph();
            for (Statement s : statements) {
                g.add(s.asTriple());
            }
            updater.add(g);
        } else {
            super.add(statements);
        }
        return this;
    }

    @Override
    public Model add(List<Statement> statements) {
        add(statements.toArray(new Statement[statements.size()]));
        return this;
    }

    @Override
    public Model read(String url) {
        Model m = ModelFactory.createDefaultModel();
        readerFactory.getReader().read(m, url);
        return add(m);
    }

    @Override
    public Model read(Reader reader, String base) {
        Model m = ModelFactory.createDefaultModel();
        readerFactory.getReader().read(m, reader, base);
        return add(m);
    }

    @Override
    public Model read(InputStream reader, String base) {
        Model m = ModelFactory.createDefaultModel();
        readerFactory.getReader().read(m, reader, base);
        return add(m);
    }

    @Override
    public Model read(String url, String lang) {
        Model m = ModelFactory.createDefaultModel();
        readerFactory.getReader(lang).read(m, url);
        return add(m);
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

    @Override
    public Model read(Reader reader, String base, String lang) {
        Model m = ModelFactory.createDefaultModel();
        readerFactory.getReader(lang).read(m, reader, base);
        return add(m);
    }

    @Override
    public Model read(InputStream reader, String base, String lang) {
        Model m = ModelFactory.createDefaultModel();
        readerFactory.getReader(lang).read(m, reader, base);
        return add(m);
    }

    @Override
    public Model remove(StmtIterator iter) {
        if (updater != null && iter != null) {
            Graph g = GraphFactory.createPlainGraph();
            while (iter.hasNext()) {
                g.add(iter.nextStatement().asTriple());
            }
            updater.remove(g);
        } else {
            super.remove(iter);
        }
        return this;
    }

    @Override
    public Model remove(Model m) {
        if (updater != null && m != null) {
            updater.remove(m.getGraph());
        } else {
            super.remove(m);
        }
        return this;
    }

    @Override
    public Model remove(Statement[] statements) {
        if (updater != null && statements != null) {
            Graph g = GraphFactory.createPlainGraph();
            for (Statement s : statements) {
                g.add(s.asTriple());
            }
            updater.remove(g);
        } else {
            super.remove(statements);
        }
        return this;
    }

    @Override
    public Model remove(List<Statement> statements) {
        if (updater != null && statements != null) {
            Graph g = GraphFactory.createPlainGraph();
            for (Statement s : statements) {
                g.add(s.asTriple());
            }
            updater.remove(g);
        } else {
            super.remove(statements);
        }
        return this;
    }

    @Override
    public Model removeAll() {
        if (updater != null) {
            updater.removeAll();
        } else {
            super.removeAll();
        }
        return this;
    }
}
