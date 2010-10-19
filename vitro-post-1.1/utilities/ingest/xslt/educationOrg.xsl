<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:aiedu="http://vivoweb.org/ontology/activity-insight"
xmlns:dm='http://www.digitalmeasures.com/schema/data'
xmlns:xs='http://www.w3.org/2001/XMLSchema'
xmlns:vfx='http://vivoweb.org/ext/functions'	
exclude-result-prefixes='vfx xs dm aiedu'
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

<aiedu:EDUCATION_ORG_LIST>

<xsl:for-each-group select='$docs//dm:Record/dm:EDUCATION' 
	group-by='vfx:collapse(dm:SCHOOL)'>
<xsl:sort select='vfx:collapse(dm:SCHOOL)'/>

<!-- define variables here -->

<aiedu:EDUCATION_ORG>
<xsl:attribute name='index' select='position()'/>

<aiedu:EDUCATION_ORG_NAME>
<xsl:variable name='gen_orgname' select='vfx:trim(dm:SCHOOL)'/>
<xsl:choose>
  <xsl:when test='$gen_orgname != ""'>
  <xsl:value-of select='$gen_orgname'/>
  </xsl:when>
  <xsl:otherwise>
  <xsl:value-of select='"Unspecified"'/>
  </xsl:otherwise>
</xsl:choose>

</aiedu:EDUCATION_ORG_NAME>

<aiedu:EDUCATION_LIST>

<xsl:for-each select='current-group()'>
<xsl:sort select='@id'/>
<xsl:if test='dm:PUBLIC_VIEW = "Yes"'>
<aiedu:EDUCATION_INFO>
<!-- define member attributes here -->

<xsl:attribute name='id' select='@id'/>
<xsl:attribute name='ref_netid'>
<xsl:value-of select='../../dm:Record/@username'/></xsl:attribute>
<!-- define member property sub tags here -->

</aiedu:EDUCATION_INFO>
</xsl:if>

</xsl:for-each>

</aiedu:EDUCATION_LIST>
</aiedu:EDUCATION_ORG>

</xsl:for-each-group>

</aiedu:EDUCATION_ORG_LIST>
<xsl:value-of select='$NL'/>
</xsl:template>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
