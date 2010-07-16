/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.tags;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import java.io.File;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspWriter;
import javax.servlet.ServletException;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.beans.DynamicField;
import edu.cornell.mannlib.vedit.beans.DynamicFieldRow;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import edu.cornell.mannlib.vedit.tags.EditTag;

public class DynamicFieldsTag extends EditTag {

    private char PATH_SEP = File.separatorChar;

    public final String MARKUP_FILE_PATH = "templates"+PATH_SEP+"edit"+PATH_SEP+"specific"+PATH_SEP;

    private String name = null;
    private String type = null;
    private String usePage = null;

    private String preMarkup = null;
    private String templateMarkup = null;
    private String postMarkup = null;

    public void setName( String name ) {
        this.name = name;
    }

    public void setType( String type ) {
        this.type = type;
    }

    public void setUsePage( String usePage ) {
        this.usePage = usePage;
    }

    public void parseMarkup() throws JspException{
        try {

            int preStart = -1;
            int templateStart = -1;
            int postStart = -1;

            InputStream fis = new FileInputStream (pageContext.getServletContext().getRealPath(new String())+PATH_SEP+MARKUP_FILE_PATH+usePage);
            InputStream bis = new BufferedInputStream(fis);
            BufferedReader in = new BufferedReader(new InputStreamReader(bis));
            List<String> lines = new ArrayList<String>();
            lines.add(""); // 0th line
            int lineIndex = 0;
            while (in.ready()) {
                ++lineIndex;
                String currentLine = in.readLine();
                if (currentLine != null && currentLine.indexOf("<!--") ==0 && currentLine.indexOf("@pre")>0  ) {
                   preStart = lineIndex;
                } else if (currentLine != null && currentLine.indexOf("<!--") ==0 && currentLine.indexOf("@template")>0 ) {
                   templateStart = lineIndex;
                } else if (currentLine != null && currentLine.indexOf("<!--") ==0 && currentLine.indexOf("@post")>0 ) {
                   postStart = lineIndex;
                }
                lines.add(currentLine);
            }
            in.close();

            StringBuffer preMarkupB = new StringBuffer();
            StringBuffer postMarkupB = new StringBuffer();
            StringBuffer templateMarkupB = new StringBuffer();

            if (templateStart>preStart  && preStart>0) {
                for (int i=preStart+1; i<templateStart; i++) {
                    preMarkupB.append(lines.get(i)).append("\n");
                }
            } else {
                System.out.println("DynamicFieldsTag could not find @pre markup in "+MARKUP_FILE_PATH+usePage);
                throw new JspException("DynamicFieldsTag could not parse @pre markup section");
            }
            preMarkup = preMarkupB.toString();


            if (postStart>templateStart && templateStart>0) {
                for (int i=templateStart+1; i<postStart; i++) {
                    templateMarkupB.append(lines.get(i)).append("\n");
                }
            } else {
                System.out.println("DynamicFieldsTag could not find @template markup in "+MARKUP_FILE_PATH+usePage);
                throw new JspException("DynamicFieldsTag could not parse @template markup section");
            }
            templateMarkup = templateMarkupB.toString();

            if (postStart>0) {
                for (int i=postStart+1; i<lines.size(); i++) {
                    postMarkupB.append(lines.get(i)).append("\n");
                }
            } else {
                System.out.println("DynamicFieldsTag could not find @post markup in "+MARKUP_FILE_PATH+usePage);
                throw new JspException("DynamicFieldsTag could not parse @post markup section");
            }
            postMarkup = postMarkupB.toString();

        } catch (FileNotFoundException e) {
            System.out.println("DynamicFieldsTag could not find markup file at "+pageContext.getServletContext().getRealPath(new String())+"\\"+MARKUP_FILE_PATH+usePage);
        } catch (IOException ioe) {
            System.out.println("DynamicFieldsTag encountered IOException reading "+pageContext.getServletContext().getRealPath(new String())+"\\"+MARKUP_FILE_PATH+usePage);
        }

    }

    public String strReplace(String input, String pattern, String replacement) {
        String[] piece = input.split(pattern);
        StringBuffer output = new StringBuffer();
        for (int i=0; i<piece.length; i++) {
            output.append(piece[i]);
            if (i<piece.length-1)
                output.append(replacement);
        }
        return output.toString();
    }

    public int doEndTag() throws JspException {
        try {
            parseMarkup();
            JspWriter out = pageContext.getOut();
            HashMap values = null;
            try {
                FormObject foo = getFormObject();
                List<DynamicField> dynfs = foo.getDynamicFields();
                Iterator<DynamicField> dynIt = dynfs.iterator();
                int i = 9899;
                while (dynIt.hasNext()) {
                    DynamicField dynf = dynIt.next();
                    StringBuffer genTaName = new StringBuffer().append("_").append(dynf.getTable()).append("_");
                    genTaName.append("-1").append("_");
                    Iterator pparamIt = dynf.getRowTemplate().getParameterMap().keySet().iterator();
                    while(pparamIt.hasNext()) {
                        String key = (String) pparamIt.next();
                        String value = (String) dynf.getRowTemplate().getParameterMap().get(key);
                        byte[] valueInBase64 = Base64.encodeBase64(value.getBytes());
                        genTaName.append(key).append(":").append(new String(valueInBase64)).append(";");
                    }


                    String preWithVars = new String(preMarkup);
                    preWithVars = strReplace(preWithVars,type+"NN",Integer.toString(i));
                    preWithVars = strReplace(preWithVars,"\\$genTaName",genTaName.toString());
                    preWithVars = strReplace(preWithVars,"\\$fieldName",dynf.getName());

                    out.print(preWithVars);

                    Iterator<DynamicFieldRow> rowIt = dynf.getRowList().iterator();
                     while (rowIt.hasNext()) {
                        ++i;
                        DynamicFieldRow row = rowIt.next();
                        if (row.getValue()==null)
                            row.setValue("");
                        if (row.getValue().length()>0) {
                            StringBuffer taName = new StringBuffer().append("_").append(dynf.getTable()).append("_");
                            taName.append(row.getId()).append("_");
                            Iterator paramIt = row.getParameterMap().keySet().iterator();
                            while(paramIt.hasNext()) {
                                String key = (String) paramIt.next();
                                String value = (String) row.getParameterMap().get(key);
                                byte[] valueInBase64 = Base64.encodeBase64(value.getBytes());
                                taName.append(key).append(":").append(new String(valueInBase64)).append(";");
                            }
                            if (row.getValue().length()>0) {
                                String templateWithVars = new String(templateMarkup);
                                templateWithVars = strReplace(templateWithVars,type+"NN",Integer.toString(i));
                                templateWithVars = strReplace(templateWithVars,"\\$taName",taName.toString());
                                templateWithVars = strReplace(templateWithVars,"\\$\\$",row.getValue());
                                out.print(templateWithVars);
                            }
                        }
                    }


                    out.print(postMarkup);
                }
                // output the row template for the javascript to clone

                out.println("<!-- row template inserted by DynamicFieldsTag  -->");
                String hiddenTemplatePreMarkup = new String(preMarkup);
                // bit of a hack to hide the template from the user:
                hiddenTemplatePreMarkup = strReplace(hiddenTemplatePreMarkup,"display\\:none\\;","");
                hiddenTemplatePreMarkup = strReplace(hiddenTemplatePreMarkup,"display\\:block\\;","");
                hiddenTemplatePreMarkup = strReplace(hiddenTemplatePreMarkup,"display\\:inline\\;","");
                hiddenTemplatePreMarkup = strReplace(hiddenTemplatePreMarkup,"style\\=\\\"","style=\"display:none;");
                out.print(hiddenTemplatePreMarkup);
                String hiddenTemplateTemplateMarkup = new String(templateMarkup);
                hiddenTemplateTemplateMarkup = strReplace(hiddenTemplateTemplateMarkup, "\\$\\$", "");
                out.print(hiddenTemplateTemplateMarkup);
                out.print(postMarkup);

            } catch (Exception e){
                System.out.println("DynamicFieldsTag could not get the form object");
            }

        } catch(Exception ex) {
            throw new JspException(ex.getMessage());
        }
        return SKIP_BODY;
    }
}
