package edu.cornell.mannlib.vitro.webapp.dao;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import edu.cornell.mannlib.vitro.webapp.beans.Namespace;

import java.util.List;

public interface NamespaceDao {

    public abstract List<Namespace> getAllNamespaces();

    public abstract Namespace getNamespaceById(int namespaceId);

    int insertNewNamespace(Namespace namespace);

    void updateNamespace(Namespace ont);

    void deleteNamespace(int namespaceId);

    void deleteNamespace(Namespace namespace);
}