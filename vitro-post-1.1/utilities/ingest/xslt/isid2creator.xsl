<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:aiis="http://vivoweb.org/ontology/activity-insight"
	xmlns:aiic="http://vivoweb.org/ontology/activity-insight"
	xmlns="http://vivoweb.org/ontology/activity-insight"
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

<xsl:for-each-group select='$docs//IMPACT_STATEMENT' 
	group-by='@id'>
<xsl:sort select='@id'/>

<xsl:for-each select='current-group()'>
<xsl:if test='USER_REFERENCE_CREATOR ="Yes"'>
<aiis:IMPACT_STMT_CREATOR>
<xsl:attribute name='isid' select='@id'/>
<aiis:netid><xsl:value-of select='../../Record/username'/></aiis:netid>
<aiis:uid><xsl:value-of select='../../Record/userId'/></aiis:uid>
<xsl:variable name='uid'  select='../../Record/userId'/>
<xsl:variable name='invest' select='COLLABORATORS/IMPACT_STATEMENT_INVEST[FACULTY_NAME=$uid]'/>
<!-- xsl:comment> <xsl:value-of select='$invest/FACULTY_NAME'/></xsl:comment -->
<aiis:listed_investigator>
<xsl:value-of select='if($invest/FACULTY_NAME = "" or not($invest/FACULTY_NAME)) then "No" else "Yes" '/>
</aiis:listed_investigator>
</aiis:IMPACT_STMT_CREATOR>

</xsl:if>
</xsl:for-each>

</xsl:for-each-group>
</aiis:CREATOR_LIST>
</xsl:template>
</xsl:stylesheet>