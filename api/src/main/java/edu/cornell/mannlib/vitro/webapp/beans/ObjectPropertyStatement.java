/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.beans;

public interface ObjectPropertyStatement {

    public String toString();

    public String getSubjectURI();

    public void setSubjectURI(String subjectURI);

    public String getObjectURI();

    public void setObjectURI(String objectURI);

    public Individual getSubject();

    public void setSubject(Individual subject);

    public ObjectProperty getProperty();

    public void setProperty(ObjectProperty property);

    public Individual getObject();

    public void setObject(Individual object);

    public String getPropertyURI();

    public void setPropertyURI(String URI);

    public PropertyInstance toPropertyInstance();

}
