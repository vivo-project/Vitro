/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.config;

import java.util.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.icu.text.SimpleDateFormat;

/**
 * Information about the provenance of this application: release, revision
 * level, build date, etc.
 * 
 * Except for the build date, the information is stored for all levels of the
 * application. So an instance of NIHVIVO might read:
 * 
 * date: 2010-11-09 12:15:44
 * 
 * level: vitro-core, trunk, 1234:1236M
 * 
 * level: vivo, branch rel_1.1_maint, 798
 * 
 * Note that the levels should be listed from inner to outer.
 * 
 * Instances of this class are immutable.
 */
public class RevisionInfoBean {
	private static final Log log = LogFactory.getLog(RevisionInfoBean.class);

	/** A dummy bean to use if there is no real one. */
	static final RevisionInfoBean DUMMY_BEAN = new RevisionInfoBean(
			new Date(0), Collections.singleton(LevelRevisionInfo.DUMMY_LEVEL));

	/** The bean is attached to the session by this name. */
	static final String ATTRIBUTE_NAME = RevisionInfoBean.class.getName();

	// ----------------------------------------------------------------------
	// static methods
	// ----------------------------------------------------------------------

	/** Package access: should only be used during setup. */
	static void setBean(ServletContext context, RevisionInfoBean bean) {
		if (bean == null) {
			bean = DUMMY_BEAN;
		}
		context.setAttribute(ATTRIBUTE_NAME, bean);
		log.info(bean);
	}

	public static RevisionInfoBean getBean(HttpSession session) {
		if (session == null) {
			log.warn("Tried to get revision info bean with a null session!");
			return DUMMY_BEAN;
		}

		return getBean(session.getServletContext());
	}

	public static RevisionInfoBean getBean(ServletContext context) {
		if (context == null) {
			log.warn("Tried to get revision info bean from a null context");
			return DUMMY_BEAN;
		}

		Object o = context.getAttribute(ATTRIBUTE_NAME);
		if (o == null) {
			log.warn("Tried to get revision info bean, but didn't find any.");
			return DUMMY_BEAN;
		}

		if (!(o instanceof RevisionInfoBean)) {
			log.error("Tried to get revision info bean, but found an instance of "
					+ o.getClass().getName() + ": " + o);
			return DUMMY_BEAN;
		}

		return (RevisionInfoBean) o;
	}

	public static void removeBean(ServletContext context) {
		context.removeAttribute(ATTRIBUTE_NAME);
	}

	// ----------------------------------------------------------------------
	// the bean
	// ----------------------------------------------------------------------

	private final long buildDate;
	private final List<LevelRevisionInfo> levelInfos;

	RevisionInfoBean(Date buildDate, Collection<LevelRevisionInfo> levelInfos) {
		this.buildDate = buildDate.getTime();
		this.levelInfos = Collections
				.unmodifiableList(new ArrayList<LevelRevisionInfo>(levelInfos));
	}

	public Date getBuildDate() {
		return new Date(buildDate);
	}

	public List<LevelRevisionInfo> getLevelInfos() {
		return levelInfos;
	}

	public String getReleaseLabel() {
		if (levelInfos.isEmpty()) {
			return LevelRevisionInfo.DUMMY_LEVEL.getRelease();
		}

		int lastIndex = levelInfos.size() - 1;
		LevelRevisionInfo outerLevel = levelInfos.get(lastIndex);
		return outerLevel.getRelease();
	}

	@Override
	public String toString() {
		return "Revision info [build date: "
				+ new SimpleDateFormat().format(new Date(buildDate))
				+ ", level info: " + levelInfos + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof RevisionInfoBean)) {
			return false;
		}
		RevisionInfoBean that = (RevisionInfoBean) obj;
		return (this.buildDate == that.buildDate)
				&& this.levelInfos.equals(that.levelInfos);
	}

	@Override
	public int hashCode() {
		return new Long(buildDate).hashCode() ^ levelInfos.hashCode();
	}

	// ----------------------------------------------------------------------
	// helper class
	// ----------------------------------------------------------------------

	/**
	 * Revision info about one level of the application -- e.g. vitro, vivo,
	 * vivoCornell, etc.
	 */
	public static class LevelRevisionInfo {
		/** A level to use when no actual level can be found. */
		static final LevelRevisionInfo DUMMY_LEVEL = new LevelRevisionInfo(
				"no name", "unknown", "unknown");

		private final String name;
		private final String release;
		private final String revision;

		LevelRevisionInfo(String name, String release, String revision) {
			this.name = name;
			this.release = release;
			this.revision = revision;
		}

		public String getName() {
			return name;
		}

		public String getRelease() {
			return release;
		}

		public String getRevision() {
			return revision;
		}

		@Override
		public String toString() {
			return "[" + name + ", " + release + ", " + revision + "]";
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof LevelRevisionInfo)) {
				return false;
			}
			LevelRevisionInfo that = (LevelRevisionInfo) obj;
			return this.name.equals(that.name)
					&& this.release.equals(that.release)
					&& this.revision.equals(that.revision);
		}

		@Override
		public int hashCode() {
			return name.hashCode() ^ release.hashCode() ^ revision.hashCode();
		}

	}

}
