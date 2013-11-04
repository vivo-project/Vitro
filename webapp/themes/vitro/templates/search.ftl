 <#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
 
<section id="search" role="region">
    <fieldset>
        <legend>${i18n().search_form}</legend>

        <form id="search-form" action="${urls.search}" name="search" role="search" accept-charset="UTF-8" method="POST"> 
            <div id="search-field">
                <input type="text" name="querytext" class="search-vitro" value="${querytext!}" autocapitalize="off" />
                <input type="submit" value="${i18n().search_button}" class="search">
            </div>
        </form>
    </fieldset>
</section>