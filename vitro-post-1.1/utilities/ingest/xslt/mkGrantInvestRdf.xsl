<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aigrant="http://vivoweb.org/activity-insight"
	xmlns:acti="http://vivoweb.org/activity-insight#"
        xmlns="http://vivoweb.org/activity-insight"
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

<xsl:variable name='knownUri' select='vfx:knownUri(aigrant:FirstName, aigrant:MiddleName, aigrant:LastName, $extantPersons)'/>

<xsl:variable name='foafuri' select="if($knownUri != '') then $knownUri else concat($g_instance,$uno)"/>

<!-- xsl:comment><xsl:value-of select='$foafuri'/> - 
<xsl:value-of select='$knownUri'/></xsl:comment -->

<xsl:if test='$knownUri != "" and aigrant:NetId != ""'>
<rdf:Description rdf:about="{$foafuri}">
<rdf:type 
rdf:resource='http://vivoweb.org/activity-insight#ActivityInsightPerson'/>
</rdf:Description>
</xsl:if>

<xsl:if test='$knownUri = ""'>
<rdf:Description rdf:about="{$foafuri}">
<rdf:type 
rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource='http://xmlns.com/foaf/0.1/Person'/>
<xsl:if test='aigrant:NetId != ""'>
<rdf:type 
rdf:resource='http://vivoweb.org/activity-insight#ActivityInsightPerson'/>
</xsl:if>
<rdfs:label>
<xsl:value-of select='vfx:trim(aigrant:INVESTIGATOR_NAME)'/>
</rdfs:label>

<core:middleName><xsl:value-of select='aigrant:MiddleName'/></core:middleName>
<core:firstName><xsl:value-of select='aigrant:FirstName'/></core:firstName>
<foaf:firstName><xsl:value-of select='aigrant:FirstName'/></foaf:firstName>
<core:lastName><xsl:value-of select='aigrant:LastName'/></core:lastName>
<foaf:lastName><xsl:value-of select='aigrant:LastName'/></foaf:lastName>

<xsl:if test='aigrant:NetId != ""'>

<xsl:variable name='nidxml' 
select="concat($rawXmlPath,'/',aigrant:NetId , '.xml')"/>

<!-- do not bother with these if file is not available -->
<xsl:if test='doc-available($nidxml)'>
<xsl:variable name='pci' select="document($nidxml)//dm:PCI"/>
<core:workEmail><xsl:value-of select='$pci/dm:EMAIL'/></core:workEmail>
<bibo:prefixName><xsl:value-of select='$pci/dm:PREFIX'/> </bibo:prefixName>
<core:workFax>
<xsl:value-of select='$pci/dm:FAX1'/>-<xsl:value-of select='$pci/dm:FAX2'/>-<xsl:value-of select='$pci/dm:FAX3'/>
</core:workFax>
<core:workPhone>
<xsl:value-of select='$pci/dm:OPHONE1'/>-<xsl:value-of select='$pci/dm:OPHONE2'/>-<xsl:value-of select='$pci/dm:OPHONE3'/>
</core:workPhone>
</xsl:if>

</xsl:if>

</rdf:Description>
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
<xsl:result-document href='{$extPerOut}'>
<xsl:element name='ExtantPersons' namespace=''>
<xsl:for-each select='aigrant:GRANTS_BY_INVESTIGATOR'>

<xsl:variable name='ctr'  select='@index'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<xsl:variable name='knownUri' 
select='vfx:knownUri(aigrant:FirstName, 
                     aigrant:MiddleName, aigrant:LastName, $extantPersons)'/>

<xsl:variable name='foafuri' 
select="if($knownUri != '') then 
$knownUri else concat($g_instance,$uno)"/>

<!-- must prevent duplicates -->
<xsl:if test="$knownUri = ''">
<xsl:element name='person' namespace=''>
<xsl:element name='uri'  namespace=''>
<xsl:value-of select='$foafuri'/>
</xsl:element>
<xsl:element name='fname' namespace=''>
<xsl:value-of select='aigrant:FirstName'/>
</xsl:element>
<xsl:element name='mname' namespace=''>
<xsl:value-of select='aigrant:MiddleName'/>
</xsl:element>
<xsl:element name='lname' namespace=''>
<xsl:value-of select='aigrant:LastName'/>
</xsl:element>
</xsl:element>
</xsl:if>

</xsl:for-each>
</xsl:element>
</xsl:result-document>

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
<xsl:value-of select='concat( "Investigator (",vfx:trim(../../aigrant:INVESTIGATOR_NAME), ")")'/>

</rdfs:label>

<rdf:type rdf:resource='http://vivoweb.org/ontology/core#ResearcherRole'/>

<rdf:type rdf:resource='http://vivoweb.org/ontology/core:InvestigatorRole'/>

<xsl:if test='$role="CO"'>
<rdf:type rdf:resource='http://vivoweb.org/ontology/core:Co-PrincipalInvestigatorRole'/>
</xsl:if>
<xsl:if test='$role="PI"'>
<rdf:type rdf:resource='http://vivoweb.org/ontology/core:PrincipalInvestigatorRole'/>
</xsl:if>

<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>

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



<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
