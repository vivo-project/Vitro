<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers" %>
<%@ page import="org.apache.log4j.*" %>
<%@ page import="java.util.*" %>

 <%--
  This JSP will display all the log4j Logger objects, their
  levels and their appenders.  The levels of the Logger objects
  can be changed and test messages can be sent.

  Brian Cauros bdc34@cornell.edu
  based on work by Volker Mentzner. --%>

<%@taglib prefix="vitro" uri="/WEB-INF/tlds/VitroUtils.tld" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission" %>
<% request.setAttribute("requestedActions", SimplePermission.USE_MISCELLANEOUS_ADMIN_PAGES.ACTION); %>
<vitro:confirmAuthorization />

<%
try {
    String name;
    Level[] levels =  new Level[]
            {Level.OFF, Level.FATAL, Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG};

    out.write("<HTML><HEAD><TITLE>log4j configuration</TITLE></HEAD>\r\n");
    out.write("<BODY><H1>log4j</H1>\r\n");

    // handle a request with query
    if( request.getParameterMap().size() > 0){
        if( request.getParameter("doTestMsg") != null){
            //handle test message
            Logger logger = LogManager.getLogger( request.getParameter("logger") );
            Level level = (Level)Level.toLevel( request.getParameter("level"));
            logger.log(level, request.getParameter("msg"));
            out.write("<h3>message sent</h3>");
        } else {
            //handle logging level changes
            Enumeration names = request.getParameterNames();
            while(names.hasMoreElements()){
                String catname = (String)names.nextElement();
                String catval = request.getParameter(catname);
                if( "root".equalsIgnoreCase(catname))
                    LogManager.getRootLogger().setLevel((Level)Level.toLevel(catval));
                else
                    LogManager.getLogger(catname).setLevel((Level)Level.toLevel(catval));
            }
        }
    }

    out.write("<p>" + notes + "</p>");

    // output category information in a form with a simple table
    out.write("<form name=\"Formular\" ACTION=\""+request.getContextPath()+request.getServletPath()+"\" METHOD=\"POST\">");
    out.write("<table cellpadding=4>\r\n");
    out.write(" <tr>\r\n");
    out.write("  <td><b>Logger</b></td>\r\n");
    out.write("  <td><b>Level</b></td>\r\n");
    out.write("  <td><b>Appender</b></td>\r\n");
    out.write(" </tr>\r\n");

    // output for all Loggers
    List<String> logNames = new LinkedList<String>();
    for(Enumeration en = LogManager.getCurrentLoggers(); en.hasMoreElements() ;){
        logNames.add(((Logger)en.nextElement()).getName());
    }
    Collections.sort(logNames);
    logNames.add(0,"root");

    Logger cat;
    for (String logName : logNames) {
      if( "root".equalsIgnoreCase(logName))
        cat = LogManager.getRootLogger();
      else
        cat = LogManager.getLogger(logName);
      out.write(" <tr>\r\n");
      out.write("  <td>" + cat.getName() + "</td>\r\n");
      out.write("  <td>\r\n");
      out.write("   <select size=1 name=\""+ cat.getName() +"\">");
      for (int i = 0; i < levels.length; i++) {
        if (cat.getEffectiveLevel().toString().equals(levels[i].toString()))
          out.write("<option selected>"+levels[i].toString());
        else
          out.write("<option>"+levels[i].toString());
      }
      out.write("</select>\r\n");
      out.write("  </td>\r\n");
      out.write("  <td>\r\n");
      for( Appender apd : getAllAppenders( cat )){
        name = apd.getName();
        if (name == null)
          name = "<i>(no name)</i>";
        out.write(name);
        if (apd instanceof AppenderSkeleton) {
          try {
            AppenderSkeleton apskel = (AppenderSkeleton)apd;
            out.write(" [" + apskel.getThreshold().toString() + "]");
          } catch (Exception ex) {
          }
        }
        out.write("  ");
      }
      out.write("  </td>\r\n");
      out.write(" </tr>\r\n");
    }
    out.write("</table>\r\n");
    out.write("<input type=submit value=\"Submit changes to logging levels\">");
    out.write("</form>\n");  

    out.write("</BODY></HTML>\r\n");
    out.flush();
  } catch (Exception ex) {
     throw new Error( ex);
  }
%>
<%!
    String notes ="<p>Changing the level of a Logger on this form does not change the levels of that Logger's children."+
    "<p>Example: if you change the level of the Logger edu.cornell.mannlib.utils from ERROR to DEBUG, the " +
    "logging level of edu.cornell.mannlib.utils.StringUtils will not be modified." +
    "<p>Loggers will write message to all of their Appenders; you cannot have a DEBUG level For Logger A, Appender A "+
    " and a WARN level for a  Logger A, Appender B.";

%>
<%!
    private Collection<Appender> getAllAppenders(Category logger){
        HashSet<Appender> appenders = new HashSet<Appender>();
        Enumeration en = logger.getAllAppenders();
        while( en.hasMoreElements()){
            appenders.add((Appender) en.nextElement());
        }
        if( logger.getParent() != null )
          appenders.addAll( getAllAppenders(logger.getParent()));
        return appenders;
    }
%>