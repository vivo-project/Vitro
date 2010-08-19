/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.validator;

/**
 * Output from a {@link Validator}. Holds the value that was tested, whether it
 * was valid or not, and an optional message.
 */
public class ValidationObject {
	/**
	 * Create an instance that indicates successful validation.
	 */
	public static ValidationObject success(Object validatedObject) {
		ValidationObject vo = new ValidationObject();
		vo.setValid(true);
		vo.setMessage("");
		vo.setValidatedObject(validatedObject);
		return vo;
	}

	/**
	 * Create an instance that indicates failed validation.
	 */
	public static ValidationObject failure(Object validatedObject,
			String message) {
		ValidationObject vo = new ValidationObject();
		vo.setValid(false);
		vo.setMessage(message);
		vo.setValidatedObject(validatedObject);
		return vo;
	}

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
