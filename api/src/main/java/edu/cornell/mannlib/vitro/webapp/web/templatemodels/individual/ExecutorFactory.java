/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ExecutorFactory {
	
	private static final ExecutorService executor = Executors.newCachedThreadPool();

    public static ExecutorService getExecutor() {
        return executor;
    }
    
    public static void destroyExecutor() {
        executor.shutdown();
    }
    
}
 