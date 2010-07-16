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

<xsl:param name='isByInvFile'  required='yes'/>
<xsl:param name='unoMapFile'  required='yes'/>
<xsl:param name='aiisXmlPath' required='yes'/>
<xsl:param name='aiisPrefix' required='yes'/>
<xsl:param name='extPerIn' required='yes'/>
<xsl:param name='extPerOut' required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>
<xsl:variable name='NL'>
<xsl:text>
</xsl:text>
</xsl:variable>

<xsl:variable name='pfxlen' select='4'/>


<xsl:variable name='islist' 
  select="document($isByInvFile)//aiis:IMPACT_STMT_INFO"/>

<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>

<xsl:variable name='extantPersons'
	select="document($extPerIn)/ExtantPersons"/>
<!-- ================================== -->
<xsl:template match='/aiis:INVESTIGATOR_LIST'>
<rdf:RDF>

<xsl:call-template name='mkImpactProjects'/>

<xsl:for-each select='aiis:IMPACT_STMTS_BY_INVESTIGATOR'>

<!-- create a foaf:person for this investigator  
OR use one from VIVO-Cornell -->


<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<!--xsl:comment>
<xsl:value-of select='$ctr'/> - <xsl:value-of select='$uno'/>
</xsl:comment -->

<!-- =================================================== -->
<!-- Declare a foaf:Person (use extant person if foaf exists) -->

<xsl:variable name='knownUri' select='vfx:knownUri(aiis:FirstName, aiis:MiddleName, aiis:LastName, $extantPersons)'/>

<xsl:variable name='foafuri' select="if($knownUri != '') then $knownUri else concat('http://vivoweb.org/individual/',$uno)"/>

<!-- xsl:comment><xsl:value-of select='$foafuri'/> - <xsl:value-of select='$knownUri'/></xsl:comment -->

<xsl:if test='$knownUri = ""'>
<rdf:Description rdf:about="{$foafuri}">
<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource='http://xmlns.com/foaf/0.1/Person'/>

<rdfs:label>
<xsl:value-of select='vfx:trim(aiis:INVESTIGATOR_NAME)'/>
</rdfs:label>

<core:middleName><xsl:value-of select='aiis:MiddleName'/></core:middleName>
<core:firstName><xsl:value-of select='aiis:FirstName'/></core:firstName>
<foaf:firstName><xsl:value-of select='aiis:FirstName'/></foaf:firstName>
<core:lastName><xsl:value-of select='aiis:LastName'/></core:lastName>
<foaf:lastName><xsl:value-of select='aiis:LastName'/></foaf:lastName>

<xsl:if test='aiis:NetId != ""'>

<xsl:variable name='nidxml' select="concat($aiisXmlPath,'/',$aiisPrefix,aiis:NetId , '.xml')"/>

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
<!-- now process the impact stmts attributed to this investigator -->

<xsl:call-template name='process-investigator'>
<xsl:with-param name='abya' select='aiis:IMPACT_STMT_LIST'/>
<xsl:with-param name='foafref' select="$foafuri"/>
</xsl:call-template>

</xsl:for-each>

<!-- =================================================== 
 at this point we re-run part of the last for loop to get a new list of persons 
 and their uri's to save in the extant Persons Out xml file
-->
<xsl:result-document href='{$extPerOut}'>
<xsl:element name='ExtantPersons'>
<xsl:for-each select='aiis:IMPACT_STMTS_BY_INVESTIGATOR'>

<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<xsl:variable name='knownUri' select='vfx:knownUri(aiis:FirstName, aiis:MiddleName, aiis:LastName, $extantPersons)'/>

<xsl:variable name='foafuri' select="if($knownUri != '') then $knownUri else concat('http://vivoweb.org/individual/',$uno)"/>

<xsl:if test='$knownUri = ""'>
<xsl:element name='person' inherit-namespaces='no'>
<xsl:element name='uri' inherit-namespaces='no'>
<xsl:value-of select='$foafuri'/></xsl:element>
<xsl:element name='fname' inherit-namespaces='no'>
<xsl:value-of select='aiis:FirstName'/></xsl:element>
<xsl:element name='mname' inherit-namespaces='no'>
<xsl:value-of select='aiis:MiddleName'/></xsl:element>
<xsl:element name='lname' inherit-namespaces='no'>
<xsl:value-of select='aiis:LastName'/></xsl:element>
</xsl:element>
</xsl:if>

</xsl:for-each>
</xsl:element>
</xsl:result-document>

</rdf:RDF>
</xsl:template>

<!-- =================================================== -->
<!-- =================================================== -->
<xsl:template name='process-investigator'>
<xsl:param name='abya'/>
<xsl:param name='foafref'/>

<xsl:for-each select='$abya/aiis:IMPACT_STMT_INFO'>
<xsl:variable name='aiid' select='.'/>
<xsl:variable name='rank' select='@collabRank'/>

<!-- =================================================== -->
<!-- Declare property mapping acti:ImpactProject to core:ResearcherRole -->

<rdf:Description rdf:about="{concat('http://vivoweb.org/individual/',$aiid)}" >
<!-- 6 -->
<core:relatedRole
	rdf:resource="{concat('http://vivoweb.org/individual/',$aiid,'-',$rank)}"/>
</rdf:Description>

<!-- =================================================== -->
<!-- Declare core:ResearcherRole Individual Triples-->

<rdf:Description rdf:about="{concat('http://vivoweb.org/individual/',$aiid,'-',$rank)}">

<rdfs:label>
<xsl:value-of select='vfx:trim(../../aiis:INVESTIGATOR_NAME)'/>

</rdfs:label>

<rdf:type rdf:resource='http://vivoweb.org/ontology/core#ResearcherRole'/>

<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>

<!-- 14 -->
<core:ResearcherRoleOf rdf:resource='{$foafref}'/>

<core:authorRank rdf:datatype='http://www.w3.org/2001/XMLSchema#int'>
<xsl:value-of select='$rank'/>
</core:authorRank>

<!-- 7 -->
<core:roleIn
	rdf:resource="{concat('http://vivoweb.org/individual/',$aiid)}"/>

</rdf:Description>

<!-- =================================================== -->

<rdf:Description rdf:about="{$foafref}">
<!-- 15 -->
<core:hasResearcherRole rdf:resource="{concat('http://vivoweb.org/individual/',$aiid,'-',$rank)}"/>
</rdf:Description>

</xsl:for-each>

</xsl:template>

<!-- ================================== -->
<xsl:template match='aiis:IMPACT_STMT_LIST'/>

<xsl:template match='aiis:ALT_SRC_IMPACT_STMT_INFO'/>

<xsl:template match='aiis:IMPACT_STMT_INFO'/>


<!-- =================================================== -->
<!-- =================================================== -->
<xsl:template name='mkImpactProjects'>

<xsl:for-each select='$islist'>

<xsl:variable name='aiid' select='.'/>
<xsl:variable name='rawaiid' select='substring($aiid,$pfxlen)'/>
<xsl:variable name='rid' select='./@ref_netid'/>

<xsl:variable name='path' select="concat($aiisXmlPath,'/',$aiisPrefix, $rid, '.xml')"/>
<xsl:variable name='ijpath' select="document($path)//IMPACT_STATEMENT[@id=$rawaiid]"/>
<!-- xsl:comment>
<xsl:value-of select='$path'/> - <xsl:value-of select='$ijpath'/>
</xsl:comment -->
<xsl:call-template name='mkImpactProject'>
<xsl:with-param name='ijp' select="$ijpath"/>
<xsl:with-param name='aiid' select='$aiid'/>
<xsl:with-param name='rid' select='$rid'/>
</xsl:call-template>

</xsl:for-each>
</xsl:template>

<!-- ================================== -->
<xsl:template name='mkImpactProject'>
<xsl:param name='ijp'/>
<xsl:param name='aiid'/>
<xsl:param name='rid'/>

<rdf:Description rdf:about="{concat('http://vivoweb.org/individual/',$aiid)}" >
<rdf:type rdf:resource='http://vivoweb.org/activity-insight#ImpactProject'/>
<rdfs:label>
<xsl:value-of select="$ijp/TITLE"/>
</rdfs:label>

<acti:startDate><xsl:value-of select="$ijp/START_START"/></acti:startDate>
<acti:endDate><xsl:value-of select="$ijp/END_END"/></acti:endDate>
<acti:impactStatementSummary><xsl:value-of select="$ijp/SUMMARY"/></acti:impactStatementSummary>
<acti:impactStatementIssue><xsl:value-of select="$ijp/ISSUE"/></acti:impactStatementIssue>
<acti:impactStatementResponse><xsl:value-of select="$ijp/RESPONSE"/></acti:impactStatementResponse>
<acti:impactStatementImpact><xsl:value-of select="$ijp/IMPACT"/></acti:impactStatementImpact>
<core:freeTextKeyword><xsl:value-of select="$ijp/RCHTYPE"/></core:freeTextKeyword>

</rdf:Description>

</xsl:template>



<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
