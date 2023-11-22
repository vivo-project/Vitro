package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyTest;
import org.apache.commons.collections4.map.HashedMap;
import org.junit.Test;

public class AttributeValueKeyTest {

    @Test
    public void equalityTest() {
        AttributeValueKey key1 = new AttributeValueKey();
        key1.setObjectType(AccessObjectType.FAUX_DATA_PROPERTY);
        key1.setOperation(AccessOperation.DISPLAY);
        key1.setType(AccessObjectType.FAUX_DATA_PROPERTY.toString());
        key1.setRole(PolicyTest.ADMIN);
        AttributeValueKey key2 = new AttributeValueKey();
        key2.setObjectType(AccessObjectType.FAUX_DATA_PROPERTY);
        key2.setOperation(AccessOperation.DISPLAY);
        key2.setType("FAUX_DATA_PROPERTY");
        key2.setRole(PolicyTest.ADMIN);
        assertEquals(key1, key2);
        Map<AttributeValueKey, String> map = new HashedMap<>();
        map.put(key1, "1");
        map.put(key2, "2");
        assertEquals(1, map.size());
    }
}
