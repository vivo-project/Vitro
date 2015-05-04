/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;

/**
 * A request-based image of the VClassGroupCache. That means that the names of
 * the classes and groups are in the correct language for this request.
 */
public class VClassGroupsForRequest {
	private static final Log log = LogFactory
			.getLog(VClassGroupsForRequest.class);

	private final HttpServletRequest req;
	private final Map<String, VClassGroup> groupMap = new LinkedHashMap<>();
	private final Map<String, VClass> classMap = new HashMap<>();

	public VClassGroupsForRequest(HttpServletRequest req, VClassGroupCache cache) {
		this.req = req;

		for (VClassGroup vcGroup : cache.getGroups()) {
			loadGroup(vcGroup);
		}
		
		if (log.isDebugEnabled()) {
			log.debug("groups: " + groupMap.values());
			log.debug("classes: " + classMap.values());
		}
	}

	private void loadGroup(VClassGroup vcg) {
		VClassGroup newVcg = new VClassGroup(vcg);
		newVcg.setPublicName(getNameForGroup(vcg));
		groupMap.put(newVcg.getURI(), newVcg);

		for (VClass vClass : vcg) {
			loadClass(vClass, newVcg);
		}
	}

	private String getNameForGroup(VClassGroup vcGroup) {
		VClassGroup g = ModelAccess.on(req).getWebappDaoFactory()
				.getVClassGroupDao().getGroupByURI(vcGroup.getURI());
		return (g == null) ? vcGroup.getPublicName() : g.getPublicName();
	}

	private void loadClass(VClass vc, VClassGroup newVcg) {
		VClass newVc = vc.copy();
		newVc.setName(getNameForVClass(vc));
		newVc.setGroup(newVcg);
		newVcg.add(newVc);
		classMap.put(newVc.getURI(), newVc);
	}

	private String getNameForVClass(VClass vClass) {
		VClass vc = ModelAccess.on(req).getWebappDaoFactory().getVClassDao()
				.getVClassByURI(vClass.getURI());
		return (vc == null) ? vClass.getName() : vc.getName();
	}

	public VClassGroup getGroup(String vClassGroupURI) {
		VClassGroup vcg = groupMap.get(vClassGroupURI);
		log.debug("getGroup(" + vClassGroupURI + ") = " + vcg);
		return vcg;
	}

	public List<VClassGroup> getGroups() {
		ArrayList<VClassGroup> groups = new ArrayList<>(groupMap.values());
		log.debug("getGroups() returned " + groups.size() + " groups.");
		return groups;
	}

	public VClass getCachedVClass(String classUri) {
		VClass vc = classMap.get(classUri);
		log.debug("getCachedVClass(" + classUri + ") = " + vc);
		return vc;
	}

}
