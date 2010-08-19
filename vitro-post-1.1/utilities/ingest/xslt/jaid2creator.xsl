<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:aiis="http://vivoweb.org/activity-insight"
	xmlns:aiic="http://vivoweb.org/activity-insight"
	xmlns="http://vivoweb.org/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:vfx='http://vivoweb.org/ext/functions'	
	exclude-result-prefixes='vfx xs'
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

<aiis:CREATOR_LIST>

<xsl:for-each-group select='$docs//dm:INTELLCONT_JOURNAL' 
	group-by='@id'>
<xsl:sort select='@id'/>

<xsl:for-each select='current-group()'>
<xsl:if test='dm:USER_REFERENCE_CREATOR ="Yes"'>
<aiis:INTELLCONT_JOURNAL_CREATOR>
<xsl:attribute name='isid' select='@id'/>
<aiis:netid><xsl:value-of select='../../dm:Record/dm:username'/></aiis:netid>
<aiis:uid><xsl:value-of select='../../dm:Record/dm:userId'/></aiis:uid>
<xsl:variable name='uid'  select='../../dm:Record/dm:userId'/>
<xsl:variable name='author' select='dm:AuthorList/dm:INTELLCONT_JOURNAL_AUTH[dm:FACULTY_NAME=$uid]'/>
<!-- xsl:comment> <xsl:value-of select='$author/dm:FACULTY_NAME'/></xsl:comment -->
<aiis:listed_author>
<xsl:value-of select='if($author/dm:FACULTY_NAME = "" or not($author/dm:FACULTY_NAME)) then "No" else "Yes" '/>
</aiis:listed_author>
</aiis:INTELLCONT_JOURNAL_CREATOR>

</xsl:if>
</xsl:for-each>

</xsl:for-each-group>
</aiis:CREATOR_LIST>
</xsl:template>
</xsl:stylesheet>