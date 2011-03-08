/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

import javax.servlet.http.HttpServletRequest;

import org.apache.xerces.util.XMLChar;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.RoleIdentifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory;
import edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep;

public class EditN3Utils {

    public static String getEditorUri(HttpServletRequest request){
        String editorUri = "Unknown N3 Editor";
        boolean selfEditing = VitroRequestPrep.isSelfEditing(request);
        IdentifierBundle ids =
        	RequestIdentifiers.getIdBundleForRequest(request);           
        
        if( selfEditing )
            editorUri = SelfEditingIdentifierFactory.getSelfEditingUri(ids);
        else
            editorUri = RoleIdentifier.getUri(ids);
        
        return editorUri;        
    }
    
    /**
     * Strips from a string any characters that are not valid in XML 1.0
     * @param in
     * @return
     */
    public static String stripInvalidXMLChars(String in) {
        if (in == null) {
            return null;
        }
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (!XMLChar.isInvalid(c)) {
                out.append(c);
            }
        }
        return out.toString();
    }
    
    
//    public static void addModTimes( Model additions, Model retractions, Model contextModel ){    	    	
//    	Property modtime = ResourceFactory.createProperty(VitroVocabulary.MODTIME);
//    	Date time = Calendar.getInstance().getTime();
//    	
//    	//get all resources in additions and retractions that are not types
//    	additions.listStatements()
//    	
//    	Lock lock = contextModel.getLock();
//	    try {
//        
//            String existingValue = null;
//            Statement stmt = res.getProperty(modtime);
//            if (stmt != null) {
//                RDFNode object = stmt.getObject();
//                if (object != null && object.isLiteral()){
//                    existingValue = ((Literal)object).getString();
//                }
//            }
//            String formattedDateStr = xsdDateTimeFormat.format(time);
//            if ( (existingValue!=null && value == null) || (existingValue!=null && value != null && !(existingValue.equals(formattedDateStr)) ) ) {
//                model.removeAll(res, modtime, null);
//            }
//            if ( (existingValue==null && value != null) || (existingValue!=null && value != null && !(existingValue.equals(formattedDateStr)) ) ) {
//                model.add(res, modtime, formattedDateStr, XSDDatatype.XSDdateTime);
//            }
//	        
//	    } catch (Exception e) {
//	        log.error("Error in updatePropertyDateTimeValue");
//	        log.error(e, e);
//	    }
//    }
//    
//    private static List<URIResource>
}
