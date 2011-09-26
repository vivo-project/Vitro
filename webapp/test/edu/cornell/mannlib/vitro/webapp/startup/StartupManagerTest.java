/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.startup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import stubs.javax.servlet.ServletContextStub;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus.StatusItem;

/**
 * TODO
 */
public class StartupManagerTest extends AbstractTestClass {
	private static final Log log = LogFactory.getLog(StartupManagerTest.class);

	private ServletContextStub ctx;
	private ServletContextEvent sce;

	private StartupManager sm;
	private StartupStatus ss;

	@Before
	public void setup() {
		ctx = new ServletContextStub();
		sce = new ServletContextEvent(ctx);

		sm = new StartupManager();
		ss = StartupStatus.getBean(ctx);

		// setLoggerLevel(this.getClass(), Level.DEBUG);
		setLoggerLevel(StartupStatus.class, Level.OFF);
		setLoggerLevel(StartupManager.class, Level.OFF);
	}

	@After
	public void dumpForDebug() {
		if (log.isDebugEnabled()) {
			dumpStatus();
		}
	}

	@Test
	public void noSuchFile() {
		assertStartupFails((String) null);
	}

	@Test
	public void emptyFile() {
		assertStartupSucceeds();
	}

	@Test
	public void blankLine() {
		assertStartupSucceeds("    \n");
	}

	@Test
	public void commentLines() {
		assertStartupSucceeds("# comment line    \n"
				+ "    # comment line starting with spaces\n");
	}

	@Test
	public void classDoesNotExist() {
		assertStartupFails("no.such.class\n");
	}

	@Test
	public void classThrowsExceptionWhenLoading() {
		assertStartupFails(ThrowsExceptionWhenLoading.class);
	}

	@Test
	public void classIsPrivate() {
		assertStartupFails(PrivateClass.class);
	}

	@Test
	public void noDefaultConstructor() {
		assertStartupFails(NoDefaultConstructor.class);
	}

	@Test
	public void constructorIsPrivate() {
		assertStartupFails(PrivateConstructor.class);
	}

	@Test
	public void constructorThrowsException() {
		assertStartupFails(ConstructorThrowsException.class);
	}

	@Test
	public void notAServletContextListener() {
		assertStartupFails(NotAListener.class);
	}

	@Test
	public void listenerThrowsException() {
		assertStartupFails(InitThrowsException.class);
	}

	@Test
	public void listenerSetsFatalStatus() {
		assertStartupFails(InitSetsFatalStatus.class);
	}

	@Test
	public void success() {
		String listener1Name = SucceedsWithInfo.class.getName();
		String listener2Name = SucceedsWithWarning.class.getName();

		assertStartupSucceeds(SucceedsWithInfo.class, SucceedsWithWarning.class);

		// Did they initialize in the correct order?
		List<StatusItem> items = ss.getStatusItems();
		assertEquals("how many", 2, items.size());
		assertEquals("init order 1", listener1Name, items.get(0)
				.getSourceName());
		assertEquals("init order 2", listener2Name, items.get(1)
				.getSourceName());

		sm.contextDestroyed(sce);

		// Did they destroy in reverse order?
		items = ss.getStatusItems();
		assertEquals("how many", 4, items.size());
		assertEquals("destroy order 1", listener2Name, items.get(2)
				.getSourceName());
		assertEquals("destroy order 2", listener1Name, items.get(3)
				.getSourceName());
	}

	@Test
	public void duplicateListeners() {
		assertStartupFails(SucceedsWithInfo.class, SucceedsWithWarning.class,
				SucceedsWithInfo.class);
	}

