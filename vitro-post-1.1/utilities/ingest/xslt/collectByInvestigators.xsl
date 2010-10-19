<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:aiis="http://vivoweb.org/ontology/activity-insight"
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
<xsl:element name="aiis:INVESTIGATOR_LIST" 
	namespace="http://vivoweb.org/ontology/activity-insight">

<!-- =============== -->
<!-- 
 gather all AUTHOR sub-xmls for a given name and sort group collection
 by uppercased constructed name
-->
<xsl:for-each-group select='$docs/Data/LOCAL_COLLABORATOR_LIST/COLLABORATOR[@ilk="invest"]' 
	group-by='vfx:collapse(concat(LNAME, "|", FNAME, "|", MNAME))'>
<xsl:sort select='vfx:collapse(concat(LNAME, "|", FNAME, "|", MNAME))'/>

<xsl:variable name='cur_netid' select='../../Record/username'/>
<xsl:variable name='cur_aiid' select='../../Record/userId'/>

<xsl:variable name='invest' select='.'/>

<xsl:element name="aiis:IMPACT_STMTS_BY_INVESTIGATOR" >

<!-- =============== -->

<!--
   for the given INVESTIGATOR group ( . is the first in each group)

-->

   <xsl:element name='aiis:INVESTIGATOR_NAME'>
     <xsl:attribute name='cu_collabs'>
       <xsl:value-of select='count(current-group())'/></xsl:attribute>
  
     <xsl:value-of select='vfx:trim(concat(LNAME, ", ", FNAME, " ", MNAME))'/>
    
   </xsl:element>

   <xsl:element name='aiis:LastName'>
    <xsl:value-of select='$invest/LNAME'/>
   </xsl:element>

   <xsl:element name='aiis:FirstName'>
    <xsl:value-of select='$invest/FNAME'/>
   </xsl:element>

   <xsl:element name='aiis:MiddleName'>
    <xsl:value-of select='$invest/MNAME'/>
   </xsl:element>

   <xsl:element name='aiis:AiUserId'>
    <xsl:value-of select='$invest/FACULTY_NAME'/>
   </xsl:element>

   <xsl:element name='aiis:Department'>
    <xsl:value-of select='$invest/DEP'/>
   </xsl:element>

   <xsl:call-template name='idmap'>
   <xsl:with-param name='aiid' select='$invest/FACULTY_NAME'/>
   </xsl:call-template>

   <xsl:text>&#xA;</xsl:text>

<!-- =============== -->

   <xsl:element name='aiis:IMPACT_STMT_LIST'>
 
      <xsl:for-each select='current-group()'>
      <xsl:variable name='ref_netid' select="../../Record/username"/>

          <xsl:for-each select='INVESTIGATOR_IMPACT_STMT_LIST/IMPACT_STMT_INFO'>
          <xsl:sort select='IMPACT_STMT_ID'/>

          <xsl:element name='aiis:IMPACT_STMT_INFO'>
            <xsl:attribute name='ref_netid'><xsl:value-of select='$ref_netid'/></xsl:attribute>
            <xsl:attribute name='ai_userid'>
             <xsl:value-of select='../../FACULTY_NAME'/></xsl:attribute>
            <xsl:attribute name='collabRank'><xsl:value-of select='COLLABORATION_POSITION'/></xsl:attribute>
             <xsl:text>AI-</xsl:text>
             <xsl:value-of select='IMPACT_STMT_ID'/>
          </xsl:element>
         </xsl:for-each>

      </xsl:for-each>
      <!-- aiis:IMPACT_STMT_LIST -->
    </xsl:element> 


<!-- aiis:IMPACT_STMT_BY_INVESTIGATOR -->
</xsl:element>

</xsl:for-each-group>
<!-- aiis:INVESTIGATOR_LIST -->
</xsl:element> 

</xsl:template>

<!-- ============================================= -->
<!-- this template returns a netid element for given AI user id
-->
<xsl:template name='idmap'>
<xsl:param name='aiid'/>
<!--           note aiis below -->
<xsl:element name='aiis:NetId'>
<xsl:if test='$aiid'>
<xsl:for-each select='$aiid_netid'>
<xsl:if test='$aiid = mapid:aiid'>
<xsl:value-of select='mapid:netid'/>
</xsl:if>
</xsl:for-each>
</xsl:if>
</xsl:element> 
</xsl:template>



<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
