/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.examples;

import java.io.IOException;
import java.io.OutputStream;

import edu.cornell.library.scholars.webapp.controller.api.distribute.AbstractDataDistributor;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext;

/**
 * A simple example of a data distributor. It sends a greeting.
 */
public class HelloDistributor extends AbstractDataDistributor {

    private static final Object NAME_PARAMETER_KEY = "name";

    /**
     * The instance is created to service one HTTP request, and init() is
     * called.
     * 
     * The DataDistributorContext provides access to the request parameters, and
     * the triple-store connections.
     */
    @Override
    public void init(DataDistributorContext ddc)
            throws DataDistributorException {
        super.init(ddc);
    }

    /**
     * For this distributor, the browser should treat the output as simple text.
     */
    @Override
    public String getContentType() throws DataDistributorException {
        return "text/plain";
    }

    /**
     * The text written to the OutputStream will become the body of the HTTP
     * response.
     * 
     * This will only be called once for a given instance.
     */
    @Override
    public void writeOutput(OutputStream output)
            throws DataDistributorException {
        try {
            if (parameters.containsKey(NAME_PARAMETER_KEY)) {
                output.write(String
                        .format("Hello, %s!",
                                parameters.get(NAME_PARAMETER_KEY)[0])
                        .getBytes());
            } else {
                output.write("Hello, World!".getBytes());
            }
        } catch (IOException e) {
            throw new ActionFailedException(e);
        }
    }

    /**
     * Release any resources. In this case, none.
     * 
     * Garbage collection is uncertain. On the other hand, you can be confident
     * that this will be called in a timely manner.
     */
    @Override
    public void close() throws DataDistributorException {
        // Nothing to do.
    }

}
