/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.revisioninfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A harness that runs a system-level command.
 * 
 * No provision is made for standard input.
 * 
 * The standard output and standard error streams are asynchronously read, so
 * the sub-process will not block on full buffers. Warning: if either of these
 * streams contain more data than can fit into a String, then we will have a
 * problem.
 */
public class ProcessRunner {
	private int returnCode;
	private String stdOut = "";
	private String stdErr = "";
	private File workingDirectory;

	private final Map<String, String> environmentAdditions = new HashMap<String, String>();

	/** Set the directory that the command will run in. */
	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	/** Add (or replace) any environment variable. */
	public void setEnvironmentVariable(String key, String value) {
		this.environmentAdditions.put(key, value);
	}

	/**
	 * Run the command.
	 * 
	 * @param command
	 *            a list containing the operating system program and its
	 *            arguments. See
	 *            {@link java.lang.ProcessBuilder#ProcessBuilder(List)}.
	 */
	public void run(List<String> command) throws ProcessException {
		try {
			ProcessBuilder builder = new ProcessBuilder(command);

			if (workingDirectory != null) {
				builder.directory(workingDirectory);
			}

			if (!environmentAdditions.isEmpty()) {
				builder.environment().putAll(this.environmentAdditions);
			}

			Process process = builder.start();
			StreamEater outputEater = new StreamEater(process.getInputStream());
			StreamEater errorEater = new StreamEater(process.getErrorStream());

			this.returnCode = process.waitFor();

			outputEater.join();
			this.stdOut = outputEater.getContents();

			errorEater.join();
			this.stdErr = errorEater.getContents();
		} catch (IOException e) {
			throw new ProcessException("Exception when handling sub-process:",
					e);
		} catch (InterruptedException e) {
			throw new ProcessException("Exception when handling sub-process:",
					e);
		}
	}

	public int getReturnCode() {
		return returnCode;
	}

	public String getStdErr() {
		return stdErr;
	}

	public String getStdOut() {
		return stdOut;
	}

	/**
	 * A thread that reads an InputStream until it reaches end of file, then
	 * closes the stream.
	 */
	private static class StreamEater extends Thread {
		private final InputStream stream;

		private final StringWriter contents = new StringWriter();

		private final byte[] buffer = new byte[4096];

		public StreamEater(InputStream stream) {
			this.stream = stream;
			this.start();
		}

		@Override
		public void run() {
			try {
				int howMany = 0;
				while (true) {
					howMany = stream.read(buffer);
					if (howMany > 0) {
						contents.write(new String(buffer, 0, howMany));
					} else if (howMany == 0) {
						Thread.yield();
					} else {
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public String getContents() {
			return contents.toString();
		}
	}

	/**
	 * Indicates a problem when dealing with a spawned sub-process.
	 */
	public static class ProcessException extends Exception {

		public ProcessException() {
		}

		public ProcessException(String message) {
			super(message);
		}

		public ProcessException(Throwable cause) {
			super(cause);
		}

		public ProcessException(String message, Throwable cause) {
			super(message, cause);
		}

	}

}
