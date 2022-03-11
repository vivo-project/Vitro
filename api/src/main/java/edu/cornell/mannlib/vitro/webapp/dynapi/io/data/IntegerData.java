package edu.cornell.mannlib.vitro.webapp.dynapi.io.data;

public class IntegerData extends PrimitiveData<Integer> {

    public IntegerData(Integer value) {
        super(value);
    }

    @Override
    public String getType() {
        return "integer";
    }

}
