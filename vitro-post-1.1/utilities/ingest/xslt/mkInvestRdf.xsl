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

<xsl:param name='isByInvFile'  required='yes'/>
<xsl:param name='unoMapFile'  required='yes'/>
<xsl:param name='aiisXmlPath' required='yes'/>
<xsl:param name='aiisPrefix' required='yes'/>
<xsl:param name='extPerIn' required='yes'/>
<xsl:param name='extPerOut' required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl' />

<xsl:variable name='islist' 
  select="document($isByInvFile)//aiis:IMPACT_STMT_INFO"/>

<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>

<xsl:variable name='extantPersons'
	select="document($extPerIn)/ExtantPersons"/>
<!-- ================================== -->
<xsl:template match='/aiis:INVESTIGATOR_LIST'>
<rdf:RDF>

<!-- =================================== -->

<xsl:variable name='prenewps'>
<xsl:element name='ExtantPersons' inherit-namespaces='no'>
<xsl:for-each select='aiis:IMPACT_STMTS_BY_INVESTIGATOR'>

<xsl:if test='vfx:goodName(aiis:FirstName, 
	                   aiis:MiddleName, 
                           aiis:LastName)'>

<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>

<xsl:variable name='kUri' 
	select='vfx:knownUriByNetidOrName(aiis:FirstName, 
	                       		aiis:MiddleName, 
                               		aiis:LastName,
					aiis:Netid, 
                               		$extantPersons)'/>
<xsl:comment><xsl:value-of select='$kUri'/></xsl:comment>
<xsl:variable name='furi' 
select="if($kUri != '') then $kUri 
                            else concat($g_instance,$uno)"/>


<xsl:if test='$kUri = ""'>

<xsl:element name='person' inherit-namespaces='no'>
<xsl:element name='uri' inherit-namespaces='no'>
<xsl:value-of select='concat("NEW-",$furi)'/></xsl:element>
<xsl:element name='fname' inherit-namespaces='no'>
<xsl:value-of select='aiis:FirstName'/></xsl:element>
<xsl:element name='mname' inherit-namespaces='no'>
<xsl:value-of select='aiis:MiddleName'/></xsl:element>
<xsl:element name='lname' inherit-namespaces='no'>
<xsl:value-of select='aiis:LastName'/></xsl:element>
<xsl:element name='netid' inherit-namespaces='no'>
<xsl:value-of select='aiis:Netid'/></xsl:element>
</xsl:element>
</xsl:if>
</xsl:if>
</xsl:for-each>
</xsl:element>
</xsl:variable>



<xsl:variable name='newps'>
<xsl:call-template name='newPeople'>
<xsl:with-param name='knowns' select='$prenewps/ExtantPersons'/>
</xsl:call-template>
</xsl:variable>

<!-- =================================== -->

<xsl:call-template name='mkImpactProjects'/>

<xsl:for-each select='aiis:IMPACT_STMTS_BY_INVESTIGATOR'>
<xsl:if test='vfx:goodName(aiis:FirstName, 
	                   aiis:MiddleName, 
                           aiis:LastName)'>
<!-- create a foaf:person for this investigator  
OR use one from before -->


<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<!--xsl:comment>
<xsl:value-of select='$ctr'/> - <xsl:value-of select='$uno'/>
</xsl:comment -->

<!-- =================================================== -->
<!-- Declare a foaf:Person (use extant person if foaf exists) -->


<xsl:variable name='known' 
select='vfx:knownPersonByNetidOrName(aiis:FirstName, 
                     		aiis:MiddleName, 
                     		aiis:LastName, 
				aiis:Netid,
                     		$extantPersons union 
                     		$prenewps/ExtantPersons)'/>

<xsl:variable name='foafuri' 
select='if(starts-with($known/uri,"NEW-")) then 
        substring-after($known/uri,"NEW-") else 
        $known/uri'/>

<!--xsl:comment><xsl:value-of select='$knownUri'/></xsl:comment-->



<xsl:if test='starts-with($known/uri,"NEW-")'>
<xsl:if test='
not(vfx:hasIsoMatchInvestigator(., 
		preceding-sibling::aiis:IMPACT_STMTS_BY_INVESTIGATOR))'>
<rdf:Description rdf:about="{$foafuri}">

<rdf:type 
rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource='http://xmlns.com/foaf/0.1/Person'/>


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

<xsl:if test='$known/netid != ""'>

<xsl:variable name='nidxml' 
select="concat($aiisXmlPath,'/',$aiisPrefix,$known/netid , '.xml')"/>

<!-- do not bother with these if file is not available -->

<xsl:if test='doc-available($nidxml)'>
<rdf:type rdf:resource=
'http://vivoweb.org/ontology/activity-insight#ActivityInsightPerson'/>
<xsl:variable name='pci' select="document($nidxml)//dm:PCI"/>
<core:workEmail><xsl:value-of select='$pci/dm:EMAIL'/></core:workEmail>
<bibo:prefixName><xsl:value-of select='$pci/dm:PREFIX'/> </bibo:prefixName>
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
<!-- now process the impact stmts attributed to this investigator -->

<xsl:call-template name='process-investigator'>
<xsl:with-param name='isbyi' select='aiis:IMPACT_STMT_LIST'/>
<xsl:with-param name='foafref' select="$foafuri"/>
<xsl:with-param name='dep' select="aiis:Department"/>
</xsl:call-template>
</xsl:if>
</xsl:for-each>

<!-- =================================================== 
 at this point we save the new persons in the extant Persons Out xml file
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
<xsl:param name='isbyi'/>
<xsl:param name='foafref'/>
<xsl:param name='dep'/>
<xsl:for-each select='$isbyi/aiis:IMPACT_STMT_INFO'>
<xsl:if test='./@hasTitle = "Yes"'>
<xsl:variable name='aiid' select='.'/>
<xsl:variable name='rank' select='@collabRank'/>

<!-- =================================================== -->
<!-- Declare property mapping acti:ImpactProject to core:ResearcherRole -->

<rdf:Description rdf:about="{concat($g_instance,$aiid)}" >
<!-- 6 -->
<core:relatedRole
	rdf:resource="{concat($g_instance,$aiid,'-CI-',$rank)}"/>
</rdf:Description>

<!-- =================================================== -->
<!-- Declare core:ResearcherRole Individual Triples-->

<rdf:Description rdf:about="{concat($g_instance,$aiid,'-CI-',$rank)}">

<rdfs:label>
<!-- xsl:value-of select=
'concat( "Investigator (",
	vfx:simple-trim(../../aiis:INVESTIGATOR_NAME), ")")'/ -->

<xsl:value-of select='Investigator'/>
</rdfs:label>

<acti:investigatorNameAsListed>  
<xsl:value-of select='vfx:simple-trim(../../aiis:INVESTIGATOR_NAME)'/>
</acti:investigatorNameAsListed> 

<rdf:type rdf:resource='http://vivoweb.org/ontology/core#ResearcherRole'/>

<rdf:type rdf:resource=
'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<acti:investigatorDepartment>
<xsl:value-of select='$dep'/>
</acti:investigatorDepartment>

<!-- 14 -->
<core:researcherRoleOf rdf:resource='{$foafref}'/>

<acti:collaboratorRank rdf:datatype='http://www.w3.org/2001/XMLSchema#int'>
<xsl:value-of select='$rank'/>
</acti:collaboratorRank>

<!-- 7 -->
<core:roleIn
	rdf:resource="{concat($g_instance,$aiid)}"/>

</rdf:Description>

<!-- =================================================== -->

<rdf:Description rdf:about="{$foafref}">
<!-- 15 -->
<core:hasResearcherRole rdf:resource=
		"{concat($g_instance,$aiid,'-CI-',$rank)}"/>
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
<xsl:template name='mkImpactProjects'>

