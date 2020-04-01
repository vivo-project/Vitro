<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<@widget name="login" include="assets" />
<#import "lib-home-page.ftl" as lh>

<!DOCTYPE html>
<!-- vitro page-home.ftl  -->
<html lang="${country}">
    <head>
        <#include "head.ftl">
    </head>
    
    <body class="${bodyClasses!}">
        <header id="branding" role="banner">
            <#include "identity.ftl">
        </header>
        <#include "menu.ftl">

        <div id="wrapper-content" role="main">
            <section id="intro" role="region">
                <h2>${i18n().what_is_vitro}</h2>

                <p>${i18n().vitro_description}</p>
                <p>${i18n().with_vitro}</p>

                <ul>
                    <li>${i18n().vitro_bullet_one}</li>
                    <li>${i18n().vitro_bullet_two}</li>
                    <li>${i18n().vitro_bullet_three}</li>
                    <li>${i18n().vitro_bullet_four}</li>
                </ul>

                <section id="search-home" role="region">
                    <h3>${i18n().search_vitro} <span class="search-filter-selected">filteredSearch</span></h3>

                    <fieldset>
                        <legend>${i18n().search_form}</legend>
                        <form id="search-homepage" action="${urls.search}" name="search-home" role="search" method="post" >
                            <div id="search-home-field">
                                <input type="text" name="querytext" class="search-homepage" value="${querytext!}" autocapitalize="off" />
                                <input type="submit" value="${i18n().search_button}" class="search" />
                                <input type="hidden" name="classgroup" class="search-homepage" value="" autocapitalize="off" />
                            </div>

                            <a class="filter-search filter-default" href="#" title="${i18n().filter_search}"><span class="displace">${i18n().filter_search}</span></a>

                            <ul id="filter-search-nav">
                                <li><a class="active" href="">${i18n().all_capitalized}</a></li>
                                <@lh.allClassGroupNames vClassGroups! />
                            </ul>
                        </form>
                    </fieldset>
                </section> <!-- #search-home -->

            </section> <!-- #intro -->

            <@widget name="login" />

            <!-- Statistical information relating to property groups and their classes; displayed horizontally, not vertically-->
            <@lh.allClassGroups vClassGroups! />
        </div>
        
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