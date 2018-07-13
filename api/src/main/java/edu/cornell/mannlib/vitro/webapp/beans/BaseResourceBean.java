/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class BaseResourceBean implements ResourceBean {
	
	private static final Log log = LogFactory.getLog(BaseResourceBean.class.getName());
    protected String URI          = null;
    protected String namespace    = null;
    protected String localName    = null;
    protected String localNameWithPrefix = null;
    protected String pickListName = null;

	public BaseResourceBean() {
	    // default constructor
	}
	
	public BaseResourceBean(String uri) {
	    buildLocalAndNS(uri);
	}
	
    @Override
	public boolean isAnonymous() {        
    	return (this.URI==null || VitroVocabulary.PSEUDO_BNODE_NS.equals(this.getNamespace()));
    }
    
    @Override
	public String getURI() {
        return URI;
    }
    @Override
	public void setURI(String URI) {  
        if( this.localName != null || this.namespace != null)
            buildLocalAndNS(URI);
        else
            this.URI = URI;
    }

    private void buildLocalAndNS(String URI) {
        if (URI == null) {
            this.URI = null;
            this.namespace = null;
            this.localName = null;
        } else {
            this.URI = URI;
            Resource uri = ResourceFactory.createResource(URI);
            this.namespace = uri.getNameSpace();
            this.localName = uri.getLocalName();
        }
    }
    
    @Override
	public String getNamespace() {
        if( namespace == null && this.URI != null)
            buildLocalAndNS(this.URI);        
        return namespace;
    }
    @Override
	public void setNamespace(String namespace) {
        this.namespace = namespace;
        if (namespace != null && localName != null ) {
            this.URI = namespace + localName;
        }
    }
    
    @Override
	public String getLabel() {
        return getLocalName();
    }

    @Override
	public String getLocalName() {
        if( localName == null && this.URI != null)
            buildLocalAndNS(this.URI);        
        return localName;
    }
    
    @Override
	public void setLocalName(String localName) {
        this.localName = localName;
        if (namespace != null && localName != null) {
            this.URI = namespace + localName;
        }
    }

    public String getLocalNameWithPrefix() {
        return localNameWithPrefix != null ? localNameWithPrefix : 
                    getLocalName() != null ?  getLocalName() :
                        URI != null ? URI : "(no name)" ;
    }
    public void setLocalNameWithPrefix(String prefixedLocalName) {
        this.localNameWithPrefix = prefixedLocalName;
    }
    
    @Override
	public String getPickListName() {
        return pickListName==null ? getLocalName()==null ? 
                (URI==null ? "(no name)" : URI ): getLocalName() : pickListName;
    }
    public void setPickListName(String pickListName) {
        this.pickListName = pickListName;
    }
    
	@Override
	public boolean equals(Object obj) {
		if(obj == null ) 
			return false;
		else if (obj instanceof BaseResourceBean ){
			String thisURI = this.getURI();
			String thatURI = ((BaseResourceBean)obj).getURI();
			if( thisURI != null && thatURI != null ){
				return thisURI.equals(thatURI);
			}
		}
		return obj.hashCode() == this.hashCode();			
	}
	
	@Override
	public int hashCode() {
		if( getURI() != null )
			return getURI().hashCode();
		else
			return super.hashCode();
	}

    
}
