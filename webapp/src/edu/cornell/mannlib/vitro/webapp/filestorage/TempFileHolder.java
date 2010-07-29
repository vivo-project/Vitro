/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorage;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorageSetup;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.FileInfo;

/**
 * Attaches an uploaded file to the session with a listener, so the file will be
 * deleted if
 * <ul>
 * <li>The session times out.</li>
 * <li>The session is invalidated.</li>
 * <li>The server is shut down.</li>
 * <li>Another file is attached to the session with the same attribute name.</li>
 * </ul>
 * To see that the file isn't deleted, remove it from the session with a call to
 * {@link TempFileHolder#remove(HttpSession, String) remove}.
 */
public class TempFileHolder implements HttpSessionBindingListener {
	private static final Log log = LogFactory.getLog(TempFileHolder.class);

	/**
	 * Create a {@link TempFileHolder} holding the given {@link FileInfo}, and
	 * attach it to the session with the given attribute name.
	 * 
	 * If an attribute with this name already exists, it is replaced.
	 */
	public static void attach(HttpSession session, String attributeName,
			FileInfo fileInfo) {
		if (session == null) {
			throw new NullPointerException("session may not be null.");
		}
		if (attributeName == null) {
			throw new NullPointerException("attributeName may not be null.");
		}
		if (fileInfo == null) {
			throw new NullPointerException("fileInfo may not be null.");
		}
		log.debug("attach this file: " + fileInfo);
		session.setAttribute(attributeName, new TempFileHolder(fileInfo));
	}

	/**
	 * Get the {@link TempFileHolder} which is stored as an attribute on this
	 * session, extract the {@link FileInfo} from it, and remove it from the
	 * session.
	 * 
	 * If there is no such attribute, of if it is not a {@link TempFileHolder},
	 * return null.
	 */
	public static FileInfo remove(HttpSession session, String attributeName) {
		if (session == null) {
			throw new NullPointerException("session may not be null.");
		}
		if (attributeName == null) {
			throw new NullPointerException("attributeName may not be null.");
		}
		Object attribute = session.getAttribute(attributeName);
		if (attribute instanceof TempFileHolder) {
			FileInfo fileInfo = ((TempFileHolder) attribute).extractFileInfo();
			session.removeAttribute(attributeName);
			log.debug("remove this file: " + fileInfo);
			return fileInfo;
		} else if (attribute == null) {
			return null;
		} else {
			session.removeAttribute(attributeName);
			return null;
		}
	}

	private FileInfo fileInfo;

	private TempFileHolder(FileInfo fileInfo) {
		if (fileInfo == null) {
			throw new NullPointerException("fileInfo may not be null.");
		}
		this.fileInfo = fileInfo;
	}

	/**
	 * Gets the {@link FileInfo} payload, and removes it so the file won't be
	 * deleted when the value is unbound.
	 * 
	 * @return
	 */
	private FileInfo extractFileInfo() {
		FileInfo result = this.fileInfo;
		this.fileInfo = null;
		return result;
	}

	/**
	 * When attached to the session, do nothing.
	 * 
	 * @see HttpSessionBindingListener#valueBound(HttpSessionBindingEvent)
	 */
	@Override
	public void valueBound(HttpSessionBindingEvent event) {
	}

	/**
	 * When removed from the session, if the {@link #fileInfo} is not empty,
	 * delete the file. If you had wanted this file, you should have called
	 * {@link #remove(HttpSession, String) remove}.
	 * 
	 * @see HttpSessionBindingListener#valueUnbound(HttpSessionBindingEvent)
	 */
	@Override
	public void valueUnbound(HttpSessionBindingEvent event) {
		if (fileInfo == null) {
			log.trace("No file info.");
			return;
		}

		if (fileInfo.getBytestreamUri() == null) {
			log.warn("File info has no URI?");
			return;
		}

		HttpSession session = event.getSession();
		ServletContext servletContext = session.getServletContext();

		FileStorage fs = (FileStorage) servletContext
				.getAttribute(FileStorageSetup.ATTRIBUTE_NAME);
		if (fs == null) {
			log.error("Servlet context does not contain file storage at '"
					+ FileStorageSetup.ATTRIBUTE_NAME + "'");
			return;
		}

		try {
			fs.deleteFile(fileInfo.getBytestreamUri());
			log.debug("Deleted file " + fileInfo);
		} catch (IOException e) {
			log.error("Failed to delete temp file from session: " + event, e);
		}
	}

}
