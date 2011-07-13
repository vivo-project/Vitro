<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<@widget name="login" include="assets" />
<#include "browse-classgroups.ftl">

<!DOCTYPE html>
<html lang="en">
    <head>
        <#include "head.ftl">
    </head>
    
    <body class="${bodyClasses!}">
        <#include "identity.ftl">
        
        <#include "menu.ftl">
        
            <section id="intro" role="region">
                <h2>What is VITRO?</h2>
                
                <p>Vitro is a general-purpose web-based ontology and instance editor with customizable public browsing. Vitro is a Java web application that runs in a Tomcat servlet container.</p>
                <p>With Vitro, you can:</p>
                
                <ul>
                    <li>Create or load ontologies in OWL format</li>
                    <li>Edit instances and relationships</li>
                    <li>Build a public web site to display your data</li>
                    <li>Search your data</li>
                </ul>
            </section> <!-- #intro -->
            
            <@widget name="login" />
            
            <@allClassGroups vClassGroups />
        
        <#include "footer.ftl">
    </body>
</html>