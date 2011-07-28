/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import edu.cornell.mannlib.vitro.webapp.beans.Datatype;

import java.util.List;

public interface DatatypeDao {

    public abstract void updateDatatype(Datatype dtp);

    /** deletes a datatype row by getting the id from a Datatype bean **/
    public abstract void deleteDatatype(Datatype dtp);

    /** deletes a datatype row **/
    public abstract void deleteDatatype(int id);

    public abstract Datatype getDatatypeById(int id);

    public abstract Datatype getDatatypeByURI(String uri);

    public abstract int getDatatypeIdByURI(String uri);

    public abstract List<Datatype> getAllDatatypes();

}