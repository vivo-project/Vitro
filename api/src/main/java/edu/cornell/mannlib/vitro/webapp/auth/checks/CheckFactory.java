/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.checks;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.Attribute;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueSet;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueKey;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.ValueSetFactory;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyLoader;
import org.apache.jena.query.QuerySolution;

public class CheckFactory {

    public static Check createCheck(QuerySolution qs, AttributeValueKey dataSetKey) {
        String typeId = qs.getLiteral(PolicyLoader.TYPE_ID).getString();
        String checkUri = qs.getResource(PolicyLoader.CHECK).getURI();
        Attribute type = Attribute.valueOf(typeId);
        String testId = qs.getLiteral("testId").getString();
        String value = getValue(qs);
        AttributeValueSet set = ValueSetFactory.create(value, qs, dataSetKey);
        Check check = null;
        switch (type) {
            case SUBJECT_ROLE_URI:
                check = new SubjectRoleCheck(checkUri, set);
                break;
            case OPERATION:
                check = new OperationCheck(checkUri, set);
                break;
            case ACCESS_OBJECT_URI:
                check = new AccessObjectUriCheck(checkUri, set);
                break;
            case ACCESS_OBJECT_TYPE:
                check = new AccessObjectTypeCheck(checkUri, set);
                break;
            case SUBJECT_TYPE:
                check = new SubjectTypeCheck(checkUri, set);
                break;
            case STATEMENT_PREDICATE_URI:
                check = new StatementPredicateUriCheck(checkUri, set);
                break;
            case STATEMENT_SUBJECT_URI:
                check = new StatementSubjectUriCheck(checkUri, set);
                break;
            case STATEMENT_OBJECT_URI:
                check = new StatementObjectUriCheck(checkUri, set);
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
        throw new IllegalArgumentException(
                String.format("Operator '%s' can't be used in combination with multiple attribute values.", testId));
    }
}
