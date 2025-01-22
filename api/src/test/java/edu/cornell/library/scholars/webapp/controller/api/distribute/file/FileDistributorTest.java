/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.file;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import javax.servlet.ServletContext;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.ActionFailedException;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.DataDistributorException;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContextImpl;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.application.VitroHomeDirectory;
import edu.cornell.mannlib.vitro.webapp.audit.AuditModule;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.fileStorage.FileStorage;
import edu.cornell.mannlib.vitro.webapp.modules.imageProcessor.ImageProcessor;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer;
import edu.cornell.mannlib.vitro.webapp.modules.tboxreasoner.TBoxReasonerModule;
import edu.cornell.mannlib.vitro.webapp.modules.tripleSource.ConfigurationTripleSource;
import edu.cornell.mannlib.vitro.webapp.modules.tripleSource.ContentTripleSource;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import stubs.edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccessFactoryStub;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;

/**
 * Test the basic functionality of FileDistributor. Assume that the
 * ConfiguratBeanLoader works as advertised.
 */
public class FileDistributorTest extends AbstractTestClass {

    private static final String SOME_FILE = "some.file";
    private static final String NO_SUCH_FILE = "no.such.file";
    private static final String SOME_DATA = "Some very fine data";
    private static final String SOME_TYPE = "text/plain_type";

    @Rule
    public TemporaryFolder homeFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder otherFolder = new TemporaryFolder();

    private File inputFile;
    private HttpServletRequestStub req;
    private DataDistributorContext ddContext;
    private FileDistributor distributor;
    private ModelAccessFactoryStub mafs;

    @Before
    public void setupData() {
        mafs = new ModelAccessFactoryStub();
        setVitroHomeDirectory(homeFolder.getRoot());
    }

    @Before
    public void setupContext() {
        req = new HttpServletRequestStub();
        ddContext = new DataDistributorContextImpl(req);
    }

    // ----------------------------------------------------------------------
    // The tests
    // ----------------------------------------------------------------------

    @Test
    public void absolutePathSuccess()
            throws DataDistributorException, IOException {
        createInputFile(otherFolder, SOME_FILE);
        createTheDistributor(inputFile.getAbsolutePath(), SOME_TYPE);
        assertResults(SOME_TYPE, SOME_DATA);
    }

    @Test
    public void relativePathSuccess()
            throws DataDistributorException, IOException {
        createInputFile(homeFolder, SOME_FILE);
        createTheDistributor(SOME_FILE, SOME_TYPE);
        assertResults(SOME_TYPE, SOME_DATA);
    }

    @Test(expected = ActionFailedException.class)
    public void noSuchFile_throwsActionFailedException()
            throws DataDistributorException, IOException {
        createInputFile(homeFolder, SOME_FILE);
        createTheDistributor(NO_SUCH_FILE, SOME_TYPE);
        assertResults(SOME_TYPE, SOME_DATA);
    }

    // ----------------------------------------------------------------------
    // Helper methods
    // ----------------------------------------------------------------------

    private void setVitroHomeDirectory(File home) {
        try {
            Field instanceField = ApplicationUtils.class
                    .getDeclaredField("instance");
            instanceField.setAccessible(true);
            ServletContextStub ctx = new ServletContextStub();
            ctx.setRealPath("/WEB-INF/resources/home-files", NO_SUCH_FILE);
            instanceField.set(null, new ApplicationStub(
                    new VitroHomeDirectory(ctx, home.toPath(), null)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void createInputFile(TemporaryFolder folder, String file)
            throws IOException {
        inputFile = new File(folder.getRoot(), file);
        FileUtils.write(inputFile, SOME_DATA, "UTF-8");
    }

    private void createTheDistributor(String path, String type)
            throws DataDistributorException {
        distributor = new FileDistributor();
        distributor.setContentType(type);
        distributor.setPath(path);
        distributor.init(ddContext);
    }

    private void assertResults(String type, String data)
            throws DataDistributorException {
        assertEquals(type, distributor.getContentType());

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        distributor.writeOutput(output);
        assertEquals(data, new String(output.toByteArray()));
    }

    // ----------------------------------------------------------------------
    // Helper classes
    // ----------------------------------------------------------------------

    /**
     * I would use the usual one, but it doesn't support the VitroHomeDirectory.
     */
    private static class ApplicationStub implements Application {

        // ----------------------------------------------------------------------
        // Stub infrastructure
        // ----------------------------------------------------------------------

        private final VitroHomeDirectory vitroHomeDirectory;

        public ApplicationStub(VitroHomeDirectory vitroHomeDirectory) {
            this.vitroHomeDirectory = vitroHomeDirectory;
        }

        // ----------------------------------------------------------------------
        // Stub methods
        // ----------------------------------------------------------------------

        @Override
        public VitroHomeDirectory getHomeDirectory() {
            return this.vitroHomeDirectory;
        }

        // ----------------------------------------------------------------------
        // Un-implemented methods
        // ----------------------------------------------------------------------

        @Override
        public ServletContext getServletContext() {
            throw new RuntimeException("getServletContext() not implemented.");
        }

        @Override
        public SearchEngine getSearchEngine() {
            throw new RuntimeException("getSearchEngine() not implemented.");
        }

        @Override
        public SearchIndexer getSearchIndexer() {
            throw new RuntimeException("getSearchIndexer() not implemented.");
        }

        @Override
        public ImageProcessor getImageProcessor() {
            throw new RuntimeException("getImageProcessor() not implemented.");
        }

        @Override
        public FileStorage getFileStorage() {
            throw new RuntimeException("getFileStorage() not implemented.");
        }

        @Override
        public ContentTripleSource getContentTripleSource() {
            throw new RuntimeException(
                    "getContentTripleSource() not implemented.");
        }

        @Override
        public ConfigurationTripleSource getConfigurationTripleSource() {
            throw new RuntimeException(
                    "getConfigurationTripleSource() not implemented.");
        }

        @Override
        public TBoxReasonerModule getTBoxReasonerModule() {
            throw new RuntimeException(
                    "getTBoxReasonerModule() not implemented.");
        }

        @Override
        public void shutdown() {
            throw new RuntimeException("shutdown() not implemented.");
        }

        @Override
        public AuditModule getAuditModule() {
            // TODO Auto-generated method stub
            return null;
        }

    }
}
