/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import static edu.cornell.mannlib.vitro.webapp.dao.jena.SparqlGraph.sparqlNode;
import static edu.cornell.mannlib.vitro.webapp.dao.jena.SparqlGraph.sparqlNodeDelete;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryConfig;

public class JenaModelUtils {

	public static final String BNODE_ROOT_QUERY =
	        "SELECT DISTINCT ?s WHERE { ?s ?p ?o OPTIONAL { ?ss ?pp ?s } FILTER (!isBlank(?s) || !bound(?ss)) }";
    private static final Log log = LogFactory.getLog(JenaModelUtils.class.getName());

    private static final Set<String>  nonIndividualTypeURIs ;

    static {
        nonIndividualTypeURIs = new HashSet<String>();
        nonIndividualTypeURIs.add(OWL.Class.getURI());
        nonIndividualTypeURIs.add(OWL.Restriction.getURI());
        nonIndividualTypeURIs.add(OWL.ObjectProperty.getURI());
        nonIndividualTypeURIs.add(OWL.DatatypeProperty.getURI());
        nonIndividualTypeURIs.add(OWL.AnnotationProperty.getURI());
        nonIndividualTypeURIs.add(OWL.Ontology.getURI());
        nonIndividualTypeURIs.add(RDFS.Class.getURI());
        nonIndividualTypeURIs.add(RDF.Property.getURI());
    }

