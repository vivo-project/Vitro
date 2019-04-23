/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccess;

/**
 * Make this information available to each DataDistributor instance.
 */
public interface DataDistributorContext {
    /**
     * The parameter map on the HTTP request for which the instance was created.
     * 
     * Each value is a non-empty array. Null values, or zero-length arrays are
     * not allowed. Arrays may not contain nulls, but may contain empty strings.
     */
    Map<String, String[]> getRequestParameters();

    /**
     * The data structures that provide access to the content of the
     * triple-store.
     * 
     * These structures are associated with the current HTTP request.
     */
    RequestModelAccess getRequestModels();

    /**
     * Permits the DataDistributor instance to check whether the current user is
     * authorized for a given activity, such as accessing unfiltered data or
     * accessing the configuration models.
     * 
     * For example, before a DataDistributor releases information about user
     * accounts, it should execute a check of this nature: <pre>
     *    if (!ddc.isAuthorized(SimplePermission.QUERY_USER_ACCOUNTS_MODEL.ACTION)) {
     *        throw new DataDistributor.NotAuthorizedException();
     *    }
     * </pre>
     * 
     * It is probably best to execute this code in the DataDistributor's init()
     * or writeOutput() methods.
     * 
     * @param actions
     *            The activities for which the user must be authorized, or the
     *            DataDistributor will not execute.
     */
    boolean isAuthorized(AuthorizationRequest actions);

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    /**
     * Convert a parameter map to its list-based equivalent.
     */
    public static Map<String, List<String>> arraysToLists(
            Map<String, String[]> arrays) {
        Map<String, List<String>> lists = new HashMap<>();
        for (String key : arrays.keySet()) {
            lists.put(key, new ArrayList<>(Arrays.asList(arrays.get(key))));
        }
        return lists;
    }

    /**
     * Convert a list-based parameter map back to its canonical form.
     */
    public static Map<String, String[]> listsToArrays(
            Map<String, List<String>> lists) {
        Map<String, String[]> arrays = new HashMap<>();
        for (String key : lists.keySet()) {
            List<String> values = lists.get(key);
            arrays.put(key, values.toArray(new String[values.size()]));
        }
        return arrays;
    }

    /**
     * Create a copy of the parameter map: changes to the copy don't affect the
     * original.
     */
    public static Map<String, String[]> deepCopyParameters(
            Map<String, String[]> map) {
        return listsToArrays(arraysToLists(map));
    }
    
    /**
     * Format the parameter map, suitable for logging.
     */
    public static String formatParameters(DataDistributorContext ddContext) {
        return arraysToLists(ddContext.getRequestParameters()).toString();
    }
    
}
