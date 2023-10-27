/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Policy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PolicyStore implements Policies {

    private static final Comparator<Policy> comparator = getPolicyComparator();
    private static PolicyStore INSTANCE = new PolicyStore();
    private static final Log log = LogFactory.getLog(PolicyStore.class);

    private PolicyStore() {
        INSTANCE = this;
    }

    public static PolicyStore getInstance() {
        return INSTANCE;
    }

    protected List<Policy> policyList = new CopyOnWriteArrayList<>();
    protected Map<String, Policy> policyMap = new ConcurrentHashMap<>();

    @Override
    public boolean contains(Policy policy) {
        return policyList.contains(policy);
    }

    @Override
    public synchronized void remove(String policyUri) {
        Policy oldPolicy = policyMap.get(policyUri);
        if (oldPolicy != null) {
            policyList.remove(oldPolicy);
        }
        policyMap.remove(policyUri);
    }

    @Override
    public synchronized void add(Policy policy) {
        if (policy == null) {
            log.error("Policy to add is null");
            return;
        }
        Policy oldPolicy = policyMap.put(policy.getUri(), policy);
        if (oldPolicy != null) {
            policyList.remove(oldPolicy);
        }
        policyList.add(policy);
        Collections.sort(policyList, comparator);
    }

    @Override
    public synchronized void clear() {
        policyList.clear();
        policyMap.clear();
    }

    public List<String> getShortUris() {
        List<String> uris = new LinkedList<>();
        for (Policy policy : policyList) {
            uris.add(policy.getShortUri());
        }
        return uris;
    }

    private static Comparator<Policy> getPolicyComparator() {
        return new Comparator<Policy>() {
            @Override
            public int compare(Policy lps, Policy rps) {
                if (lps.getPriority() > rps.getPriority()) {
                    return -1;
                } else if (lps.getPriority() < rps.getPriority()) {
                    return 1;
                }
                return lps.getUri().compareTo(lps.getUri());
            }
        };
    }

    @Override
    public List<Policy> getList() {
        return new ArrayList<>(policyList);
    }

    @Override
    public long size() {
        return policyList.size();
    }
}
