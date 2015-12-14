<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<div>
    <h2>Display Admin and configuration</h2>
    
    <#if errorMessage??>
        <div id="errorAlert">
            <img src="../images/iconAlert.png" alt="Error alert icon" height="31" width="32">
            <p>${errorMessage}</p>
        </div>
    </#if>
    
    <#if message??>
        <p>${message}</p>
    </#if>
    
    
    <ul>
    <li> Some link to a display config and amdin page </li>
    </ul>
    
</div>