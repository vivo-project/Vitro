/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;

/**
 * Tests and gives info about the auth sysetm
 *
 * @author bdc34
 *
 */
public class AuthTestController extends VitroHttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse res )
    throws IOException, ServletException{
        super.doGet(req,res);
        IdentifierBundle ids = RequestIdentifiers.getIdBundleForRequest(req);
        ServletOutputStream out = res.getOutputStream();

        listIdentifiers(out,ids);

        checkAuths(out,ids, getServletContext());

    }

    private void listIdentifiers(ServletOutputStream out, IdentifierBundle ids) throws IOException{
        out.println("<h1>Identifiers: </h1>");
        out.println("<table>");
        for( Object obj: ids){
            if( obj == null ){
                out.println("<tr>obj was null</tr>");
                continue;
            }
            out.println("<tr>");
            out.println("<td>"+obj.getClass().getName() + "</td>");
            out.println("<td>"+obj.toString() + "</td>");
            out.println("</tr>");
        }
        out.println("</table>");
    }


    private void checkAuths(ServletOutputStream out, IdentifierBundle ids, ServletContext servletContext)
    throws IOException{
        PolicyList policy = ServletPolicyList.getPolicies(servletContext);
        out.println("<h1>Authorization tests:</h1>");

		if (policy.isEmpty()) {
			out.println("No Policy objects found in ServletContext. ");
		}
        out.println("<table>");
        for(RequestedAction action: actions){
            out.println("<tr><td>"+action.getClass().getName()+"</td>");
            try {
                PolicyDecision pd = policy.isAuthorized(ids, action);
                if( pd == null)
                    out.println("<td>ERROR: PolicyDecision was null</td><td/>");
                else{
                    out.println("<td>"+ pd.getAuthorized() +"</td>");
                    out.println("<td>"+ pd.getMessage() +"</td>");
                }
            } catch (Exception e) {
                out.println("<td> exception: " + e + "</td>");
                e.printStackTrace();
            }
        }
        out.println("</table>");
    }


    private static List<RequestedAction> actions = new ArrayList<RequestedAction>();
    static{
        actions.add(new AddDataPropStmt("http://some.non.existing.resource", "http://some.non.existing.dataproperty", "bogus value", null, null));
        actions.add(new AddObjectPropStmt("http://vivo.library.cornell.edu/abox#entity11821","vitro:headOf","http://vivo.library.cornell.edu/abox#entity1"));
        actions.add(new AddObjectPropStmt("http://vivo.library.cornell.edu/abox#entity123","vitro:headOf","http://vivo.library.cornell.edu/abox#entity1"));

//        actions.add(new AddResource("http://bogus.REsourceType.uri","http://bogus.uri"));
//        actions.add(new DropObjectPropStmt());
//        actions.add(new DefineObjectProperty());
//        actions.add(new DefineDataProperty());
//        actions.add(new RemoveOwlClass());
//        actions.add(new CreateOwlClass());
//
//        actions.add(new AddNewUser());
//        actions.add(new LoadOntology());
//        actions.add(new RebuildTextIndex());
//        actions.add(new RemoveUser());
//        actions.add(new ServerStatus());
//        actions.add(new UpdateTextIndex());
//        actions.add(new UploadFile("http://uri.of.entity.to.associate/uploaded/file/with","http://uri.of.association.property"));
    }
}
