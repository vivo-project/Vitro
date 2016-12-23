/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.sdb2tdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.riot.RDFDataMgr;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sdb.SDB;
import org.apache.jena.sdb.SDBFactory;
import org.apache.jena.sdb.Store;
import org.apache.jena.sdb.StoreDesc;
import org.apache.jena.sdb.store.DatabaseType;
import org.apache.jena.sdb.store.LayoutType;
import org.apache.jena.tdb.TDBFactory;

/**
 * Copy all of the data from an SDB triple-store to a TDB triple-store. See
 * README.txt for more details.
 * 
 * Examples of invoking it:
 * 
 * <pre>
 *    java -jar sdb2tdb.jar \ 
 *    		'jdbc:mysql://localhost/vitrodb?user=vivoUser&password=vivoPass'\ 
 *    		/usr/local/my/tdb
 *    
 *    java -Xms2048m -Xmx2048m -jar .work/sdb2tdb.jar \
 *          'jdbc:mysql://localhost/weill17?user=vivoUser&password=vivoPass' \
 *          /Users/jeb228/Testing/instances/weill-develop/vivo_home/contentTdb \
 *          force
 * </pre>
 * 
 * Each graph is copied separately. Small graphs are simply loaded into memory
 * and transferred. Large graphs are read to produce a streaming result set
 * which is written to a temporary file. That file is then read into a TDB
 * model.
 * 
 * This has been tested with graphs up to 6 million triples without crashing.
 */
public class Sdb2Tdb {
	private static final int LARGE_MODEL_THRESHOLD = 500_000;
	private final String driverClassName;
	private final String jdbcUrl;
	private final String destination;
	private final boolean force;

	private Dataset sdbDataset;
	private Dataset tdbDataset;

	public Sdb2Tdb(List<String> hardArgs) throws UsageException {
		List<String> args = new ArrayList<>(hardArgs);

		if (!args.isEmpty() && args.indexOf("force") == (args.size() - 1)) {
			this.force = true;
			args.remove(args.size() - 1);
		} else {
			this.force = false;
		}

		if (args.size() == 3) {
			this.driverClassName = args.remove(0);
		} else if (args.size() == 2) {
			this.driverClassName = "com.mysql.jdbc.Driver";
		} else {
			throw new UsageException("Wrong number of arguments: "
					+ hardArgs.size());
		}

		this.jdbcUrl = args.get(0);
		this.destination = args.get(1);

		checkDriverClass();
		checkJdbcUrl();
		checkDestination();
	}

	private void checkDriverClass() throws UsageException {
		try {
			Class.forName(this.driverClassName).newInstance();
		} catch (Exception e) {
			throw new UsageException("Can't instantiate JDBC driver: "
					+ this.driverClassName);
		}
	}

	private void checkJdbcUrl() {
		if ((!this.jdbcUrl.matches("\\buser\\b"))
				|| (!this.jdbcUrl.matches("\\bpassword\\b"))) {
			System.out.println("\nWARNING: The JDBC url probably should "
					+ "contain values for user and password.\n");
		}
	}

	private void checkDestination() throws UsageException {
		File destDir = new File(this.destination);

		if (!destDir.isDirectory()) {
			throw new UsageException(
					"The destination directory does not exist: '"
							+ this.destination + "'");
		}

		if (!destDir.canWrite()) {
			throw new UsageException("Cannot write to '" + this.destination
					+ "'");
		}

		if (!(this.force || getDestinationFilenames().isEmpty())) {
			throw new UsageException("The destination directory is not empty. "
					+ "Choose another destination, or use the 'force' option");
		}
	}

