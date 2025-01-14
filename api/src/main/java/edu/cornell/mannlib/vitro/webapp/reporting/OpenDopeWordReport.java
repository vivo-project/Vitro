/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reporting;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccess;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Template based report that uses the Docx4j library to process Word templates
 * that have the OpenDOPE controls embedded in them.
 */
public class OpenDopeWordReport extends AbstractTemplateReport implements XmlGenerator {
    /**
     * Define the docx mime type, including the UTF-8 character set
     */
    @Override
    public String getContentType() throws ReportGeneratorException {
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document; charset=UTF-8";
    }

    /**
     * Xml document is required for passing to the Docx4j library
     */
    @Override
    public Document generateXml(RequestModelAccess request, UserAccount account) throws ReportGeneratorException {
        try {
            DocumentBuilder xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document xmlDoc = xmlBuilder.newDocument();

            Element rootElement = xmlDoc.createElement("results");
            xmlDoc.appendChild(rootElement);

            // Execute all datasources
            for (DataSource dataSource : dataSources) {
                // Get the result of the datasource
                String body = dataSource.getBody(new HashMap<>(), request, account);

                // Load the JSON document
                JsonFactory factory = new JsonFactory();
                ObjectMapper mapper = new ObjectMapper(factory);
                JsonNode rootNode = mapper.readTree(body);

                // Convert the JSON into Xml
                convertObjectNodeToXml(xmlDoc, dataSource.getOutputName(), rootNode);
            }

            return xmlDoc;
        } catch (IOException | ParserConfigurationException e) {
        }

        return null;
    }

    private void convertObjectNodeToXml(Document xmlDoc, String outputName, JsonNode rootNode) {
        Element rootElement = xmlDoc.getDocumentElement();
        Element resultsElement = xmlDoc.createElement(outputName);
        rootElement.appendChild(resultsElement);

        // Only process if it is the result Json of a SELECT distributor
        if (rootNode.has("head") && rootNode.has("results")) {
            // Get the results and bindings
            JsonNode results = rootNode.findValue("results");
            JsonNode bindings = results.findValue("bindings");
            if (bindings.isArray()) {
                // For each binding
                for (int idx = 0; idx < bindings.size(); idx++) {
                    JsonNode result = bindings.get(idx);
                    // Get the results
                    Iterator<Map.Entry<String, JsonNode>> iterator = result.fields();
                    if (iterator != null) {
                        // Create a row for the results
                        Element row = xmlDoc.createElement("row");

                        // For each result
                        while (iterator.hasNext()) {
                            Map.Entry<String, JsonNode> entry = iterator.next();

                            // Get the value
                            JsonNode value = entry.getValue();
                            JsonNode type = value.get("type");

                            // Add each literal as a column
                            if (type != null && type.isTextual() && "literal".equals(type.asText())) {
                                Element column = xmlDoc.createElement(entry.getKey());
                                column.setTextContent(value.get("value").asText());
                                row.appendChild(column);
                            }
                        }
                        resultsElement.appendChild(row);
                    }
                }
            }
        }
    }

    @Override
    public void generateReport(OutputStream outputStream, RequestModelAccess request, UserAccount account) throws ReportGeneratorException {
        // Get the XML
        Document xmlDoc = generateXml(request, account);
        try {
            // Create a word processing package from the template
            WordprocessingMLPackage wordMLPackage = Docx4J.load(new ByteArrayInputStream(template));

            // Bind the xml to the template
            Docx4J.bind(wordMLPackage, xmlDoc, Docx4J.FLAG_BIND_INSERT_XML & Docx4J.FLAG_BIND_BIND_XML);

            // Process the template
            Docx4J.save(wordMLPackage, outputStream, Docx4J.FLAG_NONE);
        } catch (Docx4JException e) {
            throw new ReportGeneratorException(e);
        }
    }

    @Override
    public boolean getImplementsXml() {
        return true;
    }
}
