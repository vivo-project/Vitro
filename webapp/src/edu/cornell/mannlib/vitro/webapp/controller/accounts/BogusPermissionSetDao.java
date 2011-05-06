/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.PermissionSet;
import edu.cornell.mannlib.vitro.webapp.dao.PermissionSetDao;

/**
 * TODO Destroy this as soon as PermissionSetDaoJena is in place.
 */
public class BogusPermissionSetDao implements PermissionSetDao {
	private static final Log log = LogFactory
			.getLog(BogusPermissionSetDao.class);

	private final Map<String, PermissionSet> map;

	public BogusPermissionSetDao() {
		Map<String, PermissionSet> psMap = new HashMap<String, PermissionSet>();
		putPermissionSet(psMap, createDbaPermissionSet());
		putPermissionSet(psMap, createCuratorPermissionSet());
		putPermissionSet(psMap, createEditorPermissionSet());
		putPermissionSet(psMap, createSelfEditorPermissionSet());
		this.map = Collections.unmodifiableMap(psMap);
	}

	private void putPermissionSet(Map<String, PermissionSet> psMap,
			PermissionSet ps) {
		psMap.put(ps.getUri(), ps);
	}

	private PermissionSet createDbaPermissionSet() {
		PermissionSet ps = new PermissionSet();
		ps.setUri("http://vivo.mydomain.edu/individual/role1");
		ps.setLabel("DBA");
		return ps;
	}

	private PermissionSet createCuratorPermissionSet() {
		PermissionSet ps = new PermissionSet();
		ps.setUri("http://vivo.mydomain.edu/individual/role2");
		ps.setLabel("Curator");
		return ps;
	}

	private PermissionSet createEditorPermissionSet() {
		PermissionSet ps = new PermissionSet();
		ps.setUri("http://vivo.mydomain.edu/individual/role3");
		ps.setLabel("Editor");
		return ps;
	}

	private PermissionSet createSelfEditorPermissionSet() {
		PermissionSet ps = new PermissionSet();
		ps.setUri("http://vivo.mydomain.edu/individual/role4");
		ps.setLabel("Self-Editor");
		return ps;
	}

	@Override
	public PermissionSet getPermissionSetByUri(String uri) {
		PermissionSet permissionSet = map.get(uri);
		if (permissionSet == null) {
			log.warn("Can't find a PermissionSet for uri '" + uri + "'");
		}
		return permissionSet;
	}

	@Override
	public Collection<PermissionSet> getAllPermissionSets() {
		return new ArrayList<PermissionSet>(map.values());
	}
}
