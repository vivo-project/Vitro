/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web;

import java.io.UnsupportedEncodingException;

/**
 * A convenience class that uses UTF-8 encoding to encode URLs
 * and rethrows the unlikely UnsupportedEncodingException as an 
 * unchecked exception.
 * @author bjl23
 *
 */
public class URLEncoder {

	public static final String encode(String s) {
		try {
			return java.net.URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
}
