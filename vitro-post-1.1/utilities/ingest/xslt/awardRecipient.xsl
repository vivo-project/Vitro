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

<aiah:RECIPIENT_LIST>

<xsl:for-each-group select='$docs//dm:Record' 
	group-by='vfx:collapse(concat(
				normalize-space(dm:PCI/dm:LNAME), "|",
				normalize-space(dm:PCI/dm:FNAME), "|", 
				normalize-space(dm:PCI/dm:MNAME)))'>
<xsl:sort select='vfx:collapse(concat(
				normalize-space(dm:PCI/dm:LNAME), "|",
				normalize-space(dm:PCI/dm:FNAME), "|", 
				normalize-space(dm:PCI/dm:MNAME)))'/>


<xsl:variable name='auth' select='.'/>
<xsl:variable name='cur_netid' select='$auth/@username'/>
<xsl:variable name='cur_aiid' select='$auth/@userId'/>
<aiah:RECIPIENT>
<xsl:attribute name='counter' select='position()'/>
<aiah:netid><xsl:value-of select='$cur_netid'/></aiah:netid>
<aiah:uid><xsl:value-of select='$cur_aiid'/></aiah:uid>

<aiah:fname><xsl:value-of select='normalize-space($auth/dm:PCI/dm:FNAME)'/></aiah:fname>
<aiah:mname><xsl:value-of select='normalize-space($auth/dm:PCI/dm:MNAME)'/></aiah:mname>
<aiah:lname><xsl:value-of select='normalize-space($auth/dm:PCI/dm:LNAME)'/></aiah:lname>
<aiah:fullname><xsl:value-of select='normalize-space(concat(normalize-space($auth/dm:PCI/dm:LNAME),", ",
normalize-space($auth/dm:PCI/dm:FNAME), " ", normalize-space($auth/dm:PCI/dm:MNAME)))'/></aiah:fullname>



<aiah:AWARD_LIST>
<xsl:for-each select='current-group()'>

<xsl:for-each select='dm:AWARDHONOR'>
<aiah:AWARD>
<xsl:attribute name='ahid' select='@id'/>
<aiah:org><xsl:value-of select='dm:ORG'/></aiah:org>
<aiah:year><xsl:value-of select='dm:DTY_END'/></aiah:year>
<aiah:award_name><xsl:value-of select='dm:NAME'/></aiah:award_name>
<aiah:display><xsl:value-of select='dm:PUBLIC_VIEW'/></aiah:display>

</aiah:AWARD>
</xsl:for-each>

</xsl:for-each>
</aiah:AWARD_LIST>
</aiah:RECIPIENT>
</xsl:for-each-group>

</aiah:RECIPIENT_LIST>
</xsl:template>
<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>