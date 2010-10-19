<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:ai="http://vivoweb.org/ontology/activity-insight"
 xmlns:merge="http:www.ora.com/XSLTCookbook/mnamespaces/merge">
	
<xsl:param name="doc2"/> 
 
<xsl:template match="/*">
  <!--Copy the outter most element of the source document -->
  <xsl:copy>
    <!-- 
	For each child in the source, detemine if it should be 
    	copied to the destination based on its existence in the 
	other document.
    -->
    <xsl:for-each select="*">
    
      <!-- 
	Call a template which determines a unique key value 
	for this element. It must be
      	defined in the including stylesheet. 
      -->  
      <xsl:variable name="key-value">
        <xsl:call-template name="merge:key-value"/>
      </xsl:variable>
      
      <xsl:variable name="element" select="."/>

      <!--This for-each is simply to change context 
          to the second document 
      -->
      <xsl:for-each select="document($doc2)/*">
        <!-- 
	Use key as a mechanism for testing the precence 
        of the element in the second document. 
	The key should be defined by the 
        including stylesheet
	-->

        <xsl:if test="not(key('merge:key', $key-value))">
          <xsl:copy-of select="$element" copy-namespaces="no"/>
        </xsl:if>

      </xsl:for-each>
      
    </xsl:for-each>

    <!--Copy all elements in the second document -->
    <xsl:copy-of select="document($doc2)/*/*" copy-namespaces="no"/>
    
  </xsl:copy>
</xsl:template>


</xsl:stylesheet>
