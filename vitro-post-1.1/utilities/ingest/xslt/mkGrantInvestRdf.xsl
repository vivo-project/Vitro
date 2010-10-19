<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aigrant="http://vivoweb.org/ontology/activity-insight"
	xmlns:acti="http://vivoweb.org/ontology/activity-insight#"
        xmlns="http://vivoweb.org/ontology/activity-insight"
xmlns:dm="http://www.digitalmeasures.com/schema/data"	
	xmlns:vfx='http://vivoweb.org/ext/functions'
	exclude-result-prefixes='xs vfx'
	>


<xsl:param name='unoMapFile'  required='yes'/>
<xsl:param name='rawXmlPath' required='yes'/>
<xsl:param name='extPerIn' required='yes'/>
<xsl:param name='extPerOut' required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>

<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>

<xsl:variable name='extantPersons'
	select="document($extPerIn)/ExtantPersons"/>
<!-- ================================== -->
<xsl:template match='/aigrant:INVESTIGATOR_LIST'>
<rdf:RDF>
<!-- =================================== -->

<xsl:variable name='prenewps'>
<xsl:element name='ExtantPersons' inherit-namespaces='no'>
<xsl:for-each select='aigrant:GRANTS_BY_INVESTIGATOR'>
<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<!-- xsl:comment><xsl:value-of 
select='concat(aigrant:FirstName,"|",
	aigrant:MiddleName,"|",
	aigrant:LastName,"|"
	,aigrant:Netid)'/></xsl:comment-->
<xsl:variable name='kUri' 
	select='vfx:knownUriByNetidOrName(aigrant:FirstName, 
	                       		aigrant:MiddleName, 
                               		aigrant:LastName,
					aigrant:Netid, 
                               		$extantPersons)'/>
<!-- xsl:comment><xsl:value-of select='$kUri'/></xsl:comment -->
<xsl:variable name='furi' 
select="if($kUri != '') then $kUri 
                            else concat($g_instance,$uno)"/>


<xsl:if test='$kUri = ""'>

<xsl:element name='person' inherit-namespaces='no'>
<xsl:element name='uri' inherit-namespaces='no'>
<xsl:value-of select='concat("NEW-",$furi)'/></xsl:element>
<xsl:element name='fname' inherit-namespaces='no'>
<xsl:value-of select='aigrant:FirstName'/></xsl:element>
<xsl:element name='mname' inherit-namespaces='no'>
<xsl:value-of select='aigrant:MiddleName'/></xsl:element>
<xsl:element name='lname' inherit-namespaces='no'>
<xsl:value-of select='aigrant:LastName'/></xsl:element>
<xsl:element name='netid' inherit-namespaces='no'>
<xsl:value-of select='aigrant:Netid'/></xsl:element>
</xsl:element>

</xsl:if>
</xsl:for-each>
</xsl:element>
</xsl:variable>

<xsl:variable name='newps'>
<xsl:call-template name='newPeople'>
<!-- xsl:with-param name='list' select='aigrant:GRANTS_BY_INVESTIGATOR'/-->
<xsl:with-param name='knowns' select='$prenewps/ExtantPersons'/>
</xsl:call-template>
</xsl:variable>


<xsl:for-each select='aigrant:GRANTS_BY_INVESTIGATOR'>

<!-- create a foaf:person for this investigator  
OR use one from VIVO-Cornell -->


<xsl:variable name='ctr'  select='@index'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<!--xsl:comment>
<xsl:value-of select='$ctr'/> - <xsl:value-of select='$uno'/>
</xsl:comment -->

<!-- =================================================== -->
<!-- Declare a foaf:Person (use extant person if foaf exists) -->

<xsl:variable name='known' 
select='vfx:knownPersonByNetidOrName(aigrant:FirstName, 
				aigrant:MiddleName, 
				aigrant:LastName, 
				aigrant:NetId,
				$extantPersons union 
                     		$prenewps/ExtantPersons)'/>

