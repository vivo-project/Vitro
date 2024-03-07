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
	public static final String ABOX_ASSERTIONS_FIRSTTIME_BACKUP = ABOX_ASSERTIONS + "FirsttimeBackup";

	public static final String TBOX_ASSERTIONS = "http://vitro.mannlib.cornell.edu/default/asserted-tbox";
	public static final String TBOX_INFERENCES = "http://vitro.mannlib.cornell.edu/default/inferred-tbox";
	public static final String TBOX_UNION = "vitro:tboxOntModel";
	public static final String TBOX_ASSERTIONS_FIRSTTIME_BACKUP = TBOX_ASSERTIONS + "FirsttimeBackup";

	public static final String FULL_ASSERTIONS = "vitro:baseOntModel";
	public static final String FULL_INFERENCES = "vitro:inferenceOntModel";
	public static final String FULL_UNION = "vitro:jenaOntModel";

	public static final String APPLICATION_METADATA = "http://vitro.mannlib.cornell.edu/default/vitro-kb-applicationMetadata";
	public static final String APPLICATION_METADATA_FIRSTTIME_BACKUP = APPLICATION_METADATA + "FirsttimeBackup";
	public static final String USER_ACCOUNTS = "http://vitro.mannlib.cornell.edu/default/vitro-kb-userAccounts";
	public static final String USER_ACCOUNTS_FIRSTTIME_BACKUP = USER_ACCOUNTS + "FirsttimeBackup";
	public static final String DISPLAY = "http://vitro.mannlib.cornell.edu/default/vitro-kb-displayMetadata";
	public static final String DISPLAY_FIRSTTIME_BACKUP = DISPLAY + "FirsttimeBackup";
	public static final String DISPLAY_TBOX = "http://vitro.mannlib.cornell.edu/default/vitro-kb-displayMetadataTBOX";
	public static final String DISPLAY_TBOX_FIRSTTIME_BACKUP = DISPLAY_TBOX + "FirsttimeBackup";
	public static final String DISPLAY_DISPLAY = "http://vitro.mannlib.cornell.edu/default/vitro-kb-displayMetadata-displayModel";
	public static final String DISPLAY_DISPLAY_FIRSTTIME_BACKUP = DISPLAY_DISPLAY + "FirsttimeBackup";
	public static final String DYNAMIC_API_ABOX = "http://vitro.mannlib.cornell.edu/default/dynamic-api-abox";
	public static final String DYNAMIC_API_ABOX_FIRSTTIME_BACKUP = DYNAMIC_API_ABOX + "FirsttimeBackup";
	public static final String DYNAMIC_API_TBOX = "http://vitro.mannlib.cornell.edu/default/dynamic-api-tbox";
	public static final String DYNAMIC_API_TBOX_FIRSTTIME_BACKUP = DYNAMIC_API_TBOX + "FirsttimeBackup";
	public static final String SHAPES = "http://vitro.mannlib.cornell.edu/default/shapes";
	public static final String SHAPES_FIRSTTIME_BACKUP = SHAPES + "FirsttimeBackup";
	public static final String INTERFACE_I18N = "http://vitro.mannlib.cornell.edu/default/interface-i18n";
	public static final String INTERFACE_I18N_FIRSTTIME_BACKUP = INTERFACE_I18N + "FirsttimeBackup";
	public static final String ACCESS_CONTROL = "http://vitro.mannlib.cornell.edu/default/access-control";
	public static final String ACCESS_CONTROL_FIRSTTIME_BACKUP = ACCESS_CONTROL + "FirsttimeBackup";

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
		map.put("ABOX_ASSERTIONS_FIRSTTIME_BACKUP", ABOX_ASSERTIONS_FIRSTTIME_BACKUP);
		map.put("TBOX_ASSERTIONS", TBOX_ASSERTIONS);
		map.put("TBOX_INFERENCES", TBOX_INFERENCES);
		map.put("TBOX_UNION", TBOX_UNION);
		map.put("TBOX_ASSERTIONS_FIRSTTIME_BACKUP", TBOX_ASSERTIONS_FIRSTTIME_BACKUP);
		map.put("FULL_ASSERTIONS", FULL_ASSERTIONS);
		map.put("FULL_INFERENCES", FULL_INFERENCES);
		map.put("FULL_UNION", FULL_UNION);
		map.put("APPLICATION_METADATA", APPLICATION_METADATA);
		map.put("APPLICATION_METADATA_FIRSTTIME_BACKUP", APPLICATION_METADATA_FIRSTTIME_BACKUP);
		map.put("USER_ACCOUNTS", USER_ACCOUNTS);
		map.put("USER_ACCOUNTS_FIRSTTIME_BACKUP", USER_ACCOUNTS_FIRSTTIME_BACKUP);
		map.put("DISPLAY", DISPLAY);
		map.put("DISPLAY_FIRSTTIME_BACKUP", DISPLAY_FIRSTTIME_BACKUP);
		map.put("DISPLAY_TBOX", DISPLAY_TBOX);
		map.put("DISPLAY_TBOX_FIRSTTIME_BACKUP", DISPLAY_TBOX_FIRSTTIME_BACKUP);
		map.put("DISPLAY_DISPLAY", DISPLAY_DISPLAY);
		map.put("DISPLAY_DISPLAY_FIRSTTIME_BACKUP", DISPLAY_DISPLAY_FIRSTTIME_BACKUP);
		map.put("DYNAMIC_API_ABOX", DYNAMIC_API_ABOX);
		map.put("DYNAMIC_API_ABOX_FIRSTTIME_BACKUP", DYNAMIC_API_ABOX_FIRSTTIME_BACKUP);
		map.put("DYNAMIC_API_TBOX", DYNAMIC_API_TBOX);
		map.put("DYNAMIC_API_TBOX_FIRSTTIME_BACKUP", DYNAMIC_API_TBOX_FIRSTTIME_BACKUP);
        map.put("SHAPES", SHAPES);
        map.put("SHAPES_FIRSTTIME_BACKUP", SHAPES_FIRSTTIME_BACKUP);
		map.put("INTERFACE_I18N", INTERFACE_I18N);
		map.put("INTERFACE_I18N_FIRSTTIME_BACKUP", INTERFACE_I18N_FIRSTTIME_BACKUP);
		map.put("INTERFACE_I18N", ACCESS_CONTROL);
		map.put("INTERFACE_I18N_FIRSTTIME_BACKUP", ACCESS_CONTROL_FIRSTTIME_BACKUP);
		return Collections.unmodifiableMap(map);
	}

}
