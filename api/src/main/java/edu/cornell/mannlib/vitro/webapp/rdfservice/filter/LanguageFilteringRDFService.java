/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.filter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelChangedListener;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ResultSetConsumer;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

public class LanguageFilteringRDFService implements RDFService {

    private static final Log log = LogFactory.getLog(LanguageFilteringRDFService.class);
    private RDFService s;
    private List<String> langs;
    private LanguageFilterModel filterModel = new LanguageFilterModel();

    public LanguageFilteringRDFService(RDFService service, List<String> langs) {
        this.s = service;
        this.langs = new AcceptableLanguages(langs);
    }

    @Override
    public boolean changeSetUpdate(ChangeSet changeSet)
            throws RDFServiceException {
        return s.changeSetUpdate(changeSet);
    }

    @Override
    public void newIndividual(String individualURI, String individualTypeURI)
            throws RDFServiceException {
        s.newIndividual(individualURI, individualTypeURI);
    }

    @Override
    public void newIndividual(String individualURI,
            String individualTypeURI, String graphURI)
            throws RDFServiceException {
        s.newIndividual(individualURI, individualTypeURI, graphURI);
    }

    @Override
    public InputStream sparqlConstructQuery(String query,
            ModelSerializationFormat resultFormat)
            throws RDFServiceException {
        Model m = RDFServiceUtils.parseModel(s.sparqlConstructQuery(
                query, resultFormat), resultFormat);
        InputStream in = outputModel(filterModel.filterModel(
                m, langs), resultFormat);
        return in;
    }

    @Override
    public void sparqlConstructQuery(String query, Model model)
            throws RDFServiceException {
        if (model.isEmpty()) {
            s.sparqlConstructQuery(query, model);
            filterModel.filterModel(model, langs);
        } else {
            Model constructedModel = ModelFactory.createDefaultModel();
            s.sparqlConstructQuery(query, constructedModel);
            filterModel.filterModel(constructedModel, langs);
            model.add(constructedModel);
        }
    }

    @Override
    public InputStream sparqlDescribeQuery(String query,
            ModelSerializationFormat resultFormat)
            throws RDFServiceException {
        Model m = RDFServiceUtils.parseModel(s.sparqlDescribeQuery(query, resultFormat), resultFormat);
        return outputModel(filterModel.filterModel(m, langs), resultFormat);
    }

