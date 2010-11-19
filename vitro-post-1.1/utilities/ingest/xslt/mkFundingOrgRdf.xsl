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
<xsl:param name='extFOrgIn' required='yes'/>
<xsl:param name='extFOrgOut' required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>

<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>

<xsl:variable name='extantFOrgs'
	select="document($extFOrgIn)/ExtantOrgs"/>
<!-- ================================== -->
<xsl:template match='/aiis:FUNDING_ORG_LIST'>
<rdf:RDF>
<xsl:variable name='rawNewFundOrgs'>
<xsl:element name='ExtantOrgs' inherit-namespaces='no'>
<xsl:for-each select='aiis:IMPACT_STMTS_BY_FUNDING_ORG'>
<xsl:variable name='name' select='vfx:simple-trim(aiis:FUNDING_ORG_NAME)'/>
<xsl:variable name='ilk' select='@ilk'/>
<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>

<xsl:if test='not(contains($ilk,"OTHER"))'>

<xsl:variable name='knownUri'
select='vfx:knownOrgUri($name, $extantFOrgs)'/>
<xsl:variable name='forguri'
	select="if($knownUri != '') then 
			$knownUri else 
			concat($g_instance,$uno)"/>
<xsl:if test='$knownUri= ""'>

<xsl:element name='org' namespace=''>

<xsl:element name='uri' namespace=''>
<xsl:value-of select='concat("NEW-",$forguri)'/>
</xsl:element>

<xsl:element name='name' namespace=''>
<xsl:value-of select='$name'/>
</xsl:element>

</xsl:element>

</xsl:if>
</xsl:if>
</xsl:for-each>
</xsl:element>
</xsl:variable>

<xsl:variable name='uniqueNewFundOrgs'>
<xsl:call-template name='NewOrgs'>
<xsl:with-param name='knowns' select='$rawNewFundOrgs/ExtantOrgs'/>
</xsl:call-template>
</xsl:variable>


<xsl:for-each select='aiis:IMPACT_STMTS_BY_FUNDING_ORG'>

<!-- create a core:FundingOrganization for this Funding Org 
OR use an old one -->
<xsl:variable name='name' select='vfx:simple-trim(aiis:FUNDING_ORG_NAME)'/>
<xsl:variable name='ilk' select='@ilk'/>
<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>

<xsl:comment>
<xsl:value-of select='$name'/> - <xsl:value-of select='$ctr'/> - <xsl:value-of select='$uno'/>
</xsl:comment>

<!-- =================================================== -->
<!-- Declare a core:FundingOrganization (use extant org if it exists) -->
<!-- do not create one if in the 'OTHER' case -->
<xsl:variable name='knownUri' 
	select='vfx:knownOrgUri($name, 
			$extantFOrgs union
			$rawNewFundOrgs/ExtantOrgs)'/>

<xsl:variable name='forguri' 
	select='if(starts-with($knownUri,"NEW-")) then 
		substring-after($knownUri,"NEW-") else 
		$knownUri'/>

<xsl:comment><xsl:value-of select='$ilk'/> - <xsl:value-of select='$forguri'/> - <xsl:value-of select='$knownUri'/></xsl:comment>

<xsl:if test='starts-with($knownUri,"NEW-") and 
		not(contains($ilk,"OTHER"))'>

<rdf:Description rdf:about="{$forguri}">
<rdf:type rdf:resource=
	'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource=
	'http://vivoweb.org/ontology/core#FundingOrganization'/>

<rdfs:label>
<xsl:value-of select='vfx:simple-trim(aiis:FUNDING_ORG_NAME)'/>
</rdfs:label>
<core:description>
<xsl:value-of select='vfx:simple-trim(aiis:FUNDING_ORG_NAME)'/>
</core:description>
<acti:fundingIlk>
<xsl:value-of select='concat("FUNDING_",$ilk)'/> </acti:fundingIlk>
</rdf:Description>
</xsl:if>

<!-- =================================================== -->
<!-- now process the impact stmts attributed to this funding org -->

<xsl:call-template name='process-funding-org'>
<xsl:with-param name='isbyfo' select='aiis:IMPACT_STMT_ID_LIST'/>
<xsl:with-param name='forgref' select="$forguri"/>
<xsl:with-param name='ilk' select='$ilk'/>
<xsl:with-param name='name' select='$name'/>
</xsl:call-template>

</xsl:for-each>

<!-- =================================================== 
save new orgs
-->
<xsl:result-document href='{$extFOrgOut}'>
<xsl:element name='ExtantOrgs' namespace=''>
<xsl:for-each select='$uniqueNewFundOrgs//org'>

<xsl:element name='org' namespace=''>

<xsl:element name='uri' namespace=''>
<xsl:value-of select=
	'if(starts-with(uri,"NEW-")) then 
		substring-after(uri,"NEW-") else uri'/>
</xsl:element>

<xsl:element name='name' namespace=''>
<xsl:value-of select='name'/>
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
<xsl:template name='process-funding-org'>
<xsl:param name='isbyfo'/>
<xsl:param name='forgref'/>
<xsl:param name='ilk'/>
<xsl:param name='name'/>
<xsl:for-each select='$isbyfo/aiis:IMPACT_STMT_ID'>
<xsl:if test='./@hasTitle = "Yes" and ./@hasGoodAuthor = "Yes"'>
<xsl:variable name='aiid' select='.'/>

<!-- =================================================== -->
<!-- Declare property mapping acti:ImpactProject to 
core:FundingOrganization -->
<!-- 10 -->
<rdf:Description rdf:about="{concat($g_instance,$aiid)}" >

<xsl:if test='not(contains($ilk,"OTHER")) and $name != "Other"'>
<core:fundedByAgent
	rdf:resource="{$forgref}"/>
</xsl:if>
<xsl:if test='contains($ilk,"OTHER")'>
<xsl:choose>

<xsl:when test='$ilk = "FEDRCHOTHER"'>
<acti:otherFedResearchFunding>
<xsl:value-of select='$name'/>
</acti:otherFedResearchFunding>
</xsl:when>

<xsl:when test='$ilk = "FEDEXTOTHER"'>
<acti:otherFedExtensionFunding>
<xsl:value-of select='$name'/>
</acti:otherFedExtensionFunding>
</xsl:when>

<xsl:when test='$ilk = "ACADOTHER"'>
<acti:otherAcademicFunding>
<xsl:value-of select='$name'/>
</acti:otherAcademicFunding>
</xsl:when>

<xsl:when test='$ilk = "OTHERFED"'>
<acti:otherFederalFunding>
<xsl:value-of select='$name'/>
</acti:otherFederalFunding>
</xsl:when>

<xsl:when test='$ilk = "PRIVATEOTHER"'>
<acti:otherPrivateFunding>
<xsl:value-of select='$name'/>
</acti:otherPrivateFunding>

</xsl:when>
</xsl:choose>

</xsl:if>
</rdf:Description>

<!-- =================================================== -->
<xsl:if test='not(contains($ilk,"OTHER")) and $name != "Other"'>
<rdf:Description rdf:about="{$forgref}">
<!-- 11 -->
<core:fundingAgentFor rdf:resource="{concat($g_instance,$aiid)}"/>
</rdf:Description>
</xsl:if>

</xsl:if>
</xsl:for-each>

</xsl:template>

<!-- ================================== -->
<xsl:template match='aiis:IMPACT_STMT_LIST'/>

<xsl:template match='aiis:ALT_SRC_IMPACT_STMT_ID'/>

<xsl:template match='aiis:IMPACT_STMT_ID'/>


<!-- =================================================== -->

<!-- =================================================== -->


<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
