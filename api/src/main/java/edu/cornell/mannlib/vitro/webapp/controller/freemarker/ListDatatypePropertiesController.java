/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Datatype;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.DatatypeDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.utils.json.JacksonUtils;
import edu.cornell.mannlib.vitro.webapp.web.URLEncoder;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "ListDatatypePropertiesController", urlPatterns = {"/listDatatypeProperties"} )
public class ListDatatypePropertiesController extends FreemarkerHttpServlet {

    private static Log log = LogFactory.getLog( ListDatatypePropertiesController.class );

    private static final String TEMPLATE_NAME = "siteAdmin-objectPropHierarchy.ftl";

    @Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
		return SimplePermission.EDIT_ONTOLOGY.ACTION;
	}

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {

        Map<String, Object> body = new HashMap<String, Object>();
        try {

            body.put("displayOption", "all");
            body.put("pageTitle", "All Data Properties");
            body.put("propertyType", "data");

            String noResultsMsgStr = "No data properties found";

            String ontologyUri = vreq.getParameter("ontologyUri");

            DataPropertyDao dao = vreq.getUnfilteredWebappDaoFactory().getDataPropertyDao();
            DataPropertyDao dpDaoLangNeut = vreq.getLanguageNeutralWebappDaoFactory().getDataPropertyDao();
            VClassDao vcDao = vreq.getUnfilteredWebappDaoFactory().getVClassDao();
            VClassDao vcDaoLangNeut = vreq.getLanguageNeutralWebappDaoFactory().getVClassDao();
            DatatypeDao dDao = vreq.getUnfilteredWebappDaoFactory().getDatatypeDao();
            PropertyGroupDao pgDao = vreq.getUnfilteredWebappDaoFactory().getPropertyGroupDao();

            List<DataProperty> props = new ArrayList<DataProperty>();
            if (vreq.getParameter("propsForClass") != null) {
                noResultsMsgStr = "There are no data properties that apply to this class.";
                Collection <DataProperty> dataProps = vreq.getLanguageNeutralWebappDaoFactory()
                        .getDataPropertyDao().getDataPropertiesForVClass(
                                vreq.getParameter("vclassUri"));
                Iterator<DataProperty> dataPropIt = dataProps.iterator();
                HashSet<String> propURIs = new HashSet<String>();
                while (dataPropIt.hasNext()) {
                    DataProperty dp = dataPropIt.next();
                    if (!(propURIs.contains(dp.getURI()))) {
                        propURIs.add(dp.getURI());
                        DataProperty prop = dao.getDataPropertyByURI(dp.getURI());
                        if (prop != null) {
                            props.add(prop);
                        }
                    }
                }
            } else {
        	    props = dao.getAllDataProperties();
            }

            if (ontologyUri != null) {
                List<DataProperty> scratch = new ArrayList<DataProperty>();
                for (DataProperty p: props) {
                    if (p.getNamespace().equals(ontologyUri)) {
                        scratch.add(p);
                    }
                }
                props = scratch;
            }

            if (props != null) {
        	    sortForPickList(props, vreq);
            }

            StringBuilder json = new StringBuilder();
            int counter = 0;

            if (props != null) {
                if (props.size()==0) {
                    json = new StringBuilder("{ \"name\": \"" + noResultsMsgStr + "\" }");
                } else {
            	    for (DataProperty prop: props) {
                        if ( counter > 0 ) {
                            json.append(", ");
                        }

                        String nameStr = prop.getPickListName()==null ? prop.getName()==null ? prop.getURI()==null ? "(no name)" : prop.getURI() : prop.getName() : prop.getPickListName();

                        try {
                            json.append("{ \"name\": ").append(JacksonUtils.quote("<a href='datapropEdit?uri=" + URLEncoder.encode(prop.getURI()) + "'>" + nameStr + "</a>")).append(", ");
                        } catch (Exception e) {
                            json.append("{ \"name\": ").append(JacksonUtils.quote(nameStr)).append(", ");
                        }

                        json.append("\"data\": { \"internalName\": ").append(JacksonUtils.quote(prop.getPickListName())).append(", ");

/*                        VClass vc = null;
                        String domainStr="";
                        if (prop.getDomainClassURI() != null) {
                            vc = vcDao.getVClassByURI(prop.getDomainClassURI());
                            if (vc != null) {
                                try {
                                    domainStr="<a href=\"vclassEdit?uri="+URLEncoder.encode(prop.getDomainClassURI(),"UTF-8")+"\">"+vc.getName()+"</a>";
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
*/
                        DataProperty dpLangNeut = dpDaoLangNeut.getDataPropertyByURI(prop.getURI());
                        if(dpLangNeut == null) {
                            dpLangNeut = prop;
                        }
                        String domainStr = getVClassNameFromURI(dpLangNeut.getDomainVClassURI(), vcDao, vcDaoLangNeut);
                        json.append("\"domainVClass\": ").append(JacksonUtils.quote(domainStr)).append(", ");

                        Datatype rangeDatatype = dDao.getDatatypeByURI(prop.getRangeDatatypeURI());
                        String rangeDatatypeStr = (rangeDatatype==null)?prop.getRangeDatatypeURI():rangeDatatype.getName();
                        json.append("\"rangeVClass\": ").append(JacksonUtils.quote(rangeDatatypeStr)).append(", ");

                        if (prop.getGroupURI() != null) {
                            PropertyGroup pGroup = pgDao.getGroupByURI(prop.getGroupURI());
                            json.append("\"group\": ").append(JacksonUtils.quote((pGroup == null) ? "unknown group" : pGroup.getName())).append(" } } ");
                        } else {
                             json.append("\"group\": \"unspecified\" } }");
                        }
                        counter += 1;
                    }
                }
                body.put("jsonTree", json.toString());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return new TemplateResponseValues(TEMPLATE_NAME, body);
    }

    private String getVClassNameFromURI(String vclassURI, VClassDao vcDao, VClassDao vcDaoLangNeut) {
        if(vclassURI == null) {
            return "";
        }
        VClass vclass = vcDaoLangNeut.getVClassByURI(vclassURI);
        if(vclass == null) {
            return "";
        }
        if(vclass.isAnonymous()) {
            return vclass.getPickListName();
        } else {
            VClass vclassWLang = vcDao.getVClassByURI(vclassURI);
            return (vclassWLang != null) ? vclassWLang.getPickListName() : vclass.getPickListName();
        }
    }
}
