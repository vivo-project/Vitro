<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<@widget name="login" include="assets" />
<#import "lib-home-page.ftl" as lh>

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
                
                <section id="search-home" role="region">
                    <h3>Search VITRO <span class="search-filter-selected">filteredSearch</span></h3>
                    
                    <fieldset>
                        <legend>Search form</legend>
                        <form id="search-homepage" action="${urls.search}" name="search-home" role="search" method="post" > 
                            <div id="search-home-field">
                                <input type="text" name="querytext" class="search-homepage" value="${querytext!}" autocapitalize="off" />
                                <input type="submit" value="Search" class="search" />
                                <input type="hidden" name="classgroup" class="search-homepage" value="" autocapitalize="off" />
                            </div>
                            
                            <a class="filter-search filter-default" href="#" title="Filter search"><span class="displace">filter search</span></a>
                            
                            <ul id="filter-search-nav">
                                <li><a class="active" href="">All</a></li>
                                <@lh.allClassGroupNames vClassGroups! />
                            </ul>
                        </form>
                    </fieldset>
                </section> <!-- #search-home -->
                
            </section> <!-- #intro -->
            
            
            
            <@widget name="login" />
                        
            <!-- Statistical information relating to property groups and their classes; displayed horizontally, not vertically-->
            <@lh.allClassGroups vClassGroups! />
        
        <#include "footer.ftl">
        <script>
            // this will ensure that the hidden classgroup input is cleared if the back button is used
            // to return to the home page from the search results. Not in vitroUtils.js because that
            // gets loaded on every page.
            $(document).ready(function(){
                $('input[name="classgroup"]').val("");    
            });
        </script>
    </body>
</html>