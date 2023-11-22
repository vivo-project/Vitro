package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import java.util.Optional;

import org.apache.jena.query.QuerySolution;

public class ValueSetFactory {

    public static AttributeValueSet create(String value, QuerySolution qs, AttributeValueKey dataSetKey) {
        Optional<String> type = getSetElementsType(qs);
        if (!type.isPresent() || dataSetKey == null) {
            return new MutableAttributeValueSet(value);
        } else {
            AttributeValueKey avcKey = getAttributeValueSetKey(dataSetKey, type.get());
            AttributeValueSet avc = AttributeValueSetRegistry.getInstance().get(avcKey);
            if (avc == null) {
                return createNew(value, qs, dataSetKey, avcKey);
            } else {
                return returnFromRegistry(value, avc);
            }
        }
    }

    private static AttributeValueSet returnFromRegistry(String value, AttributeValueSet avc) {
        avc.clear();
        avc.add(value);
        return avc;
    }

    private static AttributeValueSet createNew(String value, QuerySolution qs, AttributeValueKey dataSetKey,
            AttributeValueKey avcKey) {
        AttributeValueSet avc;
        avc = new MutableAttributeValueSet(value);
        Optional<String> setUri = getSetUri(qs);
        if (setUri.isPresent()) {
            avc.setValueSetUri(setUri.get());
        }
        Optional<String> type = getSetElementsType(qs);
        if (type.isPresent()) {
            avc.setType(type.get());
        }
        Optional<String> dataSetUri = getDataSetUri(qs);
        if (dataSetUri.isPresent()) {
            avc.setDataSetUri(dataSetUri.get());
        }
        avc.setKey(dataSetKey);
        register(avc, avcKey);
        return avc;
    }

    private static AttributeValueKey getAttributeValueSetKey(AttributeValueKey dataSetKey, String type) {
        AttributeValueKey avcKey = dataSetKey.clone();
        avcKey.setType(type);
        return avcKey;
    }

    private static void register(AttributeValueSet avc, AttributeValueKey key) {
        AttributeValueSetRegistry.getInstance().put(key, avc);
    }

    private static Optional<String> getSetUri(QuerySolution qs) {
        if (qs.contains("attributeValue") && qs.get("attributeValue").isResource()) {
            String uri = qs.getResource("attributeValue").getURI();
            return Optional.of(uri);
        }
        return Optional.empty();
    }

    private static Optional<String> getSetElementsType(QuerySolution qs) {
        if (qs.contains("setElementsType") && qs.get("setElementsType").isLiteral()) {
            String setElementsType = qs.getLiteral("setElementsType").getString();
            return Optional.of(setElementsType);
        }
        return Optional.empty();
    }

    private static Optional<String> getDataSetUri(QuerySolution qs) {
        if (qs.contains("dataSetUri") && qs.get("dataSetUri").isResource()) {
            String dataSetUri = qs.getResource("dataSetUri").getURI();
            return Optional.of(dataSetUri);
        }
        return Optional.empty();
    }
}
