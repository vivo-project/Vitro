<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<script language="JavaScript" type="text/javascript">
<!--
     var bypassValidateUF = false;
     
     function forceCancel(theForm) {           // we don't want validation to stop us if we're canceling
         theForm.Md5password.value = "CANCEL"; // a dummy string to force validation to succeed
         theForm.passwordConfirmation.value = theForm.Md5password.value;
         bypassValidateUF = true;
         return true;
     }

     function forceCancelTwo(theForm) {           // called when there are no password fields displayed
         bypassValidateUF = true;
         return true;
     }

     function validateUserFields(theForm) {
         if ( bypassValidateUF ) {
             return true;
         }
         if (theForm.Username.value.length == 0 ) {
             alert("Please enter a valid Email address.");
             theForm.Username.focus();
             return false;
         }
        if (theForm.FirstName.value.length == 0 ) {
             alert("Please enter a First Name.");
             theForm.FirstName.focus();
             return false;
         }
         if (theForm.LastName.value.length == 0 ) {
             alert("Please enter a Last Name.");
             theForm.LastName.focus();
             return false;
         }
		 else {    
             return true;
         }
     }

     function validatePw(theForm) {

         if ( !validateUserFields(theForm) ) {
                 return false;
         }
         if (theForm.Md5password.value.length == 0 ) {
             alert("Please enter a password.");
             theForm.Md5password.focus();
             return false;
         }
         if (theForm.Md5password.value != theForm.passwordConfirmation.value) {
             alert("The passwords do not match.");
             theForm.Md5password.focus();
             return false;
         }
         if (theForm.Md5password.value.length < 6 || theForm.Md5password.value.length > 12) {
             alert("Please enter a password between 6 and 12 characters long."); 
             theForm.Md5password.focus();
             return false;
         } 
 		 else {    
             return true;
         }
     }

    function confirmDelete() {
        bypassValidateUF = true;
        var msg="Are you SURE you want to delete this user? If in doubt, CANCEL."
        var answer = confirm(msg)
        if ( answer ) 
                return true;
        else
                bypassValidateUF = false;
                return false;
    }

-->
</script>
