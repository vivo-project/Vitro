/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.validator.impl;

import edu.cornell.mannlib.vedit.validator.*;
import java.util.HashSet;
import java.util.Iterator;

public class EnumValuesValidator implements Validator {

    private HashSet legalValues = new HashSet();

    public ValidationObject validate(Object obj){
        ValidationObject vo = new ValidationObject();
        if (legalValues.contains((String)obj)){
            vo.setValid(true);
        } else {
            vo.setValid(false);
            if (legalValues.size()<7){
                String msgString = "Please enter one of ";
                Iterator valuesIt = legalValues.iterator();
                while (valuesIt.hasNext()) {
                    String legalValue = (String) valuesIt.next();
                    msgString += "'"+legalValue+"'";
                    if (valuesIt.hasNext())
                        msgString += ", ";
                    else
                        msgString += ".";
                }
                vo.setMessage(msgString);
            }
            else {
                vo.setMessage("Please enter a legal value.");
            }
        }
        vo.setValidatedObject(obj);
        return vo;
    }

    public EnumValuesValidator (String[] legalValues){
        for (int i=0; i<legalValues.length; i++)
            this.legalValues.add(legalValues[i]);
    }
}
