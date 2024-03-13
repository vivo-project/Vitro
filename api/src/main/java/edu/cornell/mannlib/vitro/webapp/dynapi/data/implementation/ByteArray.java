/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation;

import java.util.Base64;

public class ByteArray {

    private byte[] array;

    public ByteArray(byte[] array) {
        this.array = array;
    }

    public byte[] get() {
        return array;
    }

    public String serialize() {
        byte[] bytes = Base64.getEncoder().encode(array);
        String result = new String(bytes);
        return result;
    }

    public static ByteArray deserialize(String input) {
        byte[] array = Base64.getDecoder().decode(input);
        ByteArray byteArray = new ByteArray(array);
        return byteArray;
    }
}
