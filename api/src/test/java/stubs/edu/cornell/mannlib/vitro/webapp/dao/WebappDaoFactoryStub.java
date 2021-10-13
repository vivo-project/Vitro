/* $This file is distributed under the terms of the license in LICENSE$ */

package stubs.edu.cornell.mannlib.vitro.webapp.dao;

import edu.cornell.mannlib.vitro.webapp.dao.*;
import edu.cornell.mannlib.vitro.webapp.i18n.I18nBundle;

import java.util.List;
import java.util.Set;

/**
 * A minimal implementation of the WebappDaoFactory.
 * <p>
 * I have only implemented the methods that I needed. Feel free to implement
 * others.
 */
public class WebappDaoFactoryStub implements WebappDaoFactory {
    // ----------------------------------------------------------------------
    // Stub infrastructure
    // ----------------------------------------------------------------------

    private String defaultNamespace;
    private ApplicationDao applicationDao;
    private DataPropertyDao dataPropertyDao;
    private DatatypeDao datatypeDao;
    private FauxPropertyDao fauxPropertyDao;
    private IndividualDao individualDao;
    private MenuDao menuDao;
    private ObjectPropertyDao objectPropertyDao;
    private ObjectPropertyStatementDao objectPropertyStatementDao;
    private OntologyDao ontologyDao;
    private PropertyGroupDao propertyGroupDao;
    private PropertyInstanceDao propertyInstanceDao;
    private UserAccountsDao userAccountsDao;
    private VClassDao vClassDao;
    private VClassGroupDao vClassGroupDao;

    @Override
    public String getDefaultNamespace() {
        return this.defaultNamespace;
    }

    public void setDefaultNamespace(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

    @Override
    public ApplicationDao getApplicationDao() {
        return this.applicationDao;
    }

    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    @Override
    public DataPropertyDao getDataPropertyDao() {
        return this.dataPropertyDao;
    }

    public void setDataPropertyDao(DataPropertyDao dataPropertyDao) {
        this.dataPropertyDao = dataPropertyDao;
    }

    @Override
    public DatatypeDao getDatatypeDao() {
        return this.datatypeDao;
    }

    public void setDatatypeDao(DatatypeDao datatypeDao) {
        this.datatypeDao = datatypeDao;
    }

    @Override
    public IndividualDao getIndividualDao() {
        return this.individualDao;
    }

    public void setIndividualDao(IndividualDao individualDao) {
        this.individualDao = individualDao;
    }

    @Override
    public MenuDao getMenuDao() {
        return this.menuDao;
    }

    public void setMenuDao(MenuDao menuDao) {
        this.menuDao = menuDao;
    }

    @Override
    public ObjectPropertyDao getObjectPropertyDao() {
        return this.objectPropertyDao;
    }

    public void setObjectPropertyDao(ObjectPropertyDao objectPropertyDao) {
        this.objectPropertyDao = objectPropertyDao;
    }

    @Override
    public FauxPropertyDao getFauxPropertyDao() {
        return this.fauxPropertyDao;
    }

    // ----------------------------------------------------------------------
    // Stub methods
    // ----------------------------------------------------------------------

    public void setFauxPropertyDao(FauxPropertyDao fauxPropertyDao) {
        this.fauxPropertyDao = fauxPropertyDao;
    }

    @Override
    public ObjectPropertyStatementDao getObjectPropertyStatementDao() {
        return this.objectPropertyStatementDao;
    }

    public void setObjectPropertyStatementDao(
            ObjectPropertyStatementDao objectPropertyStatementDao) {
        this.objectPropertyStatementDao = objectPropertyStatementDao;
    }

    @Override
    public OntologyDao getOntologyDao() {
        return this.ontologyDao;
    }

    public void setOntologyDao(OntologyDao ontologyDao) {
        this.ontologyDao = ontologyDao;
    }

    @Override
    public PropertyGroupDao getPropertyGroupDao() {
        return this.propertyGroupDao;
    }

    public void setPropertyGroupDao(PropertyGroupDao propertyGroupDao) {
        this.propertyGroupDao = propertyGroupDao;
    }

    @Override
    public PropertyInstanceDao getPropertyInstanceDao() {
        return this.propertyInstanceDao;
    }

    public void setPropertyInstanceDao(
            PropertyInstanceDao propertyInstanceDao) {
        this.propertyInstanceDao = propertyInstanceDao;
    }

    @Override
    public UserAccountsDao getUserAccountsDao() {
        return this.userAccountsDao;
    }

    public void setUserAccountsDao(UserAccountsDao userAccountsDao) {
        this.userAccountsDao = userAccountsDao;
    }

    @Override
    public VClassDao getVClassDao() {
        return this.vClassDao;
    }

    public void setVClassDao(VClassDao vClassDao) {
        this.vClassDao = vClassDao;
    }

    @Override
    public VClassGroupDao getVClassGroupDao() {
        return this.vClassGroupDao;
    }

    public void setVClassGroupDao(VClassGroupDao vClassGroupDao) {
        this.vClassGroupDao = vClassGroupDao;
    }

    // ----------------------------------------------------------------------
    // Un-implemented methods
    // ----------------------------------------------------------------------

    @Override
    public String checkURI(String uriStr) {
        throw new RuntimeException(
                "WebappDaoFactory.checkURI() not implemented.");
    }

    @Override
    public String checkURIForEditableEntity(String uriStr) {
        throw new RuntimeException(
                "WebappDaoFactory.checkURIForNewEditableEntity() not implemented.");
    }

    @Override
    public boolean hasExistingURI(String uriStr) {
        throw new RuntimeException(
                "WebappDaoFactory.hasExistingURI() not implemented.");
    }

    @Override
    public Set<String> getNonuserNamespaces() {
        throw new RuntimeException(
                "WebappDaoFactory.getNonuserNamespaces() not implemented.");
    }

    @Override
    public List<String> getPreferredLanguages() {
        throw new RuntimeException(
                "WebappDaoFactory.getPreferredLanguages() not implemented.");
    }

    @Override
    public List<String> getCommentsForResource(String resourceURI) {
        throw new RuntimeException(
                "WebappDaoFactory.getCommentsForResource() not implemented.");
    }

    @Override
    public WebappDaoFactory getUserAwareDaoFactory(String userURI) {
        throw new RuntimeException(
                "WebappDaoFactory.getUserAwareDaoFactory() not implemented.");
    }

    @Override
    public String getUserURI() {
        throw new RuntimeException(
                "WebappDaoFactory.getUserURI() not implemented.");
    }

    @Override
    public DataPropertyStatementDao getDataPropertyStatementDao() {
        throw new RuntimeException(
                "WebappDaoFactory.getDataPropertyStatementDao() not implemented.");
    }

    @Override
    public DisplayModelDao getDisplayModelDao() {
        throw new RuntimeException(
                "WebappDaoFactory.getDisplayModelDao() not implemented.");
    }

    @Override
    public PageDao getPageDao() {
        throw new RuntimeException(
                "WebappDaoFactory.getPageDao() not implemented.");
    }

    @Override
    public DataDistributorDao getDataDistributorDao() {
        throw new RuntimeException(
                "WebappDaoFactory.getDataDistributorDao() not implemented.");
    }

    @Override
    public ReportingDao getReportingDao() {
        throw new RuntimeException(
                "WebappDaoFactory.getReportingDao() not implemented.");
    }

    @Override
    public void close() {
        throw new RuntimeException("WebappDaoFactory.close() not implemented.");
    }

    @Override
    public I18nBundle getI18nBundle() {
        throw new RuntimeException("WebappDaoFactory.getI18nBundle() not implemented.");
    }

}
