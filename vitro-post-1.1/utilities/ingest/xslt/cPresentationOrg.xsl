<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:aipres="http://vivoweb.org/ontology/activity-insight"
xmlns:dm='http://www.digitalmeasures.com/schema/data'
xmlns:xs='http://www.w3.org/2001/XMLSchema'
xmlns:vfx='http://vivoweb.org/ext/functions'	
exclude-result-prefixes='vfx xs dm aipres'
>


<xsl:param name='listxml' required='yes'/>
<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>
<xsl:variable name='NL'>
<xsl:text>
</xsl:text>
</xsl:variable>

<!-- define global variables here -->

<xsl:template match='/'>

<xsl:variable name='docs' as='node()*'
	select='collection($listxml)'/>

<aipres:PRESENT_ORG_LIST>

<xsl:for-each-group select='$docs//dm:Record/dm:PRESENT' 
	group-by='vfx:collapse(dm:ORG)'>
<xsl:sort select='vfx:collapse(dm:ORG)'/>

<!-- define variables here -->
<aipres:PRESENT_ORG>

<xsl:attribute name='index' select='position()'/>

<xsl:variable name='name' 
	select='vfx:simple-trim(dm:ORG)'/>
<aipres:PRESENT_ORG_NAME>
<xsl:choose>
  <xsl:when test='$name != ""'>

  <xsl:value-of select='replace($name,"^\(.*?\)$",
				substring($name,
					  2,
					  string-length($name)-2))'/>

<!--
  <xsl:value-of select='$name'/>	
-->
  </xsl:when>
  <xsl:otherwise>
  <xsl:value-of select='"Unspecified"'/>
  </xsl:otherwise>
</xsl:choose>

</aipres:PRESENT_ORG_NAME>

<aipres:PRESENT_LIST>

<xsl:for-each select='current-group()'>
<xsl:sort select='@id'/>
<xsl:if test='dm:USER_REFERENCE_CREATOR = "Yes"'>
<aipres:PRESENT_INFO>
<!-- define member attributes here -->

<xsl:attribute name='public'>
       <xsl:value-of select='../dm:PUBLIC_VIEW'/>
</xsl:attribute>

<xsl:attribute name='ref_netid'>
<xsl:value-of select='../../dm:Record/@username'/>
</xsl:attribute>

<xsl:attribute name='hasGoodAuthor' 
select='if(vfx:hasOneGoodName(dm:PRESENT_AUTH)) then "Yes" else "No"'/>
<xsl:attribute name='hasTitle' 
select='if(normalize-space(dm:TITLE) = "") then "No" else "Yes"'/>

<xsl:attribute name='hasConf' 
select='if(normalize-space(dm:NAME) = "") then "No" else "Yes"'/>

<xsl:attribute name='public' select='dm:PUBLIC_VIEW'/>


<xsl:value-of select='@id'/>

</aipres:PRESENT_INFO>
</xsl:if>

</xsl:for-each>

</aipres:PRESENT_LIST>
</aipres:PRESENT_ORG>

</xsl:for-each-group>

</aipres:PRESENT_ORG_LIST>
<xsl:value-of select='$NL'/>
</xsl:template>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
