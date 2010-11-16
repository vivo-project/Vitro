<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:aiah="http://vivoweb.org/ontology/activity-insight"
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

<aiah:AWARD_LIST>

<xsl:for-each-group select='$docs//dm:AWARDHONOR' 
	group-by='@id'>
<xsl:sort select='@id'/>

<xsl:for-each select='current-group()'>

<aiah:AWARD>
<xsl:attribute name='isid' select='@id'/>
<aiah:org>
	<xsl:value-of select='normalize-space(dm:ORG)'/>
</aiah:org>
<aiah:year>
	<xsl:value-of select='dm:DTY_END'/></aiah:year>
<aiah:award_name>
	<xsl:value-of select='normalize-space(dm:NAME)'/>
</aiah:award_name>
<aiah:display><xsl:value-of select='dm:PUBLIC_VIEW'/></aiah:display>
<aiah:recipient>

<aiah:netid><xsl:value-of select='../../dm:Record/@username'/></aiah:netid>
<aiah:uid><xsl:value-of select='../../dm:Record/@userId'/></aiah:uid>

<aiah:fname>
<xsl:value-of 
	select='normalize-space(../../dm:Record/dm:PCI/dm:FNAME)'/>
</aiah:fname>
<aiah:mname>
<xsl:value-of 
	select='normalize-space(../../dm:Record/dm:PCI/dm:MNAME)'/>
</aiah:mname>
<aiah:lname>
<xsl:value-of 
	select='normalize-space(../../dm:Record/dm:PCI/dm:LNAME)'/>
</aiah:lname>
<aiah:fullname>
<xsl:value-of 
	select='concat(
		normalize-space(../../dm:Record/dm:PCI/dm:LNAME),", ",
		normalize-space(../../dm:Record/dm:PCI/dm:FNAME), " ", 
		normalize-space(../../dm:Record/dm:PCI/dm:MNAME))'/>
</aiah:fullname>

</aiah:recipient>
</aiah:AWARD>


</xsl:for-each>

</xsl:for-each-group>
</aiah:AWARD_LIST>
</xsl:template>
</xsl:stylesheet>