/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;

public class JSONFilesLoader {
	private static final Log log = LogFactory.getLog(JSONFilesLoader.class);

	public static JSONArray pivotsIndexProperty = null;

	public JSONFilesLoader() {
		
		String jsonconfig;
		try {
			
			jsonconfig = new String(Files.readAllBytes(Paths.get(ApplicationUtils.instance().getHomeDirectory().getPath().toString() + "/json/config.json")));

			JSONObject reader = new JSONObject(jsonconfig);
			
			if (reader.has("pivotsIndexProperty")) {
				pivotsIndexProperty = reader.getJSONArray("pivotsIndexProperty");
			} else {
				log.warn("pivotsIndexProperty missing");
			}

		} catch (IOException e) {
			log.error("jsonconfig error: "+e.toString());
		}
		
	}

}
