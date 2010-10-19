<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:mapid="http://vivoweb.org/ontology/activity-insight"
	xmlns:aichr="http://vivoweb.org/ontology/activity-insight"
	xmlns="http://vivoweb.org/ontology/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:vfx='http://vivoweb.org/ext/functions'	
	exclude-result-prefixes='vfx xs dm mapid'
>

<xsl:param name='listxml' required='yes'/>
<xsl:param name='aiid2netid' required='yes'/>
<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>
<xsl:variable name='NL'>
<xsl:text>
</xsl:text>
</xsl:variable>

<xsl:variable name='aiid_netid' 
  select="document($aiid2netid)//mapid:mapterm"/>

<xsl:template match='/'>

<xsl:variable name='docs' as='node()*'
	select='collection($listxml)'/>

<xsl:element name='aichr:CHRESEARCH_PERSON_LIST'>

<xsl:for-each-group select='$docs//dm:Record//dm:CHRESEARCH_AUTH' 
group-by='vfx:collapse(concat(dm:LNAME, "|",dm:FNAME , "|", dm:MNAME))'>
<xsl:sort 
select='vfx:collapse(concat(dm:LNAME, "|",dm:FNAME , "|", dm:MNAME))'/>

<xsl:variable name='rec' select='.'/>
<xsl:variable name='cur_netid' select='../../../@username'/>
<xsl:variable name='cur_aiid' select='../../../@userId'/>

<xsl:element name="aichr:CHRESEARCH_BY_PERSON" >
<xsl:attribute name='counter' select='position()'/>

<xsl:element name='aichr:PERSON_NAME'>
<xsl:attribute name='cu_collabs'>
    <xsl:value-of select='count(current-group())'/>
</xsl:attribute>
    <xsl:value-of select='vfx:trim(concat(dm:LNAME, ", ", dm:FNAME, " ", dm:MNAME))'/>
</xsl:element>



<aichr:netid><xsl:value-of select='$cur_netid'/></aichr:netid>
<aichr:uid><xsl:value-of select='$cur_aiid'/></aichr:uid>

   <xsl:element name='aichr:LastName'>
    <xsl:value-of select='$rec/dm:LNAME'/>
   </xsl:element>

   <xsl:element name='aichr:FirstName'>
    <xsl:value-of select='$rec/dm:FNAME'/>
   </xsl:element>

   <xsl:element name='aichr:MiddleName'>
    <xsl:value-of select='$rec/dm:MNAME'/>
   </xsl:element>

   <xsl:element name='aichr:AiUserId'>
    <xsl:value-of select='$rec/dm:FACULTY_NAME'/>
   </xsl:element>

   <xsl:element name='aichr:NetId'>
    <xsl:value-of 
	select='$aiid_netid[mapid:aiid=$rec/dm:FACULTY_NAME]/mapid:netid'/>
   </xsl:element>	

   <!-- other stuff might be needed here -->

   <xsl:text>&#xA;</xsl:text>


<xsl:element name='aichr:CHRESEARCH_LIST'>
<xsl:for-each select='current-group()'>
<xsl:if test='../dm:USER_REFERENCE_CREATOR = "No"'>
   <xsl:element name='aichr:CHRESEARCH_INFO'>
       <xsl:attribute name='ref_netid'>
          <xsl:value-of select='../../../dm:Record/@username'/>
       </xsl:attribute>
   <xsl:attribute name='collabRank'>
       <xsl:value-of select='count(preceding-sibling::dm:CHRESEARCH_AUTH)+1'/>
   </xsl:attribute>
   <xsl:attribute name='public'>
       <xsl:value-of select='../dm:PUBLIC_VIEW'/>
   </xsl:attribute>
       <xsl:text>AI-</xsl:text><xsl:value-of select='../@id'/>
   </xsl:element>
</xsl:if>
</xsl:for-each>
<!-- aichr:CHRESEARCH_LIST -->
</xsl:element> 
<!-- aichr:_BY_PERSON -->
</xsl:element>
</xsl:for-each-group>
<!-- aichr:PERSON_LIST -->
</xsl:element> 



<xsl:value-of select='$NL'/>
</xsl:template>
<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
