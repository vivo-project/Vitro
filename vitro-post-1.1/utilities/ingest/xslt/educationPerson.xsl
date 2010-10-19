<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:aiedu="http://vivoweb.org/ontology/activity-insight"
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

<aiedu:EDUCATION_PERSON_LIST>

<xsl:for-each-group select='$docs//dm:Record' 
group-by='vfx:collapse(concat(dm:PCI/dm:LNAME, ", ",
dm:PCI/dm:FNAME , " ", dm:PCI/dm:MNAME))'>
<xsl:sort 
select='vfx:collapse(concat(dm:PCI/dm:LNAME, ", ",
dm:PCI/dm:FNAME , " ", dm:PCI/dm:MNAME))'/>


<xsl:variable name='rec' select='.'/>
<xsl:variable name='cur_netid' select='$rec/@username'/>
<xsl:variable name='cur_aiid' select='$rec/@userId'/>
<aiedu:PERSON>
<xsl:attribute name='index' select='position()'/>
<aiedu:netid><xsl:value-of select='$cur_netid'/></aiedu:netid>
<aiedu:uid><xsl:value-of select='$cur_aiid'/></aiedu:uid>

<aiedu:fname>
<xsl:value-of select='normalize-space($rec/dm:PCI/dm:FNAME)'/>
</aiedu:fname>
<aiedu:mname>
<xsl:value-of select='normalize-space($rec/dm:PCI/dm:MNAME)'/>
</aiedu:mname>
<aiedu:lname>
<xsl:value-of select='normalize-space($rec/dm:PCI/dm:LNAME)'/>
</aiedu:lname>
<aiedu:fullname><xsl:value-of 
select='normalize-space(concat(normalize-space($rec/dm:PCI/dm:LNAME),", ",
normalize-space($rec/dm:PCI/dm:FNAME), " ", 
normalize-space($rec/dm:PCI/dm:MNAME)))'/></aiedu:fullname>



<aiedu:EDUCATION_LIST>
<xsl:for-each select='current-group()'>

<xsl:for-each select='dm:EDUCATION'>
<xsl:if test='dm:PUBLIC_VIEW="Yes"'>
<aiedu:EDUCATION>
<xsl:attribute name='id' select='@id'/>
</aiedu:EDUCATION>
</xsl:if>
</xsl:for-each>

</xsl:for-each>
</aiedu:EDUCATION_LIST>
</aiedu:PERSON>
</xsl:for-each-group>

</aiedu:EDUCATION_PERSON_LIST>
<xsl:value-of select='$NL'/>
</xsl:template>
<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>

