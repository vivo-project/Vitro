package edu.cornell.mannlib.vitro.webapp.modules.rdfDelta;

import java.net.BindException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.atlas.lib.FileOps;
import org.seaborne.delta.server.http.DeltaServer;

import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class EmbeddedRDFDeltaServer {

    private static final Log log = LogFactory.getLog(EmbeddedRDFDeltaServer.class);

    private String deltaServerBase;

    private int deltaServerPort;

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasDeltaServerBase", minOccurs = 1, maxOccurs = 1)
    public void setDeltaServerBase(String base) {
        deltaServerBase = base;
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasDeltaServerPort", minOccurs = 1, maxOccurs = 1)
    public void setDeltaServerPort(String port) {
        deltaServerPort = Integer.parseInt(port);
    }

    public void startup(Application application, ComponentStartupStatus ss) {
        // TODO: afford maintaining data source after restart
        FileOps.ensureDir(deltaServerBase);
        FileOps.clearAll(deltaServerBase);
        DeltaServer server = DeltaServer.server(deltaServerPort, deltaServerBase);
        try {
            server.start();
            System.out.println(String.format("\n\nDelta server with base %s started on port %s\n\n", deltaServerBase, deltaServerPort));
            log.info(String.format("Delta server with base %s started on port %s", deltaServerBase, deltaServerPort));
        } catch (BindException be) {
            log.error(String.format("Can't start the patch log server: %s", be.getMessage()));
            throw new RuntimeException("Can't start the patch log server", be);
        }
    }

}
