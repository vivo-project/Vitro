/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.utils.NamespaceMapper;
import edu.cornell.mannlib.vitro.webapp.utils.NamespaceMapperFactory;

public class EntityWebUtils {
	
	private static final Log log = LogFactory.getLog(EntityWebUtils.class.getName());

    /**
        Gets the entity id from the request.
        Works for entities.id and NetId's
        @return -1 on faliure.

        This is copied from EntityServlet.java.
        Maybe this should be in package like vivo.web.EntityWebUtil?
    */
    public static Individual getEntityFromRequest(VitroRequest vreq) {
        String netIdStr = null;
        Individual entity = null;
        IndividualDao iwDao = vreq.getWebappDaoFactory().getIndividualDao();

        String entityIdStr = vreq.getParameter("id");
        if (entityIdStr == null || entityIdStr.equals(""))
            entityIdStr = vreq.getParameter("entityId");

        if( entityIdStr != null){
            try {
                String entityURI = vreq.getWebappDaoFactory().getDefaultNamespace()+"individual"+entityIdStr;
                entity = iwDao.getIndividualByURI(entityURI);
            } catch ( Exception e ) {
                log.error("Could not parse entity id: " + entityIdStr);
                return null; //some error message to logs here
            }
            return entity;
        }

        String entityURIStr = vreq.getParameter("uri");
        if (entityURIStr != null) {
            try {
                entity = iwDao.getIndividualByURI(entityURIStr);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Could not retrieve entity "+entityURIStr);
                return null;
            }
            return entity;
        }

        netIdStr = vreq.getParameter("netId");      //so we try to get the netid
        if (netIdStr==null || netIdStr.equals(""))
            netIdStr = vreq.getParameter("netid");
        if ( netIdStr != null ){
            String uri = iwDao.getIndividualURIFromNetId(netIdStr);
            return iwDao.getIndividualByURI(uri);
        }

        // see if we can get the URI from a namespace prefix and a local name
		String requestURI = vreq.getRequestURI();
		String[] requestParts = requestURI.split("/individual/");
		String[] URIParts = requestParts[1].split("/");
		String namespace = "";
		NamespaceMapper namespaceMapper = NamespaceMapperFactory.getNamespaceMapper(vreq.getSession().getServletContext());
		String t;
		namespace = ( (t = namespaceMapper.getNamespaceForPrefix(URIParts[0])) != null) ? t : "";
		String localName = URIParts[1];
		return vreq.getWebappDaoFactory().getIndividualDao().getIndividualByURI(namespace+localName);

//        entityIdStr = vreq.getParameter("adw");
//        if (entityIdStr != null && !entityIdStr.equals("")){
//            return vreq.getWebappDaoFactory.getIndividualDao().getIndividualByExternalId(ExternalIdDaoDb.ADW_DEPT_ID,entityIdStr);
//        }
//        entityIdStr = vreq.getParameter("ohr");
//        if (entityIdStr != null && !entityIdStr.equals("")){
//            return vreq.getWebappDaoFactory.getIndividualDao().getIndividualByExternalId(ExternalIdDaoDb.OHR_DEPT_ID,entityIdStr);
//        }
//        entityIdStr = vreq.getParameter("osp");
//        if (entityIdStr!=null && !entityIdStr.equals("")){
//            return vreq.getWebappDaoFactory.getIndividualDao().getIndividualByExternalId(ExternalIdDaoDb.OSP_DEPT_ID,entityIdStr);
//        }
        //return null;
    }

    private static String DEFAULT_THUMB_IMG_WIDTH = "100";
    private static String IMG_DIR = "images/";
    private static String NO_IMG_ALT = "no picture available";
}
