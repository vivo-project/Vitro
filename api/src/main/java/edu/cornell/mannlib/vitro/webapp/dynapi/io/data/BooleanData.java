package edu.cornell.mannlib.vitro.webapp.dynapi.io.data;

public class BooleanData extends PrimitiveData<Boolean> {

    public BooleanData(Boolean value) {
        super(value);
    }

    @Override
    public String getType() {
        return "boolean";
    }
}
