<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="java.util.List" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep"%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<c:set var='themeDir'><c:out value='${portalBean.themeDir}'/></c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>  <!-- formPrefix.jsp -->
<%  
	String useTinyMCE = (useTinyMCE=request.getParameter("useTinyMCE")) != null && !(useTinyMCE.equals("")) ? useTinyMCE : "false";
	if (useTinyMCE.equalsIgnoreCase("true")) {
		String height = (height=request.getParameter("height")) != null && !(height.equals("")) ? height : "200";
		String width  = (width=request.getParameter("width")) != null && !(width.equals("")) ? width : "75%";
		String defaultButtons="bold,italic,underline,separator,link,bullist,numlist,separator,sub,sup,charmap,separator,undo,redo,separator,code";
		String buttons = (buttons=request.getParameter("buttons")) != null && !(buttons.equals("")) ? buttons : defaultButtons;
		String tbLocation = (tbLocation=request.getParameter("toolbarLocation")) != null && !(tbLocation.equals("")) ? tbLocation : "top";
%>
		<script language="javascript" type="text/javascript" src="../js/tiny_mce/tiny_mce.js"></script>
	    <script language="javascript" type="text/javascript">
		tinyMCE.init({
			theme : "advanced",
			mode : "textareas",
			theme_advanced_buttons1 : "<%=buttons%>",
			theme_advanced_buttons2 : "",
			theme_advanced_buttons3 : "",
			theme_advanced_toolbar_location : "<%=tbLocation%>",
			theme_advanced_toolbar_align : "left",
			theme_advanced_statusbar_location : "bottom",
			theme_advanced_path : false,
			theme_advanced_resizing : true,
			height : "<%=height%>",
			width  : "<%=width%>",
			valid_elements : "a[href|name|title],br,p,i,em,cite,strong/b,u,sub,sup,ul,ol,li",
			fix_list_elements : true,			
			fix_nesting : true,
			cleanup_on_startup : true,
		    gecko_spellcheck : true,
		    forced_root_block: false
            //forced_root_block : 'p',
		    // plugins: "paste",
		    // theme_advanced_buttons1_add : "pastetext,pasteword,selectall",
		    // paste_create_paragraphs: false,
		    // paste_create_linebreaks: false,
		    // paste_use_dialog : true,
		    // paste_auto_cleanup_on_paste: true,
		    // paste_convert_headers_to_strong : true
			// save_callback : "customSave",
			// content_css : "example_advanced.css",
			// extended_valid_elements : "a[href|target|name]",
			// plugins : "table",
			// theme_advanced_buttons3_add_before : "tablecontrols,separator",
			// invalid_elements : "li",
			// theme_advanced_styles : "Header 1=header1;Header 2=header2;Header 3=header3;Table Row=tableRow1", // Theme specific setting CSS classes
		});
	    </script>
<%	} %>


	
<%	String useAutoComplete = (useAutoComplete=request.getParameter("useAutoComplete")) != null && !(useAutoComplete.equals("")) ? useAutoComplete : "false";
	if (useAutoComplete.equalsIgnoreCase("true")) { %>
	    <link rel="stylesheet" type="text/css" href="../js/jquery_plugins/jquery-autocomplete/jquery.autocomplete.css"/>	
<%	} %>
   

    <c:forEach var="cssFile" items="${customCss}">
        <link rel="stylesheet" type="text/css" href="${cssFile}" media="screen"/>
    </c:forEach>
       
    <link rel="stylesheet" type="text/css" href="../js/jquery_plugins/thickbox/thickbox.css"/>	
	<link rel="stylesheet" type="text/css" href="<c:url value="/${themeDir}css/screen.css"/>" media="screen"/>	
	<link rel="stylesheet" type="text/css" href="<c:url value="/${themeDir}css/formedit.css" />" media="screen"/>

    <title>Edit</title>
</head>
<body class="formsEdit">
<div id="wrap" class="container">
    <jsp:include page="/${themeDir}jsp/identity.jsp" flush="true"/>
    <jsp:include page="/${themeDir}jsp/menu.jsp" flush="true"/>
    <div id="contentwrap">
			<div id="content" class="form">
        <!-- end of formPrefix.jsp -->
