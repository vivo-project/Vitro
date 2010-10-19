<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:aiec="http://vivoweb.org/ontology/activity-insight"
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

<aiec:EDITCHAIR_PERSON_LIST>

<xsl:for-each-group select='$docs//dm:Record' 
group-by='vfx:collapse(concat(dm:PCI/dm:LNAME, "|",
dm:PCI/dm:FNAME , "|", dm:PCI/dm:MNAME))'>
<xsl:sort 
select='vfx:collapse(concat(dm:PCI/dm:LNAME, "|",
dm:PCI/dm:FNAME , "|", dm:PCI/dm:MNAME))'/>


<xsl:variable name='rec' select='.'/>
<xsl:variable name='cur_netid' select='$rec/@username'/>
<xsl:variable name='cur_aiid' select='$rec/@userId'/>
<aiec:PERSON>
<xsl:attribute name='counter' select='position()'/>
<aiec:netid><xsl:value-of select='$cur_netid'/></aiec:netid>
<aiec:uid><xsl:value-of select='$cur_aiid'/></aiec:uid>

<aiec:fname>
<xsl:value-of select='normalize-space($rec/dm:PCI/dm:FNAME)'/>
</aiec:fname>
<aiec:mname>
<xsl:value-of select='normalize-space($rec/dm:PCI/dm:MNAME)'/>
</aiec:mname>
<aiec:lname>
<xsl:value-of select='normalize-space($rec/dm:PCI/dm:LNAME)'/>
</aiec:lname>
<aiec:fullname><xsl:value-of 
select='normalize-space(concat(normalize-space($rec/dm:PCI/dm:LNAME),", ",
normalize-space($rec/dm:PCI/dm:FNAME), " ", 
normalize-space($rec/dm:PCI/dm:MNAME)))'/></aiec:fullname>



<aiec:EDITCHAIR_LIST>
<xsl:for-each select='current-group()'>

<xsl:for-each select='dm:EDITCHAIR'>
<xsl:if test='dm:PUBLIC_VIEW="Yes"'>
<aiec:EDITCHAIR>
<xsl:attribute name='id' select='@id'/>
</aiec:EDITCHAIR>
</xsl:if>
</xsl:for-each>

</xsl:for-each>
</aiec:EDITCHAIR_LIST>
</aiec:PERSON>
</xsl:for-each-group>

</aiec:EDITCHAIR_PERSON_LIST>
<xsl:value-of select='$NL'/>
</xsl:template>
<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>