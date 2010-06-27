package edu.cornell.mannlib.vitro.webapp.visualization.constants;

import java.util.HashMap;
import java.util.Map;

public class QueryConstants {
	
	/*
	 * This is not supposed to be used. Will remove after verifying that the dynamic prefix
	 * section generation works. 
	 * */
	public static final String SPARQL_QUERY_PREFIXES_DEP = "" +
			"PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#>\n" +
			"PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>\n" +
			"PREFIX vivo:  <http://vivo.library.cornell.edu/ns/0.1#>\n" +
			"PREFIX core:  <http://vivoweb.org/ontology/core#>\n" +
			"PREFIX bibo:  <http://purl.org/ontology/bibo/>\n" +
			"PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\n" +
			"PREFIX aktp:  <http://www.aktors.org/ontology/portal#>\n" +
			"PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>\n" +
			"PREFIX owl:   <http://www.w3.org/2002/07/owl#>\n" +
			"PREFIX swrl:  <http://www.w3.org/2003/11/swrl#>\n" +
			"PREFIX swrlb: <http://www.w3.org/2003/11/swrlb#>\n" +
			"PREFIX far: <http://vitro.mannlib.cornell.edu/ns/reporting#>\n" +
			"PREFIX ai: <http://vitro.mannlib.cornell.edu/ns/hotel#>\n" +
			"PREFIX akts: <http://www.aktors.org/ontology/support#>\n" +
			"PREFIX hr: <http://vivo.cornell.edu/ns/hr/0.9/hr.owl#>\n" +
			"PREFIX dcterms: <http://purl.org/dc/terms/>\n" +
			"PREFIX dcelem: <http://purl.org/dc/elements/1.1/>\n" +
			"PREFIX event: <http://purl.org/NET/c4dm/event.owl#>\n" +
			"PREFIX geo: <http://aims.fao.org/aos/geopolitical.owl#>\n" +
			"PREFIX mann: <http://vivo.cornell.edu/ns/mannadditions/0.1#>\n" +
			"PREFIX pubmed: <http://vitro.mannlib.cornell.edu/ns/pubmed#>\n" +
			"PREFIX rdfsyn: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
			"PREFIX socsci: <http://vivo.library.cornell.edu/ns/vivo/socsci/0.1#>\n" +
			"PREFIX stars: <http://vitro.mannlib.cornell.edu/ns/cornell/stars/classes#>\n" +
			"PREFIX temp: <http://vitro.mannlib.cornell.edu/ns/temp#>\n" +
			"PREFIX wos: <http://vivo.mannlib.cornell.edu/ns/ThomsonWOS/0.1#>\n";
	
	
	public static final Map<String, String> PREFIX_TO_NAMESPACE = new HashMap<String, String>() {{
		
			put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
			put("xsd", "http://www.w3.org/2001/XMLSchema#");
			put("owl", "http://www.w3.org/2002/07/owl#");
			put("swrl", "http://www.w3.org/2003/11/swrl#");
			put("swrlb", "http://www.w3.org/2003/11/swrlb#");
			put("vitro", "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#");
			put("far", "http://vitro.mannlib.cornell.edu/ns/reporting#");
			put("ai", "http://vitro.mannlib.cornell.edu/ns/hotel#");
			put("aktp", "http://www.aktors.org/ontology/portal#");
			put("akts", "http://www.aktors.org/ontology/support#");
			put("bibo", "http://purl.org/ontology/bibo/");
			put("hr", "http://vivo.cornell.edu/ns/hr/0.9/hr.owl#");
			put("dcterms", "http://purl.org/dc/terms/");
			put("dcelem", "http://purl.org/dc/elements/1.1/");
			put("event", "http://purl.org/NET/c4dm/event.owl#");
			put("foaf", "http://xmlns.com/foaf/0.1/");
			put("geo", "http://aims.fao.org/aos/geopolitical.owl#");
			put("mann", "http://vivo.cornell.edu/ns/mannadditions/0.1#");
			put("pubmed", "http://vitro.mannlib.cornell.edu/ns/pubmed#");
			put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
			put("rdfsyn", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			put("skos", "http://www.w3.org/2004/02/skos/core#");
			put("socsci", "http://vivo.library.cornell.edu/ns/vivo/socsci/0.1#");
			put("stars", "http://vitro.mannlib.cornell.edu/ns/cornell/stars/classes#");
			put("temp", "http://vitro.mannlib.cornell.edu/ns/temp#");
			put("wos", "http://vivo.mannlib.cornell.edu/ns/ThomsonWOS/0.1#");
			put("core", "http://vivoweb.org/ontology/core#");
			put("vivo", "http://vivo.library.cornell.edu/ns/0.1#");
		
	}};
	
	
	public static String getSparqlPrefixQuery() {
		
		StringBuilder prefixSection = new StringBuilder(); 
		
		for (Map.Entry prefixEntry : PREFIX_TO_NAMESPACE.entrySet()) {
			prefixSection.append("PREFIX " + prefixEntry.getKey() + ": <" + prefixEntry.getValue() + ">\n");
		}
		
		
		return prefixSection.toString();
		
	}
	
	
}


