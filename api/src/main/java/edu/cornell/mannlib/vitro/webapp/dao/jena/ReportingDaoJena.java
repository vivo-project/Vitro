/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.createSelectQueryContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import edu.cornell.mannlib.vitro.webapp.dao.ReportingDao;
import edu.cornell.mannlib.vitro.webapp.rdfservice.filter.LangAwareOntModel;
import edu.cornell.mannlib.vitro.webapp.reporting.AbstractTemplateReport;
import edu.cornell.mannlib.vitro.webapp.reporting.DataSource;
import edu.cornell.mannlib.vitro.webapp.reporting.ReportGenerator;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;

/**
 * Jena implementation of the Reporting DAO
 */
public class ReportingDaoJena extends JenaBaseDao implements ReportingDao {
    private static final String REPORT_GENERATOR_URI = "java:" + ReportGenerator.class.getName();

    // SPARQL query to retrieve all configured reports
    private static final String ALL_REPORTS = ""
            + "PREFIX : <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#> \n" + "SELECT ?report  \n" //
            + "WHERE { \n" //
            + "   ?report a <" + REPORT_GENERATOR_URI + "> . \n" //
            + "} \n";

    public ReportingDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }

    @Override
    public ReportGenerator getReportByName(String name) {
        try {
            OntModel displayModel = getWebappDaoFactory().getOntModelSelector().getDisplayModel();
            // Find a report URI that has this name
            StmtIterator iterator = displayModel.listStatements(null, displayModel.getProperty(PROPERTY_REPORTNAME),
                    name);
            if (iterator.hasNext()) {
                Statement stmt = iterator.nextStatement();
                String uri = stmt.getSubject().asResource().getURI();

                // Load the report
                ReportGenerator report = new ConfigurationBeanLoader(displayModel).loadInstance(uri,
                        ReportGenerator.class);
                report.setUri(uri);
                if (isPersistent(uri)) {
                    report.setIsPersistent(true);
                }
                return report;
            }
        } catch (ConfigurationBeanLoaderException e) {
        }

        return null;
    }

    @Override
    public ReportGenerator getReportByUri(String uri) {
        try {
            OntModel displayModel = getWebappDaoFactory().getOntModelSelector().getDisplayModel();
            ReportGenerator report = new ConfigurationBeanLoader(displayModel).loadInstance(uri, ReportGenerator.class);
            report.setUri(uri);
            if (isPersistent(uri)) {
                report.setIsPersistent(true);
            }
            return report;
        } catch (ConfigurationBeanLoaderException e) {
            log.error("Unable to load reporting configuration", e);
        }

        return null;
    }

    @Override
    public List<ReportGenerator> getAllReports() {
        List<String> uris = getReportUris();
        List<ReportGenerator> reports = new ArrayList<>();

        for (String uri : uris) {
            reports.add(getReportByUri(uri));
        }

        return reports;
    }

    @Override
    public boolean updateReport(String uri, ReportGenerator report) {
        Model existingModel = getModelByUri(uri);
        Model newModel = convertReportToModel(uri, report);

        // If we haven't got a model (e.g. the uri doesn't exist), just use an empty
        // model
        if (existingModel == null) {
            existingModel = ModelFactory.createDefaultModel();
        }

        // Calculate what statements need to be removed from the display model
        Model retractions = existingModel.difference(newModel);

        // Calculate what statements need to be added to the display model
        Model additions = newModel.difference(existingModel);

        OntModel displayModel = getWebappDaoFactory().getOntModelSelector().getDisplayModel();

        displayModel.enterCriticalSection(Lock.WRITE);
        try {
            // Remove any retractions
            if (!retractions.isEmpty()) {
                displayModel.remove(retractions);
            }

            // Add any additions
            if (!additions.isEmpty()) {
                displayModel.add(additions);
            }
        } finally {
            displayModel.leaveCriticalSection();
        }

        return true;
    }

    @Override
    public boolean deleteReport(String uri) {
        Model existingModel = getModelByUri(uri);
        Model newModel = ModelFactory.createDefaultModel();

        // If we haven't got a model (e.g. the uri doesn't exist), just use an empty
        // model
        if (existingModel != null) {
            // Calculate what statements need to be removed from the display model
            Model retractions = existingModel.difference(newModel);

            OntModel displayModel = getWebappDaoFactory().getOntModelSelector().getDisplayModel();

            displayModel.enterCriticalSection(Lock.WRITE);
            try {
                // Remove any retractions
                if (!retractions.isEmpty()) {
                    displayModel.remove(retractions);
                }
            } finally {
                displayModel.leaveCriticalSection();
            }
        }

        return true;
    }

    private Model convertReportToModel(String uri, ReportGenerator report) {
        Model model = ModelFactory.createDefaultModel();

        // The subject uri passed will be the subject of all statements in this model
        Resource subject = model.createResource(uri);

        // Add the interface and object class types to the model
        model.add(subject, RDF.type, model.getResource("java:" + report.getClass().getName()));
        model.add(subject, RDF.type, model.getResource("java:" + ReportGenerator.class.getName()));

        // Add the report name
        model.add(subject, model.getProperty(PROPERTY_REPORTNAME), report.getReportName());

        // Add all the datasources
        for (DataSource dataSource : report.getDataSources()) {
            // Generate a new URI for this datasource
            String dsUri = uri + "-ds" + UUID.randomUUID().toString();
            Resource dsSubject = model.createResource(dsUri);

            // Add the type and properties for this datasource
            model.add(subject, model.getProperty(PROPERTY_DATASOURCE), dsSubject);
            model.add(dsSubject, RDF.type, model.getResource("java:" + dataSource.getClass().getName()));
            model.add(dsSubject, model.getProperty(PROPERTY_DISTRIBUTORNAME), dataSource.getDistributorName());
            model.addLiteral(dsSubject, model.getProperty(PROPERTY_DISTRIBUTORRANK), dataSource.getRank());
            model.add(dsSubject, model.getProperty(PROPERTY_OUTPUTNAME), dataSource.getOutputName());
        }

        // If this is a template based report
        if (report instanceof AbstractTemplateReport) {
            // Add the template to the model
            String template = ((AbstractTemplateReport) report).getTemplateBase64();
            if (!StringUtils.isEmpty(template)) {
                model.add(subject, model.getProperty(PROPERTY_TEMPLATE), template);
            }
        }

        return model;
    }

    /**
     * Retrieve the URIs of all objects declared as being of type DataDistributor
     */
    public List<String> getReportUris() {
        OntModel displayModel = getWebappDaoFactory().getOntModelSelector().getDisplayModel();
        return createSelectQueryContext(displayModel, ALL_REPORTS).execute().toStringFields("report").flatten();
    }

    /**
     * Get a Jena model for all statements with the given Uri as a subject
     */
    private Model getModelByUri(String uri) {
        Model model = ModelFactory.createDefaultModel();

        OntModel displayModel = getWebappDaoFactory().getOntModelSelector().getDisplayModel();
        StmtIterator iterator = displayModel.listStatements(displayModel.getResource(uri), null, (RDFNode) null);
        if (iterator != null) {
            model.add(iterator);
        }

        // Add statements for datasources to the model
        iterator = model.listStatements(displayModel.getResource(uri), model.getProperty(PROPERTY_DATASOURCE),
                (RDFNode) null);
        if (iterator != null) {
            while (iterator.hasNext()) {
                Statement stmt = iterator.nextStatement();
                if (stmt.getObject().isResource()) {
                    model.add(getModelByUri(stmt.getObject().asResource().getURI()));
                }
            }
        }

        return model;
    }

    /**
     * Determine if the Uri is declared in the permanent store (i.e. it is not a
     * file loaded from everytime)
     */
    @Override
    public boolean isPersistent(String uri) {
        // Get the display model
        OntModel displayModel = getWebappDaoFactory().getOntModelSelector().getDisplayModel();

        // If we applied a Language filter to the OntModel, there will be an extended
        // OntModel returned
        // And we can use this to check the underlying model to see if the uri is in a
        // submodel
        if (displayModel instanceof LangAwareOntModel) {
            return !((LangAwareOntModel) displayModel).isDefinedInSubModel(uri);
        }

        // Not language filtered, so we need to check if the uri is declared in an
        // attached submodel
        ExtendedIterator<OntModel> subModels = displayModel.listSubModels();
        while (subModels.hasNext()) {
            OntModel subModel = subModels.next();

            // If the uri is in a submodel, then we should treat it as not being persistent
            if (subModel.contains(subModel.getResource(uri), RDF.type)) {
                return false;
            }
        }

        return true;
    }
}