    private InputStream outputModel(Model m, ModelSerializationFormat resultFormat) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        m.write(out, RDFServiceUtils.getSerializationFormatString(resultFormat));
        return new ByteArrayInputStream(out.toByteArray());
    }

	@Override
    public InputStream sparqlSelectQuery(String query,
            ResultFormat resultFormat) throws RDFServiceException {
    	log.debug("sparqlSelectQuery: " + query.replaceAll("\\s+", " "));
        ResultSet resultSet = ResultSetFactory.fromJSON(
                s.sparqlSelectQuery(query, RDFService.ResultFormat.JSON));
        List<QuerySolution> solnList = getSolutionList(resultSet);
        List<String> vars = resultSet.getResultVars();

        // This block loops all of the Query variables;
        //   for each QuerySolution, creates a map of the values of the other variables than the current
        //   'variable' --> a list of RowIndexedLiterals.
        // In this way, all of the QuerySolutions with equal values of their other variables are grouped.
        // This map is used subsequently to filter Literals based on lang
        for (String var : vars) {
            Map<List<RDFNode>, List<RowIndexedLiteral>> nonVarToRowIndexedLiterals = new HashMap<>();

            // First pass of solnList to populate map
            for (int i = 0; i < solnList.size(); i++) {
                QuerySolution s = solnList.get(i);
                if (s == null) {
                    continue;
                }
                RDFNode node = s.get(var);
                if (node == null || !node.isLiteral()) {
                    continue;
                }

                // Create entry representing values other than current 'var' for this QuerySolution
                List<RDFNode> nonVarList = new ArrayList(vars.size() - 1);
                for (String v : vars) {
                    if (!v.equals(var)) {
                        nonVarList.add(s.get(v));
                    }
                }

                List<RowIndexedLiteral> rowIndexedLiterals = nonVarToRowIndexedLiterals.get(nonVarList);
                if (rowIndexedLiterals == null) {
                    rowIndexedLiterals = new ArrayList();
                }
                rowIndexedLiterals.add(new RowIndexedLiteral(node.asLiteral(), i));

                // Add RowIndexedLiterals to the map
                nonVarToRowIndexedLiterals.put(nonVarList, rowIndexedLiterals);
            }

            // Second pass of solnList (via the map) to evaluate candidatesForRemoval
            for (List<RDFNode> key : nonVarToRowIndexedLiterals.keySet()) {
                List<RowIndexedLiteral> candidatesForRemoval = nonVarToRowIndexedLiterals.get(key);
                if (candidatesForRemoval.size() == 1) {
                    continue;
                }
                candidatesForRemoval.sort(new RowIndexedLiteralSortByLang(langs));
                log.debug("sorted RowIndexedLiterals: " + showSortedRILs(candidatesForRemoval));
                Iterator<RowIndexedLiteral> candIt = candidatesForRemoval.iterator();
                String langRegister = null;
                boolean chuckRemaining = false;
                while (candIt.hasNext()) {
                    RowIndexedLiteral rlit = candIt.next();
                    if (chuckRemaining) {
                        solnList.set(rlit.getIndex(), null);
                    } else if (langRegister == null) {
                        langRegister = rlit.getLiteral().getLanguage();
                    } else if (!langRegister.equals(rlit.getLiteral().getLanguage())) {
                        chuckRemaining = true;
                        solnList.set(rlit.getIndex(), null);
                    }
                }
            }
        }
        List<QuerySolution> compactedList = new ArrayList<QuerySolution>();
        for (QuerySolution soln : solnList) {
            if (soln != null) {
                compactedList.add(soln);
            }
        }
        ResultSet filtered = new FilteredResultSet(compactedList, resultSet);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        switch (resultFormat) {
           case CSV:
              ResultSetFormatter.outputAsCSV(outputStream, filtered);
              break;
           case TEXT:
              ResultSetFormatter.out(outputStream, filtered);
              break;
           case JSON:
              ResultSetFormatter.outputAsJSON(outputStream, filtered);
              break;
           case XML:
              ResultSetFormatter.outputAsXML(outputStream, filtered);
              break;
           default:
              throw new RDFServiceException("unrecognized result format");
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    @Override
    public void sparqlSelectQuery(String query, ResultSetConsumer consumer) throws RDFServiceException {
        log.debug("sparqlSelectQuery: " + query.replaceAll("\\s+", " "));
        s.sparqlSelectQuery(query, new ResultSetConsumer.Chaining(consumer) {
            List<String> vars;
            List<QuerySolution> solnList = new ArrayList<>();

            @Override
            protected void processQuerySolution(QuerySolution qs) {
                solnList.add(qs);
            }

            @Override
            protected void startProcessing() {
                vars = getResultVars();
            }

            @Override
            protected void endProcessing() {
                chainStartProcessing();

                // This block loops all of the Query variables;
                //   for each QuerySolution, creates a map of the values of the other variables than the current
                //   'variable' --> a list of RowIndexedLiterals.
                // In this way, all of the QuerySolutions with equal values of their other variables are grouped.
                // This map is used subsequently to filter Literals based on lang
                for (String var : vars) {
                    Map<List<RDFNode>, List<RowIndexedLiteral>> nonVarToRowIndexedLiterals = new HashMap<>();

                    // First pass of solnList to populate map
                    for (int i = 0; i < solnList.size(); i++) {
                        QuerySolution s = solnList.get(i);
                        if (s == null) {
                            continue;
                        }
                        RDFNode node = s.get(var);
                        if (node == null || !node.isLiteral()) {
                            continue;
                        }

                        // Create entry representing values other than current 'var' for this QuerySolution
                        List<RDFNode> nonVarList = new ArrayList(vars.size() - 1);
                        for (String v : vars) {
                            if (!v.equals(var)) {
                                nonVarList.add(s.get(v));
                            }
                        }

                        List<RowIndexedLiteral> rowIndexedLiterals = nonVarToRowIndexedLiterals.get(nonVarList);
                        if (rowIndexedLiterals == null) {
                            rowIndexedLiterals = new ArrayList();
                        }
                        rowIndexedLiterals.add(new RowIndexedLiteral(node.asLiteral(), i));

                        // Add RowIndexedLiterals to the map
                        nonVarToRowIndexedLiterals.put(nonVarList, rowIndexedLiterals);
                    }

                    // Second pass of solnList (via the map) to evaluate candidatesForRemoval
                    for (List<RDFNode> key : nonVarToRowIndexedLiterals.keySet()) {
                        List<RowIndexedLiteral> candidatesForRemoval = nonVarToRowIndexedLiterals.get(key);
                        if (candidatesForRemoval.size() == 1) {
                            continue;
                        }
                        candidatesForRemoval.sort(new RowIndexedLiteralSortByLang(langs));
                        log.debug("sorted RowIndexedLiterals: " + showSortedRILs(candidatesForRemoval));
                        Iterator<RowIndexedLiteral> candIt = candidatesForRemoval.iterator();
                        String langRegister = null;
                        boolean chuckRemaining = false;
                        while (candIt.hasNext()) {
                            RowIndexedLiteral rlit = candIt.next();
                            if (chuckRemaining) {
                                solnList.set(rlit.getIndex(), null);
                            } else if (langRegister == null) {
                                langRegister = rlit.getLiteral().getLanguage();
                            } else if (!langRegister.equals(rlit.getLiteral().getLanguage())) {
                                chuckRemaining = true;
                                solnList.set(rlit.getIndex(), null);
                            }
                        }
                    }
                }

                for (QuerySolution soln : solnList) {
                    if (soln != null) {
                        chainProcessQuerySolution(soln);
                    }
                }

                chainEndProcessing();
            }
        });
    }

	private String showSortedRILs(List<RowIndexedLiteral> candidatesForRemoval) {
		List<String> langstrings = new ArrayList<String>();
		for (RowIndexedLiteral ril: candidatesForRemoval) {
			langstrings.add(ril.getLiteral().getLanguage());
		}
		return langstrings.toString();
	}

	private class RowIndexedLiteral {

        private Literal literal;
        private int index;

        public RowIndexedLiteral(Literal literal, int index) {
            this.literal = literal;
            this.index = index;
        }

        public Literal getLiteral() {
            return this.literal;
        }

        public int getIndex() {
            return index;
        }

    }

    private List<QuerySolution> getSolutionList(ResultSet resultSet) {
        List<QuerySolution> solnList = new ArrayList<QuerySolution>();
        while (resultSet.hasNext()) {
            QuerySolution soln = resultSet.nextSolution();
            solnList.add(soln);
        }
        return solnList;
    }

    @Override
    public boolean sparqlAskQuery(String query) throws RDFServiceException {
        return s.sparqlAskQuery(query);
    }

    @Override
    public List<String> getGraphURIs() throws RDFServiceException {
        return s.getGraphURIs();
    }

    @Override
    public void getGraphMetadata() throws RDFServiceException {
        s.getGraphMetadata();
    }

    @Override
    public String getDefaultWriteGraphURI() throws RDFServiceException {
        return s.getDefaultWriteGraphURI();
    }

    @Override
	public void serializeAll(OutputStream outputStream)
			throws RDFServiceException {
    	s.serializeAll(outputStream);
	}

	@Override
	public void serializeGraph(String graphURI, OutputStream outputStream)
			throws RDFServiceException {
		s.serializeGraph(graphURI, outputStream);
	}

	@Override
	public boolean isEquivalentGraph(String graphURI,
			InputStream serializedGraph,
			ModelSerializationFormat serializationFormat) throws RDFServiceException {
		return s.isEquivalentGraph(graphURI, serializedGraph, serializationFormat);
	}

    @Override
    public boolean isEquivalentGraph(String graphURI,
                                     Model graph) throws RDFServiceException {
        return s.isEquivalentGraph(graphURI, graph);
    }

    @Override
    public void registerListener(ChangeListener changeListener)
            throws RDFServiceException {
        s.registerListener(changeListener);
    }

    @Override
    public void unregisterListener(ChangeListener changeListener)
            throws RDFServiceException {
        s.unregisterListener(changeListener);
    }

    @Override
    public void registerJenaModelChangedListener(ModelChangedListener changeListener)
            throws RDFServiceException {
        s.registerJenaModelChangedListener(changeListener);
    }

    @Override
    public void unregisterJenaModelChangedListener(ModelChangedListener changeListener)
            throws RDFServiceException {
        s.unregisterJenaModelChangedListener(changeListener);
    }


    @Override
    public ChangeSet manufactureChangeSet() {
        return s.manufactureChangeSet();
    }

    @Override
    public long countTriples(RDFNode subject, RDFNode predicate, RDFNode object) throws RDFServiceException {
        return s.countTriples(subject, predicate, object);
    }

    @Override
    public Model getTriples(RDFNode subject, RDFNode predicate, RDFNode object, long limit, long offset) throws RDFServiceException {
        return s.getTriples(subject, predicate, object, limit, offset);
    }

    @Override
    public boolean preferPreciseOptionals() {
        return s.preferPreciseOptionals();
    }

    @Override
    public void close() {
        s.close();
    }

    private class RowIndexedLiteralSortByLang extends LangSort implements Comparator<RowIndexedLiteral> {

        public RowIndexedLiteralSortByLang(List<String> langs) {            
            super(langs);
        }
        
        public int compare(RowIndexedLiteral rilit1, RowIndexedLiteral rilit2) {
            if (rilit1 == null || rilit2 == null) {
                return 0;
            }

            String t1lang = rilit1.getLiteral().getLanguage();
            String t2lang = rilit2.getLiteral().getLanguage();

            return compareLangs(t1lang, t2lang);
        }
    }

	/*
	 * UQAM-Linguistic-Management Useful among other things to transport the linguistic context in the service
	 * (non-Javadoc)
	 * @see edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService#setVitroRequest(edu.cornell.mannlib.vitro.webapp.controller.VitroRequest)
	 */
	private VitroRequest vitroRequest;

	public void setVitroRequest(VitroRequest vitroRequest) {
		this.vitroRequest = vitroRequest;
	}

	public VitroRequest getVitroRequest() {
		return vitroRequest;
	}

}



