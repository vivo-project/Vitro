/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.AUTH_VOCABULARY_PREFIX;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueKey;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueSet;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueSetRegistry;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
public class EntityPolicyController {

    private static final Log log = LogFactory.getLog(EntityPolicyController.class);
    private static Map<String, String> policyKeyToDataValueMap = new HashMap<String, String>();

    /**
     * @param entityUri - entity uniform resource identifier
     * @param aot - access object type
     * @param ao - access operation
     * @param selectedRoles - list of roles to assign
     * @param roles - list of all available roles
     */
    public static void updateEntityDataSet(String entityUri, AccessObjectType aot, AccessOperation ao,
            List<String> selectedRoles, List<String> roles) {
        if (StringUtils.isBlank(entityUri)) {
            return;
        }
        Set<String> selectedSet = new HashSet<>(selectedRoles);
        for (String role : roles) {
            if (selectedSet.contains(role)) {
                grantAccess(entityUri, aot, ao, role);
            } else {
                revokeAccess(entityUri, aot, ao, role);
            }
        }
    }

    private static AttributeValueSetRegistry getRegistry() {
        return AttributeValueSetRegistry.getInstance();
    }

    public static void revokeAccess(String entityUri, AccessObjectType aot, AccessOperation ao, String role,
            String... namedKeyComponents) {
        AttributeValueKey key = new AttributeValueKey(ao, aot, role, aot.toString(), namedKeyComponents);
        AttributeValueSet set = getRegistry().get(key);
        if (set != null) {
            if (set.contains(entityUri)) {
                set.remove(entityUri);
                String toRemove = getValueStatementString(entityUri, set.getValueSetUri());
                getLoader().updateAccessControlModel(toRemove, false);
            }
        } else {
            reduceInactiveValueSet(entityUri, aot, ao, role, namedKeyComponents);
        }
    }

    private static PolicyLoader getLoader() {
        return PolicyLoader.getInstance();
    }

    private static void reduceInactiveValueSet(String entityUri, AccessObjectType aot, AccessOperation ao,
            String role, String... namedKeyComponents) {
        StringBuilder removals = new StringBuilder();
        getDataValueStatements(entityUri, aot, ao, Collections.singleton(role), removals, namedKeyComponents);
        getLoader().updateAccessControlModel(removals.toString(), false);
    }

    public static void grantAccess(String entityUri, AccessObjectType aot, AccessOperation ao, String role,
            String... namedKeyComponents) {
        AttributeValueKey key = new AttributeValueKey(ao, aot, role, aot.toString(), namedKeyComponents);
        AttributeValueSet set = getRegistry().get(key);
        if (set != null) {
            if (!set.contains(entityUri)) {
                set.add(entityUri);
                String toAdd = getValueStatementString(entityUri, set.getValueSetUri());
                getLoader().updateAccessControlModel(toAdd, true);
            }
        } else {
            extendInactiveValueSet(entityUri, aot, ao, role, namedKeyComponents);
            loadPolicy(aot, ao, role, namedKeyComponents);
        }
    }

    private static void loadPolicy(AccessObjectType aot, AccessOperation ao, String role,
            String... namedKeyComponents) {
        String[] ids = Arrays.copyOf(namedKeyComponents, namedKeyComponents.length + 3);
        ids[ids.length - 1] = ao.toString();
        ids[ids.length - 2] = aot.toString();
        ids[ids.length - 3] = role;
        String dataSetUri = getLoader().getDataSetUriByKey(ids);
        if (dataSetUri != null) {
            DynamicPolicy policy = getLoader().loadPolicyFromTemplateDataSet(dataSetUri);
            if (policy != null) {
                PolicyStore.getInstance().add(policy);
            }
        }
    }

    private static void extendInactiveValueSet(String entityUri, AccessObjectType aot, AccessOperation ao,
            String role, String... namedKeyComponents) {
        StringBuilder additions = new StringBuilder();
        getDataValueStatements(entityUri, aot, ao, Collections.singleton(role), additions, namedKeyComponents);
        getLoader().updateAccessControlModel(additions.toString(), true);
    }

    public static boolean isGranted(String entityUri, AccessObjectType aot, AccessOperation ao, String role,
            String... namedKeyComponents) {
        if (StringUtils.isBlank(entityUri)) {
            return false;
        }
        AttributeValueSetRegistry registry = getRegistry();
        AttributeValueKey key = new AttributeValueKey(ao, aot, role, aot.toString(), namedKeyComponents);
        AttributeValueSet set = registry.get(key);
        if (set == null) {
            return false;
        }
        return set.contains(entityUri);
    }

    public static List<String> getGrantedRoles(String entityUri, AccessOperation ao, AccessObjectType aot,
            List<String> allRoles) {
        if (StringUtils.isBlank(entityUri)) {
            return Collections.emptyList();
        }
        List<String> grantedRoles = new LinkedList<>();
        for (String role : allRoles) {
            if (isUriInTestDataset(entityUri, ao, aot, role)) {
                grantedRoles.add(role);
            }
        }
        return grantedRoles;
    }

    public static void getDataValueStatements(String entityUri, AccessObjectType aot, AccessOperation ao,
            Set<String> selectedRoles, StringBuilder sb, String... namedKeyComponents) {
        if (StringUtils.isBlank(entityUri)) {
            return;
        }
        for (String role : selectedRoles) {
            String valueSetUri = getValueSetUri(aot, ao, role, namedKeyComponents);
            if (valueSetUri == null) {
                log.debug(String.format("Policy value set wasn't found by key:\n%s\n%s\n%s", ao, aot, role));
                continue;
            }
            sb.append(getValueStatementString(entityUri, valueSetUri));
        }
    }

    private static String getValueStatementString(String entityUri, String valueSetUri) {
        return "<" + valueSetUri + "> <" + AUTH_VOCABULARY_PREFIX + "value> <" + entityUri + "> .\n";
    }

    public static void deletedEntityEvent(Property oldObj) {
        log.debug("Don't delete access rule if property has been deleted " + oldObj);
    }

    public static void updatedEntityEvent(Object oldObj, Object newObj) {
        if (oldObj instanceof Property || newObj instanceof Property) {
            log.debug("update entity event old " + oldObj + " new object " + newObj);
        }
    }

    public static void insertedEntityEvent(Property newObj) {
        log.debug("Nothing to do " + newObj);
    }

    private static boolean isUriInTestDataset(String entityUri, AccessOperation ao, AccessObjectType aot, String role) {
        Set<String> values = getLoader().getDataSetValues(ao, aot, role);
        return values.contains(entityUri);
    }

    private static String getValueSetUri(AccessObjectType aot, AccessOperation ao, String role,
            String... namedKeyComponents) {
        String key = generateKey(aot, ao, role, namedKeyComponents);
        if (policyKeyToDataValueMap.containsKey(key)) {
            return policyKeyToDataValueMap.get(key);
        }
        String uri = getLoader().getEntityValueSetUri(ao, aot, role, namedKeyComponents);
        policyKeyToDataValueMap.put(key, uri);
        return uri;
    }

    private static String generateKey(AccessObjectType aot, AccessOperation ao, String role,
            String[] namedKeyComponents) {
        String key = aot.toString() + "." + ao.toString() + "." + role;
        if (namedKeyComponents.length > 0) {
            List<String> namedKeys = new ArrayList<>(Arrays.asList(namedKeyComponents));
            Collections.sort(namedKeys);
            key = key + String.join(".", namedKeys);
        }
        return key;
    }
}
