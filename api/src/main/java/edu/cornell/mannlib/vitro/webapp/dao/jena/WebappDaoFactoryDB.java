/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryConfig;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

/**
 * An extension of {@link WebappDaoFactoryJena} for databases, such as TDB.
 */
public class WebappDaoFactoryDB extends WebappDaoFactoryJena {

    private DatasetMode mode;

    /**
     * Initializer for web application DAO factory.
     *
     * @param base The web application DAO factory.
     * @param userURI The URI for the user.
     */
    public WebappDaoFactoryDB(WebappDaoFactoryDB base, String userURI) {
        super(base.ontModelSelector);

        this.ontModelSelector = base.ontModelSelector;
        this.config = base.config;
        this.userURI = userURI;
        this.dw = base.dw;
        this.rdfService = base.rdfService;
        this.mode = DatasetMode.ASSERTIONS_AND_INFERENCES;
    }

    /**
     * Initializer for web application DAO factory.
     *
     * @param rdfService The RDF service.
     * @param ontModelSelector The ontology model selector.
     */
    public WebappDaoFactoryDB(RDFService rdfService, OntModelSelector ontModelSelector) {
        this(rdfService, ontModelSelector, new WebappDaoFactoryConfig());
    }

    /**
     * Initializer for web application DAO factory.
     *
     * @param rdfService The RDF service.
     * @param ontModelSelector The ontology model selector.
     * @param config The configuration.
     */
    public WebappDaoFactoryDB(RDFService rdfService, OntModelSelector ontModelSelector,
            WebappDaoFactoryConfig config) {

        this(rdfService, ontModelSelector, config, null);
    }

    /**
     * Initializer for web application DAO factory.
     *
     * @param rdfService The RDF service.
     * @param ontModelSelector The ontology model selector.
     * @param config The configuration.
     * @param mode The data set mode.
     */
    public WebappDaoFactoryDB(RDFService rdfService, OntModelSelector ontModelSelector,
            WebappDaoFactoryConfig config, DatasetMode mode) {

        super(ontModelSelector, config);

        this.dw = new DatasetWrapper(new RDFServiceDataset(rdfService));
        this.rdfService = rdfService;

        if (mode == null) {
            this.mode = DatasetMode.ASSERTIONS_AND_INFERENCES;
        } else {
            this.mode = mode;
        }
    }

    /**
     * Convert class to a named string.
     *
     * @return The named string with a hash and notable properties.
     */
    public String toString() {
        return "WebappDaoFactoryDB[" + Integer.toString(hashCode(), 16) + ", " + mode + "]";
    }

    /**
     * Get the individual DAO.
     *
     * @return The individual DAO.
     */
    public IndividualDao getIndividualDao() {
        if (entityWebappDao != null) {
            return entityWebappDao;
        }

        return entityWebappDao = new IndividualDaoDB(dw, mode, this);
    }

    /**
     * Get the data property statement DAO for databases, such as TDB.
     *
     * @return The data property statement DAO.
     */
    public DataPropertyStatementDaoDB getDataPropertyStatementDao() {
        if (!hasDataPropertyStatementDao()) {
            setDataPropertyStatementDao(new DataPropertyStatementDaoDB(dw, this));
        }

        return (DataPropertyStatementDaoDB) super.getDataPropertyStatementDao();
    }

    /**
     * Set the data property statement DAO for databases, such as TDB.
     *
     * @param propertyStatement The data property statement DAO.
     */
    protected void setDataPropertyStatementDao(DataPropertyStatementDaoDB propertyStatement) {
        super.setDataPropertyStatementDao(propertyStatement);
    }

    /**
     * Get the object property statement DAO for databases, such as TDB.
     *
     * @return The object property statement DAO.
     */
    public ObjectPropertyStatementDaoDB getObjectPropertyStatementDao() {
        if (!hasObjectPropertyStatementDao()) {
            setObjectPropertyStatementDao(
                new ObjectPropertyStatementDaoDB(rdfService, dw, mode, this));
        }

        return (ObjectPropertyStatementDaoDB) super.getObjectPropertyStatementDao();
    }

    /**
     * Set the data property statement DAO for databases, such as TDB.
     *
     * @param propertyStatement The data property statement DAO.
     */
    protected void setObjectPropertyStatementDao(ObjectPropertyStatementDaoDB propertyStatement) {
        super.setObjectPropertyStatementDao(propertyStatement);
    }

    /**
     * Get the VClass DAO.
     *
     * @return The VClass DAO.
     */
    public VClassDao getVClassDao() {
        if (vClassDao != null) {
            return vClassDao;
        }

        return vClassDao = new VClassDaoDB(dw, mode, this, config.isUnderlyingStoreReasoned());
    }

    /**
     * Get the user aware DAO factory.
     *
     * @param userURI The URI of the user.
     *
     * @return The user aware DAO factory.
     */
    public WebappDaoFactoryDB getUserAwareDaoFactory(String userURI) {
        return new WebappDaoFactoryDB(this, userURI);
    }

    /**
     * Get the RDF Service.
     *
     * @return The RDF service.
     */
    public RDFService getRDFService() {
        return this.rdfService;
    }

    public static String getFilterBlock(String[] graphVars, DatasetMode datasetMode) {
        StringBuilder filterBlock = new StringBuilder();

        for (String graphVar : graphVars) {
            switch (datasetMode) {
            case ASSERTIONS_ONLY:
                filterBlock.append("FILTER (")
                    .append("(!bound(").append(graphVar)
                    .append(")) || (")
                    .append(graphVar)
                    .append(" != <")
                    .append(ModelNames.ABOX_INFERENCES)
                    .append("> ")
                    .append("&& ").append(graphVar).append(" != <")
                    .append(ModelNames.TBOX_INFERENCES)
                    .append(">")
                    .append(") ) \n");
                break;

            case INFERENCES_ONLY:
                filterBlock.append("FILTER (")
                    .append("(!bound(").append(graphVar)
                    .append(")) || (")
                    .append(graphVar)
                    .append(" = <")
                    .append(ModelNames.ABOX_INFERENCES)
                    .append("> || ").append(graphVar)
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

    /**
     * Close the DAO and the RDF service.
     */
    public void close() {
        super.close();

        if (this.rdfService != null) {
            this.rdfService.close();
        }
    }

}
