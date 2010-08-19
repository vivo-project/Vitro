<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:aigrant="http://vivoweb.org/activity-insight"
xmlns="http://vivoweb.org/activity-insight"
xmlns:dm='http://www.digitalmeasures.com/schema/data'
xmlns:xs='http://www.w3.org/2001/XMLSchema'
xmlns:vfx='http://vivoweb.org/ext/functions'	
exclude-result-prefixes='vfx xs'
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

<aigrant:GRANTING_ORG_LIST>

<xsl:for-each-group select='$docs//dm:Record/dm:CONGRANT' 
	group-by='vfx:collapse(dm:SPONORG)'>
<xsl:sort select='vfx:collapse(dm:SPONORG)'/>

<!-- define variables here -->

<aigrant:GRANTING_ORG>
<xsl:attribute name='index' select='position()'/>

<aigrant:GRANTING_ORG_NAME><xsl:value-of select='dm:SPONORG'/></aigrant:GRANTING_ORG_NAME>

<aigrant:GRANT_LIST>

<xsl:for-each select='current-group()'>
<xsl:sort select='@id'/>
<xsl:if test='dm:USER_REFERENCE_CREATOR = "Yes"'>
<aigrant:GRANT>
<!-- define member attributes here -->

<xsl:attribute name='id' select='@id'/>

<!-- define member property sub tags here -->

</aigrant:GRANT>
</xsl:if>

</xsl:for-each>

</aigrant:GRANT_LIST>
</aigrant:GRANTING_ORG>

</xsl:for-each-group>

</aigrant:GRANTING_ORG_LIST>
</xsl:template>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
