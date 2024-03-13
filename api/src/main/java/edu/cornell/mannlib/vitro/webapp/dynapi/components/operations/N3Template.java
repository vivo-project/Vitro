/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components.operations;

import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.ModelView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.RdfView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.AdditionsAndRetractions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditN3GeneratorVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.ProcessRdfForm;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;

public class N3Template extends AbstractOperation {
    private static final Log log = LogFactory.getLog(N3Template.class);
    private String n3TextAdditions = "";
    private String n3TextRetractions = "";
    private Parameter modelParam;
    // region @Property Setters

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasModel", minOccurs = 1, maxOccurs = 1)
    public void setTemplateModel(Parameter param) throws InitializationException {
        if (!ModelView.isModel(param)) {
            throw new InitializationException("Only model parameters accepted on hasModel");
        }
        modelParam = param;
        inputParams.add(param);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#requiresParameter")
    public void addInputParameter(Parameter param) throws InitializationException {
        if (ModelView.isModel(param)) {
            throw new InitializationException("Model parameter can't be provided as input to N3 Template");
        }
        inputParams.add(param);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#N3TextAdditions", minOccurs = 0, maxOccurs = 1)
    public void setN3TextAdditions(String n3TextAdditions) {
        this.n3TextAdditions = n3TextAdditions;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#N3TextRetractions", minOccurs = 0, maxOccurs = 1)
    public void setN3TextRetractions(String n3TextRetractions) {
        this.n3TextRetractions = n3TextRetractions;
    }

    public String getN3TextAdditions() {
        return this.n3TextAdditions;
    }

    public String getN3TextRetractions() {
        return this.n3TextRetractions;
    }

    @Override
    public OperationResult runOperation(DataStore dataStore) {
        String substitutedN3AdditionsTemplate;
        String substitutedN3RetractionsTemplate;
        try {
            substitutedN3AdditionsTemplate = insertParameters(dataStore, n3TextAdditions);
            substitutedN3RetractionsTemplate = insertParameters(dataStore, n3TextRetractions);
        } catch (InputMismatchException e) {
            log.error(e);
            return OperationResult.internalServerError();
        }

        List<Model> additionModels;
        List<Model> retractionModels;
        try {
            additionModels = ProcessRdfForm.parseN3ToRDF(Arrays.asList(substitutedN3AdditionsTemplate),
                    ProcessRdfForm.N3ParseType.REQUIRED);

            retractionModels = ProcessRdfForm.parseN3ToRDF(Arrays.asList(substitutedN3RetractionsTemplate),
                    ProcessRdfForm.N3ParseType.REQUIRED);
        } catch (Exception e) {
            log.error("Error while trying to parse N3Template string and create a Jena rdf Model", e);
            return OperationResult.internalServerError();
        }

        AdditionsAndRetractions changes = new AdditionsAndRetractions(additionModels, retractionModels);

        if (!ModelView.hasModel(dataStore, modelParam)) {
            log.error("Model not found in input parameters");
            return OperationResult.internalServerError();
        }
        Model writeModel = ModelView.getModel(dataStore, modelParam);
        try {
            RDFServiceModel rdfService = new RDFServiceModel(writeModel);
            ProcessRdfForm.applyChangesToWriteModel(changes, rdfService, "", "");
        } catch (Exception e) {
            log.error(e, e);
            return OperationResult.internalServerError();
        }

        return OperationResult.ok();
    }

    public boolean isValid() {
        boolean result = true;
        if (modelParam == null) {
            log.error("Model param is not provided in configuration");
            result = false;
        }
        return result;
    }

    private String insertParameters(DataStore input, String n3Text) throws InputMismatchException {

        EditN3GeneratorVTwo gen = new EditN3GeneratorVTwo();

        // Had to convert String to List<String> to be compatible with EditN3GeneratorVTwo methods
        List<String> n3WithParameters = Arrays.asList(n3Text);

        // region Substitute IRI variables
        Map<String, List<String>> parametersToUris = RdfView.getUrisMap(input, inputParams);

        gen.subInMultiUris(parametersToUris, n3WithParameters);
        // endregion

        // region Substitute other (literal) variables
        Map<String, List<Literal>> parametersToLiterals = RdfView.getLiteralsMap(input, inputParams);

        gen.subInMultiLiterals(parametersToLiterals, n3WithParameters);
        // endregion

        // Check if any n3 variables are left without inserted value
        /*
         * String[] leftoverVariables = Arrays.stream(n3WithParameters.get(0).split(" ")) .filter(token ->
         * token.startsWith("?")).toArray(String[]::new); if(leftoverVariables.length>0){ throw new
         * InputMismatchException("N3 template variables:'" + Arrays.toString(leftoverVariables) +
         * "' dont have corresponding input parameters with values that should be inserted in their place." ); }
         */
        return n3WithParameters.get(0);
    }

}
