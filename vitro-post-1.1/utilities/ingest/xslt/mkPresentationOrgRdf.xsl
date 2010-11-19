<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs='http://www.w3.org/2001/XMLSchema'
xmlns:core="http://vivoweb.org/ontology/core#"
xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
xmlns:bibo="http://purl.org/ontology/bibo/"
xmlns:foaf="http://xmlns.com/foaf/0.1/"
xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
xmlns:aipres="http://vivoweb.org/ontology/activity-insight"
xmlns:acti="http://vivoweb.org/ontology/activity-insight#"
xmlns:dm="http://www.digitalmeasures.com/schema/data"	
xmlns:vfx='http://vivoweb.org/ext/functions'
exclude-result-prefixes='xs vfx dm '
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

<xsl:template match='/aipres:PRESENT_ORG_LIST'>
<rdf:RDF>
<xsl:variable name='rawNewPROrgs'>
<xsl:element name='ExtantOrgs' inherit-namespaces='no'>
<xsl:for-each select='aipres:PRESENT_ORG'>

<xsl:variable name='name' 
	select='vfx:simple-trim(aipres:PRESENT_ORG_NAME)'/>
<xsl:variable name='ctr'  select='@index'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>

<xsl:if test='$name !=""'>
<xsl:variable name='knownUri'
	select='vfx:knownOrgUri($name,$extantOrgs)'/>

<xsl:variable name='orguri'
	select="if($knownUri != '') then 
			$knownUri else 
			concat($g_instance,$uno)"/>
<xsl:if test='$knownUri= ""'>

<xsl:element name='org' namespace=''>

<xsl:element name='uri' namespace=''>
<xsl:value-of select='concat("NEW-",$orguri)'/>
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

<xsl:variable name='uniqueNewPROrgs'>
<xsl:call-template name='NewOrgs'>
<xsl:with-param name='knowns' select='$rawNewPROrgs/ExtantOrgs'/>
</xsl:call-template>
</xsl:variable>

<xsl:for-each select='aipres:PRESENT_ORG'>
<xsl:variable name='name' 
	select='vfx:simple-trim(aipres:PRESENT_ORG_NAME)'/>
<xsl:variable name='ctr'  select='@index'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>

<!-- =================================================== -->
<!-- Declare a foaf:Organization (use extant org if it exists) -->
<xsl:if test='$name != ""'>

<xsl:variable name='knownUri' 
	select='vfx:knownOrgUri($name, $extantOrgs union
			$rawNewPROrgs/ExtantOrgs)'/>

<xsl:variable name='orguri' 
	select='if(starts-with($knownUri,"NEW-")) then 
		substring-after($knownUri,"NEW-") else 
		$knownUri'/>

<xsl:if test='starts-with($knownUri,"NEW-")'>
<xsl:if test=
	'not(preceding-sibling::aipres:PRESENT_ORG[
					vfx:clean(aipres:PRESENT_ORG_NAME) = 
					vfx:clean($name)])'>
<rdf:Description rdf:about="{$orguri}">
<rdf:type rdf:resource=
	'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource='http://xmlns.com/foaf/0.1/Organization'/>
<rdfs:label>
<xsl:value-of select='vfx:simple-trim(aipres:PRESENT_ORG_NAME)'/>
</rdfs:label>
</rdf:Description>
</xsl:if>
</xsl:if>
<!-- now process the PRESENT attributed to this org -->
<xsl:call-template name='process-org'>
<xsl:with-param name='list' select='aipres:PRESENT_LIST'/>
<xsl:with-param name='objref' select="$orguri"/>
</xsl:call-template>
</xsl:if>
</xsl:for-each>

<!-- =================================================== 
 	save new orgs
-->
<xsl:result-document href='{$extOrgOut}'>
<xsl:element name='ExtantOrgs' namespace=''>
<xsl:for-each select='$uniqueNewPROrgs//org'>

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
</xsl:result-document>
</rdf:RDF>
<xsl:value-of select='$NL'/>
</xsl:template>

<xsl:template name='process-org'>
<xsl:param name='list'/>
<xsl:param name='objref'/>
<xsl:for-each select='$list/aipres:PRESENT_INFO'>
<xsl:variable name='objid' select='.'/>
<xsl:variable name='hasTitle'  select='@hasTitle'/>
<xsl:variable name='hasOrg'  select='@hasOrg'/>
<xsl:variable name='hasConf'  select='@hasConf'/>
<xsl:variable name='public'  select='@public'/>
<xsl:variable name='nid' select='@ref_netid'/>
<!-- =================================================== -->
<!-- Declare object property mappings -->
<xsl:if test='$public = "Yes" and ($hasTitle = "Yes" or $hasConf = "Yes")'>
<xsl:choose>
  <xsl:when test='$hasConf = "Yes"'>

<rdf:Description 
	rdf:about="{concat($g_instance,'AI-CONF-',$objid)}" >
<bibo:organizer rdf:resource="{$objref}"/>
</rdf:Description>

  </xsl:when>
  <xsl:otherwise>
<!-- has Title -->
<rdf:Description 
	rdf:about="{concat($g_instance,'AI-',$objid)}" >
<bibo:organizer rdf:resource="{$objref}"/>
</rdf:Description>

  </xsl:otherwise>
</xsl:choose>
<!-- =================================================== -->
<!-- Declare object property mappings  -->


<xsl:choose>
  <xsl:when test='$hasConf = "Yes"'>
	<rdf:Description rdf:about="{$objref}">
	<core:organizerOf
		rdf:resource="{concat($g_instance,'AI-CONF-',$objid)}"/>
	</rdf:Description>
  </xsl:when>
  <xsl:otherwise>
	<rdf:Description rdf:about="{$objref}">
	<core:organizerOf
		rdf:resource="{concat($g_instance,'AI-',$objid)}"/>
	</rdf:Description>
  </xsl:otherwise>
</xsl:choose>




</xsl:if>

</xsl:for-each>
</xsl:template>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
