/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.flags.FlagException;
import edu.cornell.mannlib.vitro.webapp.controller.FreeMarkerHttpServlet;

public class IndividualListControllerFM extends FreeMarkerHttpServlet {

    long startTime = -1;
    
    private static final Log log = LogFactory.getLog(IndividualListControllerFM.class.getName());
    private VClass vclass = null;

    /**
     * This generates a list of entities and then sends that
     * off to a jsp to be displayed.
     *
     * Expected parameters:
     *
     * Expected Attributes:
     * entity - set to entity to display properties for.
     *
     * @author bdc34
     */
    
    // TODO Rewrite error cases to use FreeMarker templates. Restructure so we're always doing the body
    // and then calling writeOutput().
    public void doGet( HttpServletRequest req, HttpServletResponse res )
    throws IOException, ServletException {
        startTime = System.currentTimeMillis(); // TODO: remove
        try {
            super.doSetup(req, res);
            Object obj = vreq.getAttribute("vclass");
            vclass=null;
            if( obj == null ) { // look for vitroclass id parameter
                String vitroClassIdStr=req.getParameter("vclassId");
                if (vitroClassIdStr!=null && !vitroClassIdStr.equals("")) {
                    try {
                            //TODO have to change this so vclass's group and entity count are populated
                            vclass = vreq.getWebappDaoFactory().getVClassDao().getVClassByURI(vitroClassIdStr);
                            if (vclass == null) {
                                log.error("Couldn't retrieve vclass "+vitroClassIdStr);
                                response.sendRedirect(Controllers.BROWSE_CONTROLLER+"-freemarker?"+vreq.getQueryString());
                            }
                        } catch (Exception ex) {
                            throw new HelpException("EntityListControllerFM: request parameter 'vclassId' must be a URI string");
                    }
                }
            } else if (obj instanceof VClass) {
                vclass = (VClass)obj;
            } else {
                throw new HelpException("EntityListControllerFM: attribute 'vclass' must be of type "
                        + VClass.class.getName() );
            }
            if (vclass!=null){
                doBody();                
                writeOutput(response);
            }
        // RY Rewrite error cases for FreeMarker, not JSP
        } catch (HelpException help){
            doHelp(response);
        } catch (Throwable e) {
            vreq.setAttribute("javax.servlet.jsp.jspException",e);
            RequestDispatcher rd = req.getRequestDispatcher("/error.jsp");
            rd.forward(vreq, response);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException,IOException {
        doGet(request, response);
    }

    protected String getBody() {
 
        Map<String, Object> body = new HashMap<String, Object>();
        
        //get list of entities
        List<Individual> entities = vreq.getWebappDaoFactory().getIndividualDao().getIndividualsByVClass(vclass);
        body.put("entities",entities);
        
        // But the JSP version includes url rewriting via URLRewritingHttpServletResponse
        body.put("entityUrl", config.getSharedVariable("contextPath") + "/entity?home=" + config.getSharedVariable("portalId") + "&uri=");

        if (entities == null) {
            log.error("entities list is null");
        }

        // Use instead of getTitle(), because we have a subtitle too
        String title = "";
        VClassGroup classGroup=vclass.getGroup();
        if (classGroup==null) {
            title = vclass.getName();
        } else {
            title = classGroup.getPublicName();
            setSharedVariable("subTitle", vclass.getName());
        }
        setSharedVariable("title", title);

        String templateName = "entityList.ftl";
        return mergeBodyToTemplate(templateName, body);

    }

    // RY Rewrite as a template
    private void doHelp(HttpServletResponse res)
    throws IOException, ServletException {
        ServletOutputStream out = res.getOutputStream();
        res.setContentType("text/html; charset=UTF-8");
        out.println("<html><body><h2>Quick Notes on using EntityList:</h2>");
        out.println("<p>request.attributes 'entities' must be set by servlet before calling."
                +" It must be a List of Entity objects </p>");
        out.println("</body></html>");
    }

    private class HelpException extends Throwable{
        public HelpException(String string) {
            super(string);
        }
    }
}
