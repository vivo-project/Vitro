/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.text.Collator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A representation of an OWL ontology resource
 * [kwg8-08/01/07] unified get*, set* methods with style in VClass.java
 */
public class Ontology implements Comparable<Ontology>
{
	
	private static final Log log = LogFactory.getLog(Ontology.class.getName());
	
    private int myID = -1;
    public int getId()          { return myID; }
    public void setId( int id ) { myID = id; }

    private String myName = null;
    public String getName()             { return myName; }
    public void setName( String name )  { myName = name; }

    private String myType = null;
    public String getType()             { return myType; }
    public void setType( String type )  { myType = type; }
    
    private String myPrefix = null;
    public String getPrefix()               { return myPrefix; }
    public void setPrefix( String prefix )  { myPrefix = prefix; }

    private int myNamespaceID;
    public int getNamespaceId()           { return myNamespaceID; }
    public void setNamespaceId( int nid ) { myNamespaceID = nid; }

    private String myURI;
    public String getURI()              { return myURI; }
    public void setURI( String URI )    { myURI = URI; }

    private List myVClassesList = null;
    public List getVClassesList()              { return myVClassesList; }
    public void setVClassesList( List vcl )    { myVClassesList = vcl; }

    private List myPropsList = null;
    public List getPropsList()           { return myPropsList; }
    public void setPropsList( List pl )  { myPropsList = pl; }

    private List myEntitiesList = null;
    public List getEntsList()                  { return myEntitiesList; }
    public void setEntsList( List entsList )   { myEntitiesList = entsList; }
    
    public int compareTo(Ontology o2) {
    	Collator collator = Collator.getInstance();
        if (o2 == null) {
            log.error("Ontology NULL in DisplayComparator()");
            return 0;
        }
        return collator.compare(this.getName(), o2.getName());
    }
    
}