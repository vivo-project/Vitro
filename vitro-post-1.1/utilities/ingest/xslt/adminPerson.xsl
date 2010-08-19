<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:aiadm="http://vivoweb.org/activity-insight"
	xmlns="http://vivoweb.org/activity-insight"
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

<aiadm:ADMIN_PERSON_LIST>

<xsl:for-each-group select='//dm:Record' 
group-by='vfx:collapse(concat(dm:PCI/dm:LNAME, ", ",
dm:PCI/dm:FNAME , " ", dm:PCI/dm:LNAME))'>
<xsl:sort 
select='vfx:collapse(concat(dm:PCI/dm:LNAME, ", ",
dm:PCI/dm:FNAME , " ", dm:PCI/dm:LNAME))'/>

<xsl:variable name='rec' select='.'/>
<xsl:variable name='cur_netid' select='$rec/@username'/>
<xsl:variable name='cur_aiid' select='$rec/@userId'/>
<aiadm:PERSON>
<xsl:attribute name='counter' select='position()'/>
<aiadm:netid><xsl:value-of select='$cur_netid'/></aiadm:netid>
<aiadm:uid><xsl:value-of select='$cur_aiid'/></aiadm:uid>

<aiadm:fname>
<xsl:value-of select='normalize-space(/dm:PCI/dm:FNAME)'/>
</aiadm:fname>
<aiadm:mname>
<xsl:value-of select='normalize-space(/dm:PCI/dm:MNAME)'/>
</aiadm:mname>
<aiadm:lname>
<xsl:value-of select='normalize-space(/dm:PCI/dm:LNAME)'/>
</aiadm:lname>
<aiadm:fullname><xsl:value-of 
select='normalize-space(concat(normalize-space(/dm:PCI/dm:LNAME),", ",
normalize-space(/dm:PCI/dm:FNAME), " ", 
normalize-space(/dm:PCI/dm:MNAME)))'/></aiadm:fullname>


<aiadm:ADMIN_LIST>
<xsl:for-each select='current-group()'>

<xsl:for-each select='dm:ADMIN'>
<xsl:if test='dm:PUBLIC_VIEW="Yes"'>
<aiadm:ADMIN>
<xsl:attribute name='id' select='@id'/>
</aiadm:ADMIN>
</xsl:if>
</xsl:for-each>

</xsl:for-each>
</aiadm:ADMIN_LIST>

</aiadm:PERSON>
</xsl:for-each-group>

</aiadm:ADMIN_PERSON_LIST>
<xsl:value-of select='$NL'/>
</xsl:template>
<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
