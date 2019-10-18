package edu.cornell.mannlib.vitro.webapp.modules.rdfDelta;

import javax.jms.JMSException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.query.Dataset;
import org.seaborne.delta.Id;
import org.seaborne.delta.Version;
import org.seaborne.delta.client.DeltaClient;
import org.seaborne.delta.client.DeltaConnection;
import org.seaborne.delta.client.DeltaLinkHTTP;
import org.seaborne.delta.client.SyncPolicy;
import org.seaborne.delta.client.Zone;
import org.seaborne.delta.link.DeltaLink;
import org.seaborne.delta.link.DeltaLinkListener;
import org.seaborne.patch.RDFPatch;
import org.seaborne.patch.RDFPatchOps;

import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.messaging.JMSMessagingClient;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class RDFDeltaDatasetFactory {

    private static final Log log = LogFactory.getLog(RDFDeltaDatasetFactory.class);

    private String deltaServerURL;

    private String deltaClientZone;

    private DeltaLink deltaLink;

    private DeltaClient deltaClient;

    private JMSMessagingClient jmsMessagingClient;

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasDeltaServerURL", minOccurs = 1, maxOccurs = 1)
    public void setDeltaServerURL(String url) {
        this.deltaServerURL = url;
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasDeltaClientZone", minOccurs = 1, maxOccurs = 1)
    public void setDeltaClientZone(String zone) {
        this.deltaClientZone = zone;
    }

    public void startup(Application application, ComponentStartupStatus ss) {
        FileOps.ensureDir(deltaClientZone);
        FileOps.clearAll(deltaClientZone);
        Zone zone = Zone.connect(deltaClientZone);
        deltaLink = DeltaLinkHTTP.connect(deltaServerURL);
        deltaClient = DeltaClient.create(zone, deltaLink);
        jmsMessagingClient = application.getJMSMessagingClient();
        ss.info(String.format("DeltaClient connected to DeltaServer at %s with zone %s", deltaServerURL, deltaClientZone));
    }

    public Dataset wrap(String datasourceName, boolean shouldEmitMessage, Dataset dataset) {
        String datasourceURI = String.format("%s/%s", deltaServerURL, datasourceName);
        Id dsRef = deltaLink.newDataSource(datasourceName, datasourceURI);
        deltaClient.attachExternal(dsRef, dataset.asDatasetGraph());
        // Connect using SyncPolicy.TXN_W for when a write-transaction starts, ignoring reads.
        // https://github.com/afs/rdf-delta/blob/master/rdf-delta-client/src/main/java/org/seaborne/delta/client/SyncPolicy.java
        deltaClient.connect(dsRef, SyncPolicy.TXN_W);
        try (DeltaConnection dConn = deltaClient.get(dsRef)) {
            if (shouldEmitMessage && jmsMessagingClient != null) {
                dConn.addListener(new DeltaLinkListener() {
                    @Override
                    public void append(Id dsRef, Version version, RDFPatch patch) {
                        try {
                            jmsMessagingClient.send(RDFPatchOps.str(patch));
                        } catch (JMSException e) {
                            log.error(e, e);
                        }
                    }
                });
            } else {
                if (jmsMessagingClient == null) {
                    log.warn("Can not emit message without client");
                }
            }
            log.info(String.format("Wrapped dataset using datasource %s with reference %s", datasourceName, dsRef));
            return dConn.getDataset();
        }
    }

}
