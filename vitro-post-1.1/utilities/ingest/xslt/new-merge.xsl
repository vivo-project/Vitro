<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:ai="http://vivoweb.org/ontology/activity-insight"
xmlns:xs='http://www.w3.org/2001/XMLSchema'
xmlns:vfx='http://vivoweb.org/ext/functions'	
exclude-result-prefixes='vfx xs'>
	
<xsl:param name="doc2"/> 
 
<xsl:template match="/*">
  <!--Copy the outer most element of the source document -->
  <xsl:copy>
    <!-- 
	For each child in the source, detemine if it should be 
    	copied to the destination based on its existence in the 
	other document.
    -->
    <xsl:for-each select="*">
    
    
      
      <xsl:variable name="element" select="."/>

      <!--This for-each is simply to change context 
          to the second document 
      -->
      <xsl:for-each select="document($doc2)//person">
        <!-- 

	-->
	<xsl:comment><xsl:value-of select='lname'/></xsl:comment>
        <xsl:if test="not(vfx:isoName($element/fname,
				      $element/mname,
				      $element/lname,
				      fname,
				      mname,
				      lname))">
          <xsl:copy-of select="$element" copy-namespaces="no"/>
        </xsl:if>

      </xsl:for-each>
      
    </xsl:for-each>

    <!--Copy all elements in the second document -->
    <xsl:copy-of select="document($doc2)/*/*" copy-namespaces="no"/>
    
  </xsl:copy>
</xsl:template>


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
