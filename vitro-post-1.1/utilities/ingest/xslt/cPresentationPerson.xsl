<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:mapid="http://vivoweb.org/ontology/activity-insight"
	xmlns:aipres="http://vivoweb.org/ontology/activity-insight"
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

<aipres:PRESENT_PERSON_LIST>

<xsl:for-each-group 
	select='$docs//dm:Record//dm:PRESENT_AUTH' 
	group-by='vfx:collapse(concat(dm:LNAME, "|",
					dm:FNAME , "|", 
					dm:MNAME))'>
<xsl:sort 
	select='vfx:collapse(concat(dm:LNAME, "|",
					dm:FNAME , "|", 
					dm:MNAME))'/>

<xsl:variable name='rec' select='.'/>
<xsl:variable name='cur_netid' select='../../../dm:Record/@username'/>
<xsl:variable name='cur_aiid' select='../../../dm:Record/@userId'/>

<aipres:PRESENT_BY_PERSON>
<xsl:attribute name='index' select='position()'/>

<aipres:PERSON_NAME>
<xsl:attribute name='cu_collabs'>
    <xsl:value-of select='count(current-group())'/>
</xsl:attribute>
    <xsl:value-of select='
	vfx:trim(concat(dm:LNAME, ", ", dm:FNAME, " ", dm:MNAME))'/>
</aipres:PERSON_NAME>

   <aipres:LastName>
    <xsl:value-of select='normalize-space($rec/dm:LNAME)'/>
   </aipres:LastName>

   <aipres:FirstName>
    <xsl:value-of select='normalize-space($rec/dm:FNAME)'/>
   </aipres:FirstName>

   <aipres:MiddleName>
    <xsl:value-of select='normalize-space($rec/dm:MNAME)'/>
   </aipres:MiddleName>

   <xsl:element name='aipres:AiUserId'>
    <xsl:value-of select='$rec/dm:FACULTY_NAME'/>
   </xsl:element>

   <aipres:NetId>
    <xsl:value-of 
	select='$aiid_netid[mapid:aiid=$rec/dm:FACULTY_NAME]/mapid:netid'/>
   </aipres:NetId>

   <!-- other stuff might be needed here -->


<aipres:PRESENT_LIST>
<xsl:for-each select='current-group()'>

<xsl:if test='../dm:USER_REFERENCE_CREATOR = "Yes"'>
   <aipres:PRESENT_INFO>
   <xsl:attribute name='rank' 
	select='count(preceding-sibling::dm:PRESENT_AUTH)+1'/>
   <xsl:attribute name='ref_netid'
        select='../../../dm:Record/@username'/>
   <xsl:attribute name='public' select='../dm:PUBLIC_VIEW'/>
   <xsl:attribute name='hasOrg' 
      select='if(normalize-space(../dm:ORG) = "") then "No" else "Yes"'/>
   <xsl:attribute name='hasTitle'  
      select='if(normalize-space(../dm:TITLE) = "") then "No" else "Yes"'/>
   <xsl:attribute name='hasConf' 
	select='if(normalize-space(../dm:NAME) = "") then "No" else "Yes"'/>
   <xsl:attribute name='role' 
	select='vfx:presRole(dm:ROLE)'/>
    <xsl:value-of select='../@id'/>
   </aipres:PRESENT_INFO>
</xsl:if>
</xsl:for-each>
</aipres:PRESENT_LIST>


</aipres:PRESENT_BY_PERSON>
</xsl:for-each-group>
</aipres:PRESENT_PERSON_LIST>


<xsl:value-of select='$NL'/>
</xsl:template>
<!-- ================================== -->
<xsl:function name='vfx:presRole'>
<xsl:param name='r'/>
<xsl:choose>
  <xsl:when test='starts-with($r, "Presenter Only")'>
	<xsl:value-of select='"PO"'/>
  </xsl:when>
  <xsl:when test='starts-with($r, "Presenter")'>
	<xsl:value-of select='"PA"'/>
  </xsl:when>
  <xsl:when test='starts-with($r, "Author")'>
	<xsl:value-of select='"A"'/>
  </xsl:when>
  <xsl:when test='starts-with($r, "Organizer")'>
	<xsl:value-of select='"O"'/>
  </xsl:when>
  <xsl:otherwise>
	<xsl:value-of select='""'/>
  </xsl:otherwise>
</xsl:choose>
</xsl:function>


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
