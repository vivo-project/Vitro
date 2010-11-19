<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
	xmlns:event="http://purl.org/NET/c4dm/event.owl#"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aipres="http://vivoweb.org/ontology/activity-insight"
	xmlns:acti="http://vivoweb.org/ontology/activity-insight#"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"	
	xmlns:vfx='http://vivoweb.org/ext/functions'
	exclude-result-prefixes='xs vfx dm aipres'
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
<xsl:template match='/aipres:PRESENT_PERSON_LIST'>
<rdf:RDF>

<xsl:variable name='prenewps'>
<xsl:element name='ExtantPersons' inherit-namespaces='no'>
<xsl:for-each select='aipres:PRESENT_BY_PERSON'>

<xsl:if test='vfx:goodName(aipres:FirstName, 
                           aipres:MiddleName, 
			   aipres:LastName)'>

<xsl:variable name='ctr'  select='@index'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<xsl:variable name='kUri' 
	select='vfx:knownUriByNetidOrName(aipres:FirstName, 
                          		  aipres:MiddleName, 
			  		  aipres:LastName,
					  aipres:NetId,
					  $extantPersons)'/>
<xsl:variable name='furi' 
select="if($kUri != '') then $kUri 
                            else concat($g_instance,$uno)"/>


<xsl:if test='$kUri = ""'>
<xsl:element name='person' inherit-namespaces='no'>
<xsl:element name='uri' inherit-namespaces='no'>
<xsl:value-of select='concat("NEW-",$furi)'/></xsl:element>
<xsl:element name='fname' inherit-namespaces='no'>
<xsl:value-of select='aipres:FirstName'/></xsl:element>
<xsl:element name='mname' inherit-namespaces='no'>
<xsl:value-of select='aipres:MiddleName'/></xsl:element>
<xsl:element name='lname' inherit-namespaces='no'>
<xsl:value-of select='aipres:LastName'/></xsl:element>
<xsl:element name='netid' inherit-namespaces='no'>
<xsl:value-of select='aipres:NetId'/></xsl:element>
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




<xsl:for-each select='aipres:PRESENT_BY_PERSON'>

<xsl:if test='vfx:goodName(aipres:FirstName, 
                           aipres:MiddleName, 
			   aipres:LastName)'>

<xsl:variable name='ctr'  select='@index'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<!--xsl:comment>
<xsl:value-of select='$ctr'/> - <xsl:value-of select='$uno'/>
</xsl:comment -->


<xsl:variable name='known' 
	select='vfx:knownPersonByNetidOrName(aipres:FirstName, 
					     aipres:MiddleName, 
					     aipres:LastName, 
					     aipres:NetId,
					     $extantPersons union 
                     		       	 	$prenewps/ExtantPersons)'/>


<xsl:variable name='foafuri'
	select='if(starts-with($known/uri,"NEW-")) then 
		substring-after($known/uri,"NEW-") else 
		$known/uri'/>	



<xsl:if test='not(starts-with($known/uri,"NEW-")) and aipres:NetId != ""'>
<rdf:Description rdf:about="{$foafuri}">
<rdf:type rdf:resource=
	'http://vivoweb.org/ontology/activity-insight#ActivityInsightPerson'/>
</rdf:Description>
</xsl:if>

<xsl:if test='starts-with($known/uri,"NEW-")'>
<xsl:if test='
	not(vfx:hasIsoMatchAuthor(., 
			  preceding-sibling::aipres:PRESENT_BY_PERSON))'>
<rdf:Description rdf:about="{$foafuri}">
<rdf:type rdf:resource=
	'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource=
	'http://xmlns.com/foaf/0.1/Person'/>
<xsl:if test='aipres:NetId != ""'>
<rdf:type rdf:resource=
	'http://vivoweb.org/ontology/activity-insight#ActivityInsightPerson'/>
</xsl:if>
<rdfs:label>
<xsl:value-of select='vfx:simple-trim(aipres:PERSON_NAME)'/>
</rdfs:label>


<core:middleName><xsl:value-of select='$known/mname'/></core:middleName>
<core:firstName><xsl:value-of select='$known/fname'/></core:firstName>
<foaf:firstName><xsl:value-of select='$known/fname'/></foaf:firstName>
<core:lastName><xsl:value-of select='$known/lname'/></core:lastName>
<foaf:lastName><xsl:value-of select='$known/lname'/></foaf:lastName>

<xsl:if test='$known/netid != ""'>

<xsl:variable name='nidxml' 
select="concat($rawXmlPath,'/',$known/netid , '.xml')"/>


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
</xsl:if>
<!-- =================================================== -->
<!-- now process the PRESENTs attributed to this person -->

<xsl:call-template name='process-person'>
<xsl:with-param name='objbyi' select='aipres:PRESENT_LIST'/>
<xsl:with-param name='foafref' select="$foafuri"/>
</xsl:call-template>
</xsl:if>
</xsl:for-each>

<xsl:call-template name='NewPeopleOut'>
<xsl:with-param name='file' select='$extPerOut'/>
<xsl:with-param name='newpeople' select='$newps'/>
</xsl:call-template>

</rdf:RDF>
<xsl:value-of select='$NL'/>
</xsl:template>

