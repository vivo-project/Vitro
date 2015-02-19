/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.resultset.XMLInput;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

public abstract class RDFServiceImpl implements RDFService {
	
	private static final Log log = LogFactory.getLog(RDFServiceImpl.class);
	protected static final String BNODE_ROOT_QUERY = 
	        "SELECT DISTINCT ?s WHERE { ?s ?p ?o OPTIONAL { ?ss ?pp ?s } FILTER (!isBlank(?s) || !bound(?ss)) }";
	
	protected String defaultWriteGraphURI;
	protected List<ChangeListener> registeredListeners = new CopyOnWriteArrayList<ChangeListener>();
    	
	@Override
	public void newIndividual(String individualURI, 
			                  String individualTypeURI) throws RDFServiceException {
	
       newIndividual(individualURI, individualTypeURI, defaultWriteGraphURI);
	}
		
    @Override
    public void newIndividual(String individualURI, 
                              String individualTypeURI, 
                              String graphURI) throws RDFServiceException {
    
       StringBuffer containsQuery = new StringBuffer("ASK { \n");
       if (graphURI != null) {
           containsQuery.append("  GRAPH <" + graphURI + "> { ");
       }
       containsQuery.append("<");   
       containsQuery.append(individualURI);
       containsQuery.append("> ");  
       containsQuery.append("?p ?o");
       if (graphURI != null) {
           containsQuery.append(" } \n");
       }
       containsQuery.append("\n}");
       
       if (sparqlAskQuery(containsQuery.toString())) {
            throw new RDFServiceException("individual already exists");
       } else {
            Triple triple = new Triple(NodeFactory.createURI(individualURI), RDF.type.asNode(), NodeFactory.createURI(individualTypeURI));
            //addTriple(triple, graphURI);
            ChangeSet cs = this.manufactureChangeSet();
            cs.addAddition(new ByteArrayInputStream(
                    sparqlTriple(triple).getBytes()), ModelSerializationFormat.N3, graphURI);
            changeSetUpdate(cs);
       }    
    }
	
	@Override
	public String getDefaultWriteGraphURI() throws RDFServiceException {
        return defaultWriteGraphURI;
	}
		
	@Override
	public synchronized void registerListener(ChangeListener changeListener) throws RDFServiceException {
		
		if (!registeredListeners.contains(changeListener)) {
		   registeredListeners.add(changeListener);
		}
	}
	
	@Override
	public synchronized void unregisterListener(ChangeListener changeListener) throws RDFServiceException {
		registeredListeners.remove(changeListener);
	}

	public synchronized List<ChangeListener> getRegisteredListeners() {
	    return this.registeredListeners;
	}
	
	@Override
	public ChangeSet manufactureChangeSet() {
		return new ChangeSetImpl();
	}    

	// I switched the following two methods back to public so they could be 
	// used by the ListeningGraph, which is common to both implementations.
	// This could probably be improved later.  BJL
	
    public void notifyListeners(Triple triple, ModelChange.Operation operation, String graphURI) {    			
        Iterator<ChangeListener> iter = registeredListeners.iterator();

        while (iter.hasNext()) {
            ChangeListener listener = iter.next();
            if (operation == ModelChange.Operation.ADD) {
                listener.addedStatement(sparqlTriple(triple), graphURI);
            } else {
                listener.removedStatement(sparqlTriple(triple).toString(), graphURI);   			
            }
        }
    }
    
    public void notifyListenersOfEvent(Object event) {       
        Iterator<ChangeListener> iter = registeredListeners.iterator();

        while (iter.hasNext()) {
            ChangeListener listener = iter.next();
            // TODO what is the graphURI parameter for?
            listener.notifyEvent(null, event);
        }
    }    
    
    protected boolean isPreconditionSatisfied(String query, 
            RDFService.SPARQLQueryType queryType)
                    throws RDFServiceException {
        Model model = ModelFactory.createDefaultModel();
        switch (queryType) {
            case DESCRIBE:
                model.read(sparqlDescribeQuery(query,RDFService.ModelSerializationFormat.N3), null);
                return !model.isEmpty();
            case CONSTRUCT:
                model.read(sparqlConstructQuery(query,RDFService.ModelSerializationFormat.N3), null);
                return !model.isEmpty();
            case SELECT:
                return sparqlSelectQueryHasResults(query);
            case ASK:
                return sparqlAskQuery(query);
            default:
                throw new RDFServiceException("unrecognized SPARQL query type");  
        }       
    }

    protected static String getSerializationFormatString(RDFService.ModelSerializationFormat format) {
        switch (format) {
            case RDFXML: 
                return "RDF/XML";
            case N3: 
                return "TTL";
            case NTRIPLE: 
                return "N-TRIPLE";    
            default: 
                log.error("unexpected format in getFormatString");
                return null;
        }
    }

