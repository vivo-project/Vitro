/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.io.ByteArrayOutputStream;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.ByteArray;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;

public class BinaryView {

    public static boolean isByteArray(Data data) {
        return isByteArray(data.getParam());
    }

    public static boolean isByteArray(Parameter param) {
        ParameterType type = param.getType();
        if (type.hasInterface(ByteArray.class)) {
            return true;
        }
        return false;
    }

    public static void setByteArray(DataStore dataStore, Parameter reportFile, ByteArrayOutputStream baos) {
        final String name = reportFile.getName();
        Data data;
        if (dataStore.contains(name)) {
            data = dataStore.getData(name);
        } else {
            data = new Data(reportFile);
            dataStore.addData(name, data);
        }
        data.setObject(new ByteArray(baos.toByteArray()));
    }

    public static byte[] getByteArray(DataStore dataStore, Parameter templateFile) {
        Data data = dataStore.getData(templateFile.getName());
        ByteArray array = (ByteArray) data.getObject();
        return array.get();
    }
}
