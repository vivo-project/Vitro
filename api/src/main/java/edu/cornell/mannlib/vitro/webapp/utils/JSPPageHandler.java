/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.TemplateProcessingHelper;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class JSPPageHandler {
    public static void renderBasicPage(HttpServletRequest req, HttpServletResponse res, String bodyJsp) throws ServletException, IOException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("jspBody", renderToString(req, res, bodyJsp));

        if (req.getAttribute("scripts") instanceof String) {
            map.put("jspScripts", renderToString(req, res, (String)req.getAttribute("scripts")));
        }

        if (req.getAttribute("title") instanceof String) {
            map.put("title", req.getAttribute("title"));
        }

        ResponseValues values = new TemplateResponseValues("jspTransition.ftl", map);

        new FreemarkerWrapper().wrap(req, res, values);
    }

    private static String renderToString(HttpServletRequest req, HttpServletResponse res, String jsp) throws ServletException, IOException {
        StringBufferResponse customResponse  = new StringBufferResponse(res);
        RequestDispatcher rd = req.getRequestDispatcher(jsp);
        rd.include(req, customResponse);
        return customResponse.getOutput();
    }

    public static void renderPlainInclude(HttpServletRequest req, HttpServletResponse res, String pageJsp) throws ServletException, IOException {
        RequestDispatcher rd = req.getRequestDispatcher(pageJsp);
        rd.include(req, res);
    }

    public static void renderPlainPage(HttpServletRequest req, HttpServletResponse res, String pageJsp) throws ServletException, IOException {
        RequestDispatcher rd = req.getRequestDispatcher(pageJsp);
        rd.forward(req, res);
    }

    private static class StringBufferResponse extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        private final CharArrayWriter charArray = new CharArrayWriter();

        public StringBufferResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return new ServletOutputStream() {
                @Override
                public void write(int b) throws IOException {
                    byteArray.write(b);
                }
            };
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return new PrintWriter(charArray);
        }

        public String getOutput() {
            if (charArray.size() > 0) {
                return charArray.toString();
            }

            if (byteArray.size() > 0) {
                try {
                    return new String(byteArray.toByteArray(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                }
            }

            return "";
        }
    }

    private static class FreemarkerWrapper extends FreemarkerHttpServlet {
        public void wrap(HttpServletRequest req, HttpServletResponse res, ResponseValues values) {
            try {
                doTemplate(new VitroRequest(req), res, values);
            } catch (TemplateProcessingHelper.TemplateProcessingException e) {
            }
        }
    }
}
