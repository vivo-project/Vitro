package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import org.apache.jena.query.QuerySolution;

public class ValueContainerFactory {

    public static AttributeValueContainer create(String value, QuerySolution qs,
            AttributeValueKey dataSetKey) {
        String type = getContainerType(qs);
        if (type == null || dataSetKey == null) {
            return new AttributeValueContainerImpl(value);
        } else {
            AttributeValueKey avcKey = getAttributeValueContainerKey(dataSetKey, type);
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

    private static AttributeValueContainer createNew(String value, QuerySolution qs,
            AttributeValueKey dataSetKey, AttributeValueKey avcKey) {
        AttributeValueContainer avc;
        avc = new AttributeValueContainerImpl(value);
        avc.setContainerUri(getContainerUri(qs));
        avc.setType(getContainerType(qs));
        avc.setDataSetUri(getDataSetUri(qs));
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

    private static String getContainerUri(QuerySolution qs) {
        if (qs.contains("attributeValue") && qs.get("attributeValue").isResource()) {
            String uri = qs.getResource("attributeValue").getURI();
            return uri;
        }
        return null;
    }

    private static String getContainerType(QuerySolution qs) {
        if (qs.contains("containerType") && qs.get("containerType").isLiteral()) {
            String containerType = qs.getLiteral("containerType").getString();
            return containerType;
        }
        return null;
    }

    private static String getDataSetUri(QuerySolution qs) {
        if (qs.contains("dataSetUri") && qs.get("dataSetUri").isResource()) {
            String dataSetUri = qs.getResource("dataSetUri").getURI();
            return dataSetUri;
        }
        return null;
    }
}
