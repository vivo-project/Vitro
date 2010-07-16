<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<script language="JavaScript" type="text/javascript" src="js/md5.js"></script>

<script language="JavaScript" type="text/javascript">
<!--
 
     function forceCancel(theForm) {           // we don't want validation to stop us if we're canceling
         theForm.Md5password.value = "CANCEL"; // a dummy string to force validation to succeed
         theForm.passwordConfirmation.value = theForm.Md5password.value;
         return true;
     }

     function hashPw(theForm) {
         if (theForm.Md5password.value != theForm.passwordConfirmation.value) {
             alert("The passwords do not match.");
             theForm.Md5password.focus();
             return false;
         }
         if (theForm.Md5password.value.length < 6 || theForm.Md5password.value.length > 12) {
             alert("Please enter a password between 6 and 12 characters long."); 
             theForm.Md5password.focus();
             return false;
         } else {    
             theForm.Md5password.value=calcMD5(theForm.Md5password.value);
             theForm.passwordConfirmation.value=theForm.Md5password.value;
             return true;
         }
     }

    function confirmDelete() {
        var msg="Are you SURE you want to delete this user? If in doubt, CANCEL."
        return confirm(msg);
    }

-->
</script>
