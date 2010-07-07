<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.User" %>

<script type="text/javascript" src="js/jquery.js"></script> 
<script language="JavaScript" type="text/javascript" src="js/toggle.js"></script>
<script language="JavaScript" type="text/javascript" src="js/md5.js"></script>
<script language="JavaScript" type="text/javascript">
<!-- Hide from browsers without JavaScript support

function isValidLogin( theForm ) {
    if ( isEmpty( theForm.loginName.value)) {
        theForm.loginName.focus();
        return false;
    }
    if ( isEmptyPassword( theForm.loginPassword.value)) {
        theForm.loginPassword.focus();
        return false;
    }
    return true;
}

function isEmpty( aStr ) {
    if ( aStr.length == 0 ) {
        alert("Please enter a username to log in");
        return true;
    }
    return false;
}

function isEmptyPassword( aStr ) {
    if ( aStr.length == 0 ) {
        alert("Please enter a password to log in");
        return true;
    }
    return false;
}

function isReasonableNewPassword( theForm ) {
	if ( isWrongLengthPassword( theForm.newPassword.value)) {
	    theForm.newPassword.focus();
	    return false;
	}
	if ( isMismatchedPasswords( theForm.newPassword.value, theForm.confirmPassword.value)) {
	    theForm.newPassword.focus();
	    return false;
	}
}

function isWrongLengthPassword( aStr ) {
    if ( aStr.length < <%=User.MIN_PASSWORD_LENGTH%> || aStr.length > <%=User.MAX_PASSWORD_LENGTH%>) {
        alert("Please enter a password between <%=User.MIN_PASSWORD_LENGTH%> and <%=User.MAX_PASSWORD_LENGTH%> characters long");
        return true;
    }
    return false;
}

function isMismatchedPasswords( one, two ) {
    if ( one != two ) {
         alert("Passwords do not match");
         return true;
    }
    return false;
}

//Give initial focus to the password or username field 
$(document).ready(function(){
  if ($("em.passwordError").length > 0 && $("em.usernameError").length == 0) {
    $("input#password").focus();
  } else {
    $("input#username").focus();
  }
});
-->
</script>

<script language="JavaScript" type="text/javascript">
	initDHTMLAPI();
</script>

