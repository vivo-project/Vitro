<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that an account has been created. -->

<#assign strings = i18n() />

<#assign subject = strings.account_created(siteName) />

<#assign html = strings.account_created_email_html(siteName, 
                                                   subject, 
                                                   userAccount.firstName, 
                                                   userAccount.lastName, 
                                                   userAccount.emailAddress, 
                                                   passwordLink) />

<#assign text = strings.account_created_email_text(siteName, 
                                                   subject, 
                                                   userAccount.firstName, 
                                                   userAccount.lastName, 
                                                   userAccount.emailAddress, 
                                                   passwordLink) />

<@email subject=subject html=html text=text />