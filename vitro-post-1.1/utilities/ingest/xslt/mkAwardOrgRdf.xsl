<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aiah="http://vivoweb.org/ontology/activity-insight"
	xmlns:acti="http://vivoweb.org/ontology/activity-insight#"
        xmlns="http://vivoweb.org/ontology/activity-insight"
xmlns:dm="http://www.digitalmeasures.com/schema/data"	
	xmlns:vfx='http://vivoweb.org/ext/functions'
	exclude-result-prefixes='xs vfx dm'
	>

<xsl:param name='unoMapFile'  required='yes'/>

<xsl:param name='extOrgIn' required='yes'/>
<xsl:param name='extOrgOut' required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>


<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>

<xsl:variable name='extantOrgs'
	select="document($extOrgIn)/ExtantOrgs"/>

<xsl:template match='/aiah:ORG_LIST'>
<rdf:RDF>
<xsl:for-each select='aiah:ORG'>

<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>

<!-- =================================================== -->
<!-- Declare a foaf:Organization (use extant org if it exists) -->

<xsl:variable name='knownUri' select='vfx:knownOrgUri(aiah:org_name, $extantOrgs)'/>

<xsl:variable name='orguri' 
select="if($knownUri != '') then $knownUri else concat($g_instance,$uno)"/>

<xsl:if test='$knownUri = ""'>
<rdf:Description rdf:about="{$orguri}">
<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource='http://xmlns.com/foaf/0.1/Organization'/>
<rdfs:label>
<xsl:value-of select='vfx:trim(aiah:org_name)'/>
</rdfs:label>
</rdf:Description>
</xsl:if>

<!-- now process the awards attributed to this funding org -->
<xsl:call-template name='process-org-awards'>
<xsl:with-param name='list' select='aiah:AWARD_LIST'/>
<xsl:with-param name='objref' select="$orguri"/>
</xsl:call-template>

</xsl:for-each>

<!-- =================================================== 
 at this point we re-run part of the last for loop to get a new list of
 orgs
 and their uri's to save in the extant Orgs Out xml file
-->
<xsl:result-document href='{$extOrgOut}'>
<xsl:element name='ExtantOrgs' namespace=''>
<xsl:for-each select='aiah:ORG'>
<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<xsl:variable name='knownUri' select='vfx:knownOrgUri(aiah:org_name, $extantOrgs)'/>

<xsl:variable name='orguri' 
select="if($knownUri != '') then $knownUri else concat($g_instance,$uno)"/>

<xsl:element name='org' namespace=''>
<xsl:element name='uri' namespace=''>
<xsl:value-of select='$orguri'/>
</xsl:element>
<xsl:element name='name' namespace=''>
<xsl:value-of select='aiah:org_name'/>
</xsl:element>
</xsl:element>
</xsl:for-each>
</xsl:element>
</xsl:result-document>
</rdf:RDF>
<xsl:value-of select='$NL'/>
</xsl:template>

<xsl:template name='process-org-awards'>
<xsl:param name='list'/>
<xsl:param name='objref'/>
<xsl:for-each select='$list/aiah:AWARD'>
<xsl:variable name='ahid' select='@ahid'/>
<!-- =================================================== -->
<!-- Declare property mapping core:AwardOrHonor to foaf:Organization -->
<!-- 3 -->
<rdf:Description rdf:about="{concat($g_instance,'AI-',$ahid)}" >
<core:conferringOrganization
	rdf:resource="{$objref}"/>
</rdf:Description>

<!-- =================================================== -->
<!-- Declare property from foaf:Organization to core:AwardOrHonor -->
<!-- 4 -->
<rdf:Description rdf:about="{$objref}">
<core:sponsorsAward rdf:resource="{concat($g_instance,'AI-',$ahid)}"/>
</rdf:Description>
</xsl:for-each>

</xsl:template>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
