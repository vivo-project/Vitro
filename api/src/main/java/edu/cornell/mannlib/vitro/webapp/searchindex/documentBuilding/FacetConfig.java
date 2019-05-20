package edu.cornell.mannlib.vitro.webapp.searchindex.documentBuilding;

import org.json.JSONObject;

public class FacetConfig  {
	
	 public String configContextFor = null;
	 public String qualifiedBy = null;
	 public JSONObject pivotsIndexProperty = null;
	 public String regexp = null;
	 public String regexpreplace = null;
	
	 public FacetConfig(JSONObject jo) {

		 if(jo.has("qualifiedBy")) {
			 qualifiedBy = jo.getString("qualifiedBy");
		 }
		 if(jo.has("configContextFor")) {
			 configContextFor = jo.getString("configContextFor");
		 }
		 if(jo.has("pivotsIndexProperty")) {
		  pivotsIndexProperty = jo.getJSONObject("pivotsIndexProperty");
		 }
		 
		 if(jo.has("regexp")) {
			 regexp = jo.getString("regexp");
		 }
		 if(jo.has("regexpreplace")) {
			 regexpreplace = jo.getString("regexpreplace");
		 }
		 
 	 } 
}