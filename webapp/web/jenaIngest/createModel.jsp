<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

    <h2>Create New Model</h2>

    <form style="margin-bottom:2ex;" action="ingest" method="post">
        <input type="hidden" name="action" value="createModel"/>
	Model name: <input type="text" size="32" name="modelName"/>
        <input type="submit" name="submit" value="Create Model"/>
        <input type="hidden" name="modelType" value="${modelType}"/>
    </form>
