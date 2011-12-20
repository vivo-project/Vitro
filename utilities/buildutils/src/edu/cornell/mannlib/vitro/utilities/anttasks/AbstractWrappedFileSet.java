/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.anttasks;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;

/**
 * A base class for our custom-made FileSet extensions.
 */
public abstract class AbstractWrappedFileSet implements ResourceCollection {
	private Project p;
	
	/** The list of FileResources that we will yield to the task. */
	protected List<FileResource> files;
	
	/** The internal FileSet */
	private FileSet fileSet = new FileSet();
	
	public void setProject(Project p) {
		this.p = p;
		fileSet.setProject(p);
	}
	
	public void setDir(File dir) {
		fileSet.setDir(dir);
	}
	
    public PatternSet.NameEntry createInclude() {
    	return fileSet.createInclude();
    }

    public PatternSet.NameEntry createExclude() {
    	return fileSet.createExclude();
    }
    
    public PatternSet createPatternSet() {
    	return fileSet.createPatternSet();
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
	
	protected Project getProject() {
		return p;
	}
	
	protected FileSet getInternalFileSet() {
		return fileSet;
	}

}
