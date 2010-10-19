<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:aigrant="http://vivoweb.org/ontology/activity-insight"
xmlns:dm='http://www.digitalmeasures.com/schema/data'
xmlns:xs='http://www.w3.org/2001/XMLSchema'
xmlns:vfx='http://vivoweb.org/ext/functions'	
exclude-result-prefixes='vfx xs dm'
>

<!-- created 20100806102142 -->
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

<aigrant:SPONSOR_ORG_LIST>

<xsl:for-each-group select='$docs//dm:Record/dm:CONGRANT' 
	group-by='vfx:collapse(dm:SPONSOR_LEVEL)'>
<xsl:sort select='vfx:collapse(dm:SPONSOR_LEVEL)'/>

<!-- define variables here -->


<aigrant:SPONSOR_ORG>
<xsl:attribute name='index' select='position()'/>

<aigrant:SPONSOR_ORG_NAME>
<xsl:value-of select='dm:SPONSOR_LEVEL'/></aigrant:SPONSOR_ORG_NAME>

<aigrant:GRANTS_LIST>

<xsl:for-each select='current-group()'>
<xsl:sort select='@id'/>
<xsl:if test='dm:USER_REFERENCE_CREATOR = "Yes" and 
dm:STATUS = "Award Signed By All Parties"	'>
<aigrant:GRANT_INFO>
<!-- define member attributes here -->

<xsl:attribute name='grid' select='@id'/>
<xsl:attribute name='ref_netid'>
<xsl:value-of select='../../dm:Record/@username'/></xsl:attribute>

<!-- define member property sub tags here -->

</aigrant:GRANT_INFO>
</xsl:if>

</xsl:for-each>

</aigrant:GRANTS_LIST>
</aigrant:SPONSOR_ORG>

</xsl:for-each-group>

</aigrant:SPONSOR_ORG_LIST>
<xsl:value-of select='$NL'/>
</xsl:template>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
