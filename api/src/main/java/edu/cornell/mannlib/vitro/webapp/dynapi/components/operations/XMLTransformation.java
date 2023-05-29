package edu.cornell.mannlib.vitro.webapp.dynapi.components.operations;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.SimpleDataView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class XMLTransformation extends AbstractOperation {

    private static final Log log = LogFactory.getLog(XMLTransformation.class);
    private Parameter xsltParam;
    private Parameter inputXmlParam;
    private Parameter outputXmlParam;

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#xslt", minOccurs = 1, maxOccurs = 1)
    public void setXsltParam(Parameter xsltParam) throws InitializationException {
        this.xsltParam = xsltParam;
        inputParams.add(xsltParam);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#inputXml", minOccurs = 1, maxOccurs = 1)
    public void setInputXmlParam(Parameter inputXmlParam) throws InitializationException {
        this.inputXmlParam = inputXmlParam;
        inputParams.add(inputXmlParam);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#outputXml", minOccurs = 1, maxOccurs = 1)
    public void setOutputXml(Parameter outputXmlParam) throws InitializationException {
        this.outputXmlParam = outputXmlParam;
        outputParams.add(outputXmlParam);
    }

    @Override
    public OperationResult runOperation(DataStore dataStore) throws Exception {
        String is = SimpleDataView.getStringRepresentation(inputXmlParam.getName(), dataStore);
        String styles = SimpleDataView.getStringRepresentation(xsltParam.getName(), dataStore);
        ByteArrayOutputStream output = transform(is, styles);
        Data outputData = new Data(outputXmlParam);
        outputData.setRawString(output.toString(StandardCharsets.UTF_8.toString()));
        outputData.initializeFromString();
        dataStore.addData(outputXmlParam.getName(), outputData);
        return OperationResult.ok();
    }

    private ByteArrayOutputStream transform(String input, String styles) throws Exception {
        InputStream inputStream = IOUtils.toInputStream(input, StandardCharsets.UTF_8);
        InputStream styleInputStream = IOUtils.toInputStream(styles, StandardCharsets.UTF_8);
        Source stylesource = new StreamSource(styleInputStream);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer(stylesource);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(inputStream);
        transformer.transform(new DOMSource(doc), new StreamResult(output));
        return output;
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
        if (xsltParam == null ) {
            log.error("xsltParam param is not defined in the configuration");
            result = false;
        }

        if (outputXmlParam == null) {
            log.error("output xml param is not defined in the configuration");
            result = false;
        }

        if (inputXmlParam == null) {
            log.error("input xml param is not defined in the configuration");
            result = false;
        }

        return result;
    }
}
