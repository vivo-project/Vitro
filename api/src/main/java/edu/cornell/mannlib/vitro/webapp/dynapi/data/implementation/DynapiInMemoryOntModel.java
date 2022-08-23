package edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation;

import java.io.StringReader;

import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Model;

public class DynapiInMemoryOntModel {

	public static String serialize(Model input){
		//TODO: implement
		return "";
	}
	
	public static Model deserialize(String input){
		//TODO: implement 
		OntModelImpl model = new OntModelImpl(OntModelSpec.OWL_MEM);
		return model.read(new StringReader(input), null, "n3");
	}
	
	
}
