/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import edu.cornell.mannlib.vitro.webapp.view.IndividualView;

public class IndividualListController extends FreeMarkerHttpServlet {

    long startTime = -1;
    
    private static final long serialVersionUID = 1L;   
    private static final Log log = LogFactory.getLog(IndividualListController.class.getName());
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
                                response.sendRedirect(Routes.BROWSE + "?"+vreq.getQueryString());
                            }
                        } catch (Exception ex) {
                            throw new HelpException("IndividualListController: request parameter 'vclassId' must be a URI string");
                    }
                }
            } else if (obj instanceof VClass) {
                vclass = (VClass)obj;
            } else {
                throw new HelpException("IndividualListController: attribute 'vclass' must be of type "
                        + VClass.class.getName() );
            }
            if (vclass!=null){
                setBody();                
                write(response);
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
        
        // Create list of individuals
        List<Individual> individualList = vreq.getWebappDaoFactory().getIndividualDao().getIndividualsByVClass(vclass);
        List<IndividualView> individuals = new ArrayList<IndividualView>(individualList.size());
        Iterator<Individual> i = individualList.iterator();
        while (i.hasNext()) {
            individuals.add(new IndividualView(i.next()));
        }        
        body.put("individuals", individuals);
        
        // But the JSP version includes url rewriting via URLRewritingHttpServletResponse
        // RY *** FIX  - define getUrl method of IndividualView
        body.put("individualUrl", getUrl("/entity?home=" + portalId + "&uri="));

        if (individuals == null) {
            log.error("individuals list is null");
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

        String templateName = "individualList.ftl";
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
