<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Confirmation that the user has changed his email account. -->

<#assign strings = i18n() />

<#assign subject = strings.email_changed_subject(siteName) />

<#assign html = strings.email_changed_html(siteName, 
                                           subject, 
                                           userAccount.firstName, 
                                           userAccount.lastName, 
                                           userAccount.emailAddress) />

<#assign text = strings.email_changed_text(siteName, 
                                           subject, 
                                           userAccount.firstName, 
                                           userAccount.lastName, 
                                           userAccount.emailAddress) />

<@email subject=subject html=html text=text />