    protected boolean sparqlSelectQueryHasResults(String queryStr) throws RDFServiceException {
        ResultSet rs = XMLInput.fromXML(sparqlSelectQuery(queryStr, ResultFormat.XML));
        return rs.hasNext();         
    }

    protected static String sparqlTriple(Triple triple) {
        StringBuffer serializedTriple = new StringBuffer();
        serializedTriple.append(sparqlNodeUpdate(triple.getSubject(), ""));
        serializedTriple.append(" ");
        serializedTriple.append(sparqlNodeUpdate(triple.getPredicate(), ""));
        serializedTriple.append(" ");
        serializedTriple.append(sparqlNodeUpdate(triple.getObject(), ""));
        serializedTriple.append(" .");
        return serializedTriple.toString();

    }
    
    protected static String sparqlNodeUpdate(Node node, String varName) {
        if (node.isBlank()) {
            return "_:" + node.getBlankNodeLabel().replaceAll("\\W", "");
        } else {
            return sparqlNode(node, varName);
        }
    }

    protected static String sparqlNode(Node node, String varName) {
        if (node == null || node.isVariable()) {
            return varName;
        } else if (node.isBlank()) {
            return "<fake:blank>"; // or throw exception?
        } else if (node.isURI()) {
            StringBuffer uriBuff = new StringBuffer();
            return uriBuff.append("<").append(node.getURI()).append(">").toString();
        } else if (node.isLiteral()) {
            StringBuffer literalBuff = new StringBuffer();
            literalBuff.append("\"");
            pyString(literalBuff, node.getLiteralLexicalForm());
            literalBuff.append("\"");
            if (node.getLiteralDatatypeURI() != null) {
                literalBuff.append("^^<").append(node.getLiteralDatatypeURI()).append(">");
            } else if (node.getLiteralLanguage() != null && node.getLiteralLanguage().length() > 0) {
                literalBuff.append("@").append(node.getLiteralLanguage());
            }
            return literalBuff.toString();
        } else {
            return varName;
        }
    }
       
     // see http://www.python.org/doc/2.5.2/ref/strings.html
     // or see jena's n3 grammar jena/src/com/hp/hpl/jena/n3/n3.g  
    protected static void pyString(StringBuffer sbuff, String s)  {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            // Escape escapes and quotes
            if (c == '\\' || c == '"' )
            {
                sbuff.append('\\') ;
                sbuff.append(c) ;
                continue ;
            }            

            // Whitespace                        
            if (c == '\n'){ sbuff.append("\\n");continue; }
            if (c == '\t'){ sbuff.append("\\t");continue; }
            if (c == '\r'){ sbuff.append("\\r");continue; }
            if (c == '\f'){ sbuff.append("\\f");continue; }                            
            if (c == '\b'){ sbuff.append("\\b");continue; }
            if( c == 7 )  { sbuff.append("\\a");continue; }
            
            // Output as is (subject to UTF-8 encoding on output that is)
            sbuff.append(c) ;
        }
    }
    
    /**
     * Returns a pair of models.  The first contains any statement containing at 
     * least one blank node.  The second contains all remaining statements.
     * @param g
     * @return
     */
    
    protected Model[] separateStatementsWithBlankNodes(Model gm) {
        Model blankNodeModel = ModelFactory.createDefaultModel();
        Model nonBlankNodeModel = ModelFactory.createDefaultModel();
        StmtIterator sit = gm.listStatements();
        while (sit.hasNext()) {
            Statement stmt = sit.nextStatement();
            if (!stmt.getSubject().isAnon() && !stmt.getObject().isAnon()) {
                nonBlankNodeModel.add(stmt);
            } else {
                blankNodeModel.add(stmt);
            }
        }
        Model[] result = new Model[2];
        result[0] = blankNodeModel;
        result[1] = nonBlankNodeModel;
        return result;
    }
    
    protected Query createQuery(String queryString) {
        List<Syntax> syntaxes = Arrays.asList(
                Syntax.defaultQuerySyntax, Syntax.syntaxSPARQL_11, 
                Syntax.syntaxSPARQL_10, Syntax.syntaxSPARQL, Syntax.syntaxARQ);
        Query q = null;
        Iterator<Syntax> syntaxIt = syntaxes.iterator(); 
        while (q == null) {
            Syntax syntax = syntaxIt.next();
            try {
               q = QueryFactory.create(queryString, syntax);  
            } catch (QueryParseException e) {
               if (!syntaxIt.hasNext()) {
                   throw(e);
               }
            }
        }
        return q;
    }

	@Override
	public String toString() {
		return ToString.simpleName(this) + "[" + ToString.hashHex(this) + "]";
	}
    
}
