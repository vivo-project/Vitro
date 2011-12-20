/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryConfig;

public class JenaModelUtils {
    
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
     * @param wadf
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
                for (Iterator<VClass> rootClassIt = rootClasses.iterator(); 
                        rootClassIt.hasNext(); ) {
                    VClass rootClass = (VClass) rootClassIt.next();	                    
                    Individual classGroup = modelForClassgroups.createIndividual(
                            wadf.getDefaultNamespace() + "vitroClassGroup" + 
                                    rootClass.getLocalName(), classGroupClass);
                    classGroup.setLabel(rootClass.getName(), null);

                    Resource rootClassRes = modelForClassgroupAnnotations.getResource(
                            rootClass.getURI());
                    modelForClassgroupAnnotations.add(
                            rootClassRes, inClassGroupProperty, classGroup);
                    for (Iterator<String> childIt = myWebappDaoFactory.getVClassDao()
                            .getAllSubClassURIs(rootClass.getURI()).iterator(); 
                            childIt.hasNext(); ) {
                        String childURI = (String) childIt.next();
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
        if (mode = AGGRESSIVE) {
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
                                                                  VitroVocabulary.PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT)));
            tboxModel.add(construct(dataset, namespace, graphURI, ResourceFactory.createResource(
                                                                  VitroVocabulary.HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT)));
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
            StringBuffer buff = new StringBuffer();
            buff.append("PREFIX afn: <http://jena.hpl.hp.com/ARQ/function#> \n")
            .append("CONSTRUCT { \n")
            .append("  ?res <" + property.getURI() + "> ?o } WHERE { \n");
            if (graphURI != null) {
                buff.append("    GRAPH " + graphURI + " { \n");
            }
            buff.append("      ?res <" + property.getURI() + "> ?o \n");
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
        
        StringBuffer describeQueryStrBuff = new StringBuffer() 
            .append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n")
            .append("PREFIX afn: <http://jena.hpl.hp.com/ARQ/function#> \n")
            .append("DESCRIBE ?res WHERE { \n");
            if (graphURI != null) {
                describeQueryStrBuff
                .append("GRAPH " + graphURI + "{ \n");
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
        StringBuffer buff = new StringBuffer();
        if (namespace == null) {
            // exclude resources in the Vitro internal namespace or in the 
            // OWL namespace, but allow all others
            buff
            .append("    FILTER (afn:namespace(?res) != \"")
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
    
}
