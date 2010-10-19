<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:airstmt="http://vivoweb.org/ontology/activity-insight"
	xmlns="http://vivoweb.org/ontology/activity-insight"
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

<airstmt:RESEARCH_STATEMENT_PERSON_LIST>

<xsl:for-each-group select='$docs//dm:Record' 
group-by='vfx:collapse(concat(dm:PCI/dm:LNAME, "|",
dm:PCI/dm:FNAME , "|", dm:PCI/dm:MNAME))'>
<xsl:sort 
select='vfx:collapse(concat(dm:PCI/dm:LNAME, "|",
dm:PCI/dm:FNAME , "|", dm:PCI/dm:MNAME))'/>

<xsl:variable name='rec' select='.'/>
<xsl:variable name='cur_netid' select='$rec/@username'/>
<xsl:variable name='cur_aiid' select='$rec/@userId'/>
<airstmt:PERSON>
<xsl:attribute name='counter' select='position()'/>
<airstmt:netid><xsl:value-of select='$cur_netid'/></airstmt:netid>
<airstmt:uid><xsl:value-of select='$cur_aiid'/></airstmt:uid>

<airstmt:fname>
<xsl:value-of select='normalize-space($rec/dm:PCI/dm:FNAME)'/>
</airstmt:fname>
<airstmt:mname>
<xsl:value-of select='normalize-space($rec/dm:PCI/dm:MNAME)'/>
</airstmt:mname>
<airstmt:lname>
<xsl:value-of select='normalize-space($rec/dm:PCI/dm:LNAME)'/>
</airstmt:lname>
<airstmt:fullname><xsl:value-of 
select='normalize-space(concat(normalize-space($rec/dm:PCI/dm:LNAME),", ",
normalize-space($rec/dm:PCI/dm:FNAME), " ", 
normalize-space($rec/dm:PCI/dm:MNAME)))'/></airstmt:fullname>


<airstmt:RESEARCH_STATEMENT_LIST>
<xsl:for-each select='current-group()'>

<xsl:for-each select='dm:RESEARCH_STATEMENT'>
<xsl:if test='dm:PUBLIC_VIEW="Yes"'>
<airstmt:RESEARCH_STATEMENT>
<xsl:attribute name='id' select='@id'/>
</airstmt:RESEARCH_STATEMENT>
</xsl:if>
</xsl:for-each>

</xsl:for-each>
</airstmt:RESEARCH_STATEMENT_LIST>

</airstmt:PERSON>
</xsl:for-each-group>

</airstmt:RESEARCH_STATEMENT_PERSON_LIST>
<xsl:value-of select='$NL'/>
</xsl:template>
<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
