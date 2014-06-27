/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

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
    protected String myName = null;
    public  String getName()              { return myName; }
    public  void   setName( String name ) { myName = name; }

    /**
     * An example member of this VClass
     */
    protected String myExample = null;
    public  String getExample()                 { return myExample; }
    public  void   setExample( String example ) { myExample = example; }

    /**
     * Information about the type of information expected of a member of this VClass
     */
    protected String myDescription = null;
    public  String getDescription()               { return myDescription; }
    public  void   setDescription( String descr ) { myDescription = descr; }

    protected String myShortDefinition = null;
    public  String getShortDef()            { return myShortDefinition; }
    public  void   setShortDef( String sd ) { myShortDefinition = sd; }

    // TODO: [kwg8-08/01/07] What is this for?  It seems an entity count is the number of entities of
    // this type in the database.  Is this the case?
    // [bjl23 2007-08-12] Yep.  A count of individuals in the class.
    protected int  myEntityCount = -1;
    
    // rjy7 Removing deprecation since currently we have no other means to get this value.
    // @Deprecated
    public  int  getEntityCount()         { return myEntityCount; }
    
    public  void setEntityCount( int ec ) { myEntityCount = ec; }

    protected Integer  displayLimit = null;
    public  int  getDisplayLimit()  { return (displayLimit == null ? -1 : displayLimit); }
    public  void setDisplayLimit(int displayLimit) { this.displayLimit = displayLimit; }

    protected String quickEditJsp = null;
    public  String getQuickEditJsp()                    { return quickEditJsp; }
    public  void   setQuickEditJsp(String quickEditJsp) { this.quickEditJsp = quickEditJsp; }

    protected Integer  displayRank = null;
    public  int  getDisplayRank()                { return (displayRank == null ? -1 : displayRank); }
    public  void setDisplayRank(int displayRank) { this.displayRank = displayRank; }

    protected String  groupURI = null;
    public  String  getGroupURI()            { return groupURI; }
    public  void    setGroupURI(String groupURI) { this.groupURI = groupURI; }

    protected VClassGroup group=null;
    public  VClassGroup getGroup()                { return group; }
    public  void        setGroup(VClassGroup vcg) { group = vcg;  }

    protected String customEntryForm = null;
    public  String getCustomEntryForm()         { return customEntryForm; }
    public  void   setCustomEntryForm(String s) { this.customEntryForm = s; }
    
    protected String customDisplayView = null;
    public  String getCustomDisplayView()         { return customDisplayView; }
    public  void   setCustomDisplayView(String s) { this.customDisplayView = s; }
    
    protected String customShortView = null;
    public  String getCustomShortView()         { return customShortView; }
    public  void   setCustomShortView(String s) { this.customShortView = s; }
    
    protected String customSearchView = null;
    public  String getCustomSearchView()         { return customSearchView; }
    public  void   setCustomSearchView(String s) { this.customSearchView = s; }    

    protected Float searchBoost = null;
    public Float getSearchBoost() { return searchBoost; }
    public void setSearchBoost( Float boost ){ searchBoost = boost;}
    
    public boolean isUnion() { return false; }
    public List<VClass> getUnionComponents() { return new ArrayList<VClass>(); }
    
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
        this.namespace = namespace;
        this.localName = localName;
        URI = namespace + localName;
    }

    /**
     * Constructs the VClass with a given URI
     *   @param uriString The source string with which to create this URI
     */
    public VClass( String uriString )
    {
        super(uriString);
        myName = getLocalName();
    }
    
    /**
	 * Constructs the VClass as a deep copy of an existing VClass. 
	 */
    public VClass copy() {
    	VClass that = new VClass();
    	copyFields(that);
    	return that;
    }
    
	protected void copyFields(VClass that) {
		that.myName = this.myName;
    	that.namespace = this.namespace;
    	that.localName = this.localName;
    	that.URI = this.URI;
    	that.myExample = this.myExample;
    	that.myDescription = this.myDescription;
    	that.myShortDefinition = this.myShortDefinition;
    	that.myEntityCount = this.myEntityCount;
    	that.displayLimit = this.displayLimit;
    	that.displayRank = this.displayRank;
    	that.quickEditJsp = this.quickEditJsp;
    	that.groupURI = this.groupURI;
    	that.group = this.group;
    	that.customEntryForm = this.customEntryForm;
    	that.customDisplayView = this.customDisplayView;
    	that.customShortView = this.customShortView;
    	that.customSearchView = this.customSearchView;
	}
    
    /**
     * Sorts alphabetically by name
     */
    public int compareTo (VClass o1) {
        if (this.getName() == null) {
            return 1;
        } else if (o1.getName() == null) {
            return -1;
        } else {
            Collator collator = Collator.getInstance();
            return collator.compare(this.getName(),o1.getName());
        }
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
