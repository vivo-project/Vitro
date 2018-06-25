package org.vivoweb.linkeddatafragments.views;

import org.linkeddatafragments.datasource.IDataSource;
import org.linkeddatafragments.views.ILinkedDataFragmentWriter;
import org.linkeddatafragments.views.RdfWriterImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory for {@link ILinkedDataFragmentWriter}s.
 * 
 * @author Miel Vander Sande
 */
public class LinkedDataFragmentWriterFactory {
    
    private final static String HTML = "text/html";
    
    /**
     * Creates {@link ILinkedDataFragmentWriter} for a given mimeType
     * 
     * @param prefixes Configured prefixes to be used in serialization
     * @param datasources Configured datasources
     * @param mimeType mimeType to create writer for
     * @return created writer
     */
    public static ILinkedDataFragmentWriter create(Map <String, String> prefixes, HashMap<String, IDataSource> datasources, String mimeType) throws IOException {
        switch (mimeType) {
            case HTML:
                return new HtmlTriplePatternFragmentWriterImpl(prefixes, datasources);
            default:
                return new RdfWriterImpl(prefixes, datasources, mimeType);
        }
    }
}
