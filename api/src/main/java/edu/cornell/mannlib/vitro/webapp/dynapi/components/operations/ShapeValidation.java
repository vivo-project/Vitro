/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components.operations;

import static java.lang.String.format;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.dynapi.ShapesGraphComponent;
import edu.cornell.mannlib.vitro.webapp.dynapi.ShapesGraphPool;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.NullProcedure;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Procedure;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ProcedureDescriptor;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ProcedureDescriptorCall;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.BooleanView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.ModelView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.StringView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.shacl.arq.SHACLFunctions;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.util.ModelPrinter;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.validation.ValidationEngineConfiguration;
import org.topbraid.shacl.validation.ValidationEngineFactory;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;

public class ShapeValidation extends AbstractOperation {

    private final Log log = LogFactory.getLog(this.getClass());
    private static ShapesGraphPool shapesGraphPool = ShapesGraphPool.getInstance();

    private Parameter dataModelParam;
    private Parameter shapesModelParam;
    private boolean inputCalculated;
    private boolean validateShapes = true;
    private boolean details = false;
    private boolean cache = false;
    private int maximumViolationsBeforeAbort = -1;
    private Parameters internalParams = new Parameters();
    private Map<String, ProcedureDescriptor> dependencies = new HashMap<>();
    private ProcedureDescriptor shapesProcedureDescriptor;

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#dataModel", minOccurs = 1, maxOccurs = 1)
    public void setDataModel(Parameter model) throws InitializationException {
        if (!ModelView.isModel(model)) {
            throw new InitializationException("setDataModel accepts only model parameters");
        }
        dataModelParam = model;
        inputParams.add(model);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#providesParameter", minOccurs = 1, maxOccurs = 1)
    public void addOutputParam(Parameter param) throws InitializationException {
        outputParams.add(param);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#setCache", maxOccurs = 1)
    public void setCache(Boolean cache) throws InitializationException {
        this.cache = cache;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#details", maxOccurs = 1)
    public void setDetails(Boolean details) throws InitializationException {
        this.details = details;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#validateShapes", maxOccurs = 1)
    public void setValidateShapes(Boolean validateShapes) throws InitializationException {
        this.validateShapes = validateShapes;
    }

    // Procedure should return a model contains SHACL rules
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#ShapesProcecedureDescriptor", maxOccurs = 1)
    public void setShapesProcedureDescriptor(ProcedureDescriptor pd) {
        shapesProcedureDescriptor = pd;
        dependencies.put(pd.getUri(), pd);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#shapesModel", maxOccurs = 1)
    public void setShapesModel(Parameter model) throws InitializationException {
        if (!ModelView.isModel(model)) {
            throw new InitializationException("setDataModel accepts only model parameters");
        }
        shapesModelParam = model;
        inputParams.add(model);
    }

    @Override
    public OperationResult runOperation(DataStore dataStore) {
        try {
            ValidationEngine engine = configureEngine(dataStore);
            executeValidation(dataStore, engine);
        } catch (Exception e) {
            log.error(e, e);
            return OperationResult.internalServerError();
        }
        return OperationResult.ok();
    }

    private void executeValidation(DataStore dataStore, ValidationEngine engine) throws InterruptedException {
        Resource reportResource;
        boolean result = false;
        String report = null;
        try {
            engine.applyEntailments();
            reportResource = engine.validateAll();
            result = isValidationSuccessfull(reportResource);
            if (reportResource != null) {
                report = ModelPrinter.get().print(reportResource.getModel());
            }
        } catch (Exception e) {
            log.error(e, e);
            if (report == null) {
                report = e.getLocalizedMessage();
            } else {
                report += e.getLocalizedMessage();
            }
            result = false;
        }
        if (report == null) {
            report = "Report is null";
        }
        for (String name : outputParams.getNames()) {
            Parameter param = outputParams.get(name);
            if (BooleanView.isBoolean(param)) {
                Data data = BooleanView.createData(name, result);
                dataStore.addData(name, data);
            }
            if (StringView.isPlainString(param)) {
                Data data = StringView.createData(name, report);
                dataStore.addData(name, data);
            }
        }
    }

    private ValidationEngine configureEngine(DataStore dataStore) throws ConversionException, InitializationException {
        String shapesGraphName = getShapesGraphName();
        URI shapesGraphURI = SHACLUtil.createRandomShapesGraphURI();
        ShapesGraph shapesGraph = null;
        if (cache) {
            shapesGraph = getShapesGraphFromCache(shapesGraphName);
        }
        if (shapesGraph == null) {
            Model shapesModel = null;
            if (isShapesFromInput()) {
                shapesModel = ModelView.getModel(dataStore, shapesModelParam);

            } else {
                shapesModel = getShapesModel(dataStore);
            }
            shapesModel = ValidationUtil.ensureToshTriplesExist(shapesModel);
            SHACLFunctions.registerFunctions(shapesModel);
            shapesGraph = new ShapesGraph(shapesModel);
            if (cache) {
                addShapesGraphToCache(shapesGraphName, shapesGraph);
            }
        }
        Model dataModel = ModelView.getModel(dataStore, dataModelParam);
        Dataset dataset = ARQFactory.get().getDataset(dataModel);
        Model shapesModel = shapesGraph.getShapesModel();
        dataset.addNamedModel(shapesGraphURI.toString(), shapesModel);
        ValidationEngine engine = ValidationEngineFactory.get().create(dataset, shapesGraphURI, shapesGraph, null);
        ValidationEngineConfiguration configuration = engine.getConfiguration();
        configuration.setReportDetails(details);
        configuration.setValidationErrorBatch(maximumViolationsBeforeAbort);
        configuration.setValidateShapes(validateShapes);
        return engine;
    }

    public boolean isValidationSuccessfull(Resource report) {
        if (report == null) {
            return false;
        } else if (report.getModel().listStatements(null, SH.resultSeverity, SH.Violation).toList().size() != 0) {
            return false;
        } else {
            return true;
        }
    }

    private void addShapesGraphToCache(String shapesGraphName, ShapesGraph shapesGraph) {
        shapesGraphPool.add(shapesGraphName, new ShapesGraphComponent(shapesGraph));
    }

    private ShapesGraph getShapesGraphFromCache(String shapesGraphName) {
        ShapesGraph shapesGraph;
        ShapesGraphComponent shapesGraphComponent = shapesGraphPool.get(shapesGraphName);
        if (shapesGraphComponent == null) {
            return null;
        }
        shapesGraph = shapesGraphComponent.getShapesGraph();
        return shapesGraph;
    }

    private boolean isShapesFromInput() {
        return shapesModelParam != null;
    }

    private String getShapesGraphName() {
        if (isShapesFromInput()) {
            return shapesModelParam.getName();
        } else {
            Parameters pdOutputParams = shapesProcedureDescriptor.getOutputParams();
            Parameter model = getModelParam(pdOutputParams);
            return model.getName();
        }
    }

    private Model getShapesModel(DataStore dataStore) throws ConversionException, InitializationException {
        DataStore localStore = new DataStore();
        ProcedureDescriptorCall.initilaizeLocalStore(dataStore, localStore, shapesProcedureDescriptor.getInputParams(),
                internalParams);
        ProcedureDescriptorCall.execute(shapesProcedureDescriptor, localStore);
        return getModel(localStore, shapesProcedureDescriptor.getOutputParams());
    }

    protected Model getModel(DataStore dataStore, Parameters params) {
        for (String name : params.getNames()) {
            Parameter model = params.get(name);
            if (ModelView.isModel(model)) {
                Data data = dataStore.getData(name);
                if (data == null) {
                    throw new RuntimeException(String.format("Data '%s' not null", name));
                }
                return ModelView.getModel(dataStore, model);
            }
        }
        throw new RuntimeException("Model is not returned");
    }

    protected void calculateInputParams() {
        if (shapesProcedureDescriptor != null) {
            inputParams.addAll(shapesProcedureDescriptor.getInputParams());
        }
        inputParams.removeAll(internalParams);
        inputCalculated = true;
    }

    protected boolean isValid(DataStore dataStore) {
        if (!isValid()) {
            return false;
        }
        if (!areDescriptorsValid(dataStore)) {
            return false;
        }
        for (String name : inputParams.getNames()) {
            if (!dataStore.contains(name)) {
                log.error(String.format("Input parameter '%s' is not provided in data store", name));
                return false;
            }
        }
        return true;
    }

    private boolean areDescriptorsValid(DataStore dataStore) {
        for (ProcedureDescriptor descriptor : dependencies.values()) {
            if (!isValidDescriptor(descriptor, dataStore)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidDescriptor(ProcedureDescriptor descriptor, DataStore dataStore) {
        Parameter uriParam = descriptor.getUriParam();
        if (uriParam != null) {
            if (dataStore.contains(uriParam.getName())) {
                return true;
            } else {
                log.error("Callable procedure uri provided as uriParameter not found in data store.");
                return false;
            }
        }
        String uri = descriptor.getUri();
        if (uri == null) {
            log.error("Uri not provided. Loop descriptor validation failed.");
            return false;
        }
        Map<String, Procedure> map = dataStore.getDependencies();
        if (!map.containsKey(uri)) {
            log.error(format("Dependency with uri: '%s' expected, but not provided. Loop validation failed.", uri));
            return false;
        }
        Procedure dependency = map.get(uri);
        if (dependency == null) {
            log.error(format("Dependency with uri: '%s' expected, but null provided. Loop validation failed.", uri));
            return false;
        }
        if (NullProcedure.getInstance().equals(dependency)) {
            log.error(format(
                    "Dependency with uri: '%s' expected, but default null object provided. Loop validation failed.",
                    uri));
            return false;
        }
        return true;
    }

    public boolean isValid() {
        if (dataModelParam == null) {
            log.error("Data model parameter is required, but not provided.");
            return false;
        }
        if (shapesModelParam == null && shapesProcedureDescriptor == null) {
            log.error(
                    "Either shapes model parameter or shapes procedure descriptor is required, but neither provided.");
            return false;
        }
        if (shapesModelParam != null && shapesProcedureDescriptor != null) {
            log.error(
                    "Either shapes model parameter or shapes procedure descriptor is required, " +
                    "but both were provided.");
            return false;
        }
        if (shapesProcedureDescriptor != null) {
            if (!isShapesProcedureDescriptorProvidesExactlyOneModel()) {
                log.error("Shapes procedure descriptor should provide one model parameter (shapes).");
            }
        }
        return true;
    }

    private boolean isShapesProcedureDescriptorProvidesExactlyOneModel() {
        Parameters pdOutputParams = shapesProcedureDescriptor.getOutputParams();
        int count = 0;
        for (String name : pdOutputParams.getNames()) {
            Parameter model = pdOutputParams.get(name);
            if (ModelView.isModel(model)) {
                count++;
            }
        }
        return count == 1;
    }

    private Parameter getModelParam(Parameters params) {
        for (String name : params.getNames()) {
            Parameter model = params.get(name);
            if (ModelView.isModel(model)) {
                return model;
            }
        }
        throw new RuntimeException("Model parameter not found");
    }

    @Override
    public Parameters getInputParams() {
        if (!inputCalculated) {
            calculateInputParams();
        }
        return inputParams;
    }

    @Override
    public Map<String, ProcedureDescriptor> getDependencies() {
        return dependencies;
    }
}
