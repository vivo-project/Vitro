<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:aipres="http://vivoweb.org/ontology/activity-insight"
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

<aipres:PRESENT_LIST>

<xsl:for-each-group select='$docs//dm:PRESENT'
	group-by='@id'>
<xsl:sort select='@id'/>

<aipres:PRESENT>

<xsl:attribute name='index' select='position()'/>
<xsl:for-each select='current-group()'>
<xsl:sort select='@id'/>
<xsl:if test='dm:USER_REFERENCE_CREATOR = "Yes"'>

<aipres:netid><xsl:value-of select='../../dm:Record/@username'/></aipres:netid>

<aipres:PRESENT_ID>
<xsl:attribute name='public'>
       <xsl:value-of select='dm:PUBLIC_VIEW'/>
</xsl:attribute>
<xsl:attribute name='hasGoodAuthor' 
select='if(vfx:hasOneGoodName(dm:PRESENT_AUTH)) then "Yes" else "No"'/>

<xsl:attribute name='hasTitle'>
<xsl:value-of
select='if(normalize-space(dm:TITLE) = "") then "No" else "Yes"'/>
</xsl:attribute>

<xsl:attribute name='hasOrg'>
<xsl:value-of 
select='if(normalize-space(dm:ORG) = "") then "No" else "Yes"'/>
</xsl:attribute>

<xsl:attribute name='hasConf'>
<xsl:value-of 
select='if(normalize-space(dm:NAME) = "") then "No" else "Yes"'/>
</xsl:attribute>

<xsl:value-of select='@id'/>

</aipres:PRESENT_ID>
</xsl:if>
<!-- other stuff may go here -->

</xsl:for-each>

</aipres:PRESENT>

</xsl:for-each-group>
</aipres:PRESENT_LIST>

<xsl:value-of select='$NL'/>
</xsl:template>

<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
