package org.json;
/**
 * The {@code JSONString} interface allows a {@code toJSONString()}
 * method so that a class can change the behavior of 
 * {@code JSONObject.toString()}, {@code JSONArray.toString()},
 * and {@code JSONWriter.value(}Object{@code )}. The
 * {@code toJSONString} method will be used instead of the default behavior
 * of using the Object's {@code toString()} method and quoting the result.
 */
public interface JSONString {
	/**
	 * The {@code toJSONString} method allows a class to produce its own JSON
	 * serialization. 
	 * 
	 * @return A strictly syntactically correct JSON text.
	 */
	public String toJSONString();
}
