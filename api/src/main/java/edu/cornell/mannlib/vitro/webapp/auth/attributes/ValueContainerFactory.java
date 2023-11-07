package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import java.util.Optional;

import org.apache.jena.query.QuerySolution;

public class ValueContainerFactory {

    public static AttributeValueContainer create(String value, QuerySolution qs, AttributeValueKey dataSetKey) {
        Optional<String> type = getContainerType(qs);
        if (!type.isPresent() || dataSetKey == null) {
            return new MutableAttributeValueContainer(value);
        } else {
            AttributeValueKey avcKey = getAttributeValueContainerKey(dataSetKey, type.get());
            AttributeValueContainer avc = AttributeValuesRegistry.getInstance().get(avcKey);
            if (avc == null) {
                return createNew(value, qs, dataSetKey, avcKey);
            } else {
                return returnFromRegistry(value, avc);
            }
        }
    }

    private static AttributeValueContainer returnFromRegistry(String value, AttributeValueContainer avc) {
        avc.clear();
        avc.add(value);
        return avc;
    }

    private static AttributeValueContainer createNew(String value, QuerySolution qs, AttributeValueKey dataSetKey,
            AttributeValueKey avcKey) {
        AttributeValueContainer avc;
        avc = new MutableAttributeValueContainer(value);
        Optional<String> containerUri = getContainerUri(qs);
        if (containerUri.isPresent()) {
            avc.setContainerUri(containerUri.get());
        }
        Optional<String> type = getContainerType(qs);
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

    private static AttributeValueKey getAttributeValueContainerKey(AttributeValueKey dataSetKey, String type) {
        AttributeValueKey avcKey = dataSetKey.clone();
        avcKey.setContainerType(type);
        return avcKey;
    }

    private static void register(AttributeValueContainer avc, AttributeValueKey key) {
        AttributeValuesRegistry.getInstance().put(key, avc);
    }

    private static Optional<String> getContainerUri(QuerySolution qs) {
        if (qs.contains("attributeValue") && qs.get("attributeValue").isResource()) {
            String uri = qs.getResource("attributeValue").getURI();
            return Optional.of(uri);
        }
        return Optional.empty();
    }

    private static Optional<String> getContainerType(QuerySolution qs) {
        if (qs.contains("containerType") && qs.get("containerType").isLiteral()) {
            String containerType = qs.getLiteral("containerType").getString();
            return Optional.of(containerType);
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
