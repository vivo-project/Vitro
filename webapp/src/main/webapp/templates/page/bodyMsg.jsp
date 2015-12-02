<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%  /***********************************************
     A JSP for displaying a simple message.
     This JSP is intened to be wraped by basicPage.jsp
         
     request.attributes:
         "msg" the message to display on the page.
                     
     bdc34 2009-02-03 created        
     **********************************************/
          
     if (request.getAttribute("msg") == null ){
         throw new JspException(
                 "bodyMsg.jsp expects that request parameter 'msg' be set to"
                 + " the message for the page.\n"
          );
     }
   // only get the msg from the attributes, because of cross site exploits, 
   // never from the parameters.
   out.print( (String)request.getAttribute("msg") );
%>        
