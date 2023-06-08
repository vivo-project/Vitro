package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.OperationGroup;
import edu.cornell.mannlib.vitro.webapp.beans.Property;

public class EntityPolicyController {
    
    private static final Log log = LogFactory.getLog(EntityPolicyController.class);
    /**
     * @param entityUri - entity uniform resource identifier
     * @param aot - Access object type
     * @param og - operation group
     * @param selectedRoles - list of roles to assign
     * @param allRoles - list of all available roles
     */
    public static void updateEntityPolicy(String entityUri, AccessObjectType aot, OperationGroup og,
            List<String> selectedRoles, List<String> allRoles) {
        if (StringUtils.isBlank(entityUri)) {
            return;
        }
        Set<String> selectedSet = new HashSet<>(selectedRoles);
        for (String role : allRoles) {
            boolean isInDataSet = isUriInTestDataset(entityUri, og, aot, role);
            boolean isSelected = selectedSet.contains(role);
            final PolicyLoader loader = PolicyLoader.getInstance();
            final String policyUri = loader.getPolicyUriByKey(og, aot, role);
            if (policyUri == null) {
                log.debug(String.format("Policy wasn't found by key:\n%s\n%s\n%s", og.toString(), aot.toString(), role));
                continue;
            }
            if (isSelected && !isInDataSet) {
                loader.addEntityToPolicyDataSet(entityUri, aot, og, role);
                DynamicPolicy policy = loader.loadPolicy(policyUri);
                PolicyStore.getInstance().add(policy);
            } else if (!isSelected && isInDataSet) {
                loader.removeEntityFromPolicyDataSet(entityUri, aot, og, role);
                DynamicPolicy policy = loader.loadPolicy(policyUri);
                PolicyStore.getInstance().add(policy);
            }
        }
    }
    
    public static List<String> getGrantedRoles(String entityUri, OperationGroup og, AccessObjectType aot, List<String> allRoles) {
        if (StringUtils.isBlank(entityUri)) {
            return Collections.emptyList();
        }
        List<String> grantedRoles = new LinkedList<>();
        for (String role : allRoles) {
            if (isUriInTestDataset(entityUri, og, aot, role)) {
                grantedRoles.add(role);
            }
        }
        return grantedRoles;
    }
    
    private static boolean isUriInTestDataset(String entityUri, OperationGroup og, AccessObjectType aot, String role) {
        Set<String> values = PolicyLoader.getInstance().getPolicyDataSetValues(og, aot, role);
        return values.contains(entityUri);
    }

    public static void deletedEntityEvent(Property oldObj) {
        log.debug("Don't delete access rule if property has been deleted " + oldObj );
    }

    public static void updatedEntityEvent(Object oldObj, Object newObj) {
        if (oldObj instanceof Property || newObj instanceof Property) {
            log.debug("update entity event old " + oldObj + " new object " + newObj );    
        }
    }

    public static void insertedEntityEvent(Property newObj) {
        log.debug("Nothing to do " + newObj );
    }
}
