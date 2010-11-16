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
	select="document($extOrgIn)/ExtantCEOrgs"/>
<!-- ================================== -->
<xsl:template match='/aiis:COLLAB_ENTITY_LIST'>
<rdf:RDF>
<xsl:variable name='rawNewCEOrgs'>
<xsl:element name='ExtantCEOrgs' inherit-namespaces='no'>
<xsl:for-each select='aiis:IMPACT_STMTS_BY_COLLAB_ENTITY'>
<xsl:variable name='name' select='vfx:simple-trim(aiis:Name)'/>
<xsl:variable name='sc' select='vfx:simple-trim(aiis:StateCountry)'/>
<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>

<xsl:if test='$name!= ""'>
<xsl:variable name='knownUri'
	select='vfx:knownCEOrgUri($name, $sc, $extantOrgs)'/>

<xsl:variable name='ceorguri'
	select="if($knownUri != '') then 
			$knownUri else 
			concat($g_instance,$uno)"/>
<xsl:if test='$knownUri= ""'>
<xsl:element name='org' namespace=''>

<xsl:element name='uri' namespace=''>
<xsl:value-of select='concat("NEW-",$ceorguri)'/>
</xsl:element>

<xsl:element name='name' namespace=''>
<xsl:value-of select='$name'/>
</xsl:element>

<xsl:element name='stateCountry' namespace=''>
<xsl:value-of select='$sc'/>
</xsl:element>

</xsl:element>
</xsl:if>
</xsl:if>

</xsl:for-each>
</xsl:element>
</xsl:variable>

<xsl:variable name='uniqueNewCEOrgs'>
<xsl:call-template name='NewCEOrgs'>
<xsl:with-param name='knowns' select='$rawNewCEOrgs/ExtantCEOrgs'/>
</xsl:call-template>
</xsl:variable>

<xsl:for-each select='aiis:IMPACT_STMTS_BY_COLLAB_ENTITY'>

<!-- create a foaf:Organization for this entity 
OR use one from before -->

<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>

<!--xsl:comment>
<xsl:value-of select='$ctr'/> - <xsl:value-of select='$uno'/>
</xsl:comment -->

<!-- =================================================== -->
<!-- Declare a foaf:Organization (use extant org if foaf exists) -->
<xsl:variable name='name' select='vfx:simple-trim(aiis:Name)'/>
<xsl:variable name='sc' select='vfx:simple-trim(aiis:StateCountry)'/>

<xsl:if test='$name!= ""'>
<xsl:variable name='knownUri' 
	select='vfx:knownCEOrgUri($name, $sc, $extantOrgs union
						$rawNewCEOrgs/ExtantCEOrgs)'/>

<xsl:variable name='ceorguri' 
	select='if(starts-with($knownUri,"NEW-")) then 
		substring-after($knownUri,"NEW-") else 
		$knownUri'/>


<xsl:comment>
<xsl:value-of select='$ceorguri'/> - <xsl:value-of select='$knownUri'/>
</xsl:comment>

<xsl:if test='starts-with($knownUri,"NEW-")'>
<rdf:Description rdf:about="{$ceorguri}">
<rdf:type rdf:resource=
	'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource='http://xmlns.com/foaf/0.1/Organization'/>

<rdfs:label>
<xsl:value-of select='$name'/>
</rdfs:label>
<core:description>
<xsl:value-of select='$name'/><xsl:text>; </xsl:text>
<xsl:value-of select='$sc'/>
</core:description>
</rdf:Description>
</xsl:if>

<!-- =================================================== -->
<!-- now process the impact stmts attributed to this collab entity -->

<xsl:call-template name='process-collab-entity'>
<xsl:with-param name='isbyce' select='aiis:IMPACT_STMT_LIST'/>
<xsl:with-param name='ceorgref' select="$ceorguri"/>
</xsl:call-template>
</xsl:if>
</xsl:for-each>

<!-- =================================================== 
 at this point we re-run part of the last 
for loop to get a new list of persons 
 and their uri's to save in the extant Orgs Out xml file
-->
<xsl:result-document href='{$extOrgOut}'>
<xsl:element name='ExtantCEOrgs' namespace=''>
<xsl:for-each select='$uniqueNewCEOrgs//org'>

<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>

<xsl:element name='org' namespace=''>

<xsl:element name='uri' namespace=''>
<xsl:value-of select=
	'if(starts-with(uri,"NEW-")) then 
		substring-after(uri,"NEW-") else uri'/>
</xsl:element>

<xsl:element name='name' namespace=''>
<xsl:value-of select='name'/>
</xsl:element>

<xsl:element name='stateCountry' namespace=''>
<xsl:value-of select='stateCountry'/>
</xsl:element>

</xsl:element>


</xsl:for-each>
</xsl:element>
<xsl:value-of select='$NL'/>
</xsl:result-document>

</rdf:RDF>
<xsl:value-of select='$NL'/>

</xsl:template>

<!-- =================================================== -->
<!-- =================================================== -->
<xsl:template name='process-collab-entity'>
<xsl:param name='isbyce'/>
<xsl:param name='ceorgref'/>

<xsl:for-each select='$isbyce/aiis:IMPACT_STMT_INFO'>
<xsl:if test='./@hasTitle = "Yes" and ./@hasGoodAuthor = "Yes"'>
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

<rdf:type rdf:resource=
	'http://vivoweb.org/ontology/activity-insight#PartnerRole'/>

<rdf:type rdf:resource=
	'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<!-- 12 -->
<acti:partnerRoleOf rdf:resource='{$ceorgref}'/>

<acti:collaboratorRank rdf:datatype='http://www.w3.org/2001/XMLSchema#int'>
<xsl:value-of select='$rank'/>
</acti:collaboratorRank>
<!-- 8 -->
<core:roleIn
	rdf:resource="{concat($g_instance,$aiid)}"/>

</rdf:Description>

<!-- =================================================== -->


<rdf:Description rdf:about="{$ceorgref}">
<!-- 13 -->
<acti:hasPartnerRole rdf:resource="{concat($g_instance,$aiid,'-CE-',$rank)}"/>
</rdf:Description>


</xsl:if>
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
