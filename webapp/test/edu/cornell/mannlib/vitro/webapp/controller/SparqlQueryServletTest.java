/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.github.jsonldjava.core.JSONLD;
import com.github.jsonldjava.core.JSONLDProcessingError;
import com.github.jsonldjava.impl.JenaRDFParser;
import com.github.jsonldjava.utils.JSONUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class SparqlQueryServletTest {

    @Test
    public void testJSONLD() throws JSONLDProcessingError {
        //just check if we can use JSONLD-JAVA
        
        final String turtle = "@prefix const: <http://foo.com/> .\n"
                + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n"
                + "<http://localhost:8080/foo1> const:code \"123\" .\n"
                + "<http://localhost:8080/foo2> const:code \"ABC\"^^xsd:string .\n";

        final List<Map<String, Object>> expected = new ArrayList<Map<String, Object>>() {
            {
                add(new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://localhost:8080/foo1");
                        put("http://foo.com/code", new ArrayList<Object>() {
                            {
                                add(new LinkedHashMap<String, Object>() {
                                    {
                                        put("@value", "123");
                                    }
                                });
                            }
                        });
                    }
                });
                add(new LinkedHashMap<String, Object>() {
                    {
                        put("@id", "http://localhost:8080/foo2");
                        put("http://foo.com/code", new ArrayList<Object>() {
                            {
                                add(new LinkedHashMap<String, Object>() {
                                    {
                                        put("@value", "ABC");
                                    }
                                });
                            }
                        });
                    }
                });
            }
        };

        final Model modelResult = ModelFactory.createDefaultModel().read(
                new ByteArrayInputStream(turtle.getBytes()), "", "TURTLE");
        final JenaRDFParser parser = new JenaRDFParser();
        final Object json = JSONLD.fromRDF(modelResult, parser);

        assertTrue(JSONUtils.equals(json, expected));        
        
        
    }

}
