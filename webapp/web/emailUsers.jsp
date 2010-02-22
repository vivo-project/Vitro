<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<div id="content" class="staticPageBackground">

<div class="feedbackForm">

<h2>${siteName} Email Users Form </h2>



<hr/>

		<form name = "contact_form" action="mailusers" method="post">
		<input type="hidden" name="home" value="${portalId}"/>
		<input type="hidden" name="RequiredFields" value="comments"/>
		<input type="hidden" name="RequiredFieldsNames" value="Comments"/>
		<input type="hidden" name="EmailFields" value="webuseremail"/>
		<input type="hidden" name="EmailFieldsNames" value="emailaddress"/>
		<input type="hidden" name="DeliveryType" value="comment"/>
		
		<p class="normal">My email address is (e.g., userid<b>@institution.edu</b>):</p>
		<input style="width:25%;" type="text" name="webuseremail" maxlength="255"/><br/><br/>
		<p class="normal">My full name is:</p>
		<input style="width:33%;" type="text" name="webusername" maxlength="255"/><br/><br/>

		<p class="normal"><i>${siteName} is a service that depends on regular updates and feedback.
			Please help us out by providing any necessary corrections and suggestions for additional content (people, departments, courses, research services, etc.)
			that you would like to see represented.</i></p>
		<h3>Enter your comments, questions, or suggestions in the box below.</h3>

		<textarea name="s34gfd88p9x1" rows="10" cols="90"></textarea>
		<div>
  		<input type="submit" value="Send Mail" class="yellowbutton"/>
  		<input type="reset" value="Clear Form" class="plainbutton"/>
		</div

		<p style="font-weight: bold; margin-top: 1em">Thank you!</p>
		</form>

</div><!--feedbackForm-->

</div><!--content, staticPageBackground-->
