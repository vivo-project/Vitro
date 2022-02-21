package edu.cornell.mannlib.vitro.webapp.dynapi;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_UNION;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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

	private static final Log log = LogFactory.getLog(ActionPool.class);

	private static ActionPool INSTANCE = null;
	
	private static Object mutex = new Object();
	
	private ConcurrentHashMap<String, Action> actions;
	private ServletContext ctx;
	private ConfigurationBeanLoader loader;
	private ContextModelAccess modelAccess;
	private OntModel dynamicAPIModel;
	private ConcurrentLinkedQueue<Action> obsoleteActions;

	private ActionPool(){
		actions = new ConcurrentHashMap<>();
		obsoleteActions = new ConcurrentLinkedQueue<>();
		INSTANCE = this;
	}
	
	public static ActionPool getInstance() {
		ActionPool result = INSTANCE;
		if (result == null) {
			synchronized (mutex) {
				result = INSTANCE;
				if (result == null) {
					INSTANCE = new ActionPool();
					result = INSTANCE;
				}
			}
		}
		return result;
	}
	
	/**
	 * Returns an action and registers current thread as action client
	 * @param name
	 * @return action
	 */
	public Action getByName(String name) {
		Action action = actions.get(name);
		if (action == null) {
			action = new DefaultAction();
		} else {
			action.addClient();
		}
		return action;
	}
	
	public void printActionNames() {
		for (Map.Entry<String, Action> entry : actions.entrySet()) {
			log.debug("Action in pool: '" + entry.getKey() + "'");
		}
	}
	
	public synchronized void reload() {
		if (ctx == null ) {
			log.error("Context is null. Can't reload action pool.");
			return;
		}
		if (loader == null ) {
			log.error("Loader is null. Can't reload action pool.");
			return;
		}
		ConcurrentHashMap<String, Action> newActions = new ConcurrentHashMap<>();
		loadActions(newActions);
		ConcurrentHashMap<String, Action> oldActions = this.actions;
		actions = newActions;
		for (Map.Entry<String, Action> action : oldActions.entrySet()) {
			obsoleteActions.add(action.getValue());
			oldActions.remove(action.getKey());
		}
		unloadObsoleteActions();
	}

	public void init(ServletContext ctx) {
		this.ctx = ctx;
		modelAccess = ModelAccess.on(ctx);
		dynamicAPIModel = modelAccess.getOntModel(FULL_UNION);
		loader = new ConfigurationBeanLoader(dynamicAPIModel, ctx);
		log.debug("Context Initialization ...");
		loadActions(actions);
	}

	public long obsoleteActionsCount() {
		return obsoleteActions.size();
	}

	public long actionsCount() {
		return actions.size();
	}

	private void loadActions(ConcurrentHashMap<String, Action> actions) {
		Set<Action> newActions = loader.loadEach(Action.class);
		log.debug("Context Initialization. actions loaded: " + actions.size());
		for (Action action : newActions) {
			if (action.isValid()) {
				actions.put(action.getName(), action);
			} else {
				log.error("Action with rpcName " + action.getName() + " is invalid.");
			}
		}
		log.debug("Context Initialization finished. " + actions.size() + " actions loaded.");
	}

	private void unloadObsoleteActions() {
		for (Action action : obsoleteActions) {
			if (!isActionInUse(action)) {
				action.dereference();
				obsoleteActions.remove(action);
			} 
		}
	}

	private boolean isActionInUse(Action action) {
		if (!action.hasClients()) {
			return false;
		}
		action.removeDeadClients();
		if (!action.hasClients()) {
			return false;
		}
		return true;
	}

}
