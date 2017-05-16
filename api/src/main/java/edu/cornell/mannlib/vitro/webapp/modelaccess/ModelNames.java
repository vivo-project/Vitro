/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A central place to record the URIs of the models that we rely on.
 */
public class ModelNames {
	public static final String ABOX_ASSERTIONS = "http://vitro.mannlib.cornell.edu/default/vitro-kb-2";
	public static final String ABOX_INFERENCES = "http://vitro.mannlib.cornell.edu/default/vitro-kb-inf";
	public static final String ABOX_UNION = "vitro:aboxOntModel";

	public static final String TBOX_ASSERTIONS = "http://vitro.mannlib.cornell.edu/default/asserted-tbox";
	public static final String TBOX_INFERENCES = "http://vitro.mannlib.cornell.edu/default/inferred-tbox";
	public static final String TBOX_UNION = "vitro:tboxOntModel";

	public static final String FULL_ASSERTIONS = "vitro:baseOntModel";
	public static final String FULL_INFERENCES = "vitro:inferenceOntModel";
	public static final String FULL_UNION = "vitro:jenaOntModel";

	public static final String APPLICATION_METADATA = "http://vitro.mannlib.cornell.edu/default/vitro-kb-applicationMetadata";
	public static final String USER_ACCOUNTS = "http://vitro.mannlib.cornell.edu/default/vitro-kb-userAccounts";
	public static final String DISPLAY = "http://vitro.mannlib.cornell.edu/default/vitro-kb-displayMetadata";
	public static final String DISPLAY_TBOX = "http://vitro.mannlib.cornell.edu/default/vitro-kb-displayMetadataTBOX";
	public static final String DISPLAY_DISPLAY = "http://vitro.mannlib.cornell.edu/default/vitro-kb-displayMetadata-displayModel";

	/**
	 * A map of the URIS, keyed by their short names, intended only for display
	 * purposes.
	 */
	public static final Map<String, String> namesMap = populateNamesMap();

	private static Map<String, String> populateNamesMap() {
		Map<String, String> map = new HashMap<>();
		map.put("ABOX_ASSERTIONS", ABOX_ASSERTIONS);
		map.put("ABOX_INFERENCES", ABOX_INFERENCES);
		map.put("ABOX_UNION", ABOX_UNION);
		map.put("TBOX_ASSERTIONS", TBOX_ASSERTIONS);
		map.put("TBOX_INFERENCES", TBOX_INFERENCES);
		map.put("TBOX_UNION", TBOX_UNION);
		map.put("FULL_ASSERTIONS", FULL_ASSERTIONS);
		map.put("FULL_INFERENCES", FULL_INFERENCES);
		map.put("FULL_UNION", FULL_UNION);
		map.put("APPLICATION_METADATA", APPLICATION_METADATA);
		map.put("USER_ACCOUNTS", USER_ACCOUNTS);
		map.put("DISPLAY", DISPLAY);
		map.put("DISPLAY_TBOX", DISPLAY_TBOX);
		map.put("DISPLAY_DISPLAY", DISPLAY_DISPLAY);
		return Collections.unmodifiableMap(map);
	}

}
