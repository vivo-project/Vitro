/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.util.Date;

public interface PropertyInstanceIface {
//needed for PropertyInstance
//object property statements
    public abstract String getPropertyURI();
    public abstract String getObjectEntURI();
    public abstract String getSubjectEntURI();

//  entities
    public abstract String getSubjectName();
    public abstract String getObjectName();

//needed for Any Property
//properties
    public abstract String getPropertyName();
    public abstract String getDomainPublic();
    public abstract String getRangePublic();

//classs
    public abstract String getDomainClassName();
    public abstract String getRangeClassName();
    public abstract String getDomainQuickEditJsp();
    public abstract String getRangeQuickEditJsp();

//classs2relations
    public abstract String getRangeClassURI();
    public abstract String getDomainClassURI();

    public abstract boolean getSubjectSide();

/******************* setters ************************/

    public abstract void setPropertyURI(String in);

    public abstract void setSubjectName(String in);
    public abstract void setObjectName(String in);
    public abstract void setRangeClassURI(String in);
    public abstract void setDomainClassURI(String in);
    public abstract void setDomainClassName(String in);
    public abstract void setRangeClassName(String in);

    public abstract void setSubjectEntURI(String in);
    public abstract void setObjectEntURI(String in);

    public abstract void setPropertyName(String in);

    public abstract void setDomainPublic(String in);

    public abstract void setRangePublic(String in);

    public abstract void setSubjectSide(boolean in);

    public abstract void setDomainQuickEditJsp(String in);

    public abstract void setRangeQuickEditJsp(String in);

}
