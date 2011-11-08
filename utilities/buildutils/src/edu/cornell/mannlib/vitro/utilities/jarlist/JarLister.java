/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.jarlist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.tonicsystems.jarjar.AbstractDepHandler;
import com.tonicsystems.jarjar.DepHandler;
import com.tonicsystems.jarjar.KlugedDepFind;

/**
 * This takes the place of the JarJar main routine, in doing a Find operation.
 * 
 * One thing this lets us do is to call KlugedDepFind instead of DepFind.
 * KlugedDepFind was created because JarJar had a known bug that wasn't fixed in
 * the latest release. (see http://code.google.com/p/jarjar/issues/detail?id=6).
 * I had to put KlugedDepFind into the com.tonicsystems.jarjar package so it
 * wauld have access to DepFindVisitor, which is package-private.
 * 
 * The other thing we can do is to provide a custom DepHandler which records the
 * dependencies directly instead of writing them to a file which we would need
 * to parse. Since we have the dependencies in a data structure, it's easy to
 * walk the tree and find out what JARs are required, even through several
 * layers of dependency.
 * 
 * When calling this, pass 2 arguments. The first is the path to the JAR which
 * contains the Vitro (or VIVO) classes. The second is the path to the directory
 * that contains the JARs. (shouldn't end with a slash)
 * 
 * There is a list of JARs which we know we need but which aren't shown by the
 * analysis. For example, the MySQL driver is loaded dynamically by name, so an
 * analysis of the class files in the JARs won't show that it is used. For now,
 * these known dependencies are hard-coded, but it would be nice to read them
 * from a data file instead.
 */
public class JarLister {
	/**
	 * <pre>
	 * 
	 * What I originally wanted to do was this:
	 * 
	 * <target name="jarlist" depends="jar" description="Figure out what JARs are needed">
	 *   <java classname="com.tonicsystems.jarjar.Main" fork="no" failonerror="true">
	 *     <classpath refid="utility.run.classpath" />
	 *     <arg value="find" />
	 *     <arg value="jar" />
	 *     <arg value="${build.dir}/${ant.project.name}.jar" />
	 *     <arg value="${appbase.dir}/lib/*" />
	 *   </java>
	 * </target>
	 * 
	 * I ended up with this instead:
	 * 
	 * <target name="jarlist" depends="jar" description="Figure out what JARs are needed">
	 *   <java classname="edu.cornell.mannlib.vitro.utilities.jarlist.JarLister" fork="no" failonerror="true">
	 *     <classpath refid="utility.run.classpath" />
	 *     <arg value="${build.dir}/${ant.project.name}.jar" />
	 *     <arg value="${appbase.dir}/lib" />
	 *     <arg value="${appbase.dir}/config/jarlist/known_dependencies.txt" />
	 *   </java>
	 * </target>
	 * 
	 * </pre>
	 */

	private final String topJar;
	private final String libDirectory;
	private final List<String> knownDependencies;

	private final Map<String, Set<String>> dependencyMap = new HashMap<String, Set<String>>();
	private final Set<String> dependencySet = new TreeSet<String>();

	public JarLister(String[] args) throws IOException {
		topJar = args[0];
		libDirectory = args[1];
		knownDependencies = Collections
				.unmodifiableList(readKnownDependencies(args[2]));
	}

	private List<String> readKnownDependencies(String knownDependenciesFilename)
			throws IOException {
		List<String> list = new ArrayList<String>();

		BufferedReader r = new BufferedReader(new FileReader(
				knownDependenciesFilename));

		String line;
		while (null != (line = r.readLine())) {
			line = line.trim();
			if (!(line.startsWith("#") || line.isEmpty())) {
				list.add(line);
			}
		}
		return list;
	}

	public void runDepFind() throws IOException {
		DepHandler handler = new AbstractDepHandler(DepHandler.LEVEL_JAR) {
			@Override
			public void handle(String from, String to) {
				addToMap(from, to);
			}
		};

		String fullPath = topJar + ":" + libDirectory + "/*";

		new KlugedDepFind().run(fullPath, fullPath, handler);
	}

	private void addToMap(String fromPath, String toPath) {
		String fromName = new File(fromPath).getName();
		String toName = new File(toPath).getName();

		if (!dependencyMap.containsKey(fromName)) {
			dependencyMap.put(fromName, new HashSet<String>());
		}
		dependencyMap.get(fromName).add(toName);
		// System.out.println("Adding " + fromName + " ==> " + toName);
	}

	public void populateDependencySet() {
		String topJarName = new File(topJar).getName();
		addDependenciesFor(topJarName);

		for (String known : knownDependencies) {
			dependencySet.add(known);
			addDependenciesFor(known);
		}
	}

	private void addDependenciesFor(String name) {
		if (!dependencyMap.containsKey(name)) {
			return;
		}
		for (String depend : dependencyMap.get(name)) {
			if (!dependencySet.contains(depend)) {
				dependencySet.add(depend);
				// System.out.println("Depend: " + depend);
				addDependenciesFor(depend);
			}
		}
	}

	public void dumpDependencySet(PrintWriter w) {
		w.println("--------------------");
		w.println("Known required JARs");
		w.println("--------------------");
		for (String d : knownDependencies) {
			w.println("    " + d);
		}
		w.println();

		w.println("--------------------");
		w.println("Dependent JARs");
		w.println("--------------------");
		for (String d : dependencySet) {
			w.println("    " + d);
		}
		w.println();

		File libDir = new File(libDirectory);
		SortedSet<String> unused = new TreeSet<String>(Arrays.asList(libDir
				.list()));
		unused.removeAll(dependencySet);

		w.println("--------------------");
		w.println("Unused JARs");
		w.println("--------------------");
		for (String d : unused) {
			w.println("    " + d);
		}
		w.println();
	}

	public static void main(String[] args) throws IOException {
		JarLister jl = new JarLister(args);
		jl.runDepFind();
		jl.populateDependencySet();

		PrintWriter output = new PrintWriter(System.out, true);
		jl.dumpDependencySet(output);
		output.close();
	}
}
