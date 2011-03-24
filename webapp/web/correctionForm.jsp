<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep"%>
    
<c:set var='themeDir'><c:out value='${portalBean.themeDir}' /></c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>  <!-- from formPrefix.jsp -->
	<link rel="stylesheet" type="text/css" href="<c:url value="/${themeDir}css/screen.css"/>" media="screen"/>
    <title>VIVO Correction Form</title>
</head>
<body class="formsEdit">
<div id="wrap">
<jsp:include page="/${themeDir}jsp/identity.jsp" flush="true"/>
<div id="contentwrap">
<jsp:include page="/${themeDir}jsp/menu.jsp" flush="true"/>
<!-- end of formPrefix.jsp -->

<div id="content" class="staticPageBackground">

<div class="feedbackForm">

<h2>VIVO Correction and Update Form</h2>
<p/>
<p>
		Please submit corrections and/or updates on the form below.
</p>
<p>
		Staff on duty during regular business hours will contact you as soon as possible
		to confirm or clarify your requested changes.
</p>

<hr/>

		<form name = "contact_form" action="sendmail" method="post" onsubmit="return ValidateForm('contact_form');">
		<input type="hidden" name="home" value="${portalBean.portalId}"/>
		<input type="hidden" name="RequiredFields" value="webuseremail,webusername,comments"/>
		<input type="hidden" name="RequiredFieldsNames" value="Email address,Name,Comments"/>
		<input type="hidden" name="EmailFields" value="webuseremail"/>
		<input type="hidden" name="EmailFieldsNames" value="emailaddress"/>
		<input type="hidden" name="DeliveryType" value="correction"/>
		
		<p class="normal">My email address (e.g., userid<b>@institution.edu</b>) is:</p>
		<input style="width:25%;" type="text" name="webuseremail" maxlength="255"/><br/><br/>
		<p class="normal">My full name is:</p>
		<input style="width:33%;" type="text" name="webusername" maxlength="255"/><br/><br/>

		<p class="normal">
		    <i>			
			Please also optionally include any suggestions for additional content (people, departments, courses, research services, etc.)
			that you would like to see represented in VIVO.
			</i>
	    </p>
		<h3>Enter your corrections, questions, or suggestions in the box below.</h3>

		<textarea name="s34gfd88p9x1" rows="10" cols="90"></textarea>
		<p/>
		<input type="submit" value="Send Mail" class="yellowbutton"/>
		<input type="reset" value="Clear Form" class="plainbutton"/>

		<h3>Thank you!</h3>
		</form>

</div><!--feedbackForm-->

</div><!--staticPageBackground-->

<c:set var='themeDir'><c:out value='${portalBean.themeDir}'/></c:set>
</div> <!-- contentwrap -->
     <jsp:include page="/${themeDir}jsp/footer.jsp" flush="true"/>
</div><!-- wrap -->
</body>
</html>
