/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components.operations;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.ModelView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.SimpleDataView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

public class XMLTransformation extends AbstractOperation {

    private static final Log log = LogFactory.getLog(XMLTransformation.class);
    private static final ErrorListener errorListener = createXMLErrorListener();
    private Parameter xsltParam;
    private Parameter inputXmlParam;
    private Parameter outputXmlParam;
    private Templates transformTemplates;

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#xslt", minOccurs = 1, maxOccurs = 1)
    public void setXsltParam(Parameter xsltParam) throws InitializationException {
        this.xsltParam = xsltParam;
        inputParams.add(xsltParam);
        prepareTransformTemplates();
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
        String is = getInputString(dataStore);
        String styles = SimpleDataView.getStringRepresentation(xsltParam.getName(), dataStore);
        ByteArrayOutputStream output = transform(is, styles);
        Data outputData = new Data(outputXmlParam);
        outputData.setRawString(output.toString(StandardCharsets.UTF_8.toString()));
        outputData.initializeFromString();
        dataStore.addData(outputXmlParam.getName(), outputData);
        return OperationResult.ok();
    }

    private String getInputString(DataStore dataStore) {
        if (ModelView.isModel(inputXmlParam)) {
            return ModelView.getModelRDFXmlRepresentation(dataStore, inputXmlParam);
        }
        return SimpleDataView.getStringRepresentation(inputXmlParam.getName(), dataStore);
    }

    private ByteArrayOutputStream transform(String input, String styles) throws Exception {
        InputStream inputStream = IOUtils.toInputStream(input, StandardCharsets.UTF_8);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Transformer transformer = getTransformer(styles);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(inputStream);
        transformer.transform(new DOMSource(doc), new StreamResult(output));
        return output;
    }

    private void prepareTransformTemplates() throws InitializationException {
        try {
            if (xsltParam.isInternal() && !xsltParam.isOptional()) {
                String defaultValue = xsltParam.getDefaultValue();
                if (defaultValue != null) {
                    InputStream styleInputStream = IOUtils.toInputStream(defaultValue, StandardCharsets.UTF_8);
                    Source stylesource = new StreamSource(styleInputStream);
                    TransformerFactory transformerFactory = TransformerFactory.newInstance(
                            "net.sf.saxon.TransformerFactoryImpl", null);
                    transformerFactory.setErrorListener(errorListener);
                    transformTemplates = transformerFactory.newTemplates(stylesource);
                }
            }
        } catch (Exception e) {
            throw new InitializationException(e.getMessage());
        }
    }

    private Transformer getTransformer(String styles) throws TransformerFactoryConfigurationError,
            TransformerConfigurationException, Exception {
        if (transformTemplates != null) {
            return transformTemplates.newTransformer();
        }
        InputStream styleInputStream = IOUtils.toInputStream(styles, StandardCharsets.UTF_8);
        Source stylesource = new StreamSource(styleInputStream);
        TransformerFactory transformerFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl",
                null);
        transformerFactory.setErrorListener(errorListener);
        Transformer transformer = transformerFactory.newTransformer(stylesource);
        if (transformer == null) {
            throw new Exception("Failed to initialize transformer. Check styles.");
        }
        return transformer;
    }

    @Override
    public boolean isInputValid(DataStore dataStore) {
        if (!super.isInputValid(dataStore)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isOutputValid(DataStore dataStore) {
        if (!super.isOutputValid(dataStore)) {
            return false;
        }
        return true;
    }

    public boolean isValid() {
        boolean result = true;
        if (xsltParam == null) {
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

    private static ErrorListener createXMLErrorListener() {
        return new ErrorListener() {
            @Override
            public void warning(TransformerException e) throws TransformerException {
                log.warn(e, e);
            }

            @Override
            public void error(TransformerException e) throws TransformerException {
                log.error(e, e);
            }

            @Override
            public void fatalError(TransformerException e) throws TransformerException {
                log.error(e, e);
            }
        };
    }
}
