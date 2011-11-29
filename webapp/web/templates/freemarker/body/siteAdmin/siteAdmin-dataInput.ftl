<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for Site Administration data input panel -->

<#import "lib-form.ftl" as form>

<#if dataInput?has_content>
    <section class="pageBodyGroup" role="region">
        <h3>Data Input</h3>

        <form action="${dataInput.formAction}" method="get">
            <select id="VClassURI" name="typeOfNew" class="form-item" role="select">
                <@form.optionGroups groups=dataInput.groupedClassOptions />
            </select>
            <input type="hidden" name="editForm" value="edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.NewIndividualFormGenerator" role="input" />
            <input type="submit" id="submit" value="Add individual of this class" role="button" />
        </form>
    </section>
</#if>