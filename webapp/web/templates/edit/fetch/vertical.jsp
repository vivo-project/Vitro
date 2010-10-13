<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page isThreadSafe = "false" %>
<%@ page import="java.util.*,java.lang.String.*" %>

<% if (request.getAttribute("title") != null) { %>
  <h2><%=request.getAttribute("title")%></h2>
<% } %>

<%
String headerStr = (String)request.getAttribute("header");
if ( headerStr == null || (!headerStr.equalsIgnoreCase("noheader")) ) { %>
<%  } %>
<jsp:useBean id="results" class="java.util.ArrayList" scope="request" />
<%
int rows = 0;

String minEditRoleStr = (String)request.getAttribute("min_edit_role");

String firstValue = "null";
Integer columnCount = (Integer)request.getAttribute("columncount");
rows = columnCount.intValue();

String clickSortStr = (String)request.getAttribute("clicksort");

if ( rows > 0  && results.size() > rows ) { // avoid divide by zero error in next statement
    String suppressStr = null;
    int columns = results.size() / rows;
    if ( ( suppressStr = (String)request.getAttribute("suppressquery")) == null ) { // only inserted into request if true %>
        <p><i><b><%= columns - 1 %></b> results were retrieved in <b><%= rows %></b> rows for query "<%=request.getAttribute("querystring")%>".</i></p>
<%  }
    if ( clickSortStr != null && clickSortStr.equals("true")) {
        if ( columns > 2 ) { %>
            <p><i>Click on the row header to sort columns by that row.</i></p>
<%      }
    }  %>
    <table border="0" cellpadding="2" cellspacing="0" width="100%">
<%  String[] resultsArray = new String[results.size()]; // see Core Java Vol. 1 p.216
    results.toArray( resultsArray );
    firstValue = resultsArray[ rows ];
    request.setAttribute("firstvalue",firstValue);
    //secondValue= resultsArray[ rows + 1 ];
    String classString = "";
    boolean[] postQHeaderCols = new boolean[ columns ];
    for ( int eachcol=0; eachcol < columns; eachcol++ ) {
        postQHeaderCols[ eachcol ] = false;
    }
    for ( int thisrow = 0; thisrow < rows; thisrow++ ) {
        //int currentPostQCol = 0;
        boolean dropRow = false;
            for ( int thiscol = 0; thiscol < columns; thiscol++ ) {
            String thisResult= resultsArray[ (rows * thiscol) + thisrow ];
            if ( "+".equals(thisResult) ) { /* occurs all in first row, so postQHeaderCols should be correctly initialized */
                classString = "postheaderright";
                postQHeaderCols[ thiscol ] = true;
                //++currentPostQCol;
                thisResult="&nbsp;";
            } else if ( thisResult != null && thisResult.indexOf("@@")== 0) {
                classString="postheadercenter";
                thisResult ="query values"; //leave as follows for diagnostics: thisResult.substring(2);
                thisResult = thisResult.substring(2);
            } else {
                if ( postQHeaderCols[ thiscol ] == true )
                    classString = "postheaderright";
                else if ( thiscol == 1 && thisrow < 1 ) // jc55 was thisrow<2
                    classString = "rowbold";
                else
                    classString = "row";
                if ( thisResult == null ||  "".equals(thisResult) )
                    thisResult="&nbsp;";
            }
            if ( thiscol == 0 ) { // 1st column of new row
                if ( thisrow > 0 ) { // first must close prior row %>
                    </tr>
                        <%                  if ("XX".equals(thisResult) ) {
                        dropRow = true;
                    } %>
                    <tr valign="top" class="rowvert">  <!-- okay to start even a dropRow because it will get no <td> elements -->
<%              } else { %>
                    <tr valign="top" class="header">  <!-- okay to start even a dropRow because it will get no <td> elements -->
<%              }
                if ( !dropRow ) { %>
                    <td width="15%" class="verticalfieldlabel">
                    <%=thisResult%>
<%              }
            } else { // 2nd or higher column
                if ( !dropRow ) { %>
                    <td class="<%=classString%>" >
                        <%                  if ("XX".equals(thisResult) ) { %>
                        <%="&nbsp;"%>
<%                  } else { %>
                        <%=thisResult%>
<%                  }
                }
            }
            if ( !dropRow ) { %>
                </td>
<%          }
        }
    }  %>
    </tr>
    </table>
<%
} else {
    System.out.println("No results reported when " + rows + " rows and a result array size of " + results.size()); %>
    No results retrieved for query "<%=request.getAttribute("querystring")%>".
<%  Iterator errorIter = results.iterator();
    while ( errorIter.hasNext()) {
        String errorResult = (String)errorIter.next(); %>
        <p>Error returned: <%= errorResult%></p>
<%  }
} %>


