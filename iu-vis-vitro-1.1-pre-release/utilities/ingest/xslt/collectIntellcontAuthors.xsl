<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
	xmlns:aiic="http://vivoweb.org/activity-insight"
	xmlns="http://vivoweb.org/activity-insight"
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

<!--xsl:variable name='aiid_netid' 
  select="document('../aiid-netid.xml')//aiic:mapterm"/ -->

<!-- 
The variable aiid_netid contains 
a mapping of Activity Insight User Id -> netid (i.e. aka username)
It is used to look up details from an AI .xml when all we have is 
the AI user id.

Sample Xml File identified by the required param aiid2netid:

<?xml version="1.0" encoding="UTF-8"?>
<aiic:aiidmap xmlns:aiic="http://vivoweb.org/activity-insight">
   <aiic:mapterm>
      <aiic:netid>aa299</aiic:netid>
      <aiic:aiid>643172</aiic:aiid>
   </aiic:mapterm>
   <aiic:mapterm>
      <aiic:netid>aa337</aiic:netid>
      <aiic:aiid>349666</aiic:aiid>
   </aiic:mapterm>
.
.
.

-->

<xsl:variable name='aiid_netid' 
  select="document($aiid2netid)//aiic:mapterm"/>

<!-- ================================================= -->

 
<xsl:template match='/'>

<!-- the required param listxml identifies an xml file that
contains a list of the partially processed AI xmls.

<?xml version='1.0'?>
<collection>
        <doc href='./AIXMLS/AIICJA_20100610120510/AIICJA_aa299.xml'/>
        <doc href='./AIXMLS/AIICJA_20100610120510/AIICJA_aa337.xml'/>
        <doc href='./AIXMLS/AIICJA_20100610120510/AIICJA_aa34.xml'/>
        <doc href='./AIXMLS/AIICJA_20100610120510/AIICJA_aad4.xml'/>
.
.
.
</collection>


-->
<xsl:variable name='docs' as='node()*'
	select='collection($listxml)'/>

<!-- begin wrapper element -->
<xsl:element name="aiic:INTELLCONT_AUTHOR_LIST" 
	namespace="http://vivoweb.org/activity-insight">

<!-- =============== -->
<!-- 
 gather all AUTHOR sub-xmls for a given name and sort group collection
 by uppercased constructed name
-->
<xsl:for-each-group select='$docs/dm:Data/ai:INTELLCONT_AUTHORSHIP/ai:AUTHOR' 
	group-by='vfx:clean(concat(ai:LNAME, ", ", ai:FNAME))'>
<xsl:sort select='vfx:clean(concat(ai:LNAME, ", ", ai:FNAME))'/>

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
  
     <xsl:value-of select='vfx:trim(concat(ai:LNAME, ", ", ai:FNAME))'/>
    
   </xsl:element>

   <xsl:element name='aiic:LastName'>
    <xsl:value-of select='$auth/ai:LNAME'/>
   </xsl:element>

   <xsl:element name='aiic:FirstName'>
    <xsl:value-of select='$auth/ai:FNAME'/>
   </xsl:element>

   <xsl:element name='aiic:MiddleName'>
    <xsl:value-of select='$auth/ai:MNAME'/>
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
<!-- this template returns a netid for given AI user id
-->
<xsl:template name='idmap'>
<xsl:param name='aiid'/>
<xsl:element name='aiic:NetId'>
<xsl:if test='$aiid'>
<xsl:for-each select='$aiid_netid'>
<xsl:if test='$aiid = aiic:aiid'>
<xsl:value-of select='aiic:netid'/>
</xsl:if>
</xsl:for-each>
</xsl:if>
</xsl:element> 
</xsl:template>

<!-- ============================================= -->
<!-- this extension function  
	1. removes whitespace and . and , from the argument string
	2. shifts alphabetic characters to upper case
	3. returns the adjusted string for comparison
-->
<xsl:function name='vfx:clean' as='xs:string'>
<xsl:param name='s1'/>
<xsl:variable name='res' select='replace($s1, "\s", "")'/>
<xsl:value-of select='upper-case(replace($res,"[.,]",""))'/>
</xsl:function>

<!-- ============================================= -->
<!-- this extension function 
removes leading and trailing whitespace from the argument string
-->
<xsl:function name='vfx:trim' as='xs:string'>
<xsl:param name='s1'/>
<xsl:choose>
<xsl:when test='$s1 != ""'>
<xsl:analyze-string select='$s1' regex='^\s*(.+?)\s*$'>
<xsl:matching-substring>
<xsl:value-of select='regex-group(1)'/>
</xsl:matching-substring>
</xsl:analyze-string>
</xsl:when>
<xsl:otherwise>
<xsl:text>Unknown</xsl:text>
</xsl:otherwise>
</xsl:choose>
</xsl:function>

</xsl:stylesheet>
