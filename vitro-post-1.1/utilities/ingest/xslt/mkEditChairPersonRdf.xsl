<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aiec="http://vivoweb.org/ontology/activity-insight"
	xmlns:acti="http://vivoweb.org/ontology/activity-insight#"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"	
	xmlns:vfx='http://vivoweb.org/ext/functions'
	exclude-result-prefixes='xs vfx dm aiec'
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


<xsl:template match='/aiec:EDITCHAIR_PERSON_LIST'>
<rdf:RDF>

<!-- =================================== -->

<xsl:variable name='prenewps'>
<xsl:element name='ExtantPersons' inherit-namespaces='no'>
<xsl:for-each select='aiec:PERSON'>
<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<!-- xsl:comment><xsl:value-of 
select='concat(aiec:fname,"|",
	aiec:mname,"|",
	aiec:lname,"|"
	,aiec:netid)'/></xsl:comment -->
<xsl:variable name='kUri' 
	select='vfx:knownUriByNetidOrName(aiec:fname, 
	                       		aiec:mname, 
                               		aiec:lname,
					aiec:netid, 
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
<xsl:value-of select='aiec:fname'/></xsl:element>
<xsl:element name='mname' inherit-namespaces='no'>
<xsl:value-of select='aiec:mname'/></xsl:element>
<xsl:element name='lname' inherit-namespaces='no'>
<xsl:value-of select='aiec:lname'/></xsl:element>
<xsl:element name='netid' inherit-namespaces='no'>
<xsl:value-of select='aiec:netid'/></xsl:element>
</xsl:element>

</xsl:if>
</xsl:for-each>
</xsl:element>

</xsl:variable>
<xsl:variable name='newps'>
<xsl:call-template name='newPeople'>
<xsl:with-param name='knowns' select='$prenewps/ExtantPersons'/>
</xsl:call-template>
</xsl:variable>

<xsl:for-each select='aiec:PERSON'>

<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>

<xsl:variable name='known' 
	select='vfx:knownPersonByNetidOrName(aiec:fname, 
					aiec:mname, 
					aiec:lname,
					aiec:netid, 
					$extantPersons union 
                     			$prenewps/ExtantPersons)'/>

<xsl:variable name='peruri' 
	select='if(starts-with($known/uri,"NEW-")) then 
		substring-after($known/uri,"NEW-") else 
		$known/uri'/>

<xsl:if test='not(starts-with($known/uri,"NEW-")) and $known/netid != ""'>

<rdf:Description rdf:about="{$peruri}">
<rdf:type 
rdf:resource='http://vivoweb.org/ontology/activity-insight#ActivityInsightPerson'/>
</rdf:Description>
</xsl:if>

<xsl:if test='starts-with($known/uri,"NEW-")'>

<xsl:if test='
not(vfx:hasIsoMatchRecipient(., 
			  preceding-sibling::aiec:PERSON))'>

<rdf:Description rdf:about="{$peruri}">
<rdf:type rdf:resource=
	'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource='http://xmlns.com/foaf/0.1/Person'/>

<xsl:if test='$known/netid != ""'>
<rdf:type rdf:resource=
	'http://vivoweb.org/ontology/activity-insight#ActivityInsightPerson'/>
</xsl:if>

<rdfs:label>
<xsl:value-ofselect='concat(vfx:simple-trim($known/lname),", ",
               vfx:simple-trim($known/fname)," ", 
               vfx:simple-trim($known/mname))'/>
</rdfs:label>
<core:middleName><xsl:value-of select='$known/mname'/></core:middleName>
<core:firstName><xsl:value-of select='$known/fname'/></core:firstName>
<foaf:firstName><xsl:value-of select='$known/fname'/></foaf:firstName>
<core:lastName><xsl:value-of select='$known/lname'/></core:lastName>
<foaf:lastName><xsl:value-of select='$known/lname'/></foaf:lastName>

<xsl:if test='$known/netid != ""'>

<xsl:variable name='nidxml' select="concat($rawXmlPath,'/',$known/netid , '.xml')"/>

<!-- do not bother with these if file is not available -->
<xsl:if test='doc-available($nidxml)'>
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

<xsl:call-template name='process-editchair'>
<xsl:with-param name='list' select='aiec:EDITCHAIR_LIST'/>
<xsl:with-param name='objref' select="$peruri"/>
</xsl:call-template>
</xsl:for-each>

<xsl:call-template name='NewPeopleOut'>
<xsl:with-param name='file' select='$extPerOut'/>
<xsl:with-param name='newpeople' select='$newps'/>
</xsl:call-template>

</rdf:RDF>
</xsl:template>

<!-- =================================================== -->
<!-- =================================================== -->
<xsl:template name='process-editchair'>
<xsl:param name='list'/>
<xsl:param name='objref'/>

<xsl:for-each select='$list/aiec:EDITCHAIR'>
<!-- =================================================== -->
<!-- Declare property mapping acti:EditChair to foaf:Person -->
<!-- 1 -->
<xsl:variable name='ecid' select='@id'/>
<rdf:Description rdf:about="{concat($g_instance,'AI-',$ecid)}" >
<acti:editChairFor rdf:resource="{$objref}"/>
</rdf:Description>
<!-- =================================================== -->
<!-- Declare property mapping foaf:Person to acti:EditChair -->
<!-- 2 -->

<rdf:Description rdf:about="{$objref}">
<core:hasEditChair rdf:resource="{concat($g_instance,'AI-',$ecid)}" />
</rdf:Description>

</xsl:for-each>
</xsl:template>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
