<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

    <h2>Connect to Jena Database</h2>

    <form action="ingest" method="post">
        <input type="hidden" name="action" value="connectDB"/>

    
	<input type="text" style="width:80%;" name="jdbcUrl" value="jdbc:mysql://localhost/"/>
    <p>JDBC URL</p>
 
    <input type="text" name="username"/>
    <p>username</p>

    <input type="password" name="password"/>
    <p>password</p>


		<input id="tripleStoreRDB" name="tripleStore" type="radio" checked="checked" value="RDB"/>
			<label for="tripleStoreRDB">Jena RDB</label>
		<input id="tripleStoreSDB" name="tripleStore" type="radio" value="SDB"/>
			<label for="tripleStoreRDB">Jena SDB (hash layout)</label>

    
        <select name="dbType">
            <option value="MySQL">MySQL</option>
        </select>
    <p>database type</p>

    <input type="submit" value="Connect Database"/>
