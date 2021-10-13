/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reporting;

import org.w3c.dom.Document;

/**
 * Marker interface to say that the report generates an intermediate Xml
 * document
 */
public interface XmlGenerator {
    Document generateXml() throws ReportGeneratorException;
}
