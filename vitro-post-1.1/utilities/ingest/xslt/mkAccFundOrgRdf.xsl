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

<xsl:param name='extFOrgIn' required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>

<xsl:variable name='extantFOrgs'
	select="document($extFOrgIn)/ExtantOrgs"/>
<!-- ================================== -->
<xsl:template match='/aiis:FUNDING_ORG_LIST'>
<rdf:RDF>
<xsl:for-each select='aiis:IMPACT_STMTS_BY_FUNDING_ORG'>

<!-- create a core:FundingOrganization for this Funding Org 
OR use an old one -->
<xsl:variable name='name' select='vfx:simple-trim(aiis:FUNDING_ORG_NAME)'/>
<xsl:variable name='ilk' select='@ilk'/>

<xsl:variable name='knownUri' 
	select='vfx:knownOrgUri($name,$extantFOrgs)'/>


<xsl:if test='$knownUri != "" and not(contains($ilk,"OTHER"))'>

<rdf:Description rdf:about="{$knownUri}">
<rdf:type rdf:resource=
	'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource=
	'http://vivoweb.org/ontology/core#FundingOrganization'/>
</rdf:Description>
</xsl:if>
</xsl:for-each>
</rdf:RDF>
<xsl:value-of select='$NL'/>

</xsl:template>

<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