<xsl:variable name='foafuri' 
	select='if(starts-with($known/uri,"NEW-")) then 
		substring-after($known/uri,"NEW-") else 
		$known/uri'/>

<!-- xsl:comment><xsl:value-of select='$foafuri'/> - 
<xsl:value-of select='$knownUri'/></xsl:comment -->

<xsl:if test='not(starts-with($known/uri,"NEW-")) and $known/netid != ""'>
<rdf:Description rdf:about="{$foafuri}">
<rdf:type rdf:resource=
	'http://vivoweb.org/ontology/activity-insight#ActivityInsightPerson'/>
</rdf:Description>
</xsl:if>

<xsl:if test='starts-with($known/uri,"NEW-")'>
<xsl:if test='
not(vfx:hasIsoMatchInvestigator(., 
		preceding-sibling::aigrant:GRANTS_BY_INVESTIGATOR))'>
<rdf:Description rdf:about="{$foafuri}">
<rdf:type rdf:resource=
	'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource='http://xmlns.com/foaf/0.1/Person'/>
<xsl:if test='$known/netid != ""'>
<rdf:type rdf:resource=
	'http://vivoweb.org/ontology/activity-insight#ActivityInsightPerson'/>
</xsl:if>

<rdfs:label>
<xsl:value-of select='concat(vfx:simple-trim($known/lname),", ",
               vfx:simple-trim($known/fname)," ", 
               vfx:simple-trim($known/mname))'/>
</rdfs:label>

<core:middleName><xsl:value-of select='$known/mname'/></core:middleName>
<core:firstName><xsl:value-of select='$known/fname'/></core:firstName>
<foaf:firstName><xsl:value-of select='$known/fname'/></foaf:firstName>
<core:lastName><xsl:value-of select='$known/lname'/></core:lastName>
<foaf:lastName><xsl:value-of select='$known/lname'/></foaf:lastName>

<xsl:if test='$known/netid = ""'>

<acti:college><xsl:value-of select='aigrant:College'/></acti:college>
<acti:dept><xsl:value-of select='aigrant:College'/></acti:dept>
</xsl:if>

<xsl:if test='$known/netid != ""'>

<xsl:variable name='nidxml' 
select="concat($rawXmlPath,'/',$known/netid , '.xml')"/>

<!-- do not bother with these if file is not available -->
<xsl:if test='doc-available($nidxml)'>
<xsl:variable name='pci' select="document($nidxml)//dm:PCI"/>
<core:workEmail>
<xsl:value-of select='$pci/dm:EMAIL'/>
</core:workEmail>
<bibo:prefixName>
<xsl:value-of select='$pci/dm:PREFIX'/> 
</bibo:prefixName>
<core:workFax>
<xsl:value-of select='$pci/dm:FAX1'/>-
<xsl:value-of select='$pci/dm:FAX2'/>-
<xsl:value-of select='$pci/dm:FAX3'/>
</core:workFax>
<core:workPhone>
<xsl:value-of select='$pci/dm:OPHONE1'/>-
<xsl:value-of select='$pci/dm:OPHONE2'/>-
<xsl:value-of select='$pci/dm:OPHONE3'/>
</core:workPhone>
</xsl:if>

</xsl:if>

</rdf:Description>
</xsl:if>
</xsl:if>
<!-- =================================================== -->
<!-- now process the grants attributed to this investigator -->

<xsl:call-template name='process-investigator'>
<xsl:with-param name='grbyi' select='aigrant:GRANTS_LIST'/>
<xsl:with-param name='foafref' select="$foafuri"/>
</xsl:call-template>

</xsl:for-each>

<!-- =================================================== 
 at this point we re-run part of the last for loop 
to get a new list of persons 
 and their uri's to save in the extant Persons Out xml file
-->
<xsl:call-template name='NewPeopleOut'>
<xsl:with-param name='file' select='$extPerOut'/>
<xsl:with-param name='newpeople' select='$newps'/>
</xsl:call-template>

</rdf:RDF>
<xsl:value-of select='$NL'/>
</xsl:template>

