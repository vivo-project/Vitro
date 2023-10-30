/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.checks;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.Attribute;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueContainer;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueKey;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.ValueContainerFactory;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyLoader;
import org.apache.jena.query.QuerySolution;

public class CheckFactory {

    public static Check createCheck(QuerySolution qs, AttributeValueKey dataSetKey) {
        String typeId = qs.getLiteral(PolicyLoader.TYPE_ID).getString();
        String checkUri = qs.getResource(PolicyLoader.CHECK).getURI();
        Attribute type = Attribute.valueOf(typeId);
        String testId = qs.getLiteral("testId").getString();
        String value = getValue(qs);
        AttributeValueContainer container = ValueContainerFactory.create(value, qs, dataSetKey);
        Check check = null;
        switch (type) {
            case SUBJECT_ROLE_URI:
                check = new SubjectRoleCheck(checkUri, container);
                break;
            case OPERATION:
                check = new OperationCheck(checkUri, container);
                break;
            case ACCESS_OBJECT_URI:
                check = new AccessObjectUriCheck(checkUri, container);
                break;
            case ACCESS_OBJECT_TYPE:
                check = new AccessObjectTypeCheck(checkUri, container);
                break;
            case SUBJECT_TYPE:
                check = new SubjectTypeCheck(checkUri, container);
                break;
            case STATEMENT_PREDICATE_URI:
                check = new StatementPredicateUriCheck(checkUri, container);
                break;
            case STATEMENT_SUBJECT_URI:
                check = new StatementSubjectUriCheck(checkUri, container);
                break;
            case STATEMENT_OBJECT_URI:
                check = new StatementObjectUriCheck(checkUri, container);
                break;
            default:
                check = null;
        }
        check.setType(CheckType.valueOf(testId));
        return check;
    }

    private static String getValue(QuerySolution qs) {
        if (!qs.contains(PolicyLoader.LITERAL_VALUE) || !qs.get(PolicyLoader.LITERAL_VALUE).isLiteral()) {
            String value = qs.getResource(PolicyLoader.ATTR_VALUE).getURI();
            return value;
        } else {
            String value = qs.getLiteral(PolicyLoader.LITERAL_VALUE).toString();
            return value;
        }
    }


    public static void extendAttribute(Check check, QuerySolution qs) throws Exception {
        String testId = qs.getLiteral("testId").getString();
        if (CheckType.ONE_OF.toString().equals(testId) || CheckType.NOT_ONE_OF.toString().equals(testId)) {
            check.addValue(getValue(qs));
            return;
        }
        throw new Exception();
    }
}
