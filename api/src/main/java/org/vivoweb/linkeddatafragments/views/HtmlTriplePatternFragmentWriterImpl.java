package org.vivoweb.linkeddatafragments.views;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.jena.atlas.io.StringWriterI;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.linkeddatafragments.datasource.IDataSource;
import org.linkeddatafragments.datasource.index.IndexDataSource;
import org.linkeddatafragments.fragments.ILinkedDataFragment;
import org.linkeddatafragments.fragments.tpf.ITriplePatternElement;
import org.linkeddatafragments.fragments.tpf.ITriplePatternFragment;
import org.linkeddatafragments.fragments.tpf.ITriplePatternFragmentRequest;
import org.linkeddatafragments.views.ILinkedDataFragmentWriter;
import org.linkeddatafragments.views.TriplePatternFragmentWriterBase;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO: Refactor to a composable & flexible architecture using DataSource types, fragments types and request types

/**
 * Serializes an {@link ILinkedDataFragment} to the HTML format
 *
 * @author Miel Vander Sande
 */
public class HtmlTriplePatternFragmentWriterImpl extends TriplePatternFragmentWriterBase implements ILinkedDataFragmentWriter {
    private final Configuration cfg;
    
    private final Template indexTemplate;
    private final Template datasourceTemplate;
    private final Template notfoundTemplate;
    private final Template errorTemplate;
    
    private final String HYDRA = "http://www.w3.org/ns/hydra/core#";

    private static String contextPath;

    public static void setContextPath(String path) {
        contextPath = path;
        if (!contextPath.endsWith("/")) {
            contextPath += "/";
        }
    }
    
    /**
     *
     * @param prefixes
     * @param datasources
     * @throws IOException
     */
    public HtmlTriplePatternFragmentWriterImpl(Map<String, String> prefixes, HashMap<String, IDataSource> datasources) throws IOException {
        super(prefixes, datasources);
        
        cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setClassForTemplateLoading(getClass(), "/tpf");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        
        indexTemplate = cfg.getTemplate("index.ftl.html");
        datasourceTemplate = cfg.getTemplate("datasource.ftl.html");
        notfoundTemplate = cfg.getTemplate("notfound.ftl.html");
        errorTemplate = cfg.getTemplate("error.ftl.html");
    }
    
    /**
     *
     * @param outputStream
     * @param datasource
     * @param fragment
     * @param tpfRequest
     * @throws IOException
     * @throws TemplateException
     */
    @Override
    public void writeFragment(ServletOutputStream outputStream, IDataSource datasource, ITriplePatternFragment fragment, ITriplePatternFragmentRequest tpfRequest) throws IOException, TemplateException{
        Map data = new HashMap();
        
        // base.ftl.html
        data.put("homePath", (contextPath != null ? contextPath : "") + "tpf");
        data.put("assetsPath", (contextPath != null ? contextPath : "") + "tpf/assets/");
        data.put("header", datasource.getTitle());
        data.put("date", new Date());
        
        // fragment.ftl.html
        data.put("datasourceUrl", tpfRequest.getDatasetURL());
        data.put("datasource", datasource);
        
        // Parse controls to template variables
        StmtIterator controls = fragment.getControls();
        while (controls.hasNext()) {
            Statement control = controls.next();
            
            String predicate = control.getPredicate().getURI();
            RDFNode object = control.getObject();
            if (!object.isAnon()) {
                String value = object.isURIResource() ? object.asResource().getURI() : object.asLiteral().getLexicalForm();
                data.put(predicate.replaceFirst(HYDRA, ""), value);
            }
        }
        
        // Add metadata
        data.put("totalEstimate", fragment.getTotalSize());
        data.put("itemsPerPage", fragment.getMaxPageSize());
        
        // Add triples and datasources
        List<Statement> triples = fragment.getTriples().toList();
        data.put("triples", triples);
        data.put("datasources", getDatasources());
        
        // Calculate start and end triple number
        Long start = ((tpfRequest.getPageNumber() - 1) * fragment.getMaxPageSize()) + 1;
        data.put("start", start);
        data.put("end", start + (triples.size() < fragment.getMaxPageSize() ? triples.size() : fragment.getMaxPageSize()));
        
        // Compose query object
        Map query = new HashMap();
        query.put("subject", !tpfRequest.getSubject().isVariable() ? handleCT(tpfRequest.getSubject().asConstantTerm()) : "");
        query.put("predicate", !tpfRequest.getPredicate().isVariable() ? handleCT(tpfRequest.getPredicate().asConstantTerm()) : "");
        query.put("object", !tpfRequest.getObject().isVariable() ? handleCT(tpfRequest.getObject().asConstantTerm()) : "");
        query.put("pattern", makeQueryPattern(tpfRequest));
        data.put("query", query);

        // Get the template (uses cache internally)
        Template temp = datasource instanceof IndexDataSource ? indexTemplate : datasourceTemplate;

        // Merge data-model with template
        temp.process(data, new OutputStreamWriter(outputStream));
    }

    private String makeQueryPattern(ITriplePatternFragmentRequest tpfRequest) {
        StringBuilder pattern = new StringBuilder();

        ITriplePatternElement<RDFNode,String,String> subject   = tpfRequest.getSubject();
        ITriplePatternElement<RDFNode,String,String> predicate = tpfRequest.getPredicate();
        ITriplePatternElement<RDFNode,String,String> object    = tpfRequest.getObject();

        pattern.append("{");

        if ( ! subject.isVariable() ) {
            appendNode(pattern.append(' '), subject.asConstantTerm());
        } else {
            pattern.append(" ?s");
        }


        if ( ! predicate.isVariable() ) {
            appendNode(pattern.append(' '), predicate.asConstantTerm());
        } else {
            pattern.append(" ?p");
        }

        if ( ! object.isVariable() ) {
            appendNode(pattern.append(' '), object.asConstantTerm());
        } else {
            pattern.append(" ?o");
        }

        pattern.append(" }");
        return pattern.toString();
    }

    private void appendNode(StringBuilder builder, RDFNode node) {
        if (node.isLiteral()) {
            builder.append(literalToString(node.asLiteral()));
        } else if (node.isURIResource()) {
            builder.append('<' + node.asResource().getURI() + '>');
        }
    }

    private String literalToString(Literal l) {
        StringWriterI sw = new StringWriterI();
        NodeFormatter fmt = new NodeFormatterTTL(null, null);
        fmt.formatLiteral(sw, l.asNode());
        return sw.toString();
    }

    private Object handleCT(Object obj) {
        if (obj instanceof LiteralImpl) {
            return ((LiteralImpl)obj).asNode().toString();
        }

        return obj;
    }

    @Override
    public void writeNotFound(ServletOutputStream outputStream, HttpServletRequest request) throws Exception {
        Map data = new HashMap();
        data.put("homePath", (contextPath != null ? contextPath : "") + "tpf");
        data.put("assetsPath", (contextPath != null ? contextPath : "") + "tpf/assets/");
        data.put("datasources", getDatasources());
        data.put("date", new Date());
        data.put("url", request.getRequestURL().toString());
        
        notfoundTemplate.process(data, new OutputStreamWriter(outputStream));
    }

    @Override
    public void writeError(ServletOutputStream outputStream, Exception ex)  throws Exception {
        Map data = new HashMap();
        data.put("homePath", (contextPath != null ? contextPath : "") + "tpf");
        data.put("assetsPath", (contextPath != null ? contextPath : "") + "tpf/assets/");
        data.put("date", new Date());
        data.put("error", ex);

        errorTemplate.process(data, new OutputStreamWriter(outputStream));
    }
}
