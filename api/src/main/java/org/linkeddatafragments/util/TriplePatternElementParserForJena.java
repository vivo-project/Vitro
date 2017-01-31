package org.linkeddatafragments.util;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * A {@link TriplePatternElementParser} for Jena-based backends.
 *
 * @author <a href="http://olafhartig.de">Olaf Hartig</a>
 */
public class TriplePatternElementParserForJena
    extends TriplePatternElementParser<RDFNode,String,String>
{
    private static TriplePatternElementParserForJena instance = null;

    /**
     *
     * @return
     */
    public static TriplePatternElementParserForJena getInstance()
    {
        if ( instance == null ) {
            instance = new TriplePatternElementParserForJena();
        }
        return instance;
    }

    /**
     *
     */
    protected TriplePatternElementParserForJena() {}

    /**
     *
     * @param varName
     * @return
     */
    @Override
    public String createNamedVariable( final String varName )
    {
        return varName;
    }

    /**
     *
     * @param label
     * @return
     */
    @Override
    public String createAnonymousVariable( final String label )
    {
        return label;
    }

    /**
     *
     * @param label
     * @return
     */
    @Override
    public RDFNode createBlankNode(final String label )
    {
        return ResourceFactory.createResource();
    }

    /**
     *
     * @param uri
     * @return
     */
    @Override
    public RDFNode createURI(final String uri )
    {
        return ResourceFactory.createResource( uri );
    }

    /**
     *
     * @param label
     * @param typeURI
     * @return
     */
    @Override
    public RDFNode createTypedLiteral(final String label,
                                      final String typeURI )
    {
        final RDFDatatype dt = TypeMapper.getInstance()
                                         .getSafeTypeByName( typeURI );
        return ResourceFactory.createTypedLiteral( label, dt );
    }

    /**
     *
     * @param label
     * @param languageTag
     * @return
     */
    @Override
    public RDFNode createLanguageLiteral(final String label,
                                         final String languageTag )
    {
        return ResourceFactory.createLangLiteral( label, languageTag );
    }

    /**
     *
     * @param label
     * @return
     */
    @Override
    public RDFNode createPlainLiteral(final String label )
    {
        return ResourceFactory.createPlainLiteral( label );
    }

    /**
     *
     * @param parameter
     * @return
     */
    @Override
    public RDFNode handleUnparsableParameter(final String parameter )
    {
        return CommonResources.INVALID_URI;
    }
}
