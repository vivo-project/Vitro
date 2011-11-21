/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.sql.SQLException;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * This listener will open and close DB models as it performs edits to avoid
 * wasting DB connections
 * @author bjl23
 *
 */
public class MemToDBModelSynchronizer extends StatementListener {

	private static long IDLE_MILLIS = 2000; // how long to let a model site idle
	                                        // after an edit has been performed
	
	SQLGraphGenerator generator;
	Model model;
	boolean editInProgress;
	boolean cleanupThreadActive;
	long lastEditTimeMillis;
	
	public MemToDBModelSynchronizer(SQLGraphGenerator generator) {
		this.generator = generator;
	}
	
	private Model getModel() {
		if ( model != null && !model.isClosed() ) {
			return model;
		} else {
			Graph g = generator.generateGraph();
			model = ModelFactory.createModelForGraph(g);
			return model;
		}
	}
	
	@Override
	public void addedStatement(Statement stmt) {
		this.editInProgress = true;
		try {
			getModel().add(stmt);
		} finally {
			lastEditTimeMillis = System.currentTimeMillis();
			this.editInProgress = false;
			if (!cleanupThreadActive) {
				(new Thread(
						new Cleanup(this), "MemToDBModelSynchronizer")).start();
			}
		}
	}
	
	@Override
	public void removedStatement(Statement stmt) {
		this.editInProgress = true;
		try {
			getModel().remove(stmt);
		} finally {
			lastEditTimeMillis = System.currentTimeMillis();
			this.editInProgress = false;
			if (!cleanupThreadActive) {
				(new Thread(
						new Cleanup(this), "MemToDBModelSynchronizer")).start();
			}
		}
	}
	
	private class Cleanup implements Runnable {
		
		private MemToDBModelSynchronizer s;
				
		public Cleanup(MemToDBModelSynchronizer s) {
			this.s = s;
		}
		
		public void run() {
			s.cleanupThreadActive = true;
			while( (s.editInProgress) 
					|| (System.currentTimeMillis() 
							- s.lastEditTimeMillis < IDLE_MILLIS ) ) {
				try {
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException e) {
					throw new RuntimeException(
							"Interrupted cleanup thread in " 
							+ this.getClass().getName(), e);
				}
			}
			if (s.model != null) {
				s.model.close();
				s.model = null;
			} else {
				throw new RuntimeException(
						this.getClass().getName() + "Model already null");
			}
			java.sql.Connection c = generator.getConnection();
			try {
				if ( (c != null) && (!c.isClosed()) ) {
					c.close();
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			s.cleanupThreadActive = false;
		}
		
	}
	
}
