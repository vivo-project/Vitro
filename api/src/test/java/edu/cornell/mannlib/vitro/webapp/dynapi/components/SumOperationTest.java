package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.operations.SumOperation;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.BigIntegerView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.IntegerView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.BigIntegerParam;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation.IntegerParam;

@RunWith(Parameterized.class)
public class SumOperationTest {


    @org.junit.runners.Parameterized.Parameter(0)
    public String inputType1;

    @org.junit.runners.Parameterized.Parameter(1)
    public String inputName1;
    
    @org.junit.runners.Parameterized.Parameter(2)
    public String inputValue1;
    
    @org.junit.runners.Parameterized.Parameter(3)
    public String inputType2;

    @org.junit.runners.Parameterized.Parameter(4)
    public String inputName2;
    
    @org.junit.runners.Parameterized.Parameter(5)
    public String inputValue2;
    
    @org.junit.runners.Parameterized.Parameter(6)
    public String outputType;

    @org.junit.runners.Parameterized.Parameter(7)
    public String outputName;
    
    @org.junit.runners.Parameterized.Parameter(8)
    public String outputValue;
    
    @org.junit.runners.Parameterized.Parameter(9)
    public boolean outputProvided;
    
    @Test
    public void test() throws InitializationException {
        SumOperation so = new SumOperation();
        Parameter inputParam1 = getParam(inputName1, inputType1);
        Parameter inputParam2 = getParam(inputName2, inputType2);
        Parameter outputParam = getParam(outputName, outputType);
        so.addInputParameter(inputParam1);
        so.addInputParameter(inputParam2);
        so.addOutputParameter(outputParam);
        
        DataStore store = new DataStore();
        createData(inputParam1, inputValue1, store);
        createData(inputParam2, inputValue2, store);
        if (outputProvided) {
            createData(outputParam, "0", store);
        }
        assertEquals(OperationResult.ok(), so.run(store));
        assertTrue(store.contains(outputName));
        Data output = store.getData(outputName);
        assertEquals(outputType, getActualType(output));
        assertEquals(outputValue, output.getSerializedValue());
    }

    private String getActualType(Data output) {
        Parameter param = output.getParam();
        if (BigIntegerView.isBigInteger(param)) {
            return "big integer";
        }
        if (IntegerView.isInteger(param)) {
            return "integer";
        }
        throw new RuntimeException("type not known");
    }

    private void createData(Parameter param, String value, DataStore store) {
        Data data = new Data(param);
        data.setRawString(value);
        data.initializeFromString();
        store.addData(param.getName(), data);
    }

    private Parameter getParam(String name, String type) {
        Parameter param;
        if (type.equals("integer")) {
            param = new IntegerParam(name);
            return param;
        } else if (type.equals("big integer")) {
            param = new BigIntegerParam(name);
            return param;
        }
        throw new RuntimeException("type not known");
    }
    
    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
            {"integer", "var1", "2" ,"integer", "var2", "2", "integer", "var3", "4", true },
            {"integer", "var1", "2" ,"integer", "var2", "2", "integer", "var3", "4", false },
            {"integer", "var1", "1" ,"integer", "var2", "-5", "integer", "var3", "-4", true },
            {"integer", "var1", "1" ,"integer", "var2", "-5", "integer", "var3", "-4", false },
            {"integer", "var1", "-1" ,"integer", "var2", "-1", "integer", "var3", "-2", true },
            {"integer", "var1", "-1" ,"integer", "var2", "-1", "integer", "var3", "-2", false },
            
            {"big integer", "var1", "2" ,"integer", "var2", "2", "integer", "var3", "4", true },
            {"big integer", "var1", "2" ,"integer", "var2", "2", "integer", "var3", "4", false },
            {"big integer", "var1", "1" ,"integer", "var2", "-5", "integer", "var3", "-4", true },
            {"big integer", "var1", "1" ,"integer", "var2", "-5", "integer", "var3", "-4", false },
            {"big integer", "var1", "-1" ,"integer", "var2", "-1", "integer", "var3", "-2", true },
            {"big integer", "var1", "-1" ,"integer", "var2", "-1", "integer", "var3", "-2", false },
            
            {"big integer", "var1", "2" ,"big integer", "var2", "2", "integer", "var3", "4", true },
            {"big integer", "var1", "2" ,"big integer", "var2", "2", "integer", "var3", "4", false },
            {"big integer", "var1", "1" ,"big integer", "var2", "-5", "integer", "var3", "-4", true },
            {"big integer", "var1", "1" ,"big integer", "var2", "-5", "integer", "var3", "-4", false },
            {"big integer", "var1", "-1" ,"big integer", "var2", "-1", "integer", "var3", "-2", true },
            {"big integer", "var1", "-1" ,"big integer", "var2", "-1", "integer", "var3", "-2", false },
            
            {"big integer", "var1", "2" ,"big integer", "var2", "2", "big integer", "var3", "4", true },
            {"big integer", "var1", "2" ,"big integer", "var2", "2", "big integer", "var3", "4", false },
            {"big integer", "var1", "1" ,"big integer", "var2", "-5", "big integer", "var3", "-4", true },
            {"big integer", "var1", "1" ,"big integer", "var2", "-5", "big integer", "var3", "-4", false },
            {"big integer", "var1", "-1" ,"big integer", "var2", "-1", "big integer", "var3", "-2", true },
            {"big integer", "var1", "-1" ,"big integer", "var2", "-1", "big integer", "var3", "-2", false },
            
            {"integer", "var1", "2" ,"integer", "var2", "2", "integer", "var2", "4", false },
            {"integer", "var1", "1" ,"integer", "var2", "-5", "integer", "var2", "-4", false },
            {"integer", "var1", "-1" ,"integer", "var2", "-1", "integer", "var2", "-2", false },
            
            {"big integer", "var1", "2" ,"integer", "var2", "2", "integer", "var2", "4", false },
            {"big integer", "var1", "1" ,"integer", "var2", "-5", "integer", "var2", "-4", false },
            {"big integer", "var1", "-1" ,"integer", "var2", "-1", "integer", "var2", "-2", false },
                       
            {"big integer", "var1", "2" ,"big integer", "var2", "2", "big integer", "var2", "4", false },
            {"big integer", "var1", "1" ,"big integer", "var2", "-5", "big integer", "var2", "-4", false },
            {"big integer", "var1", "-1" ,"big integer", "var2", "-1", "big integer", "var2", "-2", false },
        });
    }
}
