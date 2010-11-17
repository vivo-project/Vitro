/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ServletIdentifierBundleFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory.NetId;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DefaultInconclusivePolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AddDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AddObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AddResource;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.DropDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.DropObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.DropResource;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.EditDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.EditObjPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.AddNewUser;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.LoadOntology;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.RebuildTextIndex;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.RemoveUser;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.ServerStatus;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.UpdateTextIndex;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.UploadFile;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.AdminRequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.OntoRequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.CreateOwlClass;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.DefineDataProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.DefineObjectProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.RemoveOwlClass;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * This policy looks for a netid in the IdentifierBundle and will use that netid
 * as a anchor in SPARQL queries.  These queries are intended to specify the relations
 * that allow authorization.
 *
 * We could use things other than SPARQL.  Other possibilities:
 * Some java driven code that worked with the the jena Model
 * Fresnel Selector Language (FSL)
 * SWRL?
 *
 * example of how to set up the xml:
 *
 * <code>
<edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy>
  <name>Example Policy</name>
  <prefixes>PREFIX rdf: &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#&gt;
PREFIX rdfs: &lt;http://www.w3.org/2000/01/rdf-schema#&gt;
PREFIX vivoa: &lt;http://vivo.library.cornell.edu/abox#&gt;
PREFIX vivo: &lt;http://vivo.library.cornell.edu/ns/0.1#&gt;
PREFIX vitro: &lt;http://lowe.mannlib.cornell.edu/ns/vitro/0.1/vitro.owl#&gt;
</prefixes>
  <actionToQueryStr>
    <entry>
      <string>edu.cornell.mannlib.vitro.webapp.auth.requestedAction.DropDataPropStmt</string>
      <list>
        <string>ASK WHERE { ?subject  vitro:netid ?netid  }</string>
        <string>ASK WHERE { ?object   vitro:netid ?netid  }</string>
      </list>
    </entry>
    <entry>
      <string>edu.cornell.mannlib.vitro.webapp.auth.requestedAction.DropObjectPropStmt</string>
      <list>
        <string>ASK WHERE { ?subject  vitro:netid ?netid  }</string>
        <string>ASK WHERE { ?object   vitro:netid ?netid  }</string>
      </list>
    </entry>
    <entry>
      <string>edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AddObjectPropStmt</string>
      <list>
        <string>ASK WHERE { ?subject  vitro:netid ?netid  }</string>
        <string>ASK WHERE { ?object   vitro:netid ?netid  }</string>
      </list>
    </entry>
    <entry>
      <string>edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AddDataPropStmt</string>
      <list>
        <string>ASK WHERE { ?subject  vitro:netid ?netid  }</string>
        <string>ASK WHERE { ?object   vitro:netid ?netid  }</string>
      </list>
    </entry>
  </actionToQueryStr>
</edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy>
</code>

 * @author bdc34
 *
 */
public class JenaNetidPolicy extends DefaultInconclusivePolicy implements VisitingPolicyIface {


    protected  transient Model model = ModelFactory.createDefaultModel();
    private  transient HashMap<String,Query> queryStrToQuery = new HashMap<String,Query>();

    /** human readable name for this policy */
    protected String name="Unnamed Policy";

    /** prefixes for SPARQL queries. */
    protected String prefixes = DEFAULT_PREFIXES;

    /** Specifies the type of Authorization returned when the SPARQL query succeeds.  This allows us to
     * create a JenaNetidPolicy that returns UNAUTHORIZED when the some set of conditions are meet. */
    protected Authorization authForSuccessfulQuery = Authorization.AUTHORIZED;

    /** The SPARQL queries.  They should all be of the type ASK and
     * they should all have the variable ?netid  */
    protected HashMap<String,List<String>> actionToQueryStr = new HashMap<String,List<String>>();

    /* *************************** Constructors ******************************* */

    /**
     * See JenaNetidPolicy.setupDefault() for the sparql queries that will
     * be used by the default JenaNetidPolicy.
     */
    public JenaNetidPolicy(Model model){
        if( model == null ){
            this.model = ModelFactory.createDefaultModel();
        }else{
            this.model = model;
        }
        setupDefault();
    }

    /**
     * Loads sparql statements for policy from a JSON text file.
     *
     * @param model
     * @param sparqlStmts
     */
    public JenaNetidPolicy(Model model, InputStream policySpec){
        this(model, policySpec, Authorization.AUTHORIZED);
    }

    /*
     * Load xml policy files with this.getClass().getResourceAsStream()
     * Notice that / is the path seperator and strings that lack
     * a leading slash are relative to the package of the this.getClass().
     */
    public JenaNetidPolicy(Model model, String resource){
        this(model, JenaNetidPolicy.class.getResourceAsStream(resource));
    }

