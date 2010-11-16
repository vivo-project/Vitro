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
<xsl:element name="aiis:FUNDING_ORG_LIST" 
	namespace="http://vivoweb.org/ontology/activity-insight">

<!-- =============== -->
<!-- 
 gather all AUTHOR sub-xmls for a given name and sort group collection
 by uppercased constructed name
-->
<xsl:for-each-group select='$docs/Data/LOCAL_FUNDING_ORG_LIST/FORG' 
	group-by='vfx:collapse(FORG_NAME)'>
<xsl:sort select='vfx:collapse(FORG_NAME)'/>

<xsl:variable name='cur_netid' select='../../Record/username'/>
<xsl:variable name='cur_aiid' select='../../Record/userId'/>



<xsl:element name="aiis:IMPACT_STMTS_BY_FUNDING_ORG" >
 <xsl:attribute name='ilk'>
       <xsl:value-of select='@ilk'/></xsl:attribute>
<!-- =============== -->

<!--
   for the given FUNDING_ORG group ( . is the first in each group)

-->

   <xsl:element name='aiis:FUNDING_ORG_NAME'>
     <xsl:attribute name='netid_count'>
       <xsl:value-of select='count(current-group())'/></xsl:attribute>
  
     <xsl:value-of select='vfx:trim(FORG_NAME)'/>
    
   </xsl:element>



   <xsl:text>&#xA;</xsl:text>

<!-- =============== -->

   <xsl:element name='aiis:IMPACT_STMT_ID_LIST'>
      <xsl:for-each select='current-group()'>
      <xsl:variable name='ref_netid' select="../../Record/username"/>

          <xsl:for-each select='IMPACT_STMT_ID_LIST/IMPACT_STMT_ID'>
          <xsl:sort select='IMPACT_STMT_ID'/>

          <xsl:element name='aiis:IMPACT_STMT_ID'>
            <xsl:attribute name='ref_netid'>
		<xsl:value-of select='$ref_netid'/>
	    </xsl:attribute>
            <xsl:attribute name='hasTitle' 
		select='./@hasTitle'/>
            <xsl:attribute name='hasGoodAuthor' 
		select='./@hasGoodAuthor'/>
           
             <xsl:text>AI-</xsl:text>
             <xsl:value-of select='.'/>
          </xsl:element>
         </xsl:for-each>

      </xsl:for-each>
      <!-- aiis:IMPACT_STMT_ID_LIST -->
    </xsl:element> 


<!-- aiis:IMPACT_STMT_BY_FUNDING_ORG-->
</xsl:element>

</xsl:for-each-group>
<!-- aiis:FUNDING_ORG_LIST -->
</xsl:element> 

</xsl:template>

<!-- ============================================= -->
<!-- this template returns a netid element for given AI user id
-->
<xsl:template name='idmap'>
<xsl:param name='aiid'/>
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
