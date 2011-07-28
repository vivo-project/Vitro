/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.beans.XMLEncoder;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.openrdf.model.impl.URIImpl;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

/**
 * a class representing an object property
 *
 */
public class ObjectProperty extends Property implements Comparable<ObjectProperty>
{
	private static final Log log = LogFactory.getLog(ObjectProperty.class.getName());
	
    private String parentURI = null;

    private String domainVClassURI = null;
    private VClass domainVClass = null;
    private String domainEntityURI = null;
    private String domainSidePhasedOut = null;
    private String domainPublic = null;

    private String rangeVClassURI = null;
    private VClass rangeVClass = null;
    private String rangeEntityURI = null;
    private String rangeSidePhasedOut = null;
    private String rangePublic = null;

    private boolean transitive = false;
    private boolean symmetric = false;
    private boolean functional = false;
    private boolean inverseFunctional = false;
    
    private List<ObjectPropertyStatement> objectPropertyStatements = null;
    private String example = null;
    private String description = null;
    private String publicDescription = null;

    private String URIInverse = null;
    private String namespaceInverse = null;
    private String localNameInverse = null;

    private String domainEntitySortField = null;
    private String domainEntitySortDirection = null;
    private String domainDisplayTier = "-1";
    private int domainDisplayLimit = 5;
    private String domainQuickEditJsp = null;

    private String objectIndividualSortPropertyURI = null;
    
    private String rangeEntitySortField = null;
    private String rangeEntitySortDirection = null;
    private String rangeDisplayTier = "-1";
    private int rangeDisplayLimit = 5;
    private String rangeQuickEditJsp = null;
    
    private boolean selectFromExisting = true;
    private boolean offerCreateNewOption = false;
    private boolean stubObjectRelation = false;
    
    private boolean collateBySubclass = false;
    
    public ObjectProperty() {
        super();
    }

    /** for debugging */
    public void xmlToSysOut(){
        XMLEncoder e = new XMLEncoder(System.out);
        e.writeObject(this);
    }


    public String getDomainVClassURI() {
        return domainVClassURI;
    }
    public void setDomainVClassURI(String domainClassURI) {
        this.domainVClassURI = domainClassURI;
    }

    public String getDomainEntityURI() {
        return domainEntityURI;
    }
    public void setDomainEntityURI(String domainEntityURI) {
        this.domainEntityURI = domainEntityURI;
    }

    public String getDomainPublic() {
        return domainPublic;
    }
    public void setDomainPublic(String domainPublic) {
        this.domainPublic = domainPublic;
    }

    public String getDomainSidePhasedOut() {
        return domainSidePhasedOut;
    }
    public void setDomainSidePhasedOut(String domainSidePhasedOut) {
        this.domainSidePhasedOut = domainSidePhasedOut;
    }
    public VClass getDomainVClass() {
        return domainVClass;
    }
    public void setDomainVClass(VClass domainVClass) {
        this.domainVClass = domainVClass;
    }
    public String getParentURI() {
        return parentURI;
    }
    public void setParentURI(String parentURI) {
        this.parentURI = parentURI;
    }
    public String getRangeVClassURI() {
        return rangeVClassURI;
    }
    public void setRangeVClassURI(String rangeClassURI) {
        this.rangeVClassURI = rangeClassURI;
    }
    public String getRangeEntityURI() {
        return rangeEntityURI;
    }
    public void setRangeEntityURI(String rangeEntityURI) {
        this.rangeEntityURI = rangeEntityURI;
    }

    public String getRangePublic() {
        return rangePublic;
    }
    public void setRangePublic(String rangePublic) {
        this.rangePublic = rangePublic;
    }

