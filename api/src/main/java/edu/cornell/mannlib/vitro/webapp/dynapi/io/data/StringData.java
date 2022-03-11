package edu.cornell.mannlib.vitro.webapp.dynapi.io.data;

public class StringData extends PrimitiveData<String> {

    public StringData(String value) {
        super(value);
    }

    @Override
    public String getType() {
        return "string";
    }

}
