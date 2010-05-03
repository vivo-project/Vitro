/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.udd;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.filters.PortalPickerFilter;
import edu.cornell.mannlib.vitro.webapp.utils.NamespaceMapper;
import edu.cornell.mannlib.vitro.webapp.utils.NamespaceMapperFactory;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class UrlDirective implements TemplateDirectiveModel {

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars,
            TemplateDirectiveBody body) throws TemplateException, IOException {
        
        if (body != null) {
            body.render(new UrlFilterWriter(env.getOut()));
        } else {
            throw new RuntimeException("missing body");
        }        
    }

    private static class UrlFilterWriter extends Writer {
        
        private final Writer out;
           
        UrlFilterWriter (Writer out) {
            this.out = out;
        }

        public void write(char[] cbuf, int off, int len)
                throws IOException {
            char[] transformedCbuf = new char[len];
            for (int i = 0; i < len; i++) {
                transformedCbuf[i] = Character.toUpperCase(cbuf[i + off]);
            }
            out.write(transformedCbuf);
        }
        
        public void write(String url) throws IOException {           
            

        }

        public void flush() throws IOException {
            out.flush();
        }

        public void close() throws IOException {
            out.close();
        }
    }       
}