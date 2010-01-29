package edu.cornell.mannlib.vedit.validator;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

public class ValidationObject {

    private boolean valid = false;
    private String message;
    private Object validatedObject = null;

    public boolean getValid(){
        return valid;
    }

    public void setValid(boolean valid){
        this.valid = valid;
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public Object getValidatedObject(){
        return validatedObject;
    }

    public void setValidatedObject(Object validatedObject){
        this.validatedObject = validatedObject;
    }


}
