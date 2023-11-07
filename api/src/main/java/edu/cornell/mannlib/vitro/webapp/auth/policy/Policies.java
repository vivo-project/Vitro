/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Policy;

public interface Policies {

    List<Policy> getList();

    boolean contains(Policy policy);

    void add(Policy policy);

    long size();

    void clear();

    void remove(String policyUri);

}
