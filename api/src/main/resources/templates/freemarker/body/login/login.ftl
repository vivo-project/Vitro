<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- Main template for the login page -->
<#import "widget-login.ftl" as login>

<@login.assets/>
<@login.loginForm/>

<script>
$('div.vivoAccount').show();
</script>

