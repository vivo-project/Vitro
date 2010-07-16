<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aiis="http://vivoweb.org/activity-insight"
	xmlns:acti="http://vivoweb.org/activity-insight#"
        xmlns="http://vivoweb.org/activity-insight"
	xmlns:ai="http://www.digitalmeasures.com/schema/data"
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
<xsl:variable name='NL'>
<xsl:text>
</xsl:text>
</xsl:variable>

<xsl:variable name='pfxlen' select='4'/>


<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>

<xsl:variable name='extantFOrgs'
	select="document($extFOrgIn)/ExtantFOrgs"/>
<!-- ================================== -->
<xsl:template match='/aiis:FUNDING_ORG_LIST'>
<rdf:RDF>


<xsl:for-each select='aiis:IMPACT_STMTS_BY_FUNDING_ORG'>

<!-- create a core:FundingOrganization for this Funding Org 
OR use an old one -->


<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<!--xsl:comment>
<xsl:value-of select='$ctr'/> - <xsl:value-of select='$uno'/>
</xsl:comment -->

<!-- =================================================== -->
<!-- Declare a core:FundingOrganization (use extant org if it exists) -->

<xsl:variable name='knownUri' select='vfx:knownFOrgUri(aiis:FUNDING_ORG_NAME, $extantFOrgs)'/>

<xsl:variable name='forguri' select="if($knownUri != '') then $knownUri else concat('http://vivoweb.org/individual/',$uno)"/>

<!-- xsl:comment><xsl:value-of select='$forguri'/> - <xsl:value-of select='$knownUri'/></xsl:comment -->

<xsl:if test='$knownUri = ""'>
<rdf:Description rdf:about="{$forguri}">
<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource='http://vivoweb.org/ontology/core#FundingOrganization'/>

<rdfs:label>
<xsl:value-of select='vfx:trim(aiis:FUNDING_ORG_NAME)'/>
</rdfs:label>
<core:description>
<xsl:value-of select='vfx:trim(aiis:FUNDING_ORG_NAME)'/>
</core:description>
</rdf:Description>
</xsl:if>

<!-- =================================================== -->
<!-- now process the impact stmts attributed to this collab entity -->

<xsl:call-template name='process-funding-org'>
<xsl:with-param name='isbyfo' select='aiis:IMPACT_STMT_ID_LIST'/>
<xsl:with-param name='forgref' select="$forguri"/>
</xsl:call-template>

</xsl:for-each>

<!-- =================================================== 
 at this point we re-run part of the last for loop to get a new list of
 funding orgs
 and their uri's to save in the extant Orgs Out xml file
-->
<xsl:result-document href='{$extFOrgOut}'>
<xsl:element name='ExtantFOrgs'>
<xsl:for-each select='aiis:IMPACT_STMTS_BY_FUNDING_ORG'>

<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<xsl:variable name='knownUri' select='vfx:knownFOrgUri(aiis:FUNDING_ORG_NAME, $extantFOrgs)'/>

<xsl:variable name='forguri' select="if($knownUri != '') then $knownUri else concat('http://vivoweb.org/individual/',$uno)"/>

<xsl:if test='$knownUri = ""'>
<xsl:element name='forg' inherit-namespaces='no'>
<xsl:element name='uri' inherit-namespaces='no'>
<xsl:value-of select='$forguri'/></xsl:element>
<xsl:element name='name' inherit-namespaces='no'>
<xsl:value-of select='aiis:FUNDING_ORG_NAME'/></xsl:element>

</xsl:element>
</xsl:if>

</xsl:for-each>
</xsl:element>
</xsl:result-document>

</rdf:RDF>
</xsl:template>

<!-- =================================================== -->
<!-- =================================================== -->
<xsl:template name='process-funding-org'>
<xsl:param name='isbyfo'/>
<xsl:param name='forgref'/>

<xsl:for-each select='$isbyfo/aiis:IMPACT_STMT_ID'>
<xsl:variable name='aiid' select='.'/>

<!-- =================================================== -->
<!-- Declare property mapping acti:ImpactProject to 
core:FundingORganization -->
<!-- 10 -->
<rdf:Description rdf:about="{concat('http://vivoweb.org/individual/',$aiid)}" >
<core:fundedByAgent
	rdf:resource="{concat('http://vivoweb.org/individual/',$aiid)}"/>
</rdf:Description>



<!-- =================================================== -->

<rdf:Description rdf:about="{$forgref}">
<!-- 11 -->
<core:fundingAgentFor rdf:resource="{concat('http://vivoweb.org/individual/',$aiid)}"/>
</rdf:Description>



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
