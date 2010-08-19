<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
 <head> 
  
  <script src="../js/jquery.js" type="text/javascript" language="javascript"></script>
  <script src="../js/jquery_plugins/jquery.MultiFile.js" type="text/javascript" language="javascript"></script>

  <title> file upload for ${projectName} </title>
</head>
<body>
  <form action="fileUploadProcess.jsp" method="post" enctype="multipart/form-data">  
    <input type="text" name="creator" value="Brian Caruso"/></br>
    <input type="file" class="multi" name="file1" value="file1"/></br>
    <input type="submit" name="submit" value="Submit"/>
  </form>
  
   <form action="" class="P10">
       <input type="file" class="multi" accept="gif|jpg"/>
      </form>
</body>
</html>
