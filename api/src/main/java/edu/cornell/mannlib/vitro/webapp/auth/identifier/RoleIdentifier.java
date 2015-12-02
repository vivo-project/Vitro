/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier;

public abstract class  RoleIdentifier implements Identifier {
    public abstract String getRole();
    public abstract String getUri();
    
    public static String getUri( Identifier id){
        if( id == null ) return null;
        if( id instanceof RoleIdentifier ){
            return ((RoleIdentifier)id).getUri();
        }else{
            return null;
        }
    }
    
    public static String getUri( IdentifierBundle idb){
        for( Identifier id : idb ){
            if (id instanceof RoleIdentifier) {
                RoleIdentifier roleId = (RoleIdentifier) id;
                return roleId.getUri();                
            }
        }
        return null;
    }
}
