/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit.listing;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;

public class ListingControllerWebUtils {

	private static final Log log = LogFactory.getLog(ListingControllerWebUtils.class.getName());

	public static synchronized String formatIndividualLink(Individual ind) {
		try {
			System.out.println(ind.getURI());
			String nameStr = (ind.getName() != null) ? ind.getName() : ind.getURI();
    		return "<a href=\"entityEdit?uri="+URLEncoder.encode(ind.getURI(),"UTF-8")+"\">"+nameStr+"</a>";
		} catch (NullPointerException npe) {
			return "?";
    	} catch (UnsupportedEncodingException e) {
    		return ind.getName();
    	}
	}

	public static synchronized String formatVClassLinks(List<VClass> vList) {
	    StringBuilder linksStr= new StringBuilder();
	    if (vList!=null) {
	        int count=0;
	        for (Object obj : vList) {
	        	try {
	                if (count>0) linksStr.append(" | ");
		            VClass vclass = (VClass) obj;
		            try {
		                linksStr.append("<a href=\"vclassEdit?uri=").append(URLEncoder.encode(vclass.getURI(), "UTF-8")).append("\">").append(vclass.getName()).append("</a>");
		            } catch (UnsupportedEncodingException e) {
		                linksStr.append(vclass.getName());
		            }
	                ++count;
	        	} catch (Exception e) {
	        		if (obj == null) {
	        			log.error(ListingControllerWebUtils.class.getName()+" could not format null VClass");
	        		}
	        	}
	        }
	    }
	    return linksStr.toString();
	}

}
