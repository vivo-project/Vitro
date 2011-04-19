<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for Site Administration data input panel -->

<#import "lib-form.ftl" as form>

<#if dataInput?hasContent>
    <div class="pageBodyGroup">
        
        <h3>Data Input</h3>

        <form action="${dataInput.formAction}" method="get">
            <select id="VClassURI" name="typeOfNew" class="form-item">
                <@form.optionGroups groups=dataInput.groupedClassOptions />
            </select>
            <input type="hidden" name="editform" value="newIndividualForm.jsp"/>
            <input type="submit" id="submit" value="Add individual of this class"/>
        </form>
            
    </div>
</#if>