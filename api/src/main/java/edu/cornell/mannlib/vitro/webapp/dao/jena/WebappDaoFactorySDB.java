/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

import org.apache.jena.query.Dataset;
import org.apache.jena.sdb.SDBFactory;
import org.apache.jena.sdb.Store;
import org.apache.jena.sdb.StoreDesc;
import org.apache.jena.sdb.sql.SDBConnection;

import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryConfig;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.SimpleReasonerSetup;

public class WebappDaoFactorySDB extends WebappDaoFactoryJena {
	 
    private SDBDatasetMode datasetMode = SDBDatasetMode.ASSERTIONS_AND_INFERENCES;
    
	public WebappDaoFactorySDB(RDFService rdfService,
	                          OntModelSelector ontModelSelector) { 
		this(rdfService, ontModelSelector, new WebappDaoFactoryConfig());
	}
	
    public WebappDaoFactorySDB(RDFService rdfService,
                               OntModelSelector ontModelSelector,
                               WebappDaoFactoryConfig config) {
        this(rdfService, ontModelSelector, config, null);
    }
    
    public WebappDaoFactorySDB(RDFService rdfService,
                               OntModelSelector ontModelSelector, 
                               WebappDaoFactoryConfig config,
                               SDBDatasetMode datasetMode) {
        super(ontModelSelector, config);
        this.dwf = new StaticDatasetFactory(new RDFServiceDataset(rdfService));
        this.rdfService = rdfService;
        if (datasetMode != null) {
            this.datasetMode = datasetMode;
        }
    }
    
	@Override
	public String toString() {
		return "WebappDaoFactorySDB[" + Integer.toString(hashCode(), 16) + ", "
				+ datasetMode + "]";
	}

	public WebappDaoFactorySDB(WebappDaoFactorySDB base, String userURI) {
        super(base.ontModelSelector);
        this.ontModelSelector = base.ontModelSelector;
        this.config = base.config;
        this.userURI = userURI;
        this.dwf = base.dwf;
        this.rdfService = base.rdfService;
    }
	
	@Override
    public IndividualDao getIndividualDao() {
        if (entityWebappDao != null)
            return entityWebappDao;
        else
            return entityWebappDao = new IndividualDaoSDB(
                    dwf, datasetMode, this);
    }
	
	@Override
	public DataPropertyStatementDao getDataPropertyStatementDao() {
		if (dataPropertyStatementDao != null) 
			return dataPropertyStatementDao;
		else
			return dataPropertyStatementDao = new DataPropertyStatementDaoSDB(
			        dwf, datasetMode, this);
	}
	
	@Override
	public ObjectPropertyStatementDao getObjectPropertyStatementDao() {
		if (objectPropertyStatementDao != null) 
			return objectPropertyStatementDao;
		else
			return objectPropertyStatementDao = 
			    new ObjectPropertyStatementDaoSDB(rdfService, dwf, datasetMode, this);
	}
	
	@Override
	public VClassDao getVClassDao() {
		if (vClassDao != null) 
			return vClassDao;
		else
			return vClassDao = new VClassDaoSDB(dwf, datasetMode, this, config.isUnderlyingStoreReasoned());
	}
	
	@Override
	public WebappDaoFactory getUserAwareDaoFactory(String userURI) {
        return new WebappDaoFactorySDB(this, userURI);
    }
	
	public RDFService getRDFService() {
	    return this.rdfService;
	}
	
	public enum SDBDatasetMode {
	    ASSERTIONS_ONLY, INFERENCES_ONLY, ASSERTIONS_AND_INFERENCES
	}
	
	public static String getFilterBlock(String[] graphVars, 
	                                    SDBDatasetMode datasetMode) {
	    StringBuffer filterBlock = new StringBuffer();
	    for (int i = 0; i < graphVars.length; i++) {
	        switch (datasetMode) {
	            case ASSERTIONS_ONLY :  
	                    filterBlock.append("FILTER (")
	                        .append("(!bound(").append(graphVars[i])
	                        .append(")) || (")
	                        .append(graphVars[i])
	                        .append(" != <")
	                        .append(ModelNames.ABOX_INFERENCES)
	                        .append("> ")
	                        .append("&& ").append(graphVars[i]).append(" != <")
	                        .append(ModelNames.TBOX_INFERENCES)
	                        .append(">")
	                        .append(") ) \n");
	                    break;
	            case INFERENCES_ONLY :  
                    filterBlock.append("FILTER (")
                        .append("(!bound(").append(graphVars[i])
                        .append(")) || (")
                        .append(graphVars[i])
                        .append(" = <")
                        .append(ModelNames.ABOX_INFERENCES)
                        .append("> || ").append(graphVars[i])
                        .append(" = <")
                        .append(ModelNames.TBOX_INFERENCES)
                        .append(">) )\n");
                    break;
	            default:
	                break;
	        }
	    }
	    return filterBlock.toString();
	}
	
	@Override
	public void close() {
	    super.close();
	    if (this.rdfService != null) {
	        this.rdfService.close();
	    }
	}
	
	private class ReconnectingDatasetFactory implements DatasetWrapperFactory {
	    
	    private BasicDataSource _bds;
	    private StoreDesc _storeDesc;
	    
	    public ReconnectingDatasetFactory(BasicDataSource bds, 
                                          StoreDesc storeDesc) {
	        _bds = bds;
	        _storeDesc = storeDesc;
	    }
	    
	    public DatasetWrapper getDatasetWrapper() {
	        try {
                Connection sqlConn = _bds.getConnection();
                SDBConnection conn = new SDBConnection(sqlConn) ;
                Store store = SDBFactory.connectStore(conn, _storeDesc);
                Dataset dataset = SDBFactory.connectDataset(store);
                return new DatasetWrapper(dataset, conn);
            } catch (SQLException sqe) {
                throw new RuntimeException(
                		"Unable to connect to database", sqe);
            }
	    }
	    
	}    
	
}