    /**
     * Creates a set of vitro:ClassGroup resources for each root class in
     * an ontology.  Also creates annotations to place each root class and all
     * of its children in the appropriate groups.  In the case of multiple
     * inheritance, classgroup assignment will be arbitrary.
     * @param wadf DAO Factory
     * @param tboxModel containing ontology classes
     * @return resultArray of OntModels, where resultArray[0] is the model containing
     * the triples about the classgroups, and resultArray[1] is the model containing
     * annotation triples assigning ontology classes to classgroups.
     */
    public synchronized static OntModel[] makeClassGroupsFromRootClasses(
            WebappDaoFactory wadf, Model tboxModel) {

        OntModel ontModel = ModelFactory.createOntologyModel(
                OntModelSpec.OWL_DL_MEM, tboxModel);
        OntModel modelForClassgroups = ModelFactory.createOntologyModel(
                OntModelSpec.OWL_DL_MEM);
        OntModel modelForClassgroupAnnotations = ModelFactory.createOntologyModel(
                OntModelSpec.OWL_DL_MEM);
        SimpleOntModelSelector oms = new SimpleOntModelSelector();
        oms.setTBoxModel(ontModel);
        oms.setApplicationMetadataModel(modelForClassgroups);
        WebappDaoFactoryConfig config = new WebappDaoFactoryConfig();
        config.setDefaultNamespace(wadf.getDefaultNamespace());
        WebappDaoFactory myWebappDaoFactory = new WebappDaoFactoryJena(
                new SimpleOntModelSelector(ontModel), config, null);

        Resource classGroupClass = ResourceFactory.createResource(
                VitroVocabulary.CLASSGROUP);
        Property inClassGroupProperty = ResourceFactory.createProperty(
                VitroVocabulary.IN_CLASSGROUP);

        ontModel.enterCriticalSection(Lock.READ);
        try {
            try {
                List<VClass> rootClasses = myWebappDaoFactory.getVClassDao()
                        .getRootClasses();
                for (VClass rootClass : rootClasses) {
                    Individual classGroup = modelForClassgroups.createIndividual(
                            wadf.getDefaultNamespace() + "vitroClassGroup" +
                                    rootClass.getLocalName(), classGroupClass);
                    classGroup.setLabel(rootClass.getName(), null);

                    Resource rootClassRes = modelForClassgroupAnnotations.getResource(
                            rootClass.getURI());
                    modelForClassgroupAnnotations.add(
                            rootClassRes, inClassGroupProperty, classGroup);
                    for (String childURI : myWebappDaoFactory.getVClassDao()
                            .getAllSubClassURIs(rootClass.getURI())) {
                        Resource childClass = modelForClassgroupAnnotations
                                .getResource(childURI);
                        if (!modelForClassgroupAnnotations.contains(
                                childClass, inClassGroupProperty, (RDFNode) null)) {
                            childClass.addProperty(inClassGroupProperty, classGroup);
                        }
                    }
                }
            } catch (Exception e) {
                String errMsg = "Unable to create class groups automatically " +
                        "based on class hierarchy";
                log.error(errMsg, e);
                throw new RuntimeException(errMsg, e);
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
        OntModel[] resultArray = new OntModel[2];
        resultArray[0] = modelForClassgroups;
        resultArray[1] = modelForClassgroupAnnotations;
        return resultArray;
    }

    private final OntModelSpec DEFAULT_ONT_MODEL_SPEC = OntModelSpec.OWL_MEM;
    private final boolean NORMAL = false;
    private final boolean AGGRESSIVE = true;


    public OntModel extractTBox( Model inputModel ) {
        return extractTBox(inputModel, null);
    }

    public OntModel extractTBox( Model inputModel, boolean MODE ) {
        Dataset dataset = DatasetFactory.create(inputModel);
        return extractTBox(dataset, null, null, MODE);
    }

    public OntModel extractTBox( Model inputModel, String namespace ) {
        Dataset dataset = DatasetFactory.create(inputModel);
        return extractTBox( dataset, namespace, null, NORMAL );
    }

    public OntModel extractTBox( Dataset dataset, String namespace, String graphURI) {
        return extractTBox( dataset, namespace, graphURI, NORMAL);
    }

    public OntModel extractTBox( Dataset dataset, String namespace, String graphURI, boolean mode ) {
        OntModel tboxModel = ModelFactory.createOntologyModel(DEFAULT_ONT_MODEL_SPEC);

        List<String> queryStrList = new LinkedList<String>();

        // Use SPARQL DESCRIBE queries to extract the RDF for named ontology entities

        queryStrList.add( makeDescribeQueryStr( OWL.Class.getURI(), namespace, graphURI ) );
        queryStrList.add( makeDescribeQueryStr( OWL.Restriction.getURI(), namespace, graphURI ) );
        queryStrList.add( makeDescribeQueryStr( OWL.ObjectProperty.getURI(), namespace, graphURI ) );
        queryStrList.add( makeDescribeQueryStr( OWL.DatatypeProperty.getURI(), namespace, graphURI ) );
        queryStrList.add( makeDescribeQueryStr( OWL.AnnotationProperty.getURI(), namespace, graphURI ) );
        // if we're using to a hash namespace, the URI of the Ontology resource will be
        // that namespace minus the final hash mark.
        if ( namespace != null && namespace.endsWith("#") ) {
            queryStrList.add( makeDescribeQueryStr( OWL.Ontology.getURI(), namespace.substring(0,namespace.length()-1), graphURI ) );
        } else {
            queryStrList.add( makeDescribeQueryStr( OWL.Ontology.getURI(), namespace, graphURI ) );
        }

        // Perform the SPARQL DESCRIBEs
        for ( String queryStr : queryStrList ) {
            Query tboxSparqlQuery = QueryFactory.create(queryStr);
            QueryExecution qe = QueryExecutionFactory.create(tboxSparqlQuery,dataset);
            try {
                dataset.getLock().enterCriticalSection(Lock.READ);
                qe.execDescribe(tboxModel);
            } finally {
                dataset.getLock().leaveCriticalSection();
            }
        }

        // Perform possibly-redundant extraction to try ensure we don't miss
        // individual axioms floating around.  We still might miss things;
        // this approach isn't perfect.
        if (mode == AGGRESSIVE) {
            tboxModel.add(construct(dataset, namespace, graphURI, RDFS.subClassOf));
            tboxModel.add(construct(dataset, namespace, graphURI, RDFS.subPropertyOf));
            tboxModel.add(construct(dataset, namespace, graphURI, OWL.equivalentClass));
            tboxModel.add(construct(dataset, namespace, graphURI, OWL.unionOf));
            tboxModel.add(construct(dataset, namespace, graphURI, OWL.intersectionOf));
            tboxModel.add(construct(dataset, namespace, graphURI, OWL.complementOf));
            tboxModel.add(construct(dataset, namespace, graphURI, OWL.onProperty));
            tboxModel.add(construct(dataset, namespace, graphURI, OWL.allValuesFrom));
            tboxModel.add(construct(dataset, namespace, graphURI, OWL.someValuesFrom));
            tboxModel.add(construct(dataset, namespace, graphURI, OWL.hasValue));
            tboxModel.add(construct(dataset, namespace, graphURI, OWL.minCardinality));
            tboxModel.add(construct(dataset, namespace, graphURI, OWL.maxCardinality));
            tboxModel.add(construct(dataset, namespace, graphURI, OWL.cardinality));
            tboxModel.add(construct(dataset, namespace, graphURI, OWL.disjointWith));
            tboxModel.add(construct(dataset, namespace, graphURI, ResourceFactory.createResource(
                                                                  VitroVocabulary.DISPLAY_LIMIT)));
            tboxModel.add(construct(dataset, namespace, graphURI, ResourceFactory.createResource(
                                                                  VitroVocabulary.DISPLAY_RANK_ANNOT)));
            tboxModel.add(construct(dataset, namespace, graphURI, ResourceFactory.createResource(
                                                                  VitroVocabulary.IN_CLASSGROUP)));
            tboxModel.add(construct(dataset, namespace, graphURI, ResourceFactory.createResource(
                                                                  VitroVocabulary.IN_CLASSGROUP)));
            tboxModel.add(construct(dataset, namespace, graphURI, ResourceFactory.createResource(
                                                                  VitroVocabulary.PROPERTY_INPROPERTYGROUPANNOT)));
            tboxModel.add(construct(dataset, namespace, graphURI, ResourceFactory.createResource(
                                                                  VitroVocabulary.DESCRIPTION_ANNOT)));
            tboxModel.add(construct(dataset, namespace, graphURI, ResourceFactory.createResource(
                                                                  VitroVocabulary.SHORTDEF)));
            tboxModel.add(construct(dataset, namespace, graphURI, ResourceFactory.createResource(
                                                                  VitroVocabulary.EXAMPLE_ANNOT)));
            tboxModel.add(construct(dataset, namespace, graphURI, ResourceFactory.createResource(
                                                                  VitroVocabulary.vitroURI + "extendedLinkedData")));
            tboxModel.add(construct(dataset, namespace, graphURI, ResourceFactory.createResource(
                                                                  VitroVocabulary.PROPERTY_OFFERCREATENEWOPTIONANNOT)));
            tboxModel.add(construct(dataset, namespace, graphURI, ResourceFactory.createResource(
                                                                  VitroVocabulary.PROPERTY_COLLATEBYSUBCLASSANNOT)));
            tboxModel.add(construct(dataset, namespace, graphURI, ResourceFactory.createResource(
                                                                  VitroVocabulary.PROPERTY_CUSTOM_LIST_VIEW_ANNOT)));
            tboxModel.add(construct(dataset, namespace, graphURI, ResourceFactory.createResource(
                                                                  VitroVocabulary.PROPERTY_CUSTOMDISPLAYVIEWANNOT)));
            tboxModel.add(construct(dataset, namespace, graphURI, ResourceFactory.createResource(
                                                                  VitroVocabulary.PROPERTY_CUSTOMENTRYFORMANNOT)));
            tboxModel.add(construct(dataset, namespace, graphURI, ResourceFactory.createResource(
                                                                  VitroVocabulary.PROPERTY_CUSTOMSEARCHVIEWANNOT)));
            tboxModel.add(construct(dataset, namespace, graphURI, ResourceFactory.createResource(
                                                                  VitroVocabulary.PROPERTY_CUSTOMSHORTVIEWANNOT)));
            tboxModel.add(construct(dataset, namespace, graphURI, ResourceFactory.createResource(
                                                                  VitroVocabulary.PROPERTY_ENTITYSORTDIRECTION)));
            tboxModel.add(construct(dataset, namespace, graphURI, ResourceFactory.createResource(
                                                                  VitroVocabulary.PROPERTY_ENTITYSORTFIELD)));
            tboxModel.add(construct(dataset, namespace, graphURI, ResourceFactory.createResource(
                                                                  VitroVocabulary.PROPERTY_OBJECTINDIVIDUALSORTPROPERTY)));
            tboxModel.add(construct(dataset, namespace, graphURI, ResourceFactory.createResource(
                                                                  VitroVocabulary.PROPERTY_SELECTFROMEXISTINGANNOT)));
        }
        return tboxModel;
    }

    private Model construct(Dataset dataset,
                            String namespace,
                            String graphURI,
                            Resource property) {
        dataset.getLock().enterCriticalSection(Lock.READ);
        try {
            StringBuilder buff = new StringBuilder();
            buff.append("CONSTRUCT { \n").append("  ?res <").append(property.getURI()).append("> ?o } WHERE { \n");
            if (graphURI != null) {
                buff.append("    GRAPH ").append(graphURI).append(" { \n");
            }
            buff.append("      ?res <").append(property.getURI()).append("> ?o \n");
            buff.append(getNamespaceFilter(namespace));
            if (graphURI != null) {
                buff.append("    } \n");
            }
            buff.append("}");
            Query constructProp = QueryFactory.create(buff.toString());
            QueryExecution qe = QueryExecutionFactory.create(constructProp, dataset);
            try {
                return qe.execConstruct();
            } finally {
                qe.close();
            }
        } finally {
            dataset.getLock().leaveCriticalSection();
        }
    }

    private String makeDescribeQueryStr( String typeURI, String namespace ) {
        return makeDescribeQueryStr( typeURI, namespace, null );
    }

    private String makeDescribeQueryStr( String typeURI, String namespace, String graphURI ) {

        StringBuilder describeQueryStrBuff = new StringBuilder()
            .append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n")
            .append("DESCRIBE ?res WHERE { \n");
            if (graphURI != null) {
                describeQueryStrBuff.append("GRAPH ").append(graphURI).append("{ \n");
            }
            describeQueryStrBuff
            .append("    ?res rdf:type <").append(typeURI).append("> . \n");

            describeQueryStrBuff
            .append("    FILTER (!isBlank(?res)) \n")

            .append(getNamespaceFilter(namespace));

        if (graphURI != null) {
            describeQueryStrBuff
            .append("} \n");
        }

        describeQueryStrBuff.append("} \n");

        return describeQueryStrBuff.toString();

    }

    private String getNamespaceFilter(String namespace) {
        StringBuilder buff = new StringBuilder();
        if (namespace == null) {
            // exclude resources in the Vitro internal namespace or in the
            // OWL namespace, but allow all others
            buff
            .append("    FILTER (REPLACE(STR(?res),\"^(.*)(#)(.*)$\", \"$1$2\") != \"")
            .append("http://www.w3.org/2002/07/owl#")
            .append("\") \n")
            .append("    FILTER (?res != <")
            .append("http://www.w3.org/2002/07/owl")
            .append(">) \n");
        } else {
            // limit resources to those in the supplied namespace
            buff
            .append("    FILTER (regex(str(?res), \"^")
            .append(namespace)
            .append("\")) \n");
        }
        return buff.toString();
    }

    public Model extractABox(Model inputModel){
        Dataset dataset = DatasetFactory.create(inputModel);
        return extractABox(dataset, null, null);
    }

    public Model extractABox( Dataset unionDataset, Dataset baseOrInfDataset, String graphURI ) {

        Model aboxModel = ModelFactory.createDefaultModel();

        // iterate through all classes and DESCRIBE each of their instances
        // Note that this could be simplified if we knew that the model was a
        // reasoning model: we could then simply describe all instances of
        // owl:Thing.

        //OntModel ontModel = ( inputModel instanceof OntModel )
        //? (OntModel)inputModel
        //: ModelFactory.createOntologyModel( DEFAULT_ONT_MODEL_SPEC, inputModel );
        OntModel ontModel = extractTBox(unionDataset, null, graphURI);

        try {
            ontModel.enterCriticalSection(Lock.READ);
            Iterator classIt = ontModel.listNamedClasses();
            QueryExecution qe = null;
            while ( classIt.hasNext() ) {

                OntClass ontClass = (OntClass) classIt.next();
                //if ( !(ontClass.getNameSpace().startsWith(OWL.getURI()) )
                // && !(ontClass.getNameSpace().startsWith(VitroVocabulary.vitroURI))    ) {
             if(!(ontClass.getNameSpace().startsWith(OWL.getURI()))){

                    String queryStr = makeDescribeQueryStr( ontClass.getURI(), null, graphURI );

                    Query aboxSparqlQuery = QueryFactory.create(queryStr);
                    if(baseOrInfDataset != null){
                        qe = QueryExecutionFactory.create(aboxSparqlQuery,baseOrInfDataset);
                    }
                    else{
                        qe = QueryExecutionFactory.create(aboxSparqlQuery,unionDataset);
                    }
                    if(baseOrInfDataset != null){
                        try {
                            baseOrInfDataset.getLock().enterCriticalSection(Lock.READ);
                            qe.execDescribe(aboxModel); // puts the statements about each resource into aboxModel.
                        } finally {
                            baseOrInfDataset.getLock().leaveCriticalSection();
                        }
                    }
                    else{
                        try {
                            unionDataset.getLock().enterCriticalSection(Lock.READ);
                            qe.execDescribe(aboxModel); // puts the statements about each resource into aboxModel.
                        } finally {
                            unionDataset.getLock().leaveCriticalSection();
                        }
                    }

                }
            }
        } finally {
            ontModel.leaveCriticalSection();
        }

        return aboxModel;

    }
    
    /**
     * Remove statements from a model by separating statements
     * containing blank nodes from those that have no blank nodes.
     * The blank node statements are removed by treating blank nodes as variables and
     * constructing the matching subgraphs for deletion.
     * The other statements are removed normally.
     * @param toRemove containing statements to be removed
     * @param removeFrom from which statements should be removed
     */
    public static void removeWithBlankNodesAsVariables(Model toRemove, Model removeFrom) {
    	List<Statement> blankNodeStatements = new ArrayList<Statement>();
    	List<Statement> nonBlankNodeStatements = new ArrayList<Statement>();
    	StmtIterator stmtIt = toRemove.listStatements();
        while (stmtIt.hasNext()) {
            Statement stmt = stmtIt.nextStatement();
            if (stmt.getSubject().isAnon() || stmt.getObject().isAnon()) {
                blankNodeStatements.add(stmt);
            } else {
            	nonBlankNodeStatements.add(stmt);
            }
        }
        if(!blankNodeStatements.isEmpty()) {
        	Model blankNodeModel = ModelFactory.createDefaultModel();
        	blankNodeModel.add(blankNodeStatements);
        	removeBlankNodesUsingSparqlConstruct(blankNodeModel, removeFrom);	
        }
        if(!nonBlankNodeStatements.isEmpty()) {
            try {
            	removeFrom.enterCriticalSection(Lock.WRITE);
            	removeFrom.remove(nonBlankNodeStatements);
            } finally {
            	removeFrom.leaveCriticalSection();
            }        	
        }
    }
    
    private static void removeBlankNodesUsingSparqlConstruct(Model blankNodeModel,
    		Model removeFrom) {
        log.debug("blank node model size " + blankNodeModel.size());
        if (blankNodeModel.size() == 1) {
            log.debug("Deleting single triple with blank node: " + blankNodeModel);
            log.debug("This could result in the deletion of multiple triples"
            		+ " if multiple blank nodes match the same triple pattern.");
        }
        Query rootFinderQuery = QueryFactory.create(BNODE_ROOT_QUERY);
        QueryExecution qe = QueryExecutionFactory.create(rootFinderQuery, blankNodeModel);
        try {
            ResultSet rs = qe.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                Resource s = qs.getResource("s");
                String treeFinder = makeDescribe(s);
                Query treeFinderQuery = QueryFactory.create(treeFinder);
                QueryExecution qee = QueryExecutionFactory.create(treeFinderQuery, blankNodeModel);
                try {
                    Model tree = qee.execDescribe();
                    JenaModelUtils.removeUsingSparqlConstruct(tree, removeFrom);
                } finally {
                    qee.close();
                }
            }
        } finally {
            qe.close();
        }
    }

    private static String makeDescribe(Resource s) {
        StringBuilder query = new StringBuilder("DESCRIBE <") ;
        if (s.isAnon()) {
            query.append("_:").append(s.getId().toString());
        } else {
            query.append(s.getURI());
        }
        query.append(">");
        return query.toString();
    }

    private static final boolean WHERE_CLAUSE = true;
    
    /**
     * Remove statements from a model by first constructing
     * the statements to be removed with a SPARQL query that treats
     * each blank node ID as a variable.
     * This allows matching blank node structures to be removed even though
     * the internal blank node IDs are different.
     * @param toRemove containing statements to be removed
     * @param removeFrom from which statements should be removed
     */
    public static void removeUsingSparqlConstruct(Model toRemove, Model removeFrom) {
        if(toRemove.isEmpty()) {
        	return;
        }
        List<Statement> stmts = toRemove.listStatements().toList();
        stmts = sort(stmts);
        StringBuffer queryBuff = new StringBuffer();
        queryBuff.append("CONSTRUCT { \n");
        addStatementPatterns(stmts, queryBuff, !WHERE_CLAUSE);
        queryBuff.append("} WHERE { \n");
        addStatementPatterns(stmts, queryBuff, WHERE_CLAUSE);
        queryBuff.append("} \n");
        String queryStr = queryBuff.toString();
        log.debug(queryBuff.toString());
        Query construct = QueryFactory.create(queryStr);
        QueryExecution qe = QueryExecutionFactory.create(construct, removeFrom);
        try {
            Model constructedRemovals = qe.execConstruct();
            try {
            	removeFrom.enterCriticalSection(Lock.WRITE);
            	removeFrom.remove(constructedRemovals);
            } finally {
            	removeFrom.leaveCriticalSection();
            }
        } finally {
            qe.close();
        }
    }
    
    private static List<Statement> sort(List<Statement> stmts) {
        List<Statement> output = new ArrayList<Statement>();
        int originalSize = stmts.size();
        if(originalSize == 1) {
            return stmts;
        }
        List <Statement> remaining = stmts;
        ConcurrentLinkedQueue<Resource> subjQueue = new ConcurrentLinkedQueue<Resource>();
        for(Statement stmt : remaining) {
            if(stmt.getSubject().isURIResource()) {
                subjQueue.add(stmt.getSubject());
                break;
            }
        }
        if (subjQueue.isEmpty()) {
            log.warn("No named subject in statement patterns");
            return stmts;
        }
        while(remaining.size() > 0) {
            if(subjQueue.isEmpty()) {
                subjQueue.add(remaining.get(0).getSubject());
            }
            while(!subjQueue.isEmpty()) {
                Resource subj = subjQueue.poll();
                List<Statement> temp = new ArrayList<Statement>();
                for (Statement stmt : remaining) {
                    if(stmt.getSubject().equals(subj)) {
                        output.add(stmt);
                        if (stmt.getObject().isResource()) {
                            subjQueue.add((Resource) stmt.getObject());
                        }
                    } else {
                        temp.add(stmt);
                    }
                }
                remaining = temp;
            }
        }
        if(output.size() != originalSize) {
            throw new RuntimeException("original list size was " + originalSize +
                    " but sorted size is " + output.size());
        }
        return output;
    }

    private static void addStatementPatterns(List<Statement> stmts,
    		StringBuffer patternBuff, boolean whereClause) {
        Set<String> lines = new HashSet<>();
        for(Statement stmt : stmts) {
            Triple t = stmt.asTriple();
            String lineWithoutVars = getLine(t, false);
            if (lines.contains(lineWithoutVars)) {
                continue;
            } else {
                lines.add(lineWithoutVars);
            }
            patternBuff.append(getLine(t, true));
            if (whereClause) {
                if (t.getSubject().isBlank()) {
                    patternBuff.append("    FILTER(isBlank(").append(
                    		sparqlNodeDelete(t.getSubject(), null)).append(")) \n");
                }
                if (t.getObject().isBlank()) {
                    patternBuff.append("    FILTER(isBlank(").append(
                    		sparqlNodeDelete(t.getObject(), null)).append(")) \n");
                }
            }
        }
    }

    private static String getLine(Triple t, boolean createBlankNodeVariables) {
        if (createBlankNodeVariables) {
            return sparqlNodeDelete(t.getSubject(), null) +
                    " " +
                    sparqlNodeDelete(t.getPredicate(), null) +
                    " " +
                    sparqlNodeDelete(t.getObject(), null) +
                    " .\n";
        } else {
            return sparqlNode(t.getSubject(), null) +
                    " " +
                    sparqlNode(t.getPredicate(), null) +
                    " " +
                    sparqlNode(t.getObject(), null) +
                    " .\n";
        }
    }

}
