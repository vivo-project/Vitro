/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore;

import static edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore.DumpRestoreController.PARAMETER_FORMAT;
import static edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore.DumpRestoreController.PARAMETER_SOURCE_FILE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore.DumpRestoreController.BadRequestException;
import edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore.DumpRestoreController.RestoreFormat;

/**
 * TODO
 * In progress.
 */
public class RestoreModelsAction extends AbstractDumpRestoreAction {

	private final FileItem sourceFile;
	private final RestoreFormat format;

	RestoreModelsAction(HttpServletRequest req, HttpServletResponse resp)
			throws BadRequestException {
		super(req);
		this.sourceFile = getFileItem(PARAMETER_SOURCE_FILE);
		this.format = getEnumFromParameter(RestoreFormat.class,
				PARAMETER_FORMAT);
	}

	private FileItem getFileItem(String key) throws BadRequestException {
		FileItem fileItem = new VitroRequest(req).getFileItem(key);
		if (fileItem == null) {
			throw new BadRequestException("Request has no file item named '"
					+ key + "'");
		}
		return fileItem;
	}

	long restoreModels() throws IOException {
		long lineCount = 0;
		try (InputStream is = sourceFile.getInputStream();
				Reader isr = new InputStreamReader(is, "UTF-8");
				BufferedReader br = new BufferedReader(isr)) {
			String line;
			while (null != (line = br.readLine())) {
				processLine(line);
				lineCount++;
			}
		}
		return lineCount;
	}

	private void processLine(String line) {
		System.out.println("TOTALLY BOGUS RESTORE");
	}

}
