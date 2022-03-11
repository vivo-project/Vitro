package edu.cornell.mannlib.vitro.webapp.dynapi.io.data;

public class DecimalData extends PrimitiveData<Double> {

    public DecimalData(Double value) {
        super(value);
    }

    @Override
    public String getType() {
        return "double";
    }

}