<!-- =================================================== -->
<!-- =================================================== -->
<xsl:template name='process-investigator'>
<xsl:param name='grbyi'/>
<xsl:param name='foafref'/>
<xsl:for-each select='$grbyi/aigrant:GRANT_INFO'>
<xsl:variable name='aiid' select='.'/>
<xsl:variable name='rank' select='@collabRank'/>
<xsl:variable name='role' select='../../aigrant:Role'/>
<!-- =================================================== -->
<!-- Declare property mapping core:Grant to core:ResearcherRole -->

<rdf:Description rdf:about="{concat($g_instance,$aiid)}" >
<!-- 1 -->
<core:relatedRole
	rdf:resource="{concat($g_instance,$aiid,'-GR-',$rank)}"/>
</rdf:Description>

<!-- =================================================== -->
<!-- Declare core:ResearcherRole Individual Triples-->

<rdf:Description rdf:about="{concat($g_instance,$aiid,'-GR-',$rank)}">

<rdfs:label>
<xsl:value-of 
select='concat( "Investigator (",
		vfx:simple-trim(../../aigrant:INVESTIGATOR_NAME), ")")'/>

</rdfs:label>
<acti:investigatorNameAsListed>  
<xsl:value-of select='vfx:simple-trim(../../aigrant:INVESTIGATOR_NAME)'/>
</acti:investigatorNameAsListed> 

<rdf:type rdf:resource='http://vivoweb.org/ontology/core#ResearcherRole'/>

<rdf:type rdf:resource='http://vivoweb.org/ontology/core:InvestigatorRole'/>

<xsl:if test='$role="CO"'>
<rdf:type rdf:resource=
	'http://vivoweb.org/ontology/core:Co-PrincipalInvestigatorRole'/>
</xsl:if>
<xsl:if test='$role="PI"'>
<rdf:type rdf:resource=
	'http://vivoweb.org/ontology/core:PrincipalInvestigatorRole'/>
</xsl:if>

<rdf:type rdf:resource=
	'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>

<!-- 15 -->
<core:researcherRoleOf rdf:resource='{$foafref}'/>

<acti:collaboratorRank rdf:datatype='http://www.w3.org/2001/XMLSchema#int'>
<xsl:value-of select='$rank'/>
</acti:collaboratorRank>

<!-- 2 -->
<core:roleIn
	rdf:resource="{concat($g_instance,$aiid)}"/>

</rdf:Description>

<!-- =================================================== -->

<rdf:Description rdf:about="{$foafref}">
<!-- 16 -->
<core:hasResearcherRole rdf:resource="{concat($g_instance,$aiid,'-GR-',$rank)}"/>
</rdf:Description>

</xsl:for-each>

</xsl:template>


<xsl:template name='hasIsoMatchInvestigator'>
<xsl:param name='n'/>
<xsl:param name='nlist'/>
<xsl:param name='res' select='false()'/>
<xsl:choose>
<xsl:when test='$nlist and not($res)'>
<xsl:variable name='comp' select='vfx:isoName($n/aigrant:FirstName,
						$n/aigrant:MiddleName,
						$n/aigrant:LastName,
						$nlist[1]/aigrant:FirstName,
						$nlist[1]/aigrant:MiddleName,
						$nlist[1]/aigrant:LastName)'/>

<xsl:call-template name='hasIsoMatchInvestigator'>
<xsl:with-param name='n' select='$n'/>
<xsl:with-param name='nlist' select='$nlist[position()>1]'/>
<xsl:with-param name='res' select='$res or $comp'/>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='$res'/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:function name='vfx:hasIsoMatchInvestigator' as='xs:boolean'>
<xsl:param name='n'/>
<xsl:param name='nlist'/>
<xsl:call-template name='hasIsoMatchInvestigator'>
<xsl:with-param name='n' select='$n'/>
<xsl:with-param name='nlist' select='$nlist'/>
</xsl:call-template>
</xsl:function>


<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
