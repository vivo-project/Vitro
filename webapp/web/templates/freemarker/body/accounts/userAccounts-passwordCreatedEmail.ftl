<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that an password has been created. -->

<#assign strings = i18n() />

<#assign subject = strings.password_created_subject(siteName) />

<#assign html = strings.password_created_email_html(siteName, 
                                                   subject, 
                                                   userAccount.firstName, 
                                                   userAccount.lastName, 
                                                   userAccount.emailAddress) />

<#assign text = strings.password_created_email_text(siteName, 
                                                   subject, 
                                                   userAccount.firstName, 
                                                   userAccount.lastName, 
                                                   userAccount.emailAddress) />

<@email subject=subject html=html text=text />