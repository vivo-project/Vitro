<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation email for user account password reset -->

<#assign strings = i18n() />

<#assign subject = strings.password_reset_pending_subject(siteName) />

<#assign html = strings.password_reset_pending_email_html(siteName, 
                                                   subject, 
                                                   userAccount.firstName, 
                                                   userAccount.lastName, 
                                                   userAccount.emailAddress, 
                                                   passwordLink) />

<#assign text = strings.password_reset_pending_email_text(siteName, 
                                                   subject, 
                                                   userAccount.firstName, 
                                                   userAccount.lastName, 
                                                   userAccount.emailAddress, 
                                                   passwordLink) />

<@email subject=subject html=html text=text />