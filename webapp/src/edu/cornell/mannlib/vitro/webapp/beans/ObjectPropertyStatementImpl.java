/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.util.Date;
import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * a class representing a particular instance of an object property
 *
 */
public class ObjectPropertyStatementImpl implements VitroTimeWindowedResource, ObjectPropertyStatement
{
    private String subjectURI = null;
    private Individual subject = null;
    private String objectURI = null;
    private Individual object = null;

    private String propertyURI = null;
    private ObjectProperty property = null;
    private String qualifier = null;
    private boolean subjectOriented = true; //is the range the item of interest?
    private String description = null;  //generated desc based on subjectOriented during sql query.

    /*
     fields in db:
id
domainId
rangeId
relationId
modTime
propertyId
sunrise
sunset
qualifier
    */
    
   public ObjectPropertyStatementImpl() { }

   public ObjectPropertyStatementImpl(String subjectUri, String propertyUri, String objectUri) {
        subjectURI = subjectUri;
        propertyURI = propertyUri;
        objectURI = objectUri;        
    }

/* (non-Javadoc)
 * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#toString()
 */
public String toString(){
       String prop = (getProperty()!=null)?getProperty().getDomainPublic():"by propURI"+getPropertyURI();
       String ran = (getObject()!= null)?getObject().getName():"objectURI:"+getObjectURI();
       String dom = (getSubject()!= null)?getSubject().getName():"subjectURI:"+getSubjectURI();
       String orent = (isSubjectOriented() )?"subject oriented":"object oriented";
       return "Object Property Statements: "+dom+" "+prop+" to "+ran+" "+orent;
   }

    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#getDescription()
     */
    public String getDescription() {
        return description;
    }


    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#setDescription(java.lang.String)
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#isSubjectOriented()
     */
    public boolean isSubjectOriented() {
        return subjectOriented;
    }


    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#setSubjectOriented(boolean)
     */
    public void setSubjectOriented(boolean subjectOriented) {
        this.subjectOriented = subjectOriented;
    }

    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#getSubjectURI()
     */
    public String getSubjectURI() {
        return subjectURI;
    }


    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#setSubjectURI(java.lang.String)
     */
    public void setSubjectURI(String subjectURI) {
        this.subjectURI = subjectURI;
    }

    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#getQualifier()
     */
    public String getQualifier() {
        return qualifier;
    }


    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#setQualifier(java.lang.String)
     */
    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }


    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#getObjectURI()
     */
    public String getObjectURI() {
        return objectURI;
    }


    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#setObjectURI(java.lang.String)
     */
    public void setObjectURI(String objectURI) {
        this.objectURI = objectURI;
    }


    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#getSubject()
     */
    public Individual getSubject() {
        return subject;
    }


    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#setSubject(edu.cornell.mannlib.vitro.webapp.beans.Individual)
     */
    public void setSubject(Individual subject) {
        this.subject = subject;
    }


    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#getProperty()
     */
    public ObjectProperty getProperty() {
        return property;
    }


    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#setProperty(edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty)
     */
    public void setProperty(ObjectProperty property) {
        if(property != null )
            setPropertyURI(property.getURI());
        else
            setPropertyURI(null);
        this.property = property;
    }


    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#getObject()
     */
    public Individual getObject() {
        return object;
    }


    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#setObject(edu.cornell.mannlib.vitro.webapp.beans.Individual)
     */
    public void setObject(Individual object) {
        this.object = object;
    }


    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#getPropertyURI()
     */
    public String getPropertyURI() {
        return this.propertyURI;
    }
    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#setPropertyURI(java.lang.String)
     */
    public void setPropertyURI(String URI){
        this.propertyURI = URI;
    }


    /**
     * Change the values of this object property statement object
     * so that it is from the other side of the relation.
     * This must be associative.
     * This does NOT modify this.property
     *
    public void reflect(){
        Individual tmp = getSubject();
        setSubject(getObject());
        setObject(tmp);
        setSubjectURI(getSubject().getURI());
        setObjectURI(getObject().getURI());
        setSubjectOriented( !isSubjectOriented() );
    }*/

    /**
     * Sorts entity object for display presentation.
     * @author bdc34
     */
    public static class DisplayComparator implements Comparator{
    	private static final Log log = LogFactory.getLog(DisplayComparator.class.getName());
    	
        public int compare(Object o1, Object o2) {
            Individual ent1 = ((ObjectPropertyStatement) o1).getSubject();
            Individual ent2 = ((ObjectPropertyStatement) o2).getSubject();
            log.debug("Comparing "+ent1.getName()+" to"+ent2.getName());
            return ent1.getName().compareTo(ent2.getName());
        }
    }

    private Date sunrise = null;
    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#setSunrise(java.util.Date)
     */
    public void setSunrise(Date date) {
        sunrise = date;
    }
    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#getSunrise()
     */
    public Date getSunrise(){
        return sunrise;
    }

    private Date sunset = null;
    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#setSunset(java.util.Date)
     */
    public void setSunset(Date date) {
        sunset = date;
    }
    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#getSunset()
     */
    public Date getSunset(){ return sunset; }

    /* (non-Javadoc)
     * @see edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement#toPropertyInstance()
     */
    public PropertyInstance toPropertyInstance(){
        PropertyInstance pi = new PropertyInstance();

        pi.setPropertyURI(propertyURI);
        pi.setSubjectEntURI(subjectURI);
        pi.setObjectEntURI(objectURI);
        pi.setQualifier(qualifier);
        pi.setSubjectSide(subjectOriented);
        return pi;
    }

}