	@Test
	public void dontExecuteAfterFailure() {
		assertStartupFails(InitThrowsException.class, SucceedsWithInfo.class);

		for (StatusItem item : ss.getStatusItems()) {
			if (item.getSourceName().equals(SucceedsWithInfo.class.getName())
					&& (item.getLevel() == StatusItem.Level.NOT_EXECUTED)) {
				return;
			}
		}
		fail("'" + SucceedsWithInfo.class.getName()
				+ "' should not have been run after '"
				+ PrivateConstructor.class.getName() + "' failed.");
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	public static class BasicListener implements ServletContextListener {
		@Override
		public void contextDestroyed(ServletContextEvent sce) {
			// does nothing
		}

		@Override
		public void contextInitialized(ServletContextEvent sce) {
			// does nothing
		}
	}

	public static class ThrowsExceptionWhenLoading extends BasicListener {
		static {
			if (true) {
				throw new IllegalStateException("can't load me.");
			}
		}
	}

	private static class PrivateClass extends BasicListener {
		// no methods
	}

	public static class NoDefaultConstructor extends BasicListener {
		public NoDefaultConstructor(String bogus) {
			bogus.length();
		}
	}

	public static class PrivateConstructor extends BasicListener {
		private PrivateConstructor() {
			// does nothing
		}
	}

	public static class ConstructorThrowsException extends BasicListener {
		public ConstructorThrowsException() {
			if (true) {
				throw new IllegalStateException("can't load me.");
			}
		}
	}

	public static class NotAListener {
		// no methods
	}

	public static class InitThrowsException extends BasicListener {
		@Override
		public void contextInitialized(ServletContextEvent sce) {
			throw new IllegalStateException("Initialization failed.");
		}
	}

	public static class InitSetsFatalStatus extends BasicListener {
		@Override
		public void contextInitialized(ServletContextEvent sce) {
			StartupStatus.getBean(sce.getServletContext()).fatal(this,
					"Set fatal status");
		}
	}

	public static class SucceedsWithInfo implements ServletContextListener {
		@Override
		public void contextInitialized(ServletContextEvent sce) {
			StartupStatus.getBean(sce.getServletContext()).info(this,
					"Set info message on init.");
		}

		@Override
		public void contextDestroyed(ServletContextEvent sce) {
			StartupStatus.getBean(sce.getServletContext()).info(this,
					"Set info message on destroy.");
		}
	}

	public static class SucceedsWithWarning implements ServletContextListener {
		@Override
		public void contextInitialized(ServletContextEvent sce) {
			StartupStatus.getBean(sce.getServletContext()).warning(this,
					"Set warning message on init.");
		}

		@Override
		public void contextDestroyed(ServletContextEvent sce) {
			StartupStatus.getBean(sce.getServletContext()).warning(this,
					"Set warning message on destroy.");
		}
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void assertStartupFails(String fileContents) {
		if (fileContents != null) {
			ctx.setMockResource(StartupManager.FILE_OF_STARTUP_LISTENERS,
					fileContents);
		}
		sm.contextInitialized(sce);
		assertTrue("expecting abort", ss.isStartupAborted());
	}

	private void assertStartupFails(Class<?>... classes) {
		assertStartupFails(joinClassNames(classes));
	}

	private void assertStartupSucceeds(String fileContents) {
		if (fileContents != null) {
			ctx.setMockResource(StartupManager.FILE_OF_STARTUP_LISTENERS,
					fileContents);
		}
		sm.contextInitialized(sce);
		assertFalse("expecting success", ss.isStartupAborted());
	}

	private void assertStartupSucceeds(Class<?>... classes) {
		assertStartupSucceeds(joinClassNames(classes));
	}

	private String joinClassNames(Class<?>[] classes) {
		if (classes == null) {
			return null;
		}
		if (classes.length == 0) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < classes.length; i++) {
			result.append(classes[i].getName()).append('\n');
		}
		return result.toString();
	}

	private void dumpStatus() {
		List<StatusItem> items = ss.getStatusItems();
		log.debug("-------------- " + items.size() + " items");
		for (StatusItem item : items) {
			log.debug(String.format("%8s %s \n  %s \n  %s", item.getLevel(),
					item.getSourceName(), item.getMessage(), item.getCause()));
		}
	}

}
