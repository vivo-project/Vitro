/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.AUTH_VOCABULARY_PREFIX;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueContainer;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueKey;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValues;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValuesRegistry;
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

    private static AttributeValues getRegistry() {
        return AttributeValuesRegistry.getInstance();
    }

    public static void revokeAccess(String entityUri, AccessObjectType aot, AccessOperation ao, String role) {
        AttributeValueKey key = new AttributeValueKey(ao, aot, role, aot.toString());
        AttributeValueContainer container = getRegistry().get(key);
        if (container != null) {
            if (container.contains(entityUri)) {
                container.remove(entityUri);
                String toRemove = getValueStatementString(entityUri, container.getContainerUri());
                getLoader().updateAccessControlModel(toRemove, false);
            }
        } else {
            reduceInactiveValueContainer(entityUri, aot, ao, role);
        }
    }

    private static PolicyLoader getLoader() {
        return PolicyLoader.getInstance();
    }

    private static void reduceInactiveValueContainer(String entityUri, AccessObjectType aot, AccessOperation ao,
            String role) {
        StringBuilder removals = new StringBuilder();
        getDataValueStatements(entityUri, aot, ao, Collections.singleton(role), removals);
        getLoader().updateAccessControlModel(removals.toString(), false);
    }

    public static void grantAccess(String entityUri, AccessObjectType aot, AccessOperation ao, String role) {
        AttributeValueKey key = new AttributeValueKey(ao, aot, role, aot.toString());
        AttributeValueContainer container = getRegistry().get(key);
        if (container != null) {
            if (!container.contains(entityUri)) {
                container.add(entityUri);
                String toAdd = getValueStatementString(entityUri, container.getContainerUri());
                getLoader().updateAccessControlModel(toAdd, true);
            }
        } else {
            extendInactiveValueContainer(entityUri, aot, ao, role);
            loadPolicy(aot, ao, role);
        }
    }

    private static void loadPolicy(AccessObjectType aot, AccessOperation ao, String role) {
        String dataSetUri =
                getLoader().getDataSetUriByKey(new String[] { role }, new String[] { ao.toString(), aot.toString() });
        if (dataSetUri != null) {
            DynamicPolicy policy = getLoader().loadPolicyFromTemplateDataSet(dataSetUri);
            if (policy != null) {
                PolicyStore.getInstance().add(policy);
            }
        }
    }

    private static void extendInactiveValueContainer(String entityUri, AccessObjectType aot, AccessOperation ao,
            String role) {
        StringBuilder additions = new StringBuilder();
        getDataValueStatements(entityUri, aot, ao, Collections.singleton(role), additions);
        getLoader().updateAccessControlModel(additions.toString(), true);
    }

    public static boolean isGranted(String entityUri, AccessObjectType aot, AccessOperation ao, String role) {
        if (StringUtils.isBlank(entityUri)) {
            return false;
        }
        AttributeValues registry = getRegistry();
        AttributeValueKey key = new AttributeValueKey(ao, aot, role, aot.toString());
        AttributeValueContainer container = registry.get(key);
        if (container == null) {
            return false;
        }
        return container.contains(entityUri);
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
            Set<String> selectedRoles, StringBuilder sb) {
        if (StringUtils.isBlank(entityUri)) {
            return;
        }
        for (String role : selectedRoles) {
            String valueContainerUri = getValueContainerUri(aot, ao, role);
            if (valueContainerUri == null) {
                log.debug(String.format("Policy value container wasn't found by key:\n%s\n%s\n%s", ao, aot, role));
                continue;
            }
            sb.append(getValueStatementString(entityUri, valueContainerUri));
        }
    }

    private static String getValueStatementString(String entityUri, String valueContainerUri) {
        return "<" + valueContainerUri + "> <" + AUTH_VOCABULARY_PREFIX + "dataValue> <" + entityUri + "> .\n";
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

    private static String getValueContainerUri(AccessObjectType aot, AccessOperation ao, String role) {
        String key = aot.toString() + "." + ao.toString() + "." + role;
        if (policyKeyToDataValueMap.containsKey(key)) {
            return policyKeyToDataValueMap.get(key);
        }
        String uri = getLoader().getEntityValueContainerUri(ao, aot, role);
        policyKeyToDataValueMap.put(key, uri);
        return uri;
    }
}
