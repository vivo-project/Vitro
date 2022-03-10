package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.AdditionsAndRetractions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditN3GeneratorVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.ProcessRdfForm;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.*;
import java.util.stream.Collectors;

public class N3Template extends Operation implements Template {

	private static final Log log = LogFactory.getLog(N3Template.class);

	private static final String ANY_URI="http://www.w3.org/2001/XMLSchema#anyURI";

	private Parameters requiredParams = new Parameters();
	private String n3Text;
	private ModelComponent templateModel;

    // region @Property Setters

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#requiresParameter")
    public void addRequiredParameter(Parameter param) {
        requiredParams.add(param);
    }

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasModel", minOccurs = 1, maxOccurs = 1)
	public void setTemplateModel(ModelComponent templateModel){ this.templateModel = templateModel; }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#n3Text", minOccurs = 1, maxOccurs = 1)
    public void setN3Text(String n3Text) {
        this.n3Text = n3Text;
    }

    // endregion

    // region Getters

    @Override
    public Parameters getRequiredParams() {
        return requiredParams;
    }

	@Override
	public Parameters getProvidedParams() {
		return new Parameters();
	}

	public String getN3Text() { return this.n3Text; }

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

		EditN3GeneratorVTwo gen = new EditN3GeneratorVTwo();

		//Had to convert String to List<String> to be compatible with EditN3GeneratorVTwo methods
		List<String> n3WithParameters = Arrays.asList(n3Text);

		//region Substitute IRI variables
		Map<String, List<String>> parametersToUris = requiredParams.getParameters().values().stream()
				.filter(value->value.getRDFDataType().getURI().equals(ANY_URI))
				.collect(Collectors.toMap(param -> param.getName(), param -> Arrays.asList(input.get(param.getName()))));

		gen.subInMultiUris(parametersToUris, n3WithParameters);
		//endregion

		//region Substitute other (literal) variables
		Map<String, List<Literal>> parametersToLiterals = requiredParams.getParameters().values().stream()
				.filter(value->!value.getRDFDataType().getURI().equals(ANY_URI))
				.collect(Collectors.toMap(
						param -> param.getName(),
						param -> Arrays.asList(ResourceFactory.createTypedLiteral(
																	input.get(param.getName())[0],
																	param.getRDFDataType()
																	)
						)));

		gen.subInMultiLiterals(parametersToLiterals, n3WithParameters);
		//endregion

		//Check if any n3 variables are left without inserted value
		String[] leftoverVariables = Arrays.stream(n3WithParameters.get(0).split(" "))
				.filter(token -> token.startsWith("?")).toArray(String[]::new);
		if(leftoverVariables.length>0){
			throw new InputMismatchException("N3 template variables:'" + Arrays.toString(leftoverVariables) +
					"' dont have corresponding input parameters with values that should be inserted in their place.");
		}
		return n3WithParameters.get(0);
	}


	@Override
	public void dereference() {}

}
