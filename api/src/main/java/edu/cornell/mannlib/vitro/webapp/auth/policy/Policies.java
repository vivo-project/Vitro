/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Policy;

public interface Policies {

    public List<Policy> getList();

    public boolean contains(Policy policy);

    public void add(Policy policy);

    public long size();

    public void clear();

    void remove(String policyUri);
}