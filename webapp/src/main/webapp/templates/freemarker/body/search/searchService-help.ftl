<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<h1>Search Web Service</h1>

<#if msg?has_content >
  <h2>Message:</h2>
  <p>${msg}</p>
</#if>

<h2>Update Search Index for URIs</h2>

<p>This service will update the search index for the list of URIs it
receives. It expectes a POST with an encoding of
multpart/form-data. The service inspect all parts of this POST for
lists of URIs to reindex. The URIs should be separated by commas and/or white space.
If no information can be found for a URI it will be ignored.</p>

<p>The request parameters email and password allow the form to be submitted 
for a user account.  Only internal accounts are supported, external authentication 
is not supported.</p>

<h2>Example form for Update Search Index for URIs</h2>
<p>The following form will post to the Update URIs in search service.</p>

<form action="${urls.base}/searchService/updateUrisInSearch"
      enctype="multipart/form-data"
      method="post">
      
    <label for="email">Account email</label>
    <input type="text" name="email" id="email"/>

    <label for="password">Account password</label>
    <input type="text" name="password" id="password"/>

    <label for="urisToUpdate">File of URIs to update in the search index</label>
    <input type="file" name="datafile" size="30" />

    <button type="submit">submit</button>
</form>