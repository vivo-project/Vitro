package edu.cornell.mannlib.vitro.webapp.rdfservice.filter;

import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.AbstractGraphDecorator;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * A graph decorator that filters find() results according to a list of 
 * preferred language strings
 */
public class LanguageFilteringGraph extends AbstractGraphDecorator 
        implements Graph {

    private List<String> langs;
    private LanguageFilterModel filterModel = new LanguageFilterModel();
    private String toString;
    
    /**
     * Return a graph wrapped in a decorator that will filter find() results
     * according to the supplied list of acceptable languages  
     * @param g the graph to wrap with language awareness
     * @param preferredLanguages a list of preferred language strings
     */
    protected LanguageFilteringGraph(Graph g, List<String> preferredLanguages) {
        super(g);
        this.langs = preferredLanguages;
        this.toString = "LanguageFilteringGraph[wrapping "
                + ToString.graphToString(g) + "]";
    }
    
    @Override 
    public String toString() {
        return this.toString;
    }

    @Override
    public ExtendedIterator<Triple> find(Triple arg0) {
        return filter(super.find(arg0));
        
    }

    @Override
    public ExtendedIterator<Triple> find(Node arg0, Node arg1, Node arg2) {
        return filter(super.find(arg0, arg1, arg2));
    }
    
    private ExtendedIterator<Triple> filter(ExtendedIterator<Triple> triples) {
        Graph tmp = new GraphMem();
        while(triples.hasNext()) {
            Triple t = triples.next();
            tmp.add(t);
        }
        Model filteredModel = filterModel.filterModel(
                ModelFactory.createModelForGraph(tmp), langs);
        return filteredModel.getGraph().find();
    }

}
