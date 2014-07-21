/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.sdb2tdb;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.tdb.TDBFactory;

/**
 * <pre>
 *    java -jar sdb2tdb.jar \ 
 *    		'jdbc:mysql://localhost/vitrodb?user=vivoUser&password=vivoPass'\ 
 *    		/usr/local/my/tdb
 * </pre>
 */
public class Sdb2Tdb {
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
		if ((!this.jdbcUrl.matches("\\busername\\b"))
				|| (!this.jdbcUrl.matches("\\bpassword\\b"))) {
			System.out.println("\nWARNING: The JDBC url probably should "
					+ "contain values for username and password.\n");
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

	private void translate() throws SQLException {
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

	private void copyGraphs() {
		for (Iterator<String> modelNames = sdbDataset.listNames(); modelNames
				.hasNext();) {
			String modelName = modelNames.next();
			Model model = sdbDataset.getNamedModel(modelName);
			System.out.println(String.format("Copying %6d triples: %s",
					model.size(), modelName));
			tdbDataset.addNamedModel(modelName, model);
			model.close();
		}
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
		} catch (SQLException e) {
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
}
