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
      <!-- xsl:comment>
	<xsl:value-of select='concat($element/fname,"|",$element/mname,"|",$element/lname)'/></xsl:comment-->
      <!--This for-each is simply to change context 
          to the second document 
      -->
      <xsl:variable name='found' 
	select='vfx:perSearch($element,document($doc2)//person)'/>

<!--
	$ele,document($doc2)//person
      <xsl:for-each select="document($doc2)//person">
      
        <xsl:if test="not(vfx:isoName($element/fname,
				      $element/mname,
				      $element/lname,
				      fname,
				      mname,
				      lname))">
          
        </xsl:if>

      </xsl:for-each>
-->
    <xsl:comment><xsl:value-of select='$found'/></xsl:comment>
    <xsl:if test='not($found)'>
	<xsl:comment><xsl:value-of select='"Copied"'/></xsl:comment>
	<xsl:copy-of select="$element" copy-namespaces="no"/>
    </xsl:if>
    </xsl:for-each>

    <!--Copy all elements in the second document -->
    <xsl:copy-of select="document($doc2)/*/*" copy-namespaces="no"/>
    
  </xsl:copy>
</xsl:template>

<xsl:template name='perSearch'>
<xsl:param name='ele'/>
<xsl:param name='nlist'/>
<xsl:param name='res' select='false()'/>
<xsl:choose>
<xsl:when test='$nlist and not($res)'>
<xsl:variable name='comp' 
select='vfx:isoName($ele/fname,
                    $ele/mname,
                    $ele/lname, 
                    $nlist[1]/fname,
                    $nlist[1]/mname,
                    $nlist[1]/lname)'/>
<xsl:call-template name='perSearch'>
<xsl:with-param name='ele' select='$ele'/>
<xsl:with-param name='nlist' select='$nlist[position()>1]'/>
<xsl:with-param name='res' select='$res or $comp'/>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='$res'/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:function name='vfx:perSearch' as='xs:boolean'>
<xsl:param name='ele'/>
<xsl:param name='nlist'/>
<xsl:call-template name='perSearch'>
<xsl:with-param name='ele' select='$ele'/>
<xsl:with-param name='nlist' select='$nlist'/>
<xsl:with-param name='res' select='false()'/>
</xsl:call-template>
</xsl:function>

<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
