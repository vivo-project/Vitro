/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyLoader;
import org.apache.jena.query.QuerySolution;

public class CheckFactory {

    public static Check createCheck(QuerySolution qs) {
        String typeId = qs.getLiteral(PolicyLoader.TYPE_ID).getString();
        String checkUri = qs.getResource(PolicyLoader.CHECK).getURI();
        Attribute type = Attribute.valueOf(typeId);
        String testId = qs.getLiteral("testId").getString();
        String value = getValue(qs);

        Check at = null;
        switch (type) {
            case SUBJECT_ROLE_URI:
                at = new SubjectRoleCheck(checkUri, value);
                break;
            case OPERATION:
                at = new OperationCheck(checkUri, value);
                break;
            case ACCESS_OBJECT_URI:
                at = new AccessObjectUriCheck(checkUri, value);
                break;
            case ACCESS_OBJECT_TYPE:
                at = new AccessObjectTypeCheck(checkUri, value);
                break;
            case SUBJECT_TYPE:
                at = new SubjectTypeCheck(checkUri, value);
                break;
            case STATEMENT_PREDICATE_URI:
                at = new StatementPredicateUriCheck(checkUri, value);
                break;
            case STATEMENT_SUBJECT_URI:
                at = new StatementSubjectUriCheck(checkUri, value);
                break;
            case STATEMENT_OBJECT_URI:
                at = new StatementObjectUriCheck(checkUri, value);
                break;
            default:
                at = null;
        }
        at.setType(CheckType.valueOf(testId));
        return at;
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
