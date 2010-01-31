/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;
import java.util.Date;

public interface ObjectPropertyStatement {

    public String toString();

    public String getDescription();

    public void setDescription(String description);

    public boolean isSubjectOriented();

    public void setSubjectOriented(boolean subjectOriented);

    public String getSubjectURI();

    public void setSubjectURI(String subjectURI);

    public String getQualifier();

    public void setQualifier(String qualifier);

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

    public void setSunrise(Date date);

    public Date getSunrise();

    public void setSunset(Date date);

    public Date getSunset();

    public PropertyInstance toPropertyInstance();

}