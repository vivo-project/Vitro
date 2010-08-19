/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.tags;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.collections.OrderedMapIterator;
import java.util.List;
import java.util.Iterator;
import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.tags.EditTag;
import org.apache.commons.lang.StringEscapeUtils;

public class OptionTag extends EditTag {
    private String name = null;

    public void setName( String name ) {
        this.name = name;
    }

    private void outputOptionsMarkup(List optList, JspWriter out) throws IOException {
        Iterator it = optList.iterator();
        while (it.hasNext()){
            Option opt = (Option) it.next();
            if (opt.getValue() == null)
                opt.setValue("");
            if (opt.getBody() == null)
                opt.setBody("");
            out.print("<option value=\""+StringEscapeUtils.escapeHtml(opt.getValue())+"\"");
            if (opt.getSelected())
                out.print(" selected=\"selected\"");
            out.print(">");
            out.print(StringEscapeUtils.escapeHtml(opt.getBody()));
            out.print("</option>\n");
        }
    }

    public int doEndTag() throws JspException {
        try {
            JspWriter out = pageContext.getOut();

            List optList = null;
            ListOrderedMap optGroups = null;

            try {
                optList = (List) getFormObject().getOptionLists().get(name);
                outputOptionsMarkup(optList,out);
            } catch (ClassCastException e){
                // maybe it's a ListOrderedMap of optgroups
                optGroups = (ListOrderedMap) getFormObject().getOptionLists().get(name);
                OrderedMapIterator ogKey = optGroups.orderedMapIterator();
                while (ogKey.hasNext()) {
                    String optGroupName = (String) ogKey.next();
                    out.println("<optgroup label=\""+StringEscapeUtils.escapeHtml(optGroupName)+"\">");
                    outputOptionsMarkup((List)optGroups.get(optGroupName),out);
                    out.println("</optgroup>");
                }
            } catch (NullPointerException npe) {
                System.out.println("OptionTag could not find option list for "+name);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new JspException(ex.getMessage());
        }
        return SKIP_BODY; // EVAL_PAGE; did colnames only //EVAL_PAGE in connection pooled version;
    }
}
