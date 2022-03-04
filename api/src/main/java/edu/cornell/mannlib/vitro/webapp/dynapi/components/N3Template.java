package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.AdditionsAndRetractions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.ProcessRdfForm;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.regex.Pattern;

public class N3Template implements Template{

	private static final Log log = LogFactory.getLog(N3Template.class);

	private Parameters requiredParams = new Parameters();
	private String n3Text;
	private ModelComponent templateModel;

  	//region @Property Setters

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#requiresParameter")
	public void addRequiredParameter(Parameter param) {
		requiredParams.add(param);
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasN3Text", minOccurs = 1, maxOccurs = 1)
	public void setN3Text(String n3Text) {
		this.n3Text = n3Text;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasTemplateModel", minOccurs = 1, maxOccurs = 1)
	public void setTemplateModel(ModelComponent templateModel){ this.templateModel = templateModel; }

	//endregion

	//region Getters

	@Override
	public Parameters getRequiredParams() {
		return requiredParams;
	}

	@Override
	public Parameters getProvidedParams() {
		return new Parameters();
	}

	//endregion

	@Override
	public OperationResult run(OperationData input) {
		if (!isInputValid(input)) {
			return new OperationResult(500);
		}

		String substitutedN3Template;
		try {
			substitutedN3Template=insertParameters(input);
		}catch (InputMismatchException e){
			log.error(e);
			return new OperationResult(500);
		}

		List<Model> additionModels;
		try {
			additionModels = ProcessRdfForm.parseN3ToRDF(Arrays.asList(substitutedN3Template), ProcessRdfForm.N3ParseType.REQUIRED);
		} catch (Exception e) {
			log.error("Error while trying to parse N3Template string and create a Jena rdf Model");
			log.error(e);
			return new OperationResult(500);
		}

		AdditionsAndRetractions changes = new AdditionsAndRetractions(additionModels, new ArrayList<Model>());
		Model writeModel = ModelAccess.on(input.getContext()).getOntModel(templateModel.getName());
		ProcessRdfForm.applyChangesToWriteModel(changes, null, writeModel,"");

		return new OperationResult(200);
	}

	private String insertParameters(OperationData input) throws InputMismatchException{

		String n3WithParameters = n3Text;

		for(String token : n3Text.split(" ")){
			if(!token.startsWith("?")){
				continue;
			}
			String varName = token.substring(1);

			//Is it safe to assume this condition is met?
			if(!input.has(varName)){
				throw new InputMismatchException("N3 template '" + varName + "' doesn't have a corresponding " +
						"variable within OperationData");
			}

			//For now assuming each n3template variable can have only one value (not lists)
			String varValue = input.get(varName)[0];

			//If variable value isn't RDF resource, a number, or boolean (true/false), assumed that it a string literal,
			// which needs to be surrounded by double quotes
			Pattern numberPattern = Pattern.compile("-?\\d+(\\.\\d+)?");
			if(!numberPattern.matcher(varValue).matches() &&
					!(varValue.startsWith("<") && varValue.endsWith(">")) &&
					!(varValue=="true" || varValue=="false")){
				varValue = "\""+varValue+"\"";
			}

			n3WithParameters.replaceAll(varName,varValue);
		}

		return n3WithParameters;
	}

	private boolean isInputValid(OperationData input) {
		for (String name : requiredParams.getNames()) {
			if (!input.has(name)) {
				log.error("Parameter " + name + " not found");
				return false;
			}
			Parameter param = requiredParams.get(name);
			String[] inputValues = input.get(name);
			if (!param.isValid(name, inputValues)){
				return false;
			}
		}
		return true;
	}
	@Override
	public void dereference() {

	}
}
