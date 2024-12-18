package edu.cornell.mannlib.vitro.webapp.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import stubs.javax.servlet.ServletContextStub;

public class VitroHomeDirectoryTest {

    private static final String FILE = "file";
    private static final String CONFIG = "config";
    private static final String RDF = "rdf";
    @Rule
    public TemporaryFolder src = new TemporaryFolder();
    @Rule
    public TemporaryFolder dst = new TemporaryFolder();

    @Test
    public void testGetHomeSrcPath() {
        ServletContextStub sc = new ServletContextStub();
        String expectedPath = "/opt/tomcat/webapp/app/WEB-INF/resources/home-files";
        sc.setRealPath("/WEB-INF/resources/home-files", expectedPath);
        VitroHomeDirectory vhd = new VitroHomeDirectory(sc, dst.getRoot().toPath(), "");
        String realPath = vhd.getSourcePath();
        assertEquals(expectedPath, realPath);
    }

    @Test
    public void testPopulate() throws Exception {
        ServletContextStub sc = new ServletContextStub();
        src.newFolder(RDF);
        src.newFile(FILE);
        src.newFolder(CONFIG);
        sc.setRealPath("/WEB-INF/resources/home-files", src.getRoot().getAbsolutePath());
        VitroHomeDirectory vhd = new VitroHomeDirectory(sc, dst.getRoot().toPath(), "");
        vhd.populate();
        Set<String> files = new HashSet<String>(Arrays.asList(dst.getRoot().list()));
        assertTrue(files.contains(CONFIG));
        assertTrue(files.contains(FILE));
        assertFalse(files.contains(RDF));
    }
}
