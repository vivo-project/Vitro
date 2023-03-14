package edu.cornell.mannlib.vitro.webapp.dynapi.components.operations;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;


import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.SimpleDataView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.StringView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class XMLValidation extends AbstractOperation {

    private static final Log log = LogFactory.getLog(XMLValidation.class);
    private Parameter schemaParam;
    private Parameter inputXmlParam;
    private Parameter validationResult;
    private Parameter errorMessage;


    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#xsd", minOccurs = 1, maxOccurs = 1)
    public void setXsltParam(Parameter xsltParam) throws InitializationException {
        this.schemaParam = xsltParam;
        inputParams.add(xsltParam);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#inputXml", minOccurs = 1, maxOccurs = 1)
    public void setInputXmlParam(Parameter inputXmlParam) throws InitializationException {
        this.inputXmlParam = inputXmlParam;
        inputParams.add(inputXmlParam);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#result", minOccurs = 1, maxOccurs = 1)
    public void setValidationResult(Parameter validationResult) throws InitializationException {
        this.validationResult = validationResult;
        outputParams.add(validationResult);
    }
    
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#errorMessage", minOccurs = 0, maxOccurs = 1)
    public void setErrorMessage(Parameter errorMessage) throws InitializationException {
        if (!StringView.isPlainString(errorMessage)) {
            throw new InitializationException("Only string parameters accepted on setErrorMessage");
        }
        this.errorMessage = errorMessage;
        outputParams.add(errorMessage);
    }
    
    @Override
    public OperationResult runOperation(DataStore dataStore) throws Exception {
        String input = SimpleDataView.getStringRepresentation(inputXmlParam.getName(), dataStore);
        String schema = SimpleDataView.getStringRepresentation(schemaParam.getName(), dataStore);
        String message = validate(input, schema);
        boolean successfullValidation = false;
        if (StringUtils.isBlank(message)) {
            successfullValidation = true;
        }
        Data resultData = new Data(validationResult);
        resultData.setRawString(Boolean.toString(successfullValidation));
        resultData.initializeFromString();
        dataStore.addData(validationResult.getName(), resultData);
        if (errorMessage != null) {
            Data messageData = new Data(errorMessage);
            messageData.setRawString(message);
            messageData.initializeFromString();
            dataStore.addData(errorMessage.getName(), messageData);
        }
        return OperationResult.ok();
    }

    private String validate(String input, String stringSchema) {
        try {
            InputStream inputStream = IOUtils.toInputStream(input, StandardCharsets.UTF_8);
            InputStream schemaInputStream = IOUtils.toInputStream(stringSchema, StandardCharsets.UTF_8);
            Source schemaSource = new StreamSource(schemaInputStream);
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(schemaSource);
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(inputStream));
        } catch (Exception e) {
            log.debug(e, e);
            return e.getMessage();
        }
        return "";
    }
    
    @Override
    public boolean isInputValid(DataStore dataStore){
        if (!super.isInputValid(dataStore)) {
            return false;
        }
        return true;
    }  
    
    @Override
    public boolean isOutputValid(DataStore dataStore){
        if (!super.isOutputValid(dataStore)) {
            return false;
        }
        return true;
    } 
    
    public boolean isValid() {
        boolean result = true;
        if (schemaParam == null ) {
            log.error("schema param is not defined in the configuration");
            result = false;
        }

        if (validationResult == null) {
            log.error("boolean result param is not defined in the configuration");
            result = false;
        }

        if (inputXmlParam == null) {
            log.error("input xml param is not defined in the configuration");
            result = false;
        }
        return result;
    }
}
