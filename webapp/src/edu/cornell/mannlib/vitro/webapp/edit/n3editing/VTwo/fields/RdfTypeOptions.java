/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;

public class RdfTypeOptions implements FieldOptions {

    String[] typeURIs;        
    
    public RdfTypeOptions(String ... superClassURIs) 
    throws Exception {
        super();
        if( superClassURIs == null )
            throw new Exception("superClassURIs must be supplied "+
            		"to constructor.");
        
        this.typeURIs = superClassURIs;        
    }


    @Override
    public Map<String, String> getOptions(
            EditConfigurationVTwo editConfig, 
            String fieldName, 
            WebappDaoFactory wdf) {        
        Map<String,String> uriToLabel = new HashMap<String,String>();
        
        for(int i=0;i<typeURIs.length; i++){
            String uri = typeURIs[i];
            VClass vc = wdf.getVClassDao().getVClassByURI( uri );
            if( vc == null ){
                uriToLabel.put(uri,uri);
                continue;
            }
            
            uriToLabel.put(uri,vc.getPickListName());
            List<String> subclassUris = wdf.getVClassDao().getAllSubClassURIs( uri );
            if( subclassUris == null )
                continue;
            
            for( String subUri : subclassUris ){
                VClass subVc = wdf.getVClassDao().getVClassByURI( subUri );
                if( vc != null ){
                    uriToLabel.put(subUri,subVc.getPickListName());
                }else{
                    uriToLabel.put(subUri,subUri);
                }
            }            
        }
        
        return uriToLabel;
    }
    
    public Comparator<String[]> getCustomComparator() {
    	return null;
    }

}