<xsl:for-each select='$islist'>
<xsl:if test='./@hasTitle = "Yes" and ./@hasGoodAuthor = "Yes"'>
<xsl:variable name='aiid' select='.'/>
<xsl:variable name='rawaiid' select='substring($aiid,$pfxlen)'/>
<xsl:variable name='rid' select='./@ref_netid'/>

<xsl:variable name='path' 
	select="concat($aiisXmlPath,'/',$aiisPrefix, $rid, '.xml')"/>
<xsl:variable name='ijpath' 
	select="document($path)//IMPACT_STATEMENT[@id=$rawaiid]"/>
<!-- xsl:comment>
<xsl:value-of select='$path'/> - <xsl:value-of select='$ijpath'/>
</xsl:comment -->
<xsl:call-template name='mkImpactProject'>
<xsl:with-param name='ijp' select="$ijpath"/>
<xsl:with-param name='aiid' select='$aiid'/>
<xsl:with-param name='rid' select='$rid'/>
</xsl:call-template>
</xsl:if>
</xsl:for-each>
</xsl:template>

<!-- ================================== -->
<xsl:template name='mkImpactProject'>
<xsl:param name='ijp'/>
<xsl:param name='aiid'/>
<xsl:param name='rid'/>

<rdf:Description rdf:about="{concat($g_instance,$aiid)}" >
<rdf:type rdf:resource=
	'http://vivoweb.org/ontology/activity-insight#ImpactProject'/>
<rdfs:label>
<xsl:value-of select="$ijp/TITLE"/>
</rdfs:label>

<xsl:if test='$ijp/START_START != ""'>
<core:startDate rdf:datatype='http://www.w3.org/2001/XMLSchema#date'>
<xsl:value-of select="$ijp/START_START"/></core:startDate>
</xsl:if>

<xsl:if test='$ijp/END_END != ""'>
<core:endDate rdf:datatype='http://www.w3.org/2001/XMLSchema#date'>
<xsl:value-of select="$ijp/END_END"/></core:endDate>
</xsl:if>

<acti:impactStatementSummary>
<xsl:value-of select="$ijp/SUMMARY"/></acti:impactStatementSummary>
<acti:impactStatementIssue>
<xsl:value-of select="$ijp/ISSUE"/></acti:impactStatementIssue>
<acti:impactStatementResponse>
<xsl:value-of select="$ijp/RESPONSE"/></acti:impactStatementResponse>
<acti:impactStatementImpact>
<xsl:value-of select="$ijp/IMPACT"/></acti:impactStatementImpact>

<acti:researchType>
<xsl:choose>
<xsl:when test='$ijp/RCHTYPE = "Both"'>
Both Basic Research and Applied Research
</xsl:when>
<xsl:when test='$ijp/RCHTYPE = "Neither"'>
Neither Basic Research nor Applied Research
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="$ijp/RCHTYPE"/>
</xsl:otherwise>
</xsl:choose>
</acti:researchType>



<acti:fundingType>
<xsl:choose>
<xsl:when test='$ijp/FUNDTYPE = "Other" and $ijp/FUNDTYPE_OTHER != "" '>
<xsl:value-of select='$ijp/FUNDTYPE_OTHER'/>
</xsl:when>
<xsl:when test='$ijp/FUNDTYPE != "Other" and $ijp/FUNDTYPE != ""'>
<xsl:value-of select='$ijp/FUNDTYPE'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='"Unspecified"'/>
</xsl:otherwise>
</xsl:choose>
</acti:fundingType>
</rdf:Description>

</xsl:template>


<xsl:template name='hasIsoMatchInvestigator'>
<xsl:param name='n'/>
<xsl:param name='nlist'/>
<xsl:param name='res' select='false()'/>
<xsl:choose>
<xsl:when test='$nlist and not($res)'>
<xsl:variable name='comp' select='vfx:isoName($n/aiis:FirstName,
						$n/aiis:MiddleName,
						$n/aiis:LastName,
						$nlist[1]/aiis:FirstName,
						$nlist[1]/aiis:MiddleName,
						$nlist[1]/aiis:LastName)'/>

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
