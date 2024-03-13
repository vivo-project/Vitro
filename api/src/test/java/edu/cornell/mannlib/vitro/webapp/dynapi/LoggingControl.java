/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi;

import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class LoggingControl {

    private Map<Class, Level> logLevels = new HashMap();

    private static LoggingControl INSTANCE = new LoggingControl();

    public static void offLog(Class clazz) {
        getInstance().offLogging(clazz);
    }

    private static LoggingControl getInstance() {
        return INSTANCE;
    }

    private void offLogging(Class clazz) {
        if (logLevels == null) {
            logLevels = new HashMap<Class, Level>();
        }
        if (!logLevels.containsKey(clazz)) {
            Logger logger = Logger.getLogger(clazz);
            Level level = logger.getLevel();
            logLevels.put(clazz, level);
            logger.setLevel(Level.FATAL);
        }
    }

    public static void restoreLog(Class clazz) {
        getInstance().restoreLogging(clazz);
    }

    private void restoreLogging(Class clazz) {
        if (logLevels == null) {
            logLevels = new HashMap();
        }
        if (logLevels.containsKey(clazz)) {
            Level level = logLevels.get(clazz);
            Logger.getLogger(clazz).setLevel(level);
            logLevels.remove(clazz);
        }
    }

    public static void offLogs() {
        offLog(RESTEndpoint.class);
        offLog(ResourceAPIPool.class);
        offLog(RPCPool.class);
        offLog(ProcedurePool.class);
        offLog(ConfigurationBeanLoader.class);
    }

    public static void restoreLogs() {
        restoreLog(RESTEndpoint.class);
        restoreLog(ResourceAPIPool.class);
        restoreLog(RPCPool.class);
        restoreLog(ProcedurePool.class);
        restoreLog(ConfigurationBeanLoader.class);
    }

}