    public String getRangeSidePhasedOut() {
        return rangeSidePhasedOut;
    }
    public void setRangeSidePhasedOut(String rangeSide) {
        this.rangeSidePhasedOut = rangeSide;
    }
    public VClass getRangeVClass() {
        return rangeVClass;
    }
    public void setRangeVClass(VClass rangeVClass) {
        this.rangeVClass = rangeVClass;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublicDescription() {
        return publicDescription;
    }
    public void setPublicDescription(String s) {
        this.publicDescription = s;
    }

    public String getExample() {
        return example;
    }
    public void setExample(String example) {
        this.example = example;
    }
    public List<ObjectPropertyStatement> getObjectPropertyStatements() {
        return objectPropertyStatements;
    }
    public void setObjectPropertyStatements(List<ObjectPropertyStatement> objectPropertyStatements) {
        this.objectPropertyStatements =  objectPropertyStatements;
    }

    public String getURIInverse() {
        return URIInverse;
    }
    public void setURIInverse(String URIInverse) {
        if (URIInverse == null) {
            this.URIInverse = null;
            this.namespaceInverse = null;
            this.localNameInverse = null;
        } else {
            this.URIInverse = URIInverse;
            URIImpl uriInverse = new URIImpl(URIInverse);
            this.namespaceInverse = uriInverse.getNamespace();
            this.localNameInverse = uriInverse.getLocalName();
        }
    }

    public String getNamespaceInverse() {
        return namespaceInverse;
    }
    public void setNamespaceInverse(String namespaceInverse) {
        this.namespaceInverse = namespaceInverse;
        if (namespaceInverse != null && localNameInverse != null) {
            this.URIInverse = namespaceInverse + localNameInverse;
        }
    }

    public String getLocalNameInverse() {
        return localNameInverse;
    }
    public void setLocalNameInverse(String localNameInverse) {
        this.localNameInverse = localNameInverse;
        if (namespaceInverse != null && localNameInverse != null) {
            this.URIInverse = namespaceInverse + localNameInverse;
        }
    }
    
    public boolean getTransitive() {
    	return transitive;
    }
    
    public void setTransitive(boolean transitive) {
    	this.transitive = transitive;
    }
    
    public boolean getSymmetric() {
    	return symmetric;
    }
    
    public void setSymmetric(boolean symmetric) {
    	this.symmetric = symmetric;
    }
    
    public boolean getFunctional() {
    	return functional;
    }
    
    public void setFunctional(boolean functional) {
    	this.functional = functional;
    }
    
    public boolean getInverseFunctional() {
    	return inverseFunctional;
    }
    
    public void setInverseFunctional(boolean inverseFunctional) {
    	this.inverseFunctional = inverseFunctional;
    }

	public void setCollateBySubclass(boolean collate) {
		collateBySubclass = collate;
	}
	
	public boolean getCollateBySubclass() {
		return collateBySubclass;
	}
    
    /**
     * adds a single ObjectPropertyStatement object to Property's object property statements List.
     * @param e2e
     */
    public final void addObjectPropertyStatement(ObjectPropertyStatement objPropertyStmt){
        if( objPropertyStmt == null ) return;
        if( getObjectPropertyStatements() == null )
            setObjectPropertyStatements(new LinkedList<ObjectPropertyStatement>() );
        getObjectPropertyStatements().add(objPropertyStmt);
    }

    public int getDomainDisplayLimit() {
        return domainDisplayLimit;
    }
    public void setDomainDisplayLimit(int domainDisplayLimit) {
        this.domainDisplayLimit = domainDisplayLimit;
    }
    public String getDomainDisplayTier() {
        return domainDisplayTier;
    }
    public void setDomainDisplayTier(String domainDisplayTier) {
        this.domainDisplayTier = domainDisplayTier;
    }

    public String getDomainEntitySortDirection() {
        return domainEntitySortDirection;
    }
    public void setDomainEntitySortDirection(String domainEntitySortDirection) {
        this.domainEntitySortDirection = domainEntitySortDirection;
    }
    public String getDomainEntitySortField() {
        return domainEntitySortField;
    }
    public void setDomainEntitySortField(String domainEntitySortField) {
        this.domainEntitySortField = domainEntitySortField;
    }

    public String getDomainQuickEditJsp() {
        return domainQuickEditJsp;
    }
    public void setDomainQuickEditJsp(String domainQuickEditJsp) {
        this.domainQuickEditJsp = domainQuickEditJsp;
    }

    public String getObjectIndividualSortPropertyURI() {
    	return this.objectIndividualSortPropertyURI;
    }
    public void setObjectIndividualSortPropertyURI(String objectIndividualSortPropertyURI) {
    	this.objectIndividualSortPropertyURI = objectIndividualSortPropertyURI;
    }

    public int getRangeDisplayLimit() {
        return rangeDisplayLimit;
    }
    public void setRangeDisplayLimit(int rangeDisplayLimit) {
        this.rangeDisplayLimit = rangeDisplayLimit;
    }
    public String getRangeDisplayTier() {
        return rangeDisplayTier;
    }
    public void setRangeDisplayTier(String rangeDisplayTier) {
        this.rangeDisplayTier = rangeDisplayTier;
    }

    public String getRangeEntitySortDirection() {
        return rangeEntitySortDirection;
    }
    public void setRangeEntitySortDirection(String rangeEntitySortDirection) {
        this.rangeEntitySortDirection = rangeEntitySortDirection;
    }
    public String getRangeEntitySortField() {
        return rangeEntitySortField;
    }
    public void setRangeEntitySortField(String rangeEntitySortField) {
        this.rangeEntitySortField = rangeEntitySortField;
    }

    public String getRangeQuickEditJsp() {
        return rangeQuickEditJsp;
    }
    public void setRangeQuickEditJsp(String rangeQuickEditJsp) {
        this.rangeQuickEditJsp = rangeQuickEditJsp;
    }

    public boolean getSelectFromExisting() {
        return selectFromExisting;
    }
    
    public void setSelectFromExisting(boolean b) {
        this.selectFromExisting = b;
    }
    
    public boolean getOfferCreateNewOption() {
        return offerCreateNewOption;
    }
    
    public void setOfferCreateNewOption(boolean b) {
        this.offerCreateNewOption = b;
    }
    
    public boolean getStubObjectRelation() {
        return stubObjectRelation;
    }
    
    public void setStubObjectRelation(boolean b) {
        this.stubObjectRelation = b;
    }
    
    /**
     * swaps the domain and range.
     *
     */
    public final void reflect(){
        int tempI = getDomainDisplayLimit();
        setDomainDisplayLimit( getRangeDisplayLimit() );
        setRangeDisplayLimit( tempI);

        String tmpS =getDomainDisplayTier();
        setDomainDisplayTier( getRangeDisplayTier() );
        setRangeDisplayTier(tmpS);

        tmpS=getDomainEntityURI();
        setDomainEntityURI(getRangeEntityURI());
        setRangeEntityURI(tmpS);

        tmpS=getDomainEntitySortDirection();
        setDomainEntitySortDirection(getRangeEntitySortDirection());
        setRangeEntitySortDirection(tmpS);

        tmpS=getDomainEntitySortField();
        setDomainEntitySortField(getRangeEntitySortField());
        setRangeEntitySortField(tmpS);

        tmpS=getDomainPublic();
        setDomainPublic(getRangePublic());
        setRangePublic(tmpS);

        tmpS=getDomainQuickEditJsp();
        setDomainQuickEditJsp(getRangeQuickEditJsp());
        setRangeQuickEditJsp(tmpS);

        tmpS=getDomainSidePhasedOut();
        setDomainSidePhasedOut(getRangeSidePhasedOut());
        setRangeSidePhasedOut(tmpS);

        VClass tmpC=getDomainVClass();
        setDomainVClass(getRangeVClass());
        setRangeVClass(tmpC);

        tmpS = getDomainVClassURI();
        setDomainVClassURI(getRangeVClassURI());
        setRangeVClassURI(tmpS);
    }
    
    /**
     * Sorts alphabetically by non-public name
     */
    public int compareTo (ObjectProperty op) {
        Collator collator = Collator.getInstance();
        return collator.compare(this.getDomainSidePhasedOut(),(op).getDomainSidePhasedOut());
    }

    /**
     * Sorts Property objects, by property rank, then alphanumeric.
     * @author bdc34
     */
    public static class DisplayComparator implements Comparator{
        public int compare(Object o1, Object o2) {
            if( !(o1 instanceof ObjectProperty ) && !(o2 instanceof ObjectProperty))
                return 0;
            String tier1 = ((ObjectProperty ) o1).getDomainDisplayTier();
            String tier2 = ((ObjectProperty ) o2).getDomainDisplayTier();
            tier1 = tier1 == null ? "0":tier1;
            tier2 = tier2 == null ? "0":tier2;
            return Integer.parseInt( tier1 ) - Integer.parseInt( tier2 );
        }
    }

    private Collator collator = Collator.getInstance();    
    
    /**
     * Sorts the object property statements taking into account the sort order.
     */
    public static List<ObjectPropertyStatement> sortObjectPropertyStatementsForDisplay(ObjectProperty prop, List objPropStmtsList) {
        if (objPropStmtsList==null) {
            log.error("incoming object property statement list is null; returning null");
            return null;
        }
        if (objPropStmtsList.size()<2) { // no need to sort
            return objPropStmtsList;
        }
        String tmpDirection = prop.getDomainEntitySortDirection(); //valid values are "desc" and "asc", anything else will default to ascending
        final boolean direction = !"desc".equalsIgnoreCase(tmpDirection);

        String objIndivSortPropURI=prop.getObjectIndividualSortPropertyURI();
        if (prop.getObjectIndividualSortPropertyURI() == null || prop.getObjectIndividualSortPropertyURI().length()==0) {
            String tmpField = prop.getDomainEntitySortField();
            log.debug("objectIndividualSortPropertyURI is null or blank so sorting by field "+tmpField);
            if( tmpField == null || tmpField.length() == 0) {
                tmpField = "name";
            }
            final String field = tmpField;
            
            Comparator fieldComp = new Comparator() {
                final String cField= field;
                final boolean cAsc = direction;
    
                public final int compare(Object o1, Object o2) {
                    ObjectPropertyStatement e2e1= (ObjectPropertyStatement)o1, e2e2=(ObjectPropertyStatement)o2;
                    Individual e1 , e2;
                    e1 = e2e1 != null ? e2e1.getObject():null;
                    e2 = e2e2 != null ? e2e2.getObject():null;
    
                    Object val1 = null, val2 = null;
                    if( e1 != null ) {
                        try {
                            val1 = e1.getField(cField);
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    } else {
                        log.debug( "PropertyWebapp.sortObjectPropertiesForDisplay() passed object property statement with no range entity.");
                    }
                    if( e2 != null ) {
                        try {
                            val2 = e2.getField(cField);
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    } else {
                    	log.debug( "PropertyWebapp.sortObjectPropertyStatementsForDisplay passed object property statement with no range entity.");
                    }
                    int rv = 0;
                    try {
                        if( val1 instanceof String ) {
                        	
                        	if (val1 == null && val2 == null) {
                        		rv = 0;
                        	} else if (val1 == null) {
                        		rv = 1;
                        	} else if (val2 == null) {
                        		rv = -1;
                        	} else {
                               	
                                Collator collator = Collator.getInstance();
                            	rv = collator.compare( ((String)val1) , ((String)val2) );
                                //rv = ((String)val1).compareTo((String)val2);                 		
                        	}
 
                        } else if( val1 instanceof Date ) {
                            DateTime dt1 = new DateTime((Date)val1);
                            DateTime dt2 = new DateTime((Date)val2);
                            rv = dt1.compareTo(dt2);
                        } else {
                            rv = 0;
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
    
                    if( cAsc ) {
                        return rv;
                    } else {
                        return rv * -1;
                    }
                }
            };
            try {
                Collections.sort(objPropStmtsList, fieldComp);
            } catch (Exception e) {
                log.error("Exception sorting object property statements for object property "+prop.getURI());
            }
        } else { // sort by specified range entity data property value instead of a property having a get() method in Individual.java
            log.debug("using data property "+prop.getObjectIndividualSortPropertyURI()+" to sort related entities");
            final boolean compDirection = direction;
            final String objIndSortPropURI = prop.getObjectIndividualSortPropertyURI();
            Comparator dpComp = new Comparator() {
                final String cDatapropURI = objIndSortPropURI;
                final boolean cAscending = compDirection;
    
                public final int compare(Object o1, Object o2){
                    ObjectPropertyStatement e2e1= (ObjectPropertyStatement)o1, e2e2=(ObjectPropertyStatement)o2;
                    Individual e1 , e2;
                    e1 = e2e1 != null ? e2e1.getObject():null;
                    e2 = e2e2 != null ? e2e2.getObject():null;
    
                    Object val1 = null, val2 = null;
                    if( e1 != null ){
                        try {
                            List<DataPropertyStatement> dataPropertyStatements = e1.getDataPropertyStatements();
                            for (DataPropertyStatement dps : dataPropertyStatements) {
                                if (cDatapropURI.equals(dps.getDatapropURI())) {
                                    if (dps.getData()!=null && dps.getData().trim().length()>0) {
                                    	if (XSDDatatype.XSDint.getURI().equals(dps.getDatatypeURI()) || XSDDatatype.XSDinteger.getURI().equals(dps.getDatatypeURI())) {
                                    		val1 = Integer.parseInt(dps.getData());
                                    	} else {
                                    		val1 = dps.getData();
                                    	}
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else
                        log.debug( "PropertyWebapp.sortObjectPropertiesForDisplay passed object property statement with no range entity.");
    
                    if( e2 != null ){
                        try {
                            List<DataPropertyStatement> dataPropertyStatements = e2.getDataPropertyStatements();
                            for (DataPropertyStatement dps : dataPropertyStatements) {
                                if (cDatapropURI.equals(dps.getDatapropURI())) {
                                    if (dps.getData()!=null && dps.getData().trim().length()>0) {
                                    	if (XSDDatatype.XSDint.getURI().equals(dps.getDatatypeURI()) || XSDDatatype.XSDinteger.getURI().equals(dps.getDatatypeURI())) {
                                    		val2 = Integer.parseInt(dps.getData());
                                    	} else {
                                    		val2 = dps.getData();
                                    	}
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        log.debug( "PropertyWebapp.sortObjectPropertyStatementsForDisplay passed object property statement with no range entity.");
                    }
                    int rv = 0;
                    try {	
                    	if (val1 == null && val2 == null) {
                    		 rv = 0;
                    	} else if (val1==null) {
                             rv = 1;
                        } else if (val2==null) {
                             rv = -1;
                        }  else {                        
	                           if( val1 instanceof String ) {
	                              Collator collator = Collator.getInstance();
	                              rv = collator.compare( ((String)val1) , ((String)val2) ); //was rv = ((String)val1).compareTo((String)val2);
	                           } else if( val1 instanceof Date ) {
	                              DateTime dt1 = new DateTime((Date)val1);
	                              DateTime dt2 = new DateTime((Date)val2);
	                              rv = dt1.compareTo(dt2);
	                           } else if( val1 instanceof Integer) {
	                        	  rv = ((Integer) val1) - ((Integer) val2);
	                           } else{
	                            rv = 0;
	                           }
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
 
                    
                    if ( !cAscending ) {
                    	rv = rv * -1;
                    }
                    
                    // sort alphabetically by name if have same dataproperty value
                    if ( rv == 0 ) {
                    	String nameValue1 = ( e1.getName() != null ) ? e1.getName() : "";
                    	String nameValue2 = ( e2.getName() != null ) ? e2.getName() : "";
                    	rv = Collator.getInstance().compare( nameValue1, nameValue2 );
                    }
                    
                    return rv;
                }
            };
            try {
                if (dpComp==null) {
                    log.error("data property comparator is null; returning unsorted object property statements list");
                    return objPropStmtsList;
                } else {
                    Collections.sort(objPropStmtsList, dpComp);
                }
            } catch (Exception e) {
                log.error("Exception sorting object property statements for object property "+prop.getURI());
            }
        }
        return objPropStmtsList;
    }

    /**
     * Produces a string representation of the contents of this class
     * @return Readable text identifying this property's attributes
     */
    public String toString(){
        String list = "null";
        if( getObjectPropertyStatements() != null ){
            Iterator it = getObjectPropertyStatements().iterator();
            if( !it.hasNext() ) list = " none";
            while(it.hasNext()){
                Object obj = it.next();
                if( obj != null && obj instanceof ObjectPropertyStatement){
                    list += "\n\t\t" + ((ObjectPropertyStatement)obj).toString();
                }else{
                    list += "\n\t\t" + obj.toString();
                }
            }
        }
        return "* Property:" +
        "id: " + getURI() + "\n\t" +
        "domainDisplayLimit: " + getDomainDisplayLimit() + " \n\t" +
        "domainDisplayTier: " + getDomainDisplayTier() + "\n\t" +
        "domainEntityId: " + getDomainEntityURI() + "\n\t" +
        "domainEntitySortDirection: " + getDomainEntitySortDirection() + "\n\t" +
        "domainEntitySortField: " + getDomainEntitySortField() + "\n\t" +
        "domainVClass: " + getDomainVClass() + "\n\t" +
        "domainClassId: " + getDomainVClassURI() + "\n\t" +
        "domainPublic: " + getDomainPublic() + "\n\t" +
        "domainQuickEditJsp: " + getDomainQuickEditJsp() + "\n\t" +
        "domainSidePhasedOut: " + getDomainSidePhasedOut() + "\n\t" +
        "parentId: " + getParentURI() + "\n\t" +
        "rangeDisplayLimit: " + getRangeDisplayLimit() + "\n\t" +
        "rangeDisplayTier: " + getRangeDisplayTier() + "\n\t" +
        "rangeEntityId: " + getRangeEntityURI() + "\n\t" +
        "rangeEntitySortDirection: " + getRangeEntitySortDirection() + "\n\t" +
        "rangeEntitySortField: " + getRangeEntitySortField() + "\n\t" +
        "rangeVClass: " + getRangeVClass() + "\n\t" +
        "rangeClassId: " + getRangeVClassURI() + "\n\t" +
        "rangePublic: " + getRangePublic() + "\n\t" +
        "rangeQuickEditJsp: " + getRangeQuickEditJsp() + "\n\t" +
        "rangeSidePhasedOut: " + getRangeSidePhasedOut() + "\n\t" +
        "customEntryForm" + getCustomEntryForm() + "\n\t" +
        "selectFromExisting" + getSelectFromExisting() + "\n\t" +
        "offerCreateNewOption" + getOfferCreateNewOption() + "\n\t" +
        "** object property statements: " + list + "\n";

    }
}