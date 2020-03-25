/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupsForRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.VClassGroupTemplateModel;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "browsecontroller", urlPatterns = {"/browse"}, loadOnStartup = 5)
public class BrowseController extends FreemarkerHttpServlet {
    static final long serialVersionUID=2006030721126L;

    private static final Log log = LogFactory.getLog(BrowseController.class);

    private static final String TEMPLATE_DEFAULT = "classGroups.ftl";

    @Override
    protected String getTitle(String siteName, VitroRequest vreq) {
        return "Index of Contents";
    }

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {

        Map<String, Object> body = new HashMap<String, Object>();
        String templateName = TEMPLATE_DEFAULT;

        List<VClassGroup> groups = null;
        VClassGroupsForRequest vcgc = VClassGroupCache.getVClassGroups(vreq);
        groups =vcgc.getGroups();
        Collections.sort(groups, publicNameComparator);
//        sortGroupListByPublicName(groups);
        List<VClassGroupTemplateModel> vcgroups = new ArrayList<VClassGroupTemplateModel>(groups.size());
        for (VClassGroup group : groups) {
            vcgroups.add(new VClassGroupTemplateModel(group));
        }
        body.put("classGroups", vcgroups);

        return new TemplateResponseValues(templateName, body);
    }
    public Comparator<VClassGroup> publicNameComparator = new Comparator<VClassGroup>() {

    	public int compare(VClassGroup s1, VClassGroup s2) {
    	   String groupName1 = s1.getPublicName().toUpperCase();
    	   String groupName2 = s2.getPublicName().toUpperCase();

    	   //ascending order
    	   return groupName1.compareTo(groupName2);

    	   //descending order
    	   //return groupName2.compareTo(groupName1);
        }};

//
//    public void sortGroupListByPublicName(List<VClassGroup> groupList) {
//        groupList.sort(new Comparator<VClassGroup>() {
//            public int compare(VClassGroup first, VClassGroup second) {
//                if (first != null) {
//                    if (second != null) {
//                        return (first.getDisplayRank() - second.getDisplayRank());
//                    } else {
//                        log.error("error--2nd VClassGroup is null in VClassGroupDao.getGroupList().compare()");
//                    }
//                } else {
//                    log.error("error--1st VClassGroup is null in VClassGroupDao.getGroupList().compare()");
//                }
//                return 0;
//            }
//        });
//    }

}
