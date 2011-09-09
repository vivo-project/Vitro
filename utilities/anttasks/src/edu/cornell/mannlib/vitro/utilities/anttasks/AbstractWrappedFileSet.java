/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.anttasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;

/**
 * A base class for our custom-made FileSet extensions.
 */
public abstract class AbstractWrappedFileSet implements ResourceCollection {
	protected List<FileResource> files;
	
	private Project p;
	private File dir;
	
	private FileSet fileSet;
	
	public void setProject(Project p) {
		this.p = p;
	}
	
	public void setDir(File dir) {
		this.dir = dir;
	}

	@Override
	public Object clone() {
		throw new BuildException(this.getClass().getSimpleName()
				+ " does not support cloning.");
	}

	@Override
	public boolean isFilesystemOnly() {
		return true;
	}

	@Override
	public Iterator<? extends Resource> iterator() {
		fillFileList();
		return files.iterator();
	}

	@Override
	public int size() {
		fillFileList();
		return files.size();
	}

	protected abstract void fillFileList();
	
	protected FileSet getInternalFileSet() {
		if (fileSet != null) {
			return fileSet;
		}
		
		fileSet = new FileSet();
		fileSet.setProject(p);
		fileSet.setDir(dir);
		return fileSet;
	}

}
