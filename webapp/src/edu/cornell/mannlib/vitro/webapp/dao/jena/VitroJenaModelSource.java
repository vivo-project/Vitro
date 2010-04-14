/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

public interface VitroJenaModelSource {

	public abstract VitroJenaModel getVitroJenaModel();
	
	public abstract VitroJenaModel getVitroJenaModel(String namespace);

	public abstract VitroJenaModel getMutableVitroJenaModel();
	
	public abstract VitroJenaModel getMutableVitroJenaModel(String namespace);
	
	public abstract VitroJenaModel getVitroAnnotationsJenaModel();
	
	public abstract VitroJenaModel getVitroAnnotationsJenaModel(String namespace);
	
}
