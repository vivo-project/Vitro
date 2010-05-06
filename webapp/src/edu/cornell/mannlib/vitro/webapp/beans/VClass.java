/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.text.Collator;

import org.openrdf.model.impl.URIImpl;

/**
 * A Java class representing an ontology ("Vitro") class
 *
 * [kwg8-07/30/07]: comments
 * [kwg8-07/30/07]: comments, naming cleanup
 */
public class VClass extends BaseResourceBean implements Comparable<VClass>
{
	
    /**
     * What this VClass is called
     */
    private String myName = null;
    public  String getName()              { return myName; }
    public  void   setName( String name ) { myName = name; }

    /**
     * An example member of this VClass
     */
    private String myExample = null;
    public  String getExample()                 { return myExample; }
    public  void   setExample( String example ) { myExample = example; }

    /**
     * Information about the type of information expected of a member of this VClass
     */
    private String myDescription = null;
    public  String getDescription()               { return myDescription; }
    public  void   setDescription( String descr ) { myDescription = descr; }

    private String myShortDefinition = null;
    public  String getShortDef()            { return myShortDefinition; }
    public  void   setShortDef( String sd ) { myShortDefinition = sd; }

    private int  myEntityCount = -1;
    
    // rjy7 Removing deprecation since currently we have no other means to get this value.
    // @Deprecated
    public  int  getEntityCount()         { return myEntityCount; }
    
    public  void setEntityCount( int ec ) { myEntityCount = ec; }

    private int  displayLimit = -1;
    public  int  getDisplayLimit() { return displayLimit; }
    public  void setDisplayLimit(int displayLimit) { this.displayLimit = displayLimit; }

    private String quickEditJsp = null;
    public  String getQuickEditJsp()                    { return quickEditJsp; }
    public  void   setQuickEditJsp(String quickEditJsp) { this.quickEditJsp = quickEditJsp; }

    private int  displayRank = -1;
    public  int  getDisplayRank()                { return displayRank; }
    public  void setDisplayRank(int displayRank) { this.displayRank = displayRank; }

    private String  groupURI = null;
    public  String  getGroupURI()            { return groupURI; }
    public  void    setGroupURI(String groupURI) { this.groupURI = groupURI; }

    private VClassGroup group=null;
    public  VClassGroup getGroup()                { return group; }
    public  void        setGroup(VClassGroup vcg) { group = vcg;  }

    private String customEntryForm = null;
    public  String getCustomEntryForm()         { return customEntryForm; }
    public  void   setCustomEntryForm(String s) { this.customEntryForm = s; }
    
    private String customDisplayView = null;
    public  String getCustomDisplayView()         { return customDisplayView; }
    public  void   setCustomDisplayView(String s) { this.customDisplayView = s; }
    
    private String customShortView = null;
    public  String getCustomShortView()         { return customShortView; }
    public  void   setCustomShortView(String s) { this.customShortView = s; }
    
    private String customSearchView = null;
    public  String getCustomSearchView()         { return customSearchView; }
    public  void   setCustomSearchView(String s) { this.customSearchView = s; }    

    protected Float searchBoost = null;
    public Float getSearchBoost() { return searchBoost; }
    public void setSearchBoost( Float boost ){ searchBoost = boost;}
    
    /**
     * Default constructor
     */
    public VClass()
    {
        super();
    }

    /**
     * Constructs the VClass from a URI that has been separated into namespace and localName components.
     * @param namespace The name-space for the URI
     * @param localName The local name for this URI
     * @param vclassName The name of the VClass
     */
    public VClass( String namespace, String localName, String vclassName )
    {
        myName = vclassName;
        namespace = namespace;
        localName = localName;
        URI = namespace + localName;
    }

    /**
     * Constructs the VClass with a given URI
     *   @param uriString The source string with which to create this URI
     */
    public VClass( String uriString )
    {
        // The URIImpl class can be used to parse a URI string into its component parts
        URIImpl uri = new URIImpl(uriString);

        // Use the URIImpl to obtain parts of this URI for local storage
        myName = uri.getLocalName();
        URI = uriString;
        namespace = uri.getNamespace();
        localName = uri.getLocalName();
    }
    
    /**
     * Sorts alphabetically by name
     */
    public int compareTo (VClass o1) {
        Collator collator = Collator.getInstance();
        return collator.compare(this.getName(),o1.getName());
    }

    /**
     * Converts the VClass to a string
     */
    public String toString()
    {
        // Get the name of this VClass
        String n = getName();

        // Set up a default name if none exists already
        if( n == null ) n = "null Name";

        // Build the return string
        return n + '(' + getURI() + ')';
    }
}
