<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%/* this is used by the FedoraDatastreamController and not by the N3 editing system.*/%>

<h2>Upload a replacement for ${fileName}</h2>
  <form action="<c:url value="/fedoraDatastreamController"/>"
        enctype="multipart/form-data" method="POST">

    <p>File <input type="file" id="fileRes" name="fileRes" /></p>
    
   <%/*  <p><input type="radio" name="useNewName" value="false" checked/>
      use existing file name</p>
    <p><input type="radio" name="useNewName" value="true"/>
      rename file to name of file being uploaded</p> */%>

    <input type="hidden" name="fileUri" value="${fileUri}"/>
    <input type="hidden" name="pid" value="${pid}"/>
    <input type="hidden" name="dsid" value="${dsid}"/>
    <!--Adding use new name set to true so that it is overwritten correctly-->
	<input type="hidden" name="useNewName" value="true"/>
    <input type="submit" class="submit" value="submit" />
  </form>
