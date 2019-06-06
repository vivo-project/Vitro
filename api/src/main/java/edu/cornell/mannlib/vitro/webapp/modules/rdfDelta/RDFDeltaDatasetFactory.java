package edu.cornell.mannlib.vitro.webapp.modules.rdfDelta;

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
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class RDFDeltaDatasetFactory {

    private static final Log log = LogFactory.getLog(RDFDeltaDatasetFactory.class);

    private String deltaServerURL;

    private String deltaClientZone;

    private Zone zone;

    private DeltaLink deltaLink;

    private DeltaClient deltaClient;

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasDeltaServerURL", minOccurs = 1, maxOccurs = 1)
    public void setDeltaServerURL(String url) {
        deltaServerURL = url;
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasDeltaClientZone", minOccurs = 1, maxOccurs = 1)
    public void setDeltaClientZone(String zone) {
        deltaClientZone = zone;
    }

    public void startup(Application application, ComponentStartupStatus ss) {
        FileOps.ensureDir(deltaClientZone);
        FileOps.clearAll(deltaClientZone);
        zone = Zone.connect(deltaClientZone);
        deltaLink = DeltaLinkHTTP.connect(deltaServerURL);
        deltaClient = DeltaClient.create(zone, deltaLink);
        log.info(String.format("DeltaClient connected to DeltaServer at %s with zone %s", deltaServerURL, deltaClientZone));
    }

    public Dataset wrap(String datasourceName, Dataset dataset) {
        String datasourceURI = String.format("%s/%s", deltaServerURL, datasourceName);
        Id dsRef = deltaLink.newDataSource(datasourceName, datasourceURI);
        deltaClient.attachExternal(dsRef, dataset.asDatasetGraph());
        deltaClient.connect(dsRef, SyncPolicy.TXN_RW);
        try (DeltaConnection dConn = deltaClient.get(dsRef)) {
            dConn.addListener(new DeltaLinkListener() {
                @Override
                public void append(Id dsRef, Version version, RDFPatch patch) {
                    if (patch == null) {
                        System.out.println("\n\nPatch is null!!\n\n");
                    } else {
                        RDFPatchOps.write(System.out, patch);
                    }
                }
            });
            System.out.println(String.format("\nWrapped dataset using datasource %s with reference %s\n", datasourceName, dsRef));
            log.info(String.format("Wrapped dataset using datasource %s with reference %s", datasourceName, dsRef));
            return dConn.getDataset();
        } catch (Exception e) {
            log.error(e, e);
            throw e;
        }
    }

}
