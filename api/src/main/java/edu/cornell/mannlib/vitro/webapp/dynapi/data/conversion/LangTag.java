package edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion;

public class LangTag {
	private static final String WEIGHT_PARAM = "q";
	private String tag;
	private Double weight;
	private static final String SPLIT_BY_SEMICOLUMN_AND_TRIM_REGEX = "\\s*;\\s*";

	public LangTag(String tag, Double weight) {
		this.tag = tag;
		this.weight = weight;
	}
	
	public LangTag(String tag) {
		this.tag = tag;
		this.weight = 1.0;
	}
	
	public static LangTag parse(String rawTag) {
		String[] parts = rawTag.split(SPLIT_BY_SEMICOLUMN_AND_TRIM_REGEX);
		Double w = 1.0;
		if (parts.length > 1) {
			String[] qparts = parts[1].split("=");
			if (qparts.length > 1 && WEIGHT_PARAM.equals(qparts[0])) {
				w = Double.parseDouble(qparts[1]);
			}
		}
		return new LangTag(parts[0], w); 
	}

	public String getTag() {
		return tag;
	}

	public Double getWeight() {
		return weight;
	}
}
