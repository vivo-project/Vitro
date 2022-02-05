package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_UNION;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.DefaultAction;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader;

public class ActionPool {

	private Map<String,Action> actions;
	private ServletContext ctx;
	private ConfigurationBeanLoader loader;
	private static ActionPool INSTANCE = null;
 	private static final Log log = LogFactory.getLog(ActionPool.class);
	private ContextModelAccess modelAccess;
	private OntModel dynamicAPIModel;

	private ActionPool(){
		this.actions = new HashMap<String,Action>();
		INSTANCE = this;
	}
	
	public static ActionPool getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ActionPool();
		}
		return INSTANCE;
	}
	
	public Action getByName(String name) {
		Action action = actions.get(name);
		if (action == null) {
			action = new DefaultAction();
		}
		return action;
	}
	
	public void printActionNames() {
		for (Map.Entry<String,Action> entry : actions.entrySet()) {
			log.debug("Action in pool: '" + entry.getKey() + "'");
		}
	}
	
	public void add(Set<Action> actionSet) {
		for (Action action : actionSet) {
			actions.put(action.getName(), action);
		}
	}
	
	public void reload() {
		if (ctx == null ) {
			log.error("Context is null. Can't reload action pool.");
			return;
		}
		if (loader == null ) {
			log.error("Loader is null. Can't reload action pool.");
			return;
		}
		for (Map.Entry<String,Action> entry : actions.entrySet()) {
			entry.getValue().dereference();
		}
		actions.clear();
		loadActions();
	}

	public void init(ServletContext ctx) {
		this.ctx = ctx;
		modelAccess = ModelAccess.on(ctx);
		dynamicAPIModel = modelAccess.getOntModel(FULL_UNION);
		loader = new ConfigurationBeanLoader(	dynamicAPIModel, ctx);
		log.debug("Context Initialization ...");
		loadActions();
	}

	private void loadActions() {
		Set<Action> actions = loader.loadEach(Action.class);
		log.debug("Context Initialization. actions loaded: " + actions.size());
		add(actions);
		log.debug("Context Initialization finished");
	}

}
