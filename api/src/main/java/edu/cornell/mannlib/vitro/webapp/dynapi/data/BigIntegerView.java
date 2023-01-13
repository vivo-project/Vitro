package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.math.BigInteger;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;

public class BigIntegerView {

    public static boolean isBigInteger(Parameter param) {
        ParameterType paramType = param.getType();
        ImplementationType implType = paramType.getImplementationType();
        return implType.getClassName().equals(BigInteger.class);
    }

    public static BigInteger getBigInteger(Data data) {
        Object object = data.getObject();
        BigInteger value = (BigInteger) object;
        return value;
    }

    public static Data createBigInteger(Parameter param, BigInteger value) {
        Data data = new Data(param);
        data.setObject(value);
        return data;
    }

}
