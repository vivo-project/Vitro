<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Template for the Revision Information page. -->

<div>
    <h1>Revision Information</h1>
    
    <h2>Build date:</h2>
    <div>
    	${revisionInfoBean.buildDate?datetime?string.full}
    </div>
    
    <h2>Levels:</h2>
    <table>
    	<tr>
    		<th>name</th>
    		<th>release</th>
    		<th>revision</th>
    	</tr>
	    <#list revisionInfoBean.levelInfos as level>
	    	<tr>
	    		<td>${level.name}</td>
	    		<td>${level.release}</td>
	    		<td>${level.revision}</td>
	    	</tr>
		</#list>
	</table>
</div>
