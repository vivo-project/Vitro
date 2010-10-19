<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:aiic="http://vivoweb.org/ontology/activity-insight"
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
<xsl:element name="aiic:INTELLCONT_AUTHOR_LIST" 
	namespace="http://vivoweb.org/ontology/activity-insight">

<!-- =============== -->
<!-- 
 gather all AUTHOR sub-xmls for a given name and sort group collection
 by uppercased constructed name

<xsl:for-each-group select='$docs/dm:Data/ai:INTELLCONT_AUTHORSHIP/ai:AUTHOR' 
	group-by='vfx:collapse(concat(ai:LNAME, "|", ai:FNAME, "|",ai:MNAME))'>
<xsl:sort select='vfx:collapse(concat(ai:LNAME, "|", ai:FNAME, "|",ai:MNAME))'/>-->

<xsl:for-each-group select='$docs/dm:Data/ai:INTELLCONT_AUTHORSHIP/ai:AUTHOR' 
	group-by='vfx:collectByName(ai:LNAME, ai:FNAME, ai:MNAME)'>
<xsl:sort select='vfx:collectByName(ai:LNAME, ai:FNAME, ai:MNAME)'/>

<xsl:variable name='cur_netid' select='../../dm:Record/dm:username'/>
<xsl:variable name='cur_aiid' select='../../dm:Record/dm:userId'/>

<xsl:variable name='auth' select='.'/>

<xsl:element name="aiic:INTELLCONT_ITEMS_BY_AUTHOR" >

<!-- =============== -->

<!--
   for the given AUTHOR group ( . is the first in each group)

-->

   <xsl:element name='aiic:AUTHOR_NAME'>
     <xsl:attribute name='cu_coauthors'>
       <xsl:value-of select='count(current-group())'/></xsl:attribute>
  
     <xsl:value-of select='vfx:trim(concat(ai:LNAME, ", ", ai:FNAME, " ", ai:MNAME))'/>
    
   </xsl:element>

   <xsl:element name='aiic:LastName'>
    <xsl:value-of select='normalize-space($auth/ai:LNAME)'/>
   </xsl:element>

   <xsl:element name='aiic:FirstName'>
    <xsl:value-of select='normalize-space($auth/ai:FNAME)'/>
   </xsl:element>

   <xsl:element name='aiic:MiddleName'>
    <xsl:value-of select='normalize-space($auth/ai:MNAME)'/>
   </xsl:element>

   <xsl:element name='aiic:AiUserId'>
    <xsl:value-of select='$auth/ai:FACULTY_NAME'/>
   </xsl:element>

   
   <xsl:call-template name='idmap'>
   <xsl:with-param name='aiid' select='$auth/ai:FACULTY_NAME'/>
   </xsl:call-template>

   <xsl:text>&#xA;</xsl:text>

<!-- =============== -->

   <xsl:element name='aiic:INTELLCONT_LIST'>
 
      <xsl:for-each select='current-group()'>
      <xsl:variable name='ref_netid' select="../../dm:Record/dm:username"/>

          <xsl:for-each select='ai:INTELLCONT_AUTHORLIST/ai:INTELLCONT_AUTHORSHIP_ORDER'>
          <xsl:sort select='ai:INTELLCONT_ID'/>

          <xsl:element name='aiic:INTELLCONT_INFO'>
            <xsl:attribute name='ref_netid'><xsl:value-of select='$ref_netid'/></xsl:attribute>
            <xsl:attribute name='ai_userid'>
             <xsl:value-of select='../../ai:FACULTY_NAME'/></xsl:attribute>
            <xsl:attribute name='authorRank'><xsl:value-of select='ai:AUTHORSHIP_POSITION'/></xsl:attribute>
            <xsl:attribute name='public'><xsl:value-of select='ai:PUBLIC'/></xsl:attribute>
            <xsl:attribute name='hasTitle' select='./ai:INTELLCONT_ID/@hasTitle'/>
	    <xsl:attribute name='hasGoodAuthor' select='./ai:INTELLCONT_ID/@hasGoodAuthor'/>
             <xsl:text>AI-</xsl:text>
             <xsl:value-of select='ai:INTELLCONT_ID'/>
          </xsl:element>
          <!-- aiic:INTELLCONT_INFO -->
         </xsl:for-each>

      </xsl:for-each>
      <!-- aiic:INTELLCONT_LIST -->
    </xsl:element> 


<!-- aiic:INTELLCONT_ITEMS_BY_AUTHOR -->
</xsl:element>

</xsl:for-each-group>
<!-- aiic:INTELLCONT_AUTHOR_LIST -->
</xsl:element> 

</xsl:template>

<!-- ============================================= -->
<!-- this template returns a netid element for given AI user id
-->
<xsl:template name='idmap'>
<xsl:param name='aiid'/>
<!--         note aiic below -->
<xsl:element name='aiic:NetId'>
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
