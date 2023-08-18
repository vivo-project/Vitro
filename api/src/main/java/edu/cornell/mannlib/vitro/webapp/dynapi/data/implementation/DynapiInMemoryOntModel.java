package edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

public class DynapiInMemoryOntModel {

  public static String serialize(Model input, String lang){
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    RDFDataMgr.write(baos, input, RDFLanguages.nameToLang(lang)) ;
    return baos.toString();
  }
    
	public static String serialize(Model input){
	  return serialize(input, "RDF/XML");
	}
	
	public static Model deserialize(String input){
		//TODO: implement 
		OntModelImpl model = new OntModelImpl(OntModelSpec.OWL_MEM);
		return model.read(new StringReader(input), null, "n3");
	}
	
	
}
