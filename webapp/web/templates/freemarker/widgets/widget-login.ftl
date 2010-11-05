<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Login widget -->

<#macro assets>
    <#if ! loginName??>
        ${stylesheets.add("/css/login.css")}
        <#-- define any js files needed for the login widget 
        ${scripts.add("")}
        ${headScripts.add("")} -->
    </#if>
</#macro>

<#macro markup>
    <#if ! loginName??>
    <section id="intro">
        <h3>What is VIVO?</h3>
        
        <p>VIVO is an open source semantic web application originally developed and implemented at Cornell. When installed and populated with researcher interests, activities, and accomplishments, it enables the discovery of research and scholarship across disciplines at that institution. VIVO supports browsing and a search function which returns faceted results for rapid retrieval of desired information. Content in any local VIVO installation may be maintained manually,  brought into VIVO in automated ways from local systems of record, such as HR, grants, course, and faculty activity databases, or from database providers such as publication aggregators and funding agencies. <a href="#">More<span class="pictos-arrow-14"> 4</span></a></p>
        <section id="search-home">
            <h3>Search VIVO</h3>
            
            <fieldset>
                <legend>Search form</legend>
                
                <form id="search-home-vivo" action="${urls.search}" method="post" name="search">
                    <div id="search-home-field">
                        <input name="search-home-vivo" class="search-home-vivo" id="search-home-vivo"  type="text" />
                        
                        <a class ="submit" href="#">Search</a>
                    </div>
                </form>
            </fieldset>
        </section> <!-- #search-home -->
    </section> <!-- #intro -->
    
        <section id="log-in">
            <h2>Log in</h2>

            <form id="log-in-form" action="${urls.home}/authenticate?login=block" method="post" name="log-in-form" />
                <label for="email">Email</label>
                <input class="text-field" name="loginName" id="loginName" type="text" required />

                <label for="password">Password</label>
                <input class="text-field" name="loginPassword" id="password" type="loginPassword" required />
                
                <p class="submit"><input name="loginForm" type="submit" class="green button" value="Log in"/></p>
    
                <input class="checkbox-remember-me" name="remember-me" type="checkbox" value="" />  
                <label class="label-remember-me" for="remember-me">Remember me</label>
            </form>

            <p class="forgot-password"><a href="#">Forgot your password?</a></p>
            <p class="request-account"><a class=" blue button" href="#">Request an account</a> </p>
        </section><!-- #log-in -->
 
    <#else><#-- Temporary fix for not showing log-in widget after users log-in-->
    <section id="intro-no-login">
        <h3>What is VIVO?</h3>
        
        <p>VIVO is an open source semantic web application originally developed and implemented at Cornell. When installed and populated with researcher interests, activities, and accomplishments, it enables the discovery of research and scholarship across disciplines at that institution. VIVO supports browsing and a search function which returns faceted results for rapid retrieval of desired information. Content in any local VIVO installation may be maintained manually,  brought into VIVO in automated ways from local systems of record, such as HR, grants, course, and faculty activity databases, or from database providers such as publication aggregators and funding agencies. <a href="#">More<span class="pictos-arrow-14"> 4</span></a></p>
        <section id="search-home">
            <h3>Search VIVO</h3>
            
            <fieldset>
                <legend>Search form</legend>
                
                <form id="search-home-vivo" action="${urls.search}" method="post" name="search">
                    <div id="search-home-field">
                        <input name="search-home-vivo" class="search-home-vivo" id="search-home-vivo"  type="text" />
                        
                        <a class ="submit" href="#">Search</a>
                    </div>
                </form>
            </fieldset>
        </section> <!-- #search-home -->
    </section> <!-- #intro -->
  
    </#if>
</#macro> -->