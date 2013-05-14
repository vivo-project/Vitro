<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that a password has been reset. -->

<#assign strings = i18n() />

<#assign subject = strings.password_reset_complete_subject(siteName) />

<#assign html = strings.password_reset_complete_email_html(siteName, 
                                                   subject, 
                                                   userAccount.firstName, 
                                                   userAccount.lastName, 
                                                   userAccount.emailAddress) />

<#assign text = strings.password_reset_complete_email_text(siteName, 
                                                   subject, 
                                                   userAccount.firstName, 
                                                   userAccount.lastName, 
                                                   userAccount.emailAddress) />

<@email subject=subject html=html text=text />