/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.jsptags;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import net.sf.jga.fn.UnaryFunctor;

/**
 * A tag to applying some string processing from the Request scope to selective
 * parts of the output.  Intended for search highlighting.
 */

public class StringProcessorTag extends BodyTagSupport {
    
    public void setPageContext(PageContext pageContext){
        this.pageContext = pageContext;
    }
    
    @Override
    public int doStartTag(){
        Object obj =  pageContext.getRequest().getAttribute(STRING_PROCESSOR) ;
        if( obj == null || !(obj instanceof UnaryFunctor) )                   
            return EVAL_BODY_INCLUDE;
        else
            return EVAL_BODY_BUFFERED;        
    }
    
    @Override
    public int doAfterBody() throws JspException{
        Object obj =  pageContext.getRequest().getAttribute(STRING_PROCESSOR) ;
        if( obj == null || !(obj instanceof UnaryFunctor) )               
            return EVAL_PAGE;
        
        UnaryFunctor<String,String> functor = (UnaryFunctor<String,String>)obj;        
        BodyContent bc = getBodyContent();
        JspWriter out = getPreviousOut();
        try{
            out.write(functor.fn(bc.getString()));
        }catch(IOException ex){} //ignore
        return SKIP_BODY;
    }

    public static void putStringProcessorInRequest(HttpServletRequest request, UnaryFunctor<String,String>processor){
        if( request==null || processor==null) return;
        
        Object obj =  request.getAttribute(STRING_PROCESSOR) ;
        if( obj == null )               
            request.setAttribute(STRING_PROCESSOR, processor);
        else{            
            UnaryFunctor<String,String> functor = (UnaryFunctor<String,String>)obj;
            request.setAttribute(STRING_PROCESSOR,processor.compose(functor));
        }
    }
    
   public static String STRING_PROCESSOR = "StringProcessor";
}
