<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:aigrant="http://vivoweb.org/ontology/activity-insight"
	xmlns:mapid="http://vivoweb.org/ontology/activity-insight"
	xmlns="http://vivoweb.org/ontology/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:vfx='http://vivoweb.org/ext/functions'	
	exclude-result-prefixes='vfx xs'
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

<!-- ================================================= -->

 
<xsl:template match='/'>


<xsl:variable name='docs' as='node()*'
	select='collection($listxml)'/>

<!-- begin wrapper element -->
<xsl:element name="aigrant:INVESTIGATOR_LIST" 
	namespace="http://vivoweb.org/ontology/activity-insight">

<!-- =============== -->
<!-- 
 gather all GRANTS sub-xmls for a given name and sort group collection
 by uppercased constructed name
-->
<xsl:for-each-group select='$docs//dm:Record//dm:CONGRANT_INVEST'
	group-by='vfx:collapse(concat(dm:LNAME, "|", dm:FNAME, "|", dm:MNAME))'>
<xsl:sort select='vfx:collapse(concat(dm:LNAME, "|", dm:FNAME, "|", dm:MNAME))'/>

<xsl:variable name='cur_netid' select='../../dm:Record/username'/>
<xsl:variable name='cur_aiid' select='../../dm:Record/userId'/>

<xsl:variable name='invest' select='.'/>

<xsl:element name="aigrant:GRANTS_BY_INVESTIGATOR" >
<xsl:attribute name='index' select='position()'/>
<!-- =============== -->

<!--
   for the given INVESTIGATOR group ( . is the first in each group)

-->

   <xsl:element name='aigrant:INVESTIGATOR_NAME'>
     <xsl:attribute name='cu_collabs'>
       <xsl:value-of select='count(current-group())'/></xsl:attribute>
  
     <xsl:value-of select='vfx:trim(concat(dm:LNAME, ", ", dm:FNAME, " ", dm:MNAME))'/>
    
   </xsl:element>

   <xsl:element name='aigrant:LastName'>
    <xsl:value-of select='$invest/dm:LNAME'/>
   </xsl:element>

   <xsl:element name='aigrant:FirstName'>
    <xsl:value-of select='$invest/dm:FNAME'/>
   </xsl:element>

   <xsl:element name='aigrant:MiddleName'>
    <xsl:value-of select='$invest/dm:MNAME'/>
   </xsl:element>

   <xsl:element name='aigrant:AiUserId'>
    <xsl:value-of select='$invest/dm:FACULTY_NAME'/>
   </xsl:element>

   <xsl:element name='aigrant:Department'>
    <xsl:value-of select='$invest/dm:DEP'/>
   </xsl:element>

   <xsl:element name='aigrant:Role'>
    <xsl:value-of select='$invest/dm:ROLE'/>
   </xsl:element>

   <xsl:element name='aigrant:College'>
    <xsl:value-of select='$invest/dm:COLLEGE'/>
   </xsl:element>


   <xsl:element name='aigrant:NetId'>
   <xsl:choose>
  	<xsl:when test='$invest/dm:FACULTY_NAME'>
	<xsl:value-of 
	select='$aiid_netid[mapid:aiid=$invest/dm:FACULTY_NAME]/mapid:netid'/>
  	</xsl:when>
	<xsl:when test='$invest/dm:NET_ID'>
	<xsl:variable name='real'>
		<xsl:value-of select='vfx:realNetid($invest/dm:NET_ID,aigrant:NetId)'/>
	</xsl:variable>
	<xsl:choose>
  		<xsl:when test='$real = "_void_"'>
			<xsl:value-of select='""'/>
  		</xsl:when>
  		<xsl:otherwise>
			<xsl:value-of select='$real'/>
  		</xsl:otherwise>
	</xsl:choose>
	
  	</xsl:when>
  	<xsl:otherwise>
	<xsl:value-of select='""'/>
  	</xsl:otherwise>
  </xsl:choose>
    
   </xsl:element>	

   <xsl:text>&#xA;</xsl:text>

<!-- =============== -->

   <xsl:element name='aigrant:GRANTS_LIST'>
 
      <xsl:for-each select='current-group()'>
 
	<xsl:if test='../dm:USER_REFERENCE_CREATOR = "Yes"'>
	<xsl:if test='../dm:STATUS = "Award Signed By All Parties"'>
          <xsl:element name='aigrant:GRANT_INFO'>
            <xsl:attribute name='ref_netid'><xsl:value-of select='../../../dm:Record/@username'/></xsl:attribute>
            <xsl:attribute name='ai_userid'>
             <xsl:value-of select='../../../dm:Record/@userId'/></xsl:attribute>
            <xsl:attribute name='collabRank'><xsl:value-of select='count(preceding-sibling::dm:CONGRANT_INVEST)+1'/></xsl:attribute>
             <xsl:text>AI-</xsl:text>
             <xsl:value-of select='../@id'/>
          </xsl:element>
       </xsl:if>
       </xsl:if>

      </xsl:for-each>
      <!-- aigrant:GRANTS_LIST -->
    </xsl:element> 


<!-- aigrant:GRANTS_BY_INVESTIGATOR -->
</xsl:element>

</xsl:for-each-group>
<!-- aigrant:INVESTIGATOR_LIST -->
</xsl:element> 

</xsl:template>



<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
