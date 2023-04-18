<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#--Breaking this out so this can be utilized by other pages such as the jsp advanced tools pages-->

<section id="search" role="region">
    <fieldset>
        <legend>${i18n().search_form}</legend>

            <div id="search-field">
                <input form="extended-search-form" id="filter_input_querytext" type="text" name="querytext" class="search-vivo" placeholder="${i18n().search_field_placeholder}"  value="${querytext!}" autocapitalize="off" />
                <input form="extended-search-form" type="submit" value="${i18n().search_button}" class="search">
            </div>
    </fieldset>
</section>
