package org.linkeddatafragments.datasource;

import org.linkeddatafragments.fragments.IFragmentRequestParser;

import java.io.Closeable;

/**
 * A data source of Linked Data Fragments.
 *
 * @author Ruben Verborgh
 * @author <a href="http://olafhartig.de">Olaf Hartig</a>
 */
public interface IDataSource extends Closeable {

    /**
     *
     * @return
     */
    public String getTitle();

    /**
     *
     * @return
     */
    public String getDescription();

    /**
     * Returns a data source specific {@link IFragmentRequestParser}.
     * @return
     */
    IFragmentRequestParser getRequestParser();

    /**
     * Returns a data source specific {@link IFragmentRequestProcessor}.
     * @return
     */
    IFragmentRequestProcessor getRequestProcessor();
}
