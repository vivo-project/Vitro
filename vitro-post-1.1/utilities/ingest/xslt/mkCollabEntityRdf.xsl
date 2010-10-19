<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aiis="http://vivoweb.org/ontology/activity-insight"
	xmlns:acti="http://vivoweb.org/ontology/activity-insight#"
        xmlns="http://vivoweb.org/ontology/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"	
	xmlns:vfx='http://vivoweb.org/ext/functions'
	exclude-result-prefixes='xs vfx'
	>


<xsl:param name='unoMapFile'  required='yes'/>
<xsl:param name='aiisXmlPath' required='yes'/>
<xsl:param name='aiisPrefix' required='yes'/>
<xsl:param name='extOrgIn' required='yes'/>
<xsl:param name='extOrgOut' required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>

<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>

<xsl:variable name='extantOrgs'
	select="document($extOrgIn)/ExtantOrgs"/>
<!-- ================================== -->
<xsl:template match='/aiis:COLLAB_ENTITY_LIST'>
<rdf:RDF>


<xsl:for-each select='aiis:IMPACT_STMTS_BY_COLLAB_ENTITY'>

<!-- create a foaf:Organization for this investigator  
OR use one from before -->


<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<!--xsl:comment>
<xsl:value-of select='$ctr'/> - <xsl:value-of select='$uno'/>
</xsl:comment -->

<!-- =================================================== -->
<!-- Declare a foaf:Organization (use extant org if foaf exists) -->

<xsl:variable name='knownUri' select='vfx:knownCEOrgUri(aiis:Name, aiis:StateCountry, $extantOrgs)'/>

<xsl:variable name='foafuri' 
select="if($knownUri != '') then $knownUri else 
concat($g_instance,$uno)"/>

<!-- xsl:comment><xsl:value-of select='$foafuri'/> - <xsl:value-of select='$knownUri'/></xsl:comment -->

<xsl:if test='$knownUri = ""'>
<rdf:Description rdf:about="{$foafuri}">
<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource='http://xmlns.com/foaf/0.1/Organization'/>

<rdfs:label>
<xsl:value-of select='vfx:trim(aiis:Name)'/>
</rdfs:label>
<core:description>
<xsl:value-of select='vfx:trim(aiis:Name)'/><xsl:text>; </xsl:text>
<xsl:value-of select='vfx:trim(aiis:StateCountry)'/>
</core:description>
</rdf:Description>
</xsl:if>

<!-- =================================================== -->
<!-- now process the impact stmts attributed to this collab entity -->

<xsl:call-template name='process-collab-entity'>
<xsl:with-param name='isbyce' select='aiis:IMPACT_STMT_LIST'/>
<xsl:with-param name='foafref' select="$foafuri"/>
</xsl:call-template>

</xsl:for-each>

<!-- =================================================== 
 at this point we re-run part of the last 
for loop to get a new list of persons 
 and their uri's to save in the extant Orgs Out xml file
-->
<xsl:result-document href='{$extOrgOut}'>
<xsl:element name='ExtantCEOrgs' namespace=''>
<xsl:for-each select='aiis:IMPACT_STMTS_BY_COLLAB_ENTITY'>

<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<xsl:variable name='knownUri' 
select='vfx:knownCEOrgUri(aiis:Name, aiis:StateCountry, $extantOrgs)'/>

<xsl:variable name='foafuri' 
select="if($knownUri != '') then $knownUri else 
concat($g_instance,$uno)"/>


<xsl:element name='org' namespace=''>

<xsl:element name='uri' namespace=''>
<xsl:value-of select='$foafuri'/>
</xsl:element>

<xsl:element name='name' namespace=''>
<xsl:value-of select='aiis:Name'/>
</xsl:element>

<xsl:element name='stateCountry' namespace=''>
<xsl:value-of select='aiis:StateCountry'/>
</xsl:element>

</xsl:element>


</xsl:for-each>
</xsl:element>
</xsl:result-document>

</rdf:RDF>
</xsl:template>

<!-- =================================================== -->
<!-- =================================================== -->
<xsl:template name='process-collab-entity'>
<xsl:param name='isbyce'/>
<xsl:param name='foafref'/>

<xsl:for-each select='$isbyce/aiis:IMPACT_STMT_INFO'>
<xsl:variable name='aiid' select='.'/>
<xsl:variable name='rank' select='@collabRank'/>

<!-- =================================================== -->
<!-- Declare property mapping acti:ImpactProject to acti:PartnerRole -->
<!-- 9 -->
<rdf:Description rdf:about="{concat($g_instance,$aiid)}" >
<core:relatedRole
	rdf:resource="{concat($g_instance,$aiid,'-CE-',$rank)}"/>
</rdf:Description>

<!-- =================================================== -->
<!-- Declare acti:PartnerRole Individual Triples-->

<rdf:Description rdf:about="{concat($g_instance,$aiid,'-CE-',$rank)}">

<rdfs:label>
<xsl:value-of select='concat("Partner (",vfx:trim(../../aiis:Name),")" )'/>
</rdfs:label>

<rdf:type rdf:resource='http://vivoweb.org/ontology/activity-insight#PartnerRole'/>

<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<!-- 12 -->
<acti:partnerRoleOf rdf:resource='{$foafref}'/>

<acti:collaboratorRank rdf:datatype='http://www.w3.org/2001/XMLSchema#int'>
<xsl:value-of select='$rank'/>
</acti:collaboratorRank>
<!-- 8 -->
<core:roleIn
	rdf:resource="{concat($g_instance,$aiid)}"/>

</rdf:Description>

<!-- =================================================== -->


<rdf:Description rdf:about="{$foafref}">
<!-- 13 -->
<acti:hasPartnerRole rdf:resource="{concat($g_instance,$aiid,'-CE-',$rank)}"/>
</rdf:Description>



</xsl:for-each>

</xsl:template>

<!-- ================================== -->
<xsl:template match='aiis:IMPACT_STMT_LIST'/>

<xsl:template match='aiis:ALT_SRC_IMPACT_STMT_INFO'/>

<xsl:template match='aiis:IMPACT_STMT_INFO'/>


<!-- =================================================== -->

<!-- =================================================== -->


<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
