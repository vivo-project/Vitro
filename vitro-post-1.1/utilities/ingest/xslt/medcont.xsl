<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:aimc="http://vivoweb.org/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:vfx='http://vivoweb.org/ext/functions'	
	exclude-result-prefixes='vfx xs dm'
>

<xsl:param name='listxml' required='yes'/>
<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>
<xsl:variable name='NL'>
<xsl:text>
</xsl:text>
</xsl:variable>


<xsl:template match='/'>


<xsl:variable name='docs' as='node()*'
	select='collection($listxml)'/>

<aimc:MEDCONT_LIST>

<xsl:for-each-group select='$docs//dm:MEDCONT' 
	group-by='@id'>
<xsl:sort select='@id'/>

<xsl:for-each select='current-group()'>

<aimc:MEDCONT>
<xsl:attribute name='id' select='@id'/>
<aimc:netid><xsl:value-of select='../../dm:Record/@username'/></aimc:netid>
<aimc:uid><xsl:value-of select='../../dm:Record/@userId'/></aimc:uid>

</aimc:MEDCONT>


</xsl:for-each>

</xsl:for-each-group>
</aimc:MEDCONT_LIST>
<xsl:value-of select='$NL'/>
</xsl:template>
</xsl:stylesheet>

