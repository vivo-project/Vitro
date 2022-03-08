package edu.cornell.mannlib.vitro.webapp.dynapi.io.data;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ArrayData extends ContainerData<List<Data>> {

	public ArrayData(List<Data> container) {
        super(container);
    }

    @Override
    public Data getElement(String fieldName) {
        if (!fieldName.contains(".")){
            try{
                int index = Integer.parseInt(fieldName);
                return (container.size() > index)?container.get(index):null;
            }
            catch (NumberFormatException ex){
                return null;
            }
        } else {
            String fieldNameFirstPart = fieldName.substring(0, fieldName.indexOf("."));
            String fieldNameSecondPart = fieldName.substring(fieldName.indexOf(".")+1);
            Data data = null;
            if (NumberUtils.isDigits(fieldNameFirstPart)){
                int index = Integer.parseInt(fieldNameFirstPart);
                data = (container.size() > index)?container.get(index):null;
            }
            if (data instanceof ContainerData)
                return ((ContainerData)data).getElement(fieldNameSecondPart);
            else
                return null;
        }
    }

    @Override
    public boolean setElement(String fieldName, Data newData) {
        if (!fieldName.contains(".")){
            if ((NumberUtils.isDigits(fieldName)) && (container.size() > Integer.parseInt(fieldName)))
                container.add(Integer.parseInt(fieldName), newData);
            else
                container.add(newData);
            return true;
        } else {
            String fieldNameFirstPart = fieldName.substring(0, fieldName.indexOf("."));
            Data internalData = null;
            if (NumberUtils.isDigits(fieldNameFirstPart)) {
                int index = Integer.parseInt(fieldNameFirstPart);
                internalData = (container.size() > index)?container.get(index):null;
                String fieldNameOtherPart = fieldName.substring(fieldName.indexOf(".") + 1);
                String fieldNameSecondPart = (fieldNameOtherPart.contains(".")) ? fieldNameOtherPart.substring(0, fieldNameOtherPart.indexOf(".")) : fieldNameOtherPart;
                if (NumberUtils.isDigits(fieldNameSecondPart)) {
                    if (!(internalData instanceof ArrayData)) {
                        internalData = new ArrayData(new ArrayList<>());
                        container.add(index, internalData);
                    }
                } else {
                    if (!(internalData instanceof ObjectData)) {
                        internalData = new ObjectData(new HashMap<>());
                        container.add(index, internalData);
                    }
                }
                return ((ContainerData) internalData).setElement(fieldNameOtherPart, newData);
            }
            else
                return false;

        }
    }

    @Override
    public List<String> getAsString() {
        List<String> retVal = new ArrayList<String>();
        for (Data item:container) {
            retVal.addAll(item.getAsString());
        }
        return retVal;
    }
}
