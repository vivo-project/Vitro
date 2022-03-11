package edu.cornell.mannlib.vitro.webapp.dynapi.io.data;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Validators;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ArrayParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ParameterType;
import org.apache.commons.lang3.math.NumberUtils;

public class ArrayData extends ContainerData<List<Data>> {

    public ArrayData() {
        super(new ArrayList<>());
    }

    public ArrayData(List<Data> container) {
        super(container);
    }

    @Override
    public Data getElement(String fieldName) {
        if (!fieldName.contains(".")) {
            try {
                int index = Integer.parseInt(fieldName);
                return (container.size() > index) ? container.get(index) : null;
            } catch (NumberFormatException ex) {
                return null;
            }
        } else {
            String fieldNameFirstPart = fieldName.substring(0, fieldName.indexOf("."));
            String fieldNameSecondPart = fieldName.substring(fieldName.indexOf(".") + 1);
            Data data = null;
            if (NumberUtils.isDigits(fieldNameFirstPart)) {
                int index = Integer.parseInt(fieldNameFirstPart);
                data = (container.size() > index) ? container.get(index) : null;
            }
            return data.getElement(fieldNameSecondPart);
        }
    }

    @Override
    public boolean setElement(String fieldName, Data newData) {
        if (!fieldName.contains(".")) {
            if ((NumberUtils.isDigits(fieldName)) && (container.size() > Integer.parseInt(fieldName))) {
                container.add(Integer.parseInt(fieldName), newData);
            } else {
                container.add(newData);
            }
            return true;
        } else {
            String fieldNameFirstPart = fieldName.substring(0, fieldName.indexOf("."));
            Data internalData = null;
            if (NumberUtils.isDigits(fieldNameFirstPart)) {
                int index = Integer.parseInt(fieldNameFirstPart);
                internalData = (container.size() > index) ? container.get(index) : null;
                String fieldNameOtherPart = fieldName.substring(fieldName.indexOf(".") + 1);
                String fieldNameSecondPart = (fieldNameOtherPart.contains("."))
                        ? fieldNameOtherPart.substring(0, fieldNameOtherPart.indexOf("."))
                        : fieldNameOtherPart;
                if (NumberUtils.isDigits(fieldNameSecondPart)) {
                    if (!(internalData instanceof ArrayData)) {
                        internalData = new ArrayData();
                        container.add(index, internalData);
                    }
                } else {
                    if (!(internalData instanceof ObjectData)) {
                        internalData = new ObjectData();
                        container.add(index, internalData);
                    }
                }

                return internalData.setElement(fieldNameOtherPart, newData);
            } else {
                return false;
            }
        }
    }

    @Override
    public String getType() {
        return "array";
    }

    @Override
    public boolean checkType(ParameterType parameterType) {
        boolean retVal = (parameterType instanceof ArrayParameterType);
        if (retVal) {
            ParameterType internalParameterType = ((ArrayParameterType) parameterType).getElementsType();
            for (Data element : container)
                if ((element == null) || (!(element.checkType(internalParameterType)))) {
                    retVal = false;
                    break;
                }
        }
        return retVal;
    }

    @Override
    public boolean isAllValid(String name, Validators validators, ParameterType type) {
        boolean retVal = true;
        ParameterType internalParameterType = ((ArrayParameterType) type).getElementsType();
        for (int i = 0; i < container.size(); i++) {
            Data element = container.get(i);
            if (!(element.isAllValid(name + "[" + i + "]", validators, internalParameterType))) {
                retVal = false;
                break;
            }
        }
        return retVal;
    }

}
