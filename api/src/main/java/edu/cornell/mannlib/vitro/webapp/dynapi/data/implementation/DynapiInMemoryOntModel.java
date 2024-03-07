package edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;

public class DynapiInMemoryOntModel {

    public static String serializeXML(Model input) {
        return serialize(input, "RDF/XML");
    }

    public static String serializeN3(Model input) {
        return serialize(input, "n3");
    }

    public static String serialize(Model input, String lang) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RDFDataMgr.write(baos, input, RDFLanguages.nameToLang(lang));
        return baos.toString();
    }

    public static Model deserializeN3(String input) {
        OntModelImpl model = new OntModelImpl(OntModelSpec.OWL_MEM);
        return model.read(new StringReader(input), null, "n3");
    }

}
