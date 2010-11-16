<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aiedu="http://vivoweb.org/ontology/activity-insight"
	xmlns:acti="http://vivoweb.org/ontology/activity-insight#"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"	
	xmlns:vfx='http://vivoweb.org/ext/functions'
	exclude-result-prefixes='xs vfx dm aiedu'
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


<xsl:template match='/aiedu:EDUCATION_PERSON_LIST'>
<rdf:RDF>

<xsl:variable name='prenewps'>
<xsl:element name='ExtantPersons' inherit-namespaces='no'>
<xsl:for-each select='aiedu:PERSON'>

<xsl:if test='vfx:goodName(aiedu:fname, 
	                   aiedu:mname, 
                           aiedu:lname)'>

<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>

<xsl:variable name='kUri' 
	select='vfx:knownUriByNetidOrName(aiedu:fname, 
	                       		aiedu:mname, 
                               		aiedu:lname,
					aiedu:netid, 
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
<xsl:value-of select='aiedu:fname'/></xsl:element>
<xsl:element name='mname' inherit-namespaces='no'>
<xsl:value-of select='aiedu:mname'/></xsl:element>
<xsl:element name='lname' inherit-namespaces='no'>
<xsl:value-of select='aiedu:lname'/></xsl:element>
<xsl:element name='netid' inherit-namespaces='no'>
<xsl:value-of select='aiedu:netid'/></xsl:element>
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

<xsl:for-each select='aiedu:PERSON'>

<xsl:variable name='ctr'  select='@index'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>

<xsl:variable name='known' 
select='vfx:knownPersonByNetidOrName(aiedu:fname, 
				     aiedu:mname, 
                                     aiedu:lname, 
				     aiedu:netid,
				     $extantPersons union 
                     		       $prenewps/ExtantPersons)'/>

<xsl:variable name='peruri' 
select='if(starts-with($known/uri,"NEW-")) then 
        substring-after($known/uri,"NEW-") else 
        $known/uri'/>

<xsl:if test='not(starts-with($known/uri,"NEW-")) and $known/netid != ""'>

<rdf:Description rdf:about="{$peruri}">
<rdf:type rdf:resource=
'http://vivoweb.org/ontology/activity-insight#ActivityInsightPerson'/>
</rdf:Description>
</xsl:if>

<xsl:if test='starts-with($known/uri,"NEW-")'>
<xsl:if test='
	not(vfx:hasIsoMatchEdPerson(., 
			  preceding-sibling::aiedu:PERSON))'>

<rdf:Description rdf:about="{$peruri}">
<rdf:type rdf:resource=
	'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource='http://xmlns.com/foaf/0.1/Person'/>
<xsl:if test='$known/netid != ""'>
<rdf:type rdf:resource=
	'http://vivoweb.org/ontology/activity-insight#ActivityInsightPerson'/>
</xsl:if>
<rdfs:label>
<xsl:value-of select='vfx:trim($known/fullname)'/>
</rdfs:label>

<core:middleName><xsl:value-of select='$known/mname'/></core:middleName>
<core:firstName><xsl:value-of select='$known/fname'/></core:firstName>
<foaf:firstName><xsl:value-of select='$known/fname'/></foaf:firstName>
<core:lastName><xsl:value-of select='$known/lname'/></core:lastName>
<foaf:lastName><xsl:value-of select='$known/lname'/></foaf:lastName>

<xsl:if test='$known/netid != ""'>

<xsl:variable name='nidxml' select="concat($rawXmlPath,'/',
					   $known/netid , '.xml')"/>

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

<xsl:call-template name='process-education'>
<xsl:with-param name='list' select='aiedu:EDUCATION_LIST'/>
<xsl:with-param name='objref' select="$peruri"/>
</xsl:call-template>
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
<xsl:template name='process-education'>
<xsl:param name='list'/>
<xsl:param name='objref'/>

<xsl:for-each select='$list/aiedu:EDUCATION'>
<!-- =================================================== -->
<!-- Declare property mapping acti:Education to foaf:Person -->
<!-- 1 -->
<xsl:variable name='edid' select='@id'/>
<rdf:Description rdf:about="{concat($g_instance,'AI-',$edid)}" >
<core:educationalTrainingOf rdf:resource="{$objref}"/>
</rdf:Description>
<!-- =================================================== -->
<!-- Declare property mapping foaf:Person to acti:Education -->
<!-- 2 -->

<rdf:Description rdf:about="{$objref}">
<core:educationalTraining
rdf:resource="{concat($g_instance,'AI-',$edid)}" />
</rdf:Description>

</xsl:for-each>
</xsl:template>

<!-- ================================== -->
<xsl:template name='hasIsoMatchEdPerson'>
<xsl:param name='n'/>
<xsl:param name='nlist'/>
<xsl:param name='res' select='false()'/>
<xsl:choose>
<xsl:when test='$nlist and not($res)'>
<xsl:variable name='comp' select='vfx:isoName($n/aiedu:fname,
						$n/aiedu:mname,
						$n/aiedu:lname,
						$nlist[1]/aiedu:fname,
						$nlist[1]/aiedu:mname,
						$nlist[1]/aiedu:lname)'/>

<xsl:call-template name='hasIsoMatchEdPerson'>
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

<xsl:function name='vfx:hasIsoMatchEdPerson' as='xs:boolean'>
<xsl:param name='n'/>
<xsl:param name='nlist'/>
<xsl:call-template name='hasIsoMatchEdPerson'>
<xsl:with-param name='n' select='$n'/>
<xsl:with-param name='nlist' select='$nlist'/>
</xsl:call-template>
</xsl:function>


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
