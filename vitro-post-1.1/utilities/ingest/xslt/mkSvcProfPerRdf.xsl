<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aisvcprof="http://vivoweb.org/ontology/activity-insight"
	xmlns:acti="http://vivoweb.org/ontology/activity-insight#"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"	
	xmlns:vfx='http://vivoweb.org/ext/functions'
	exclude-result-prefixes='xs vfx dm aisvcprof'
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


<xsl:template match='/aisvcprof:SERVICE_PROFESSIONAL_PERSON_LIST'>
<rdf:RDF>
<xsl:for-each select='aisvcprof:PERSON'>

<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>

<xsl:variable name='knownUri' 
select='vfx:knownUri(aisvcprof:fname, aisvcprof:mname, aisvcprof:lname, $extantPersons)'/>

<xsl:variable name='peruri' 
select="if($knownUri != '') then $knownUri else concat($g_instance,$uno)"/>

<xsl:if test='$knownUri != "" and aisvcprof:netid != ""'>

<rdf:Description rdf:about="{$peruri}">
<rdf:type 
rdf:resource='http://vivoweb.org/ontology/activity-insight#ActivityInsightPerson'/>
</rdf:Description>
</xsl:if>

<xsl:if test='$knownUri = ""'>
<rdf:Description rdf:about="{$peruri}">
<rdf:type 
rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource='http://xmlns.com/foaf/0.1/Person'/>
<xsl:if test='aisvcprof:netid != ""'>
<rdf:type 
rdf:resource='http://vivoweb.org/ontology/activity-insight#ActivityInsightPerson'/>
</xsl:if>
<rdfs:label>
<xsl:value-of select='vfx:trim(aisvcprof:fullname)'/>
</rdfs:label>

<core:middleName><xsl:value-of select='aisvcprof:mname'/></core:middleName>
<core:firstName><xsl:value-of select='aisvcprof:fname'/></core:firstName>
<foaf:firstName><xsl:value-of select='aisvcprof:fname'/></foaf:firstName>
<core:lastName><xsl:value-of select='aisvcprof:lname'/></core:lastName>
<foaf:lastName><xsl:value-of select='aisvcprof:lname'/></foaf:lastName>

<xsl:if test='aisvcprof:netid != ""'>

<xsl:variable name='nidxml' select="concat($rawXmlPath,'/',aisvcprof:netid , '.xml')"/>

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


<xsl:call-template name='process-object'>
<xsl:with-param name='list' select='aisvcprof:SERVICE_PROFESSIONAL_LIST'/>
<xsl:with-param name='objref' select="$peruri"/>
</xsl:call-template>
</xsl:for-each>

<xsl:result-document href='{$extPerOut}'>
<xsl:element name='ExtantPersons' namespace=''>
<xsl:for-each select='aisvcprof:SERVICE_PROFESSIONAL'>

<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<xsl:variable name='knownUri' 
select='vfx:knownUri(aisvcprof:fname, aisvcprof:mname, aisvcprof:lname, $extantPersons)'/>

<xsl:variable name='peruri' 
select="if($knownUri != '') then $knownUri else concat($g_instance,$uno)"/>

<!-- must prevent duplicates -->
<xsl:if test="$knownUri = ''">
<xsl:element name='person' namespace=''>
<xsl:element name='uri'  namespace=''>
<xsl:value-of select='$peruri'/>
</xsl:element>
<xsl:element name='fname' namespace=''>
<xsl:value-of select='aisvcprof:fname'/>
</xsl:element>
<xsl:element name='mname' namespace=''>
<xsl:value-of select='aisvcprof:mname'/>
</xsl:element>
<xsl:element name='lname' namespace=''>
<xsl:value-of select='aisvcprof:lname'/>
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
<xsl:template name='process-object'>
<xsl:param name='list'/>
<xsl:param name='objref'/>

<xsl:for-each select='$list/aisvcprof:SERVICE_PROFESSIONAL'>
<!-- =================================================== -->
<!-- Declare property mapping ???ServiceProviderRole to foaf:Person -->
<!-- 1 -->
<xsl:variable name='objid' select='@id'/>
<rdf:Description rdf:about="{concat($g_instance,'AI-',$objid)}" >
<core:serviceProviderRoleOf rdf:resource="{$objref}"/>
</rdf:Description>
<!-- =================================================== -->
<!-- Declare property mapping foaf:Person to core:ServiceProviderRole -->
<!-- 2 -->

<rdf:Description rdf:about="{$objref}">
<core:hasServiceProviderRole
rdf:resource="{concat($g_instance,'AI-',$objid)}" />
</rdf:Description>

</xsl:for-each>
</xsl:template>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
