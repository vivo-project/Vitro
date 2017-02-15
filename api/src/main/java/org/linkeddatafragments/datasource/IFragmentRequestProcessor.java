package org.linkeddatafragments.datasource;

import org.linkeddatafragments.fragments.ILinkedDataFragment;
import org.linkeddatafragments.fragments.ILinkedDataFragmentRequest;

import java.io.Closeable;

/**
 * Processes {@link ILinkedDataFragmentRequest}s and returns
 * the requested {@link ILinkedDataFragment}s.
 *
 * @author <a href="http://olafhartig.de">Olaf Hartig</a>
 */
public interface IFragmentRequestProcessor extends Closeable
{

    /**
     *
     * @param request
     * @return
     * @throws IllegalArgumentException
     */
    ILinkedDataFragment createRequestedFragment(
            final ILinkedDataFragmentRequest request)
                    throws IllegalArgumentException;
}
