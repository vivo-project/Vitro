<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that an account has been created. -->

<#assign subject = strings.account_created(siteName) />

<#assign html = strings.account_created_external_email_html(siteName, 
                                                   subject, 
                                                   userAccount.firstName, 
                                                   userAccount.lastName, 
                                                   userAccount.emailAddress) />

<#assign text = string.account_created_external_email_text(siteName, 
                                                   subject, 
                                                   userAccount.firstName, 
                                                   userAccount.lastName, 
                                                   userAccount.emailAddress) />

<@email subject=subject html=html text=text />