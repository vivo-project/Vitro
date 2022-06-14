package edu.cornell.mannlib.vitro.webapp.dynapi.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.Lock;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.util.ModelPrinter;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SHACLValidator implements ModelValidator {

    private final Log log = LogFactory.getLog(this.getClass());

    protected String queryText;

    protected final Model data;
    protected final Model scheme;
    protected Map<String, Resource> map;

    public SHACLValidator(Model data, Model scheme){
        this.data = data;
        this.scheme = scheme;
        queryText = "PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX xsd:      <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX owl:      <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX swrl:     <http://www.w3.org/2003/11/swrl#>\n" +
                "PREFIX swrlb:    <http://www.w3.org/2003/11/swrlb#>\n" +
                "PREFIX vitro:    <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>\n" +
                "PREFIX bibo:     <http://purl.org/ontology/bibo/>\n" +
                "PREFIX c4o:      <http://purl.org/spar/c4o/>\n" +
                "PREFIX cito:     <http://purl.org/spar/cito/>\n" +
                "PREFIX dcterms:  <http://purl.org/dc/terms/>\n" +
                "PREFIX event:    <http://purl.org/NET/c4dm/event.owl#>\n" +
                "PREFIX fabio:    <http://purl.org/spar/fabio/>\n" +
                "PREFIX foaf:     <http://xmlns.com/foaf/0.1/>\n" +
                "PREFIX geo:      <http://aims.fao.org/aos/geopolitical.owl#>\n" +
                "PREFIX obo:      <http://purl.obolibrary.org/obo/>\n" +
                "PREFIX ocrer:    <http://purl.org/net/OCRe/research.owl#>\n" +
                "PREFIX ocresst:  <http://purl.org/net/OCRe/statistics.owl#>\n" +
                "PREFIX ocresd:   <http://purl.org/net/OCRe/study_design.owl#>\n" +
                "PREFIX ocresp:   <http://purl.org/net/OCRe/study_protocol.owl#>\n" +
                "PREFIX ro:       <http://purl.obolibrary.org/obo/ro.owl#>\n" +
                "PREFIX skos:     <http://www.w3.org/2004/02/skos/core#>\n" +
                "PREFIX swo:      <http://www.ebi.ac.uk/efo/swo/>\n" +
                "PREFIX vcard:    <http://www.w3.org/2006/vcard/ns#>\n" +
                "PREFIX vitro-public: <http://vitro.mannlib.cornell.edu/ns/vitro/public#>\n" +
                "PREFIX vivo:     <http://vivoweb.org/ontology/core#>\n" +
                "PREFIX scires:   <http://vivoweb.org/ontology/scientific-research#>\n" +
                "PREFIX vann:     <http://purl.org/vocab/vann/>\n" +
                "PREFIX dynapi: <https://vivoweb.org/ontology/vitro-dynamic-api#> \n" +
                "\n" +
                "#\n" +
                "#\n" +
                "CONSTRUCT { ?s ?p ?o }\n" +
                "WHERE\n" +
                "{\n" +
                "  {?s ?p ?o. \n" +
                "  FILTER (?s = ?uri). }\n" +
                "  UNION\n" +
                "  {?uri ?p1 ?s .\n" +
                "  ?s ?p ?o. }\n" +
                "  \tUNION \n" +
                "  {?uri ?p1 ?o1 .\n" +
                "    ?o1 ?p2 ?s .\n" +
                "    ?s ?p ?o.\n" +
                "  } \n" +
                "  \tUNION\n" +
                "  { ?uri ?p1 ?o1 .\n" +
                "    ?o1 ?p2 ?o2 .\n" +
                "    ?o2 ?p3 ?s .\n" +
                "    ?s ?p ?o.\n" +
                "  }\n" +
                "   UNION\n" +
                "  { ?uri ?p1 ?o1 .\n" +
                "    ?o1 ?p2 ?o2 .\n" +
                "    ?o2 ?p3 ?o3 .\n" +
                "    ?o3 ?p4 ?s .\n" +
                "    ?s ?p ?o.\n" +
                "  }\n" +
                "  UNION\n" +
                "  { ?uri ?p1 ?o1 .\n" +
                "    ?o1 ?p2 ?o2 .\n" +
                "    ?o2 ?p3 ?o3 .\n" +
                "    ?o3 ?p4 ?o4 .\n" +
                "     ?o4 ?p5 ?s .\n" +
                "    ?s ?p ?o.\n" +
                "  }\n" +
                "  UNION\n" +
                "  { ?uri ?p1 ?o1 .\n" +
                "    ?o1 ?p2 ?o2 .\n" +
                "    ?o2 ?p3 ?o3 .\n" +
                "    ?o3 ?p4 ?o4 .\n" +
                "     ?o4 ?p5 ?o5 .\n" +
                "     ?o5 ?p6 ?s .\n" +
                "    ?s ?p ?o.\n" +
                "  }\n" +
                "}\n";
    }

    private void clean(){
        map = new HashMap<String, Resource>();
    }

    private Resource validateResource(String uri) {
        Model dataModel = JenaUtil.createMemoryModel();
        dataModel.add(data.getResource(uri).listProperties());
        Resource report = ValidationUtil.validateModel(dataModel, scheme, true);

        if (report.getModel().listStatements(null, SH.resultSeverity, SH.Violation).toList().size() != 0) {
            log.warn(ModelPrinter.get().print(report.getModel()));
        }
        return report;
    }

    private Model collectResourceAndLinkedObjectsBySparqlQuery(String uri){
        Model dataModel = null;
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        pss.setIri("uri", IRIFactory.uriImplementation().construct(uri));
        pss.setCommandText(queryText);
        data.enterCriticalSection(Lock.READ);
        try {
            QueryExecution qexec = QueryExecutionFactory.create(pss.toString(), data);
            try {
                dataModel = qexec.execConstruct();
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
            } finally {
                qexec.close();
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        } finally {
            data.leaveCriticalSection();
        }
        return dataModel;
    }

    private Model collectResourceAndLinkedObjectsByNavigatingJavaModel(String uri){
        Model dataModel = JenaUtil.createMemoryModel();;
        clean();
        collectHierarchy(data.getResource(uri));

        for(Resource resource:map.values()) {
            dataModel.add(resource.listProperties());
        }
        return dataModel;
    }

    protected boolean shouldBeValidated(Resource resource){
        return !map.containsKey(resource.getURI());
    }

    private void collectHierarchy(Resource resource){
        if(shouldBeValidated(resource)){
            map.put(resource.getURI(), resource);
            Set<Statement> properties = resource.listProperties().toSet();
            for (Statement statement : properties) {
                if (statement.getObject().isResource()) {
                    collectHierarchy(statement.getObject().asResource());
                }
            }
        }
    }

    private Resource validateResourceAndLinkedObjects(String uri, boolean sparql) throws InterruptedException {
//        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
//        System.out.println(uri);
//        System.out.println("start creation of model: " + dateFormat.format(new Date()));
        Model dataModel = (sparql)?collectResourceAndLinkedObjectsBySparqlQuery(uri):collectResourceAndLinkedObjectsByNavigatingJavaModel(uri);
//        System.out.println("end creation of model: " + dateFormat.format(new Date()));
//        System.out.println("dataModel size: " + dataModel.listStatements().toSet().size());
//        for (Statement statement: dataModel.listStatements().toSet()
//             ) {
//            System.out.println(statement.toString());
//        }

        Resource report = ValidationUtil.validateModel(dataModel, scheme, true);

        if (report.getModel().listStatements(null, SH.resultSeverity, SH.Violation).toList().size() != 0) {
            log.warn(ModelPrinter.get().print(report.getModel()));
        }
        return report;
    }

    private Resource validateFile(String path, String format) throws IOException {
        Model dataModel = JenaUtil.createMemoryModel();
        Path p = new File(path).toPath();

        StringReader stringReader =  new StringReader(new String(Files.readAllBytes(p)));

        dataModel.read(stringReader, null, format);

        Resource report = ValidationUtil.validateModel(dataModel.union(data), scheme, true);

        if (report.getModel().listStatements(null, SH.resultSeverity, SH.Violation).toList().size() != 0) {
            log.warn(ModelPrinter.get().print(report.getModel()));
        }
        return report;
    }

    @Override
    public boolean isValidResource(String uri, boolean deepCheck){
        Resource report = null;
        try {
            report = (deepCheck)?validateResourceAndLinkedObjects(uri, false):validateResource(uri);
        } catch (InterruptedException e) {
            log.warn("Validation of the resource " + uri + " has been interrupted.", e);
        }
        return report != null && report.getModel().listStatements(null, SH.resultSeverity, SH.Violation).toList().size() == 0;
    }

    @Override
    public boolean isValidFile(String path, String format){
        Resource report = null;
        try {
            report = validateFile(path, format);
        } catch (IOException e) {
            log.warn("File " + path + " can't be read.", e);
        }
        return report != null && report.getModel().listStatements(null, SH.resultSeverity, SH.Violation).toList().size() == 0;
    }

}
