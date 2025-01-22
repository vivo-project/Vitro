/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf;

import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.dataProperty;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.model;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.objectProperty;

import org.junit.Before;

import edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.GraphBuilderStub;
import stubs.edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContextStub;

/**
 * Show that we can do the same binding and queries against a locally built
 * graph, as we could against the RDFService from the RequestModelsAccess.
 * 
 * Note that we split the data into two GraphBuilders, to verify that the
 * distributor calls both.
 */
public class SelectFromGraphDistributorTest
        extends SelectFromContentDistributorTest {
    @Override
    @Before
    public void setup() {
        GraphBuilderStub builder1 = new GraphBuilderStub()
                .setGraph(model(dataProperty(BOOK1, HAS_TITLE, TITLE1),
                        dataProperty(BOOK2, HAS_TITLE, TITLE2),
                        dataProperty(AUTHOR1, HAS_NAME, NAME1),
                        dataProperty(AUTHOR2, HAS_NAME, NAME2)));

        GraphBuilderStub builder2 = new GraphBuilderStub()
                .setGraph(model(objectProperty(BOOK1, HAS_AUTHOR, AUTHOR1),
                        objectProperty(BOOK1, HAS_AUTHOR, AUTHOR2),
                        objectProperty(BOOK2, HAS_AUTHOR, AUTHOR1)));

        ddContext = new DataDistributorContextStub(model());

        distributor = new SelectFromGraphDistributor();
        ((SelectFromGraphDistributor) distributor).setRawQuery(RAW_QUERY);
        ((SelectFromGraphDistributor) distributor).addGraphBuilder(builder1);
        ((SelectFromGraphDistributor) distributor).addGraphBuilder(builder2);
    }
}