	private List<String> getDestinationFilenames() {
		File destDir = new File(this.destination);
		String[] filenames = destDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return !(name.equals(".") || name.equals(".."));
			}
		});
		return Arrays.asList(filenames);
	}

	private void translate() throws SQLException, IOException {
		try {
			sdbDataset = openSdbDataset();
			tdbDataset = openTdbDataset();
			copyGraphs();
		} finally {
			if (tdbDataset != null) {
				tdbDataset.close();
			}
			if (sdbDataset != null) {
				sdbDataset.close();
			}
		}
	}

	private Dataset openSdbDataset() throws SQLException {
		Connection conn = DriverManager.getConnection(this.jdbcUrl);
		Store store = SDBFactory.connectStore(conn, makeSdbStoreDesc());

		SDB.getContext().set(SDB.jdbcStream, Boolean.TRUE);
		SDB.getContext().set(SDB.jdbcFetchSize, Integer.MIN_VALUE);

		return SDBFactory.connectDataset(store);
	}

	private StoreDesc makeSdbStoreDesc() {
		return new StoreDesc(LayoutType.LayoutTripleNodesHash,
				DatabaseType.MySQL);
	}

	private Dataset openTdbDataset() {
		return TDBFactory.createDataset(new File(this.destination)
				.getAbsolutePath());
	}

	private void copyGraphs() throws IOException {
		for (Iterator<String> modelNames = sdbDataset.listNames(); modelNames
				.hasNext();) {
			String modelName = modelNames.next();
			Model model = sdbDataset.getNamedModel(modelName);
			if (model.size() < LARGE_MODEL_THRESHOLD) {
				copySmallModel(modelName, model);
			} else {
				copyLargeModel(modelName, model);
			}
		}
	}

	private void copySmallModel(String modelName, Model model) {
		System.out.println(String.format("Copying %6d triples: %s",
				model.size(), modelName));
		tdbDataset.addNamedModel(modelName, model);
	}

	private void copyLargeModel(String modelName, Model model)
			throws IOException {
		File tempFile = File.createTempFile("sdb-", ".n3");
		System.out.println(String.format("Copying %6d triples: %s %s",
				model.size(), modelName, tempFile.getAbsolutePath()));
		model.close();

		try (OutputStream os = new FileOutputStream(tempFile);
				GraphToTriples trips = new GraphToTriples(this, modelName)) {
			RDFDataMgr.writeTriples(os, trips);
		}
		System.out.println("Wrote it.");

		try (InputStream is = new FileInputStream(tempFile)) {
			tdbDataset.getNamedModel(modelName).read(is, null, "N-TRIPLE");
		}
		System.out.println("Read it.");
	}

	public static void main(String[] args) {
		try {
			Sdb2Tdb sdb2tdb = new Sdb2Tdb(Arrays.asList(args));
			sdb2tdb.translate();

		} catch (UsageException e) {
			System.out.println();
			System.out.println(e.getMessage());
			System.out.println(e.getProperUsage());
			System.out.println();
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}

	private static class UsageException extends Exception {
		public UsageException(String message) {
			super(message);
		}

		public String getProperUsage() {
			return "Usage is: java -jar sdb2tdb [driver_class] <jdbcUrl> <destination_directory> [force]";
		}
	}

	private static class GraphToTriples implements Iterator<Triple>,
			AutoCloseable {
		private static final String QUERY_TEMPLATE = "" //
				+ "SELECT ?s ?p ?o \n" //
				+ "WHERE { \n" //
				+ "   GRAPH <%s> { \n" //
				+ "      ?s ?p ?o . \n" //
				+ "   } \n" //
				+ "}";

		private final QueryExecution qe;
		private final ResultSet results;

		GraphToTriples(Sdb2Tdb parent, String graphUri) {
			String qStr = String.format(QUERY_TEMPLATE, graphUri);
			Query q = QueryFactory.create(qStr);
			qe = QueryExecutionFactory.create(q, parent.sdbDataset);
			results = qe.execSelect();
		}

		@Override
		public boolean hasNext() {
			return results.hasNext();
		}

		@Override
		public Triple next() {
			QuerySolution solution = results.nextSolution();
			Node s = solution.get("s").asNode();
			Node p = solution.get("p").asNode();
			Node o = solution.get("o").asNode();
			return new Triple(s, p, o);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void close() {
			qe.close();
		}
	}
}