<!-- =================================================== -->
<!-- =================================================== -->
<xsl:template name='process-person'>
<xsl:param name='objbyi'/>
<xsl:param name='foafref'/>

<xsl:for-each select='$objbyi/aipres:PRESENT_INFO'>

<xsl:variable name='aiid' select='.'/>
<xsl:variable name='rank' select='@rank'/>
<xsl:variable name='role' select='@role'/>
<xsl:variable name='hasTitle'  select='@hasTitle'/>
<xsl:variable name='hasOrg'  select='@hasOrg'/>
<xsl:variable name='hasConf'  select='@hasConf'/>
<xsl:variable name='public'  select='@public'/>
<xsl:variable name='nid' select='@ref_netid'/>

<xsl:variable name='nidxml' 
	select="concat($rawXmlPath,'/',$nid, '.xml')"/>
<xsl:variable name='ref' 
	select='document($nidxml)//dm:PRESENT[@id = $aiid]' />

<!-- =================================================== -->
<!-- Declare property mapping PRESENT to core:PresenterRole -->

<xsl:if test='$public = "Yes" and ($hasTitle = "Yes" or $hasConf = "Yes")'>

<xsl:choose>
  <xsl:when test='$hasTitle = "Yes"'>
	<rdf:Description rdf:about="{concat($g_instance,'AI-', $aiid)}" >

	<core:relatedRole
		rdf:resource="{concat($g_instance,
					'AI-PRES-',
					$aiid,'-',
					$rank)}"/>
	</rdf:Description>

  </xsl:when>
  <xsl:otherwise>
	<rdf:Description rdf:about="{concat($g_instance,'AI-CONF-',$aiid)}" >

	<core:relatedRole
		rdf:resource="{concat($g_instance,
					'AI-PRES-',
					$aiid,'-',
					$rank)}"/>
	</rdf:Description>
  </xsl:otherwise>
</xsl:choose>



<!-- =================================================== -->
<!-- Declare core:PresenterRole Individual Triples-->

<rdf:Description rdf:about="{concat($g_instance,'AI-PRES-',$aiid,'-',$rank)}">


<rdfs:label>

<xsl:choose>
  <xsl:when test='starts-with($role, "PO")'>
	<xsl:value-of select='"Presenter"'/>
  </xsl:when>
  <xsl:when test='starts-with($role, "PA")'>
	<xsl:value-of select='"Presenter and Author"'/>
  </xsl:when>
  <xsl:when test='starts-with($role, "A")'>
	<xsl:value-of select='"Author"'/>
  </xsl:when>
  <xsl:when test='starts-with($role, "O")'>
	<xsl:value-of select='"Organizer"'/>
  </xsl:when>
  <xsl:otherwise>
	<xsl:value-of select='"Presenter"'/>
  </xsl:otherwise>
</xsl:choose>

</rdfs:label>

<acti:authorNameAsListed>  
<xsl:value-of select='vfx:simple-trim(../../aipres:PERSON_NAME)'/>
</acti:authorNameAsListed> 
<rdf:type rdf:resource='http://vivoweb.org/ontology/core#PresenterRole'/>

<rdf:type 
rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>

<core:presenterRoleOf rdf:resource='{$foafref}'/>

<core:authorRank rdf:datatype='http://www.w3.org/2001/XMLSchema#int'>
<xsl:value-of select='$rank'/>
</core:authorRank>


<xsl:choose>
  <xsl:when test='$hasTitle = "Yes"'>
	<core:roleIn
		rdf:resource="{concat($g_instance,'AI-', $aiid)}"/>
  </xsl:when>
  <xsl:otherwise>
	<core:roleIn
		rdf:resource="{concat($g_instance,'AI-CONF-', $aiid)}"/>
  </xsl:otherwise>
</xsl:choose>



</rdf:Description>

<!-- =================================================== -->

<rdf:Description rdf:about="{$foafref}">
<!-- 4 -->
<core:hasPresenterRole 
	rdf:resource="{concat($g_instance,'AI-PRES-',$aiid,'-',$rank)}"/>
</rdf:Description>


</xsl:if>
</xsl:for-each>

</xsl:template>


<xsl:template name='hasIsoMatchAuthor'>
<xsl:param name='n'/>
<xsl:param name='nlist'/>
<xsl:param name='res' select='false()'/>
<xsl:choose>
<xsl:when test='$nlist and not($res)'>

<xsl:variable name='comp' select='vfx:isoName($n/aipres:FirstName,
						$n/aipres:MiddleName,
						$n/aipres:LastName,
						$nlist[1]/aipres:FirstName,
						$nlist[1]/aipres:MiddleName,
						$nlist[1]/aipres:LastName)'/>

<xsl:call-template name='hasIsoMatchAuthor'>
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

<xsl:function name='vfx:hasIsoMatchAuthor' as='xs:boolean'>
<xsl:param name='n'/>
<xsl:param name='nlist'/>
<xsl:call-template name='hasIsoMatchAuthor'>
<xsl:with-param name='n' select='$n'/>
<xsl:with-param name='nlist' select='$nlist'/>
</xsl:call-template>
</xsl:function>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
