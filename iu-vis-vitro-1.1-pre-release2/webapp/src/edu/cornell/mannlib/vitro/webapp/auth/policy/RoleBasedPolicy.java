/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DefaultInconclusivePolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AddDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AddObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AddResource;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.DropDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.DropObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.DropResource;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.AddNewUser;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.LoadOntology;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.RebuildTextIndex;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.RemoveUser;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.ServerStatus;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.UpdateTextIndex;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.UploadFile;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.CreateOwlClass;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.DefineDataProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.DefineObjectProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.RemoveOwlClass;

/**
 * Policy that mimics the authorization roles of the old system.  So each
 * principle that is to be authorized needs to be associated with an individual
 * in the model and the individual needs to have a ObjectPropertyStatement between it and 
 * a authorization role.
 * 
 * ex.
 * 
 * vivo:indvidual23323 vivo:cornellNetId "bdc34".
 * vivo:indvidual22323 vitro:authRole <role://50>.
 * 
 * Notice that this policy doesn't need setup because it will look for
 * an authorization role identifier in the model.
 *
 * @author bdc34
 *
 */
public class RoleBasedPolicy extends DefaultInconclusivePolicy  implements PolicyIface {
    private static final Log log = LogFactory.getLog(RoleBasedPolicy.class.getName());

    /**
     * What is the minimum AuthRole needed to perform a given action?
     */
    private static Map<Class,AuthRole> actionToMinRole = new HashMap<Class,AuthRole>();
    static{
        //anybody actions
        //view resources?

        //user actions
        //allow netid authenticated people to do things?

        //edit actions
        actionToMinRole.put(AddDataPropStmt.class, AuthRole.EDITOR);
        actionToMinRole.put(AddObjectPropStmt.class, AuthRole.EDITOR);
        actionToMinRole.put(AddResource.class, AuthRole.EDITOR);
        actionToMinRole.put(DropDataPropStmt.class, AuthRole.EDITOR);
        actionToMinRole.put(DropObjectPropStmt.class, AuthRole.EDITOR);
        actionToMinRole.put(DropResource.class, AuthRole.EDITOR);
        actionToMinRole.put(UploadFile.class, AuthRole.EDITOR);
        actionToMinRole.put(ServerStatus.class, AuthRole.EDITOR);
        actionToMinRole.put(UpdateTextIndex.class, AuthRole.EDITOR);
        //curator actions
        actionToMinRole.put(DefineDataProperty.class, AuthRole.CURATOR);
        actionToMinRole.put(DefineObjectProperty.class, AuthRole.CURATOR);
        actionToMinRole.put(CreateOwlClass.class, AuthRole.CURATOR);
        actionToMinRole.put(RemoveOwlClass.class, AuthRole.CURATOR);
        //dba actions (dba role is allowed to do anything)
        actionToMinRole.put(AddNewUser.class, AuthRole.DBA);
        actionToMinRole.put(LoadOntology.class, AuthRole.DBA);
        actionToMinRole.put(RemoveUser.class, AuthRole.DBA);
        actionToMinRole.put(RebuildTextIndex.class, AuthRole.DBA);
    };

    public PolicyDecision isAuthorized(IdentifierBundle whomToAuth, RequestedAction whatToAuth) {
        if( whomToAuth == null ){
            log.error( "null was passed as whoToAuth" );
            return new BasicPolicyDecision(Authorization.INCONCLUSIVE,"null was passed as whoToAuth");
        }
        if( whatToAuth == null ){
            log.error("null was passed as whatToAuth");
            return new BasicPolicyDecision(Authorization.INCONCLUSIVE,"null was passed as whatToAuth");
        }

        //dba can do anything
        if( AuthRole.DBA.thisRoleOrGreater(whomToAuth))
            return new BasicPolicyDecision(Authorization.AUTHORIZED,"in DBA role");

        //We need to find the class of the RequestedAction since that
        //encodes what type of action is being requested.
        Class requesetClass = whatToAuth.getClass();
        AuthRole minmumRoleForAction = actionToMinRole.get(requesetClass);

        if( minmumRoleForAction == null ){
            String msg = "no minimum role found for action " + whatToAuth.getClass().getName();
            log.error(msg);
            return new BasicPolicyDecision(Authorization.UNAUTHORIZED,msg);
        }

        if( minmumRoleForAction.thisRoleOrGreater(whomToAuth) )
            return new BasicPolicyDecision(Authorization.AUTHORIZED,"authorized for role");
        else
            return new BasicPolicyDecision(Authorization.UNAUTHORIZED,"not authorized for role");
    }

    /**
     * Because it extends AbstractPolicySetup and implements this method, RoleBasedPolicy
     * can be used as a ServletContextListener that puts its self on the ServletPolicyList
     * at servlet context initialization.
     *
     * Notice that this method also setups the IdentifierBundleFactory that it needs.
     */
//    @Override
//    public List<PolicyIface> createPolicies(ServletContextEvent sce) {
//        List<PolicyIface> list = new ArrayList<PolicyIface>(1);
//        list.add(new RoleBasedPolicy());
//
//        //notice that the idBundleFactory gets created here,
//        JenaRoleIdentifierBundleFactory jibf = new JenaRoleIdentifierBundleFactory(userModelUri);
//        ServletIdentifierBundleFactory.addIdentifierBundleFactory(sce.getServletContext(),jibf);
//
//        return list;
//    }

    /********************** Roles *****************************************/
    public static enum AuthRole implements Identifier {
//        ANYBODY("http://vitro.mannlib.cornell.edu/authRole#anybody",0),
//        USER("http://vitro.mannlib.cornell.edu/authRole#user",1),
//        EDITOR("http://vitro.mannlib.cornell.edu/authRole#editor",2),
//        CURATOR("http://vitro.mannlib.cornell.edu/authRole#curator",3),
//        DBA("http://vitro.mannlib.cornell.edu/authRole#dba",50);

        ANYBODY( "role:/0" ,LoginFormBean.ANYBODY),
        USER(    "role:/1" ,LoginFormBean.NON_EDITOR),
        EDITOR(  "role:/4" ,LoginFormBean.EDITOR),
        CURATOR( "role:/5" ,LoginFormBean.CURATOR),
        DBA(     "role:/50",LoginFormBean.DBA);

        private final String roleUri;
        private final int level;

        AuthRole(String uri, int level) {
            this.roleUri = uri;
            this.level = level;
        }

        public String roleUri()   { return roleUri; }
        public int level() {return level;}

        /** returns null if not found */
        public static AuthRole convertUriToAuthRole(String uri){
            for( AuthRole role : AuthRole.values()){
                if( role.roleUri().equals( uri ))
                    return role;
            }
            return null;
        }

        public boolean thisRoleOrGreater(IdentifierBundle ibundle){
            if( ibundle == null )
                return false;
            for(Object obj : ibundle){
                if( obj instanceof AuthRole &&
                    ((AuthRole)obj).level() >= this.level())
                    return true;
            }
            return false;
        }
    }/* end of enum AuthRole */
}/* end of class RoleBasedPolicy */
