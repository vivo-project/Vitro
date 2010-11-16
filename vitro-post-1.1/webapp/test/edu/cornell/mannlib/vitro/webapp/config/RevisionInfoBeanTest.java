/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.config;

import static edu.cornell.mannlib.vitro.webapp.config.RevisionInfoBean.DUMMY_BEAN;
import static edu.cornell.mannlib.vitro.webapp.config.RevisionInfoBean.LevelRevisionInfo.DUMMY_LEVEL;
import static junit.framework.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpSessionStub;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.config.RevisionInfoBean.LevelRevisionInfo;

/**
 * Tests for RevisionInfoBean
 */
public class RevisionInfoBeanTest extends AbstractTestClass {
	private static final Date SAMPLE_DATE = new Date();

	private static final LevelRevisionInfo LEVEL_1_INFO = new LevelRevisionInfo(
			"level1name", "level1release", "level1revision");
	private static final LevelRevisionInfo LEVEL_2_INFO = new LevelRevisionInfo(
			"level2name", "level2release", "level2revision");
	private static final LevelRevisionInfo LEVEL_3_INFO = new LevelRevisionInfo(
			"level3name", "level3release", "level3revision");

	private static final RevisionInfoBean BEAN_NO_LEVEL = buildBean(SAMPLE_DATE);
	private static final RevisionInfoBean BEAN_1_LEVEL = buildBean(SAMPLE_DATE,
			LEVEL_1_INFO);
	private static final RevisionInfoBean BEAN_MULTI_LEVEL = buildBean(
			SAMPLE_DATE, LEVEL_1_INFO, LEVEL_2_INFO, LEVEL_3_INFO);

	private static RevisionInfoBean buildBean(Date date,
			LevelRevisionInfo... levels) {
		return new RevisionInfoBean(date, Arrays.asList(levels));
	}

	private ServletContextStub context;
	private HttpSessionStub session;

	@Before
	public void setupContext() {
		context = new ServletContextStub();
		session = new HttpSessionStub();
		session.setServletContext(context);
	}

	@Before
	public void suppressInfoMessages() {
		setLoggerLevel(RevisionInfoBean.class, Level.WARN);
	}

	@Test
	public void setBeanNormal() {
		RevisionInfoBean.setBean(context, BEAN_1_LEVEL);
		assertEquals("stored bean", BEAN_1_LEVEL,
				RevisionInfoBean.getBean(session));
	}

	@Test
	public void setBeanNull() {
		RevisionInfoBean.setBean(context, null);
		assertEquals("dummy bean", DUMMY_BEAN,
				RevisionInfoBean.getBean(session));
	}

	@Test
	public void getBeanNoSession() {
		setLoggerLevel(RevisionInfoBean.class, Level.ERROR);

		assertEquals("noBean", DUMMY_BEAN,
				RevisionInfoBean.getBean((HttpSession) null));
	}

	@Test
	public void getBeanNoAttribute() {
		setLoggerLevel(RevisionInfoBean.class, Level.ERROR);

		assertEquals("noAttribute", DUMMY_BEAN,
				RevisionInfoBean.getBean(session));
	}

	@Test
	public void getBeanAttributeIsWrongClass() {
		setLoggerLevel(RevisionInfoBean.class, Level.OFF);

		context.setAttribute(RevisionInfoBean.ATTRIBUTE_NAME, "A string!");
		assertEquals("noAttribute", DUMMY_BEAN,
				RevisionInfoBean.getBean(session));
	}

	@Test
	public void removeBean() {
		RevisionInfoBean.setBean(context, BEAN_1_LEVEL);
		assertEquals("stored bean", BEAN_1_LEVEL,
				RevisionInfoBean.getBean(session));

		setLoggerLevel(RevisionInfoBean.class, Level.ERROR);

		RevisionInfoBean.removeBean(context);
		assertEquals("dummy bean", DUMMY_BEAN,
				RevisionInfoBean.getBean(session));
	}

	@Test
	public void getReleaseLabelOneLevel() {
		RevisionInfoBean.setBean(context, BEAN_1_LEVEL);
		assertEquals("1 level release", LEVEL_1_INFO.getRelease(),
				RevisionInfoBean.getBean(session).getReleaseLabel());
	}

	@Test
	public void getReleaseLabelManyLevels() {
		RevisionInfoBean.setBean(context, BEAN_MULTI_LEVEL);
		assertEquals("many level release", LEVEL_3_INFO.getRelease(),
				RevisionInfoBean.getBean(session).getReleaseLabel());
	}

	@Test
	public void getReleaseLabelNoLevels() {
		RevisionInfoBean.setBean(context, BEAN_NO_LEVEL);
		assertEquals("0 level release", DUMMY_LEVEL.getRelease(),
				RevisionInfoBean.getBean(session).getReleaseLabel());
	}

	@Test
	public void getReleaseLabelNoBean() {
		setLoggerLevel(RevisionInfoBean.class, Level.ERROR);

		assertEquals("no bean release", DUMMY_LEVEL.getRelease(),
				RevisionInfoBean.getBean(session).getReleaseLabel());
	}

}
