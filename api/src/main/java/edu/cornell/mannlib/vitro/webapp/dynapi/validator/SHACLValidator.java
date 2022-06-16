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
import java.util.*;

public class SHACLValidator implements ModelValidator {

    private final Log log = LogFactory.getLog(this.getClass());

    private static final String SHACL_PREFIX = "http://www.w3.org/ns/shacl";

    private static final String SPARQL_QUERY = "CONSTRUCT { ?s ?p ?o }\n" +
                                                "WHERE\n" +
                                                "{\n" +
                                                "  {?s ?p ?o . \n" +
                                                "  FILTER (?s = ?uri) . }\n" +
                                                "}\n";

    protected String sparqlQuery;

    protected Model data;
    protected Model scheme;
    protected Map<String, Resource> map;

    public SHACLValidator(Model data, Model scheme, String shaclRootUri, String sparqlQuery){
        this.data = data;
        map = new HashMap<>();
        this.scheme = (shaclRootUri!=null)   ?
                    collectConstraintsByNavigatingJavaModel(JenaUtil.createMemoryModel().union(scheme).getResource(shaclRootUri))    :
                    JenaUtil.createMemoryModel().union(scheme);
        this.sparqlQuery = (sparqlQuery!=null)?
                                sparqlQuery :
                                SHACLValidator.SPARQL_QUERY;

    }

    private void clean(){
        map = new HashMap<>();
    }

    private Resource validateResource(String uri) {
        Model dataModel = JenaUtil.createMemoryModel();
        dataModel.add(data.getResource(uri).listProperties());
        Resource report = ValidationUtil.validateModel(dataModel, scheme, true);

        return report;
    }

    private Model collectResourceAndLinkedObjectsBySparqlQuery(String uri){
        Model dataModel = null;
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        pss.setIri("uri", IRIFactory.uriImplementation().construct(uri));
        pss.setCommandText(sparqlQuery);
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
        Model dataModel = JenaUtil.createMemoryModel();
        clean();
        collectDataForValidation(data.getResource(uri));

        for(Resource resource:map.values()) {
            dataModel.add(resource.listProperties());
        }
        return dataModel;
    }

    private void collectDataForValidation(Resource resource){
        if(shouldBeValidated(resource)){
            map.put(resource.getURI(), resource);
            Set<Statement> properties = resource.listProperties().toSet();
            for (Statement statement : properties) {
                if (statement.getObject().isResource()) {
                    collectDataForValidation(statement.getObject().asResource());
                }
            }
        }
    }

    protected boolean shouldBeValidated(Resource resource){
        return !map.containsKey(resource.getURI());
    }

    private Model collectConstraintsByNavigatingJavaModel(Resource rootResource){
        clean();
        collectConstraints(rootResource);

        Model schemeModel = JenaUtil.createMemoryModel();
        for(Resource resource:map.values()) {
            schemeModel.add(resource.listProperties());
        }
        return schemeModel;
    }

    private void collectConstraints(Resource resource){
        if(shouldBeAConstraint(resource)){
            map.put(resource.getURI(), resource);
            Set<Statement> properties = resource.listProperties().toSet();
            for (Statement statement : properties) {
                if (statement.getObject().isResource()) {
                    collectConstraints(statement.getObject().asResource());
                }
            }
        }
    }

    protected boolean shouldBeAConstraint(Resource resource){
        boolean retVal = !map.containsKey(resource.getURI());
        if (retVal) {
            retVal = false;
            Set<Statement> properties = resource.listProperties().toSet();
            for (Statement statement : properties) {
                if (statement.getPredicate().getLocalName().equals("type")) {
                    if (statement.getObject().toString().contains(SHACLValidator.SHACL_PREFIX)) {
                        retVal = true;
                        break;
                    }
                }
            }
        }
        return retVal;
    }

    private Resource validateResourceAndLinkedObjects(String uri, boolean sparql) {
        Model dataModel = (sparql)?collectResourceAndLinkedObjectsBySparqlQuery(uri):collectResourceAndLinkedObjectsByNavigatingJavaModel(uri);

        Resource report = ValidationUtil.validateModel(dataModel, scheme, true);

        return report;
    }

    private Resource validateFile(String path, String format) throws IOException {
        Model dataModel = JenaUtil.createMemoryModel();
        Path p = new File(path).toPath();

        StringReader stringReader =  new StringReader(new String(Files.readAllBytes(p)));

        dataModel.read(stringReader, null, format);

        Resource report = ValidationUtil.validateModel(dataModel.union(data), scheme, true);

        return report;
    }

    @Override
    public boolean isValidResource(String uri, boolean deepCheck){
        Resource report = null;
        report = (deepCheck)?validateResourceAndLinkedObjects(uri, false):validateResource(uri);
        if (report == null) {
            return false;
        } else if (report.getModel().listStatements(null, SH.resultSeverity, SH.Violation).toList().size() != 0) {
            log.warn(ModelPrinter.get().print(report.getModel()));
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean isValidFile(String path, String format){
        Resource report = null;
        try {
            report = validateFile(path, format);
        } catch (IOException e) {
            log.warn("File " + path + " can't be read.", e);
        }
        if (report == null) {
            return false;
        } else if (report.getModel().listStatements(null, SH.resultSeverity, SH.Violation).toList().size() != 0) {
            log.warn(ModelPrinter.get().print(report.getModel()));
            return false;
        } else {
            return true;
        }
    }

}
