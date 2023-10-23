package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.DATA_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.FAUX_DATA_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.FAUX_OBJECT_PROPERTY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType.OBJECT_PROPERTY;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;

import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.EDIT;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.ADD;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.DROP;
import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.PUBLISH;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class AccessAllowedPropertiesPolicyTemplateTest extends PolicyTest {
    private static final String TEMPLATE_PROPERTY_PREFIX =
            "https://vivoweb.org/ontology/vitro-application/auth/individual/template/access-allowed-property/";
    
    public static final String POLICY_TEMPLATE_PATH = USER_ACCOUNTS_HOME_FIRSTTIME
            + "template_access_allowed_property.n3";

    @org.junit.runners.Parameterized.Parameter(0)
    public String dataSetUri;

    @org.junit.runners.Parameterized.Parameter(1)
    public AccessOperation ao;

    @org.junit.runners.Parameterized.Parameter(2)
    public AccessObjectType type;

    @org.junit.runners.Parameterized.Parameter(3)
    public String roleUri;

    @org.junit.runners.Parameterized.Parameter(4)
    public int rulesCount;

    @org.junit.runners.Parameterized.Parameter(5)
    public Set<Integer> attrCount;

    @Test
    public void testPolicy() {
        load(POLICY_TEMPLATE_PATH);
        EntityPolicyController.updateEntityDataSet("test:entity", type, ao, Arrays.asList(roleUri), ROLE_LIST);
        DynamicPolicy policy = null;
        policy = loader.loadPolicyFromTemplateDataSet(TEMPLATE_PROPERTY_PREFIX + dataSetUri);
        countRulesAndAttributes(policy, rulesCount, attrCount);
        Set<String> values = loader.getDataSetValues(ao, type, roleUri);
        assertFalse(values.isEmpty());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {

                { "AdminDisplayObjectPropertyDataSet", DISPLAY, OBJECT_PROPERTY, ADMIN, 2, num(4) },
                { "CuratorDisplayObjectPropertyDataSet", DISPLAY, OBJECT_PROPERTY, CURATOR, 2, num(4) },
                { "EditorDisplayObjectPropertyDataSet", DISPLAY, OBJECT_PROPERTY, EDITOR, 2, num(4) },
                { "PublicDisplayObjectPropertyDataSet", DISPLAY, OBJECT_PROPERTY, PUBLIC, 2, num(4) },

                { "AdminDisplayDataPropertyDataSet", DISPLAY, DATA_PROPERTY, ADMIN, 2, num(4) },
                { "CuratorDisplayDataPropertyDataSet", DISPLAY, DATA_PROPERTY, CURATOR, 2, num(4) },
                { "EditorDisplayDataPropertyDataSet", DISPLAY, DATA_PROPERTY, EDITOR, 2, num(4) },
                { "PublicDisplayDataPropertyDataSet", DISPLAY, DATA_PROPERTY, PUBLIC, 2, num(4) },

                { "AdminDisplayFauxObjectPropertyDataSet", DISPLAY, FAUX_OBJECT_PROPERTY, ADMIN, 2, num(4) },
                { "CuratorDisplayFauxObjectPropertyDataSet", DISPLAY, FAUX_OBJECT_PROPERTY, CURATOR, 2, num(4) },
                { "EditorDisplayFauxObjectPropertyDataSet", DISPLAY, FAUX_OBJECT_PROPERTY, EDITOR, 2, num(4) },
                { "PublicDisplayFauxObjectPropertyDataSet", DISPLAY, FAUX_OBJECT_PROPERTY, PUBLIC, 2, num(4) },

                { "AdminDisplayFauxDataPropertyDataSet", DISPLAY, FAUX_DATA_PROPERTY, ADMIN, 2, num(4) },
                { "CuratorDisplayFauxDataPropertyDataSet", DISPLAY, FAUX_DATA_PROPERTY, CURATOR, 2, num(4) },
                { "EditorDisplayFauxDataPropertyDataSet", DISPLAY, FAUX_DATA_PROPERTY, EDITOR, 2, num(4) },
                { "PublicDisplayFauxDataPropertyDataSet", DISPLAY, FAUX_DATA_PROPERTY, PUBLIC, 2, num(4) },

                { "AdminPublishObjectPropertyDataSet", PUBLISH, OBJECT_PROPERTY, ADMIN, 2, num(4) },
                { "CuratorPublishObjectPropertyDataSet", PUBLISH, OBJECT_PROPERTY, CURATOR, 2, num(4) },
                { "EditorPublishObjectPropertyDataSet", PUBLISH, OBJECT_PROPERTY, EDITOR, 2, num(4) },

                { "AdminPublishDataPropertyDataSet", PUBLISH, DATA_PROPERTY, ADMIN, 2, num(4) },
                { "CuratorPublishDataPropertyDataSet", PUBLISH, DATA_PROPERTY, CURATOR, 2, num(4) },
                { "EditorPublishDataPropertyDataSet", PUBLISH, DATA_PROPERTY, EDITOR, 2, num(4) },

                { "AdminPublishFauxObjectPropertyDataSet", PUBLISH, FAUX_OBJECT_PROPERTY, ADMIN, 2, num(4) },
                { "CuratorPublishFauxObjectPropertyDataSet", PUBLISH, FAUX_OBJECT_PROPERTY, CURATOR, 2, num(4) },
                { "EditorPublishFauxObjectPropertyDataSet", PUBLISH, FAUX_OBJECT_PROPERTY, EDITOR, 2, num(4) },

                { "AdminPublishFauxDataPropertyDataSet", PUBLISH, FAUX_DATA_PROPERTY, ADMIN, 2, num(4) },
                { "CuratorPublishFauxDataPropertyDataSet", PUBLISH, FAUX_DATA_PROPERTY, CURATOR, 2, num(4) },
                { "EditorPublishFauxDataPropertyDataSet", PUBLISH, FAUX_DATA_PROPERTY, EDITOR, 2, num(4) },

                { "AdminEditObjectPropertyDataSet", EDIT, OBJECT_PROPERTY, ADMIN, 2, num(4) },
                { "CuratorEditObjectPropertyDataSet", EDIT, OBJECT_PROPERTY, CURATOR, 2, num(4) },
                { "EditorEditObjectPropertyDataSet", EDIT, OBJECT_PROPERTY, EDITOR, 2, num(4) },
                { "PublicEditObjectPropertyDataSet", EDIT, OBJECT_PROPERTY, PUBLIC, 2, num(4) },

                { "AdminAddObjectPropertyDataSet", ADD, OBJECT_PROPERTY, ADMIN, 2, num(4) },
                { "CuratorAddObjectPropertyDataSet", ADD, OBJECT_PROPERTY, CURATOR, 2, num(4) },
                { "EditorAddObjectPropertyDataSet", ADD, OBJECT_PROPERTY, EDITOR, 2, num(4) },
                { "PublicAddObjectPropertyDataSet", ADD, OBJECT_PROPERTY, PUBLIC, 2, num(4) },

                { "AdminDropObjectPropertyDataSet", DROP, OBJECT_PROPERTY, ADMIN, 2, num(4) },
                { "CuratorDropObjectPropertyDataSet", DROP, OBJECT_PROPERTY, CURATOR, 2, num(4) },
                { "EditorDropObjectPropertyDataSet", DROP, OBJECT_PROPERTY, EDITOR, 2, num(4) },
                { "PublicDropObjectPropertyDataSet", DROP, OBJECT_PROPERTY, PUBLIC, 2, num(4) },

                { "AdminEditDataPropertyDataSet", EDIT, DATA_PROPERTY, ADMIN, 2, num(4) },
                { "CuratorEditDataPropertyDataSet", EDIT, DATA_PROPERTY, CURATOR, 2, num(4) },
                { "EditorEditDataPropertyDataSet", EDIT, DATA_PROPERTY, EDITOR, 2, num(4) },
                { "PublicEditDataPropertyDataSet", EDIT, DATA_PROPERTY, PUBLIC, 2, num(4) },

                { "AdminAddDataPropertyDataSet", ADD, DATA_PROPERTY, ADMIN, 2, num(4) },
                { "CuratorAddDataPropertyDataSet", ADD, DATA_PROPERTY, CURATOR, 2, num(4) },
                { "EditorAddDataPropertyDataSet", ADD, DATA_PROPERTY, EDITOR, 2, num(4) },
                { "PublicAddDataPropertyDataSet", ADD, DATA_PROPERTY, PUBLIC, 2, num(4) },

                { "AdminDropDataPropertyDataSet", DROP, DATA_PROPERTY, ADMIN, 2, num(4) },
                { "CuratorDropDataPropertyDataSet", DROP, DATA_PROPERTY, CURATOR, 2, num(4) },
                { "EditorDropDataPropertyDataSet", DROP, DATA_PROPERTY, EDITOR, 2, num(4) },
                { "PublicDropDataPropertyDataSet", DROP, DATA_PROPERTY, PUBLIC, 2, num(4) },

                { "AdminEditFauxObjectPropertyDataSet", EDIT, FAUX_OBJECT_PROPERTY, ADMIN, 2, num(4) },
                { "CuratorEditFauxObjectPropertyDataSet", EDIT, FAUX_OBJECT_PROPERTY, CURATOR, 2, num(4) },
                { "EditorEditFauxObjectPropertyDataSet", EDIT, FAUX_OBJECT_PROPERTY, EDITOR, 2, num(4) },
                { "PublicEditFauxObjectPropertyDataSet", EDIT, FAUX_OBJECT_PROPERTY, PUBLIC, 2, num(4) },

                { "AdminAddFauxObjectPropertyDataSet", ADD, FAUX_OBJECT_PROPERTY, ADMIN, 2, num(4) },
                { "CuratorAddFauxObjectPropertyDataSet", ADD, FAUX_OBJECT_PROPERTY, CURATOR, 2, num(4) },
                { "EditorAddFauxObjectPropertyDataSet", ADD, FAUX_OBJECT_PROPERTY, EDITOR, 2, num(4) },
                { "PublicAddFauxObjectPropertyDataSet", ADD, FAUX_OBJECT_PROPERTY, PUBLIC, 2, num(4) },

                { "AdminDropFauxObjectPropertyDataSet", DROP, FAUX_OBJECT_PROPERTY, ADMIN, 2, num(4) },
                { "CuratorDropFauxObjectPropertyDataSet", DROP, FAUX_OBJECT_PROPERTY, CURATOR, 2, num(4) },
                { "EditorDropFauxObjectPropertyDataSet", DROP, FAUX_OBJECT_PROPERTY, EDITOR, 2, num(4) },
                { "PublicDropFauxObjectPropertyDataSet", DROP, FAUX_OBJECT_PROPERTY, PUBLIC, 2, num(4) },

                { "AdminEditFauxDataPropertyDataSet", EDIT, FAUX_DATA_PROPERTY, ADMIN, 2, num(4) },
                { "CuratorEditFauxDataPropertyDataSet", EDIT, FAUX_DATA_PROPERTY, CURATOR, 2, num(4) },
                { "EditorEditFauxDataPropertyDataSet", EDIT, FAUX_DATA_PROPERTY, EDITOR, 2, num(4) },
                { "PublicEditFauxDataPropertyDataSet", EDIT, FAUX_DATA_PROPERTY, PUBLIC, 2, num(4) },

                { "AdminAddFauxDataPropertyDataSet", ADD, FAUX_DATA_PROPERTY, ADMIN, 2, num(4) },
                { "CuratorAddFauxDataPropertyDataSet", ADD, FAUX_DATA_PROPERTY, CURATOR, 2, num(4) },
                { "EditorAddFauxDataPropertyDataSet", ADD, FAUX_DATA_PROPERTY, EDITOR, 2, num(4) },
                { "PublicAddFauxDataPropertyDataSet", ADD, FAUX_DATA_PROPERTY, PUBLIC, 2, num(4) },

                { "AdminDropFauxDataPropertyDataSet", DROP, FAUX_DATA_PROPERTY, ADMIN, 2, num(4) },
                { "CuratorDropFauxDataPropertyDataSet", DROP, FAUX_DATA_PROPERTY, CURATOR, 2, num(4) },
                { "EditorDropFauxDataPropertyDataSet", DROP, FAUX_DATA_PROPERTY, EDITOR, 2, num(4) },
                { "PublicDropFauxDataPropertyDataSet", DROP, FAUX_DATA_PROPERTY, PUBLIC, 2, num(4) }, });
    }

    private static Set<Integer> num(int i) {
        return Collections.singleton(i);
    }

}