    public JenaNetidPolicy(Model model, InputStream policySpec, Authorization authForSuccessfulQuery){
        this.authForSuccessfulQuery = authForSuccessfulQuery;
        XStream x = new XStream(new DomDriver());
        //XStream x = new XStream();
        JenaNetidPolicy jnip =(JenaNetidPolicy) x.fromXML( policySpec );
        this.actionToQueryStr = jnip.actionToQueryStr;
        this.prefixes = jnip.prefixes;
        this.name = jnip.name;
        this.model = model;
    }

    /* *********************** Methods ************************************ */
    public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
            RequestedAction whatToAuth) {
        BasicPolicyDecision pd = new BasicPolicyDecision(Authorization.INCONCLUSIVE,"not yet set");
        if( whoToAuth == null )
            return pd.setMessage("whoToAuth was null");
        if(whatToAuth == null)
            return pd.setMessage("whatToAuth was null");

        String netid = getNetid(whoToAuth);
        if (netid == null)
            return pd.setMessage("Unable to get netid from IdBundle");

        //kick off the visitor pattern
        return whatToAuth.accept(this, whoToAuth);
    }

    /* ************************* visit methods ************************** */
    public PolicyDecision visit(IdentifierBundle ids, AddResource action) {
        log.debug("doing AddResource");

        List<String> queryStrs = actionToQueryStr.get(action.getClass().getName());
        if( queryStrs == null || queryStrs.size() ==0 )
            return new BasicPolicyDecision(Authorization.INCONCLUSIVE,
                    "no queryies found for action" + action.getClass().getName());

        QuerySolutionMap parameters = new QuerySolutionMap();
        parameters.add("netid",  model.createLiteral( getNetid(ids) ));
        parameters.add("subject",model.createResource( action.getSubjectUri() ));

        return doQueries(queryStrs,parameters,action);
    }

    public PolicyDecision visit(IdentifierBundle ids, DropResource action) {
        log.debug("doing DropResource");
        BasicPolicyDecision pd = new BasicPolicyDecision(Authorization.INCONCLUSIVE, "");

        List<String> queryStrs = actionToQueryStr.get(action.getClass().getName());
        if( queryStrs == null || queryStrs.size() ==0 )
            return new BasicPolicyDecision(Authorization.INCONCLUSIVE,
                    "no queryies found for action" + action.getClass().getName());

        QuerySolutionMap parameters = new QuerySolutionMap();
        parameters.add("netid",  model.createLiteral( getNetid(ids) ));
        parameters.add("subject",model.createResource( action.getSubjectUri() ));

        return doQueries(queryStrs,parameters,action);
    }

    public PolicyDecision visit(IdentifierBundle ids, AddObjectPropStmt action) {
        log.debug("doing AddObjectPropStmt in visit()");

        List<String> queryStrs = actionToQueryStr.get(action.getClass().getName());
        if( queryStrs == null || queryStrs.size() ==0 )
            return new BasicPolicyDecision(Authorization.INCONCLUSIVE,
                    "no queryies found for action" + action.getClass().getName());

        QuerySolutionMap parameters = new QuerySolutionMap();
        parameters.add("netid",  model.createLiteral( getNetid(ids) ));
        parameters.add("subject",model.createResource( action.getUriOfSubject() )) ;
        parameters.add("object", model.createResource( action.getUriOfObject()  )) ;
        parameters.add("predicate", model.createResource( action.getUriOfPredicate() )) ;

        return doQueries(queryStrs,parameters,action);
    }

    public PolicyDecision visit(IdentifierBundle ids, DropObjectPropStmt action) {
        log.debug("doing DropObjectPropStmt");

        List<String> queryStrs = actionToQueryStr.get(action.getClass().getName());
        if( queryStrs == null || queryStrs.size() ==0 )
            return new BasicPolicyDecision(Authorization.INCONCLUSIVE,
                    "no queryies found for action" + action.getClass().getName());

        QuerySolutionMap parameters = new QuerySolutionMap();
        parameters.add("netid",  model.createLiteral( getNetid(ids) ));
        parameters.add("subject",model.createResource( action.getUriOfSubject() )) ;
        parameters.add("object", model.createResource( action.getUriOfObject()  )) ;
        parameters.add("predicate", model.createResource( action.getUriOfPredicate() )) ;

        return doQueries(queryStrs,parameters,action);
    }

    public PolicyDecision visit(IdentifierBundle ids, AddDataPropStmt action) {
        log.debug("doing AddDataPropStmt");

        List<String> queryStrs = actionToQueryStr.get(action.getClass().getName());
        if( queryStrs == null || queryStrs.size() ==0 )
            return new BasicPolicyDecision(Authorization.INCONCLUSIVE,
                    "no queryies found for action" + action.getClass().getName());

        QuerySolutionMap parameters = new QuerySolutionMap();
        parameters.add("netid",  model.createLiteral( getNetid(ids) ));
        parameters.add("subject",model.createResource( action.getResourceUri() )) ;
        parameters.add("predicate", model.createResource( action.getDataPropUri() )) ;
        parameters.add("literalValue", model.createLiteral(action.getData() ));
        return doQueries(queryStrs,parameters,action);
    }

    public PolicyDecision visit(IdentifierBundle ids, DropDataPropStmt action) {
        log.debug("doing DropDataPropStmt");

        List<String> queryStrs = actionToQueryStr.get(action.getClass().getName());
        if( queryStrs == null || queryStrs.size() ==0 )
            return new BasicPolicyDecision(Authorization.INCONCLUSIVE,
                    "no queries found for action" + action.getClass().getName());

        QuerySolutionMap parameters = new QuerySolutionMap();
        parameters.add("netid",  model.createLiteral( getNetid(ids) ));
        parameters.add("subject",model.createResource( action.uriOfSubject() )) ;
        parameters.add("predicate", model.createResource( action.uriOfPredicate() )) ;
        parameters.add("literalValue", model.createLiteral(action.data() )); // caution: will always do untyped things
        return doQueries(queryStrs,parameters,action);
    }


    /* ******************************** utilities ****************************** */
    private PolicyDecision doQueries(List<String>queryStrs, QuerySolutionMap parameters, RequestedAction action){
        SparqlPolicyDecision pd = new SparqlPolicyDecision(Authorization.INCONCLUSIVE,"");
        for(String quStr : queryStrs){

            Query query = getQueryForQueryStr(quStr);
            pd.setQuery(query);
            QueryExecution qexec = QueryExecutionFactory.create(query, model, parameters);
            pd.setQexec(qexec);

            boolean pathFound = qexec.execAsk();
            if( pathFound ){
                pd.setAuthorized(authForSuccessfulQuery);
                pd.setMessage(action.getClass().getName() + " permited by " + quStr);
                if( log.isDebugEnabled()){
                    log.debug(action.getClass().getName() + " permited by " + quStr);
                    log.debug(query);
                }
                break;
            } else {
                if( log.isDebugEnabled()){
                    log.debug(action.getClass().getName() + " no results for " + query);
                    log.debug(query);
                }
            }
        }
        return pd;
    }

    private Query getQueryForQueryStr(String queryStr){
        Query q = queryStrToQuery.get(queryStr);
        if( q == null ){
            q = QueryFactory.create(prefixes + queryStr);
            queryStrToQuery.put(queryStr, q);
        }
        return q;
    }

    private QuerySolutionMap idsToSolutionMap(IdentifierBundle id){
        // right now we can only turn netids into QSMs
        String netid = getNetid(id);
        if( netid == null || netid.length() == 0)
            return null;

        QuerySolutionMap qsm = new QuerySolutionMap();
        Model m = ModelFactory.createDefaultModel();
        qsm.add("netid",m.createLiteral(netid));
        return qsm;
    }

    private String getNetid(IdentifierBundle whoToAuth) {
        String netidStr = null;
        for(Identifier id : whoToAuth){
            if (id instanceof NetId) {
                NetId netid = (NetId) id;
                netidStr = netid.getValue();
                break;
            }
        }
        if( log.isDebugEnabled() )
            log.debug("netid was " + (netidStr!=null?netidStr:"null") );
        return netidStr;
    }

    /**
     * An inner class used to setup everything that's needed for
     * a JenaNetidPolicy.  This setups the JenaNetidPolicy and a
     * SelfEditingIdentifierFactory.
     *
     * @author bdc34
     *
     */
    public class ContextSetup implements ServletContextListener {



        public void contextInitialized(ServletContextEvent sce) {
            try{
                log.trace("Setting up JenaNetidPolicy");

                Model model = (Model) sce.getServletContext().getAttribute("jenaOntModel");
                if( model == null ){
                    log.error("could not get jenaOntModel from JenaBaseDao, JenaNetidPolicy will not work");
                }

                JenaNetidPolicy jnip = new JenaNetidPolicy(model);
                ServletPolicyList spl = ServletPolicyList.getPolicies(sce.getServletContext());
                spl.add(jnip);

                SelfEditingIdentifierFactory niif  =new SelfEditingIdentifierFactory();
                ServletIdentifierBundleFactory.addIdentifierBundleFactory(sce.getServletContext(), niif);
            }catch(Exception e){
                log.error("could not create AuthorizationFactory: " + e);
                e.printStackTrace();
            }
        }
        public void contextDestroyed(ServletContextEvent sce) { /*nothing*/  }

    }

    private void setupDefault(){
        // --- AddObjectPropStmt ---
        // may have 4 parameters: netid, object, predicate, and subject.
        ArrayList <String> queries = new ArrayList<String>();
        queries.add( "ASK WHERE { ?subject  vitro:netid ?netid  }");
        queries.add( "ASK WHERE { ?object   vitro:netid ?netid  }");
        actionToQueryStr.put( AddObjectPropStmt.class.getName(), queries);
        // --- DropObjectPropStmt ---
        queries = new ArrayList<String>();
        queries.add( "ASK WHERE { ?subject  vitro:netid ?netid  }");
        queries.add( "ASK WHERE { ?object   vitro:netid ?netid  }");
        actionToQueryStr.put( DropObjectPropStmt.class.getName(), queries);

        // --- DropDataPropStmt ---
        queries = new ArrayList<String>();
        queries.add( "ASK WHERE { ?subject  vitro:netid ?netid  }");
        queries.add( "ASK WHERE { ?object   vitro:netid ?netid  }");
        actionToQueryStr.put( DropDataPropStmt.class.getName(), queries);
        // --- AddDataPropStmt ---
        queries = new ArrayList<String>();
        queries.add( "ASK WHERE { ?subject  vitro:netid ?netid  }");
        queries.add( "ASK WHERE { ?object   vitro:netid ?netid  }");
        actionToQueryStr.put( AddDataPropStmt.class.getName(), queries);

        // --- DropResource ---
        queries = new ArrayList<String>();
        queries.add( "ASK WHERE { ?subject  vitro:netid ?netid  }");
        queries.add( "ASK WHERE { ?object   vitro:netid ?netid  }");
        actionToQueryStr.put( DropObjectPropStmt.class.getName(), queries);
        // --- AddResource ---
        queries = new ArrayList<String>();
        queries.add( "ASK WHERE { ?subject  vitro:netid ?netid  }");
        queries.add( "ASK WHERE { ?object   vitro:netid ?netid  }");
        actionToQueryStr.put( DropObjectPropStmt.class.getName(), queries);
    }

    public final static String netIdPropUri = VitroVocabulary.vitroURI+ "netid";
    private static final Log log = LogFactory.getLog(JenaNetidPolicy.class.getName());
    public final static String DEFAULT_PREFIXES =
    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
    "PREFIX vivoa: <http://vivo.library.cornell.edu/abox#>\n"+
    "PREFIX vivo: <http://vivo.library.cornell.edu/ns/0.1#>\n"+
    "PREFIX vitro: <"+ VitroVocabulary.vitroURI+">\n";

    public PolicyDecision visit(IdentifierBundle ids, CreateOwlClass action) {
        return UNAUTH;
    }

    public PolicyDecision visit(IdentifierBundle ids, RemoveOwlClass action) {
        return UNAUTH;
    }

    public PolicyDecision visit(IdentifierBundle ids, DefineDataProperty action) {
        return UNAUTH;
    }

    public PolicyDecision visit(IdentifierBundle ids, DefineObjectProperty action){
        return UNAUTH;
    }

    public PolicyDecision visit(IdentifierBundle ids, AddNewUser action) {
        return UNAUTH;
    }

    public PolicyDecision visit(IdentifierBundle ids, RemoveUser action) {
        return UNAUTH;
    }

    public PolicyDecision visit(IdentifierBundle ids, LoadOntology action) {
        return UNAUTH;
    }

    public PolicyDecision visit(IdentifierBundle ids, RebuildTextIndex action) {
        return UNAUTH;
    }

    public PolicyDecision visit(IdentifierBundle ids, UpdateTextIndex action) {
        return UNAUTH;
    }

    public PolicyDecision visit(IdentifierBundle ids, UploadFile action) {
        return UNAUTH;
    }

    public PolicyDecision visit(IdentifierBundle ids, ServerStatus action) {
        return UNAUTH;
    }

    public PolicyDecision visit(IdentifierBundle ids, EditDataPropStmt action) {
        return UNAUTH;
    }

    public PolicyDecision visit(IdentifierBundle ids, EditObjPropStmt action) {
        return UNAUTH;
    }

    private final PolicyDecision UNAUTH = new PolicyDecision(){
        public Authorization getAuthorized() {return Authorization.UNAUTHORIZED; }
        public String getMessage() {
            return "JenaNetidPolicy doesn't authorize admin or onto editing actions";
        }
        public String getDebuggingInfo() { return ""; }
        public String getStackTrace() { return ""; }
    };
}
