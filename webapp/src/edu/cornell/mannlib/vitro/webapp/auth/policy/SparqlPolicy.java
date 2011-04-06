/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

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

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DefaultInconclusivePolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;
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
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.EditDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.EditObjPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.resource.AddResource;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.resource.DropResource;

/**
 * This policy maps strings in the IdentifierBundle to a QuerySolutioinMap in order
 * to bind identifiers with unbound variables in SPARQL queries.
 * These queries are intended to specify the relations that allow authorization.
 * If the query return no rows will be interpreted as unauthorized and a
 * query returning one or more rows will be interpreted as authorized.
 *
 * @author bdc34
 *
 */
public class SparqlPolicy extends DefaultInconclusivePolicy implements VisitingPolicyIface{
    protected  Model model = ModelFactory.createDefaultModel();
    private  HashMap<String,Query> queryStrToQuery = new HashMap<String,Query>();

    /** human readable name for this policy */
    protected String name="Unnamed Policy";

    /** prefixes for SPARQL queries. */
    protected String prefixes = "";

    /** The SPARQL queries.  They should all be of the type ASK */
    protected HashMap<String,List<String>> actionToQueryStr = new HashMap<String,List<String>>();

    /** Function to transform identifiers into a QuerySolutionMap    */
    private Ids2QueryBindings binder;

    private String resource = null;
    
    /**
     * Load XML policy files with this.getClass().getResourceAsStream()
     * Notice that / is the path separator and strings that lack
     * a leading slash are relative to the package of the this.getClass().
     */
    public SparqlPolicy(Model model, Ids2QueryBindings binder, String resource){
        if( model == null )
            throw new IllegalArgumentException("model must not be null.");                   
        if( binder == null )
            throw new IllegalArgumentException("binder must not be null.");
        if( resource == null )
            throw new IllegalArgumentException("resource must not be null.");
        
        this.model = model;
        this.binder = binder;
        this.resource  = resource;
        loadPolicy();        
    }
    
    public void loadPolicy(){
        InputStream policySpec = SparqlPolicy.class.getResourceAsStream(resource);
        XStream x = new XStream(new DomDriver());
        SparqlPolicy jnip =(SparqlPolicy) x.fromXML( policySpec );
        this.actionToQueryStr = jnip.actionToQueryStr;
        this.prefixes = jnip.prefixes;
        this.name = jnip.name;
        try{
            policySpec.close();
        }catch(Throwable th){}    
    }

