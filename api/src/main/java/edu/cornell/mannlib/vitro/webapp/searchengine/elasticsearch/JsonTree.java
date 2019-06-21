/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Build a Map of Objects, suitable for marshalling by Jackson.
 * 
 * Include conditional logic, so null values, empty maps, or empty lists will
 * not be added, unless you use the special values.
 */
public class JsonTree {
    /**
     * Empty maps will not be added, except for this one.
     */
    public static final Map<String, Object> EMPTY_JSON_MAP = Collections
            .emptyMap();

    /**
     * Empty lists will not be added, except for this one.
     */
    public static final List<Object> EMPTY_JSON_LIST = Collections.emptyList();

    /**
     * Create the tree
     */
    public static JsonTree tree() {
        return new JsonTree();
    }

    /**
     * This will cause negative integers to be ignored.
     */
    public static Integer ifPositive(int i) {
        return (i > 0) ? i : null;
    }

    private Map<String, Object> map = new HashMap<>();

    public JsonTree put(String key, Object value) {
        if (isSignificant(value)) {
            storeIt(key, value);
        }
        return this;
    }

    private boolean isSignificant(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Map && ((Map) value).isEmpty()
                && value != EMPTY_JSON_MAP) {
            return false;
        }
        if (value instanceof List && ((List) value).isEmpty()
                && value != EMPTY_JSON_LIST) {
            return false;
        }
        return true;
    }

    private void storeIt(String key, Object value) {
        if (value instanceof JsonTree) {
            map.put(key, ((JsonTree) value).asMap());
        } else {
            map.put(key, value);
        }
    }

    public Map<String, Object> asMap() {
        return new HashMap<>(map);
    }
}