    /* *********************** Methods ************************************ */
    @Override
	public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
            RequestedAction whatToAuth) {        
        if( whoToAuth == null )
            return new BasicPolicyDecision(Authorization.INCONCLUSIVE,"whoToAuth was null");
        if(whatToAuth == null)
            return new BasicPolicyDecision(Authorization.INCONCLUSIVE,"whatToAuth was null");
        List<String> queryStrs = actionToQueryStr.get(whatToAuth.getClass().getName());
        if( queryStrs == null || queryStrs.size() ==0 )
            return new BasicPolicyDecision(Authorization.INCONCLUSIVE,
                    "no queryies found for action" + whatToAuth.getClass().getName());

        //kick off the visitor pattern which generally just calls doQueries()
        return whatToAuth.accept(this, whoToAuth);
    }

    private PolicyDecision doQueries(List<String>queryStrs, IdentifierBundle ids, RequestedAction action){
        SparqlPolicyDecision pd = new SparqlPolicyDecision(Authorization.INCONCLUSIVE,"");
        List<QuerySolutionMap> bindings = binder.makeScopeBinding(ids, action);
        for( QuerySolutionMap scope: bindings ){
            for(String quStr : queryStrs){
                Query query = getQueryForQueryStr(quStr);
                pd.setQuery(query);
                QueryExecution qexec = QueryExecutionFactory.create(query, model, scope);
                pd.setQexec(qexec);
                boolean pathFound = qexec.execAsk();
                if( pathFound ){                   
                    pd.setAuthorized(Authorization.AUTHORIZED);
                    pd.setMessage(action.getClass().getName() + " permited by " + quStr);
                    if( log.isDebugEnabled()){
                        log.debug(action.getClass().getName() + " permited by " + quStr);
                        log.debug(query);
                    }
                    return pd;
                } else {
                    if( log.isDebugEnabled()){
                        log.debug(action.getClass().getName() + " no results for " + query);
                        log.debug(query);
                    }
                }
            }
        }
        return pd;
    }

    private Query getQueryForQueryStr(String queryStr){
        //memoize queries
        Query q = queryStrToQuery.get(queryStr);
        if( q == null ){
            q = QueryFactory.create(prefixes + queryStr);
            queryStrToQuery.put(queryStr, q);
        }
        return q;
    }

    /* ***************** Visit methods ********************** */
    private final String pkg = "edu.cornell.mannlib.vitro.webapp.auth.requestedAction.";
    @Override
	public PolicyDecision visit(IdentifierBundle ids, AddObjectPropStmt action) {
        return doQueries(actionToQueryStr.get(pkg +"AddObjectPropStmt"),ids,action);
    }

    @Override
	public PolicyDecision visit(IdentifierBundle ids, DropResource action) {
        return doQueries(actionToQueryStr.get(pkg +"DropResource"),ids,action);
    }

    @Override
	public PolicyDecision visit(IdentifierBundle ids, DropDataPropStmt action) {
        return doQueries(actionToQueryStr.get(pkg +"DropDataPropStmt"),ids,action);
    }

    @Override
	public PolicyDecision visit(IdentifierBundle ids, DropObjectPropStmt action) {
        return doQueries(actionToQueryStr.get(pkg +"DropObjectPropStmt"),ids,action);
    }

    @Override
	public PolicyDecision visit(IdentifierBundle ids, AddResource action) {
        return doQueries(actionToQueryStr.get(pkg +"AddResource"),ids,action);
    }

    @Override
	public PolicyDecision visit(IdentifierBundle ids, AddDataPropStmt action) {
        return doQueries(actionToQueryStr.get(pkg +"AddDataPropStmt"),ids,action);
    }

    @Override
	public PolicyDecision visit(IdentifierBundle ids, UploadFile action) {
        return doQueries(actionToQueryStr.get(pkg +"UploadFile"),ids,action);
    }


    @Override
	public PolicyDecision visit(IdentifierBundle ids, EditDataPropStmt action) {
        return doQueries(actionToQueryStr.get(pkg +"EditDataPropStmt"),ids,action);
    }

    @Override
	public PolicyDecision visit(IdentifierBundle ids, EditObjPropStmt action) {
        return doQueries(actionToQueryStr.get(pkg +"EditObjPropStmt"),ids,action);
    }

    /* **** Currently the following actions are unauthorized by this policy **** */
    @Override
	public PolicyDecision visit(IdentifierBundle ids, CreateOwlClass action) {
        return UNAUTH;
    }
    @Override
	public PolicyDecision visit(IdentifierBundle ids, RemoveOwlClass action) {
        return UNAUTH;
    }
    @Override
	public PolicyDecision visit(IdentifierBundle ids, DefineDataProperty action) {
        return UNAUTH;
    }
    @Override
	public PolicyDecision visit(IdentifierBundle ids,
            DefineObjectProperty action) {
        return UNAUTH;
    }
    @Override
	public PolicyDecision visit(IdentifierBundle ids, ServerStatus action) {
        return UNAUTH;
    }
    @Override
	public PolicyDecision visit(IdentifierBundle ids, AddNewUser action) {
        return UNAUTH;
    }
    @Override
	public PolicyDecision visit(IdentifierBundle ids, RemoveUser action) {
        return UNAUTH;
    }
    @Override
	public PolicyDecision visit(IdentifierBundle ids, LoadOntology action) {
        return UNAUTH;
    }
    @Override
	public PolicyDecision visit(IdentifierBundle ids, RebuildTextIndex action) {
        return UNAUTH;
    }
    @Override
	public PolicyDecision visit(IdentifierBundle ids, UpdateTextIndex action) {
        return UNAUTH;
    }

    private static final Log log = LogFactory.getLog(SparqlPolicy.class.getName());
    
    private final PolicyDecision UNAUTH = new PolicyDecision(){
        @Override
		public Authorization getAuthorized() {return Authorization.UNAUTHORIZED; }
        @Override
		public String getMessage() {
            return name + " SparqlPolicy doesn't authorize admin or onto editing actions";
        }
        @Override
		public String getDebuggingInfo() { return ""; }
        @Override
		public String getStackTrace() { return ""; }
    };

/*
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
 */
}
