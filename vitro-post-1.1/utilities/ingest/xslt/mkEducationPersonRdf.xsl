<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aied="http://vivoweb.org/ontology/activity-insight"
	xmlns:acti="http://vivoweb.org/ontology/activity-insight#"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"	
	xmlns:vfx='http://vivoweb.org/ext/functions'
	exclude-result-prefixes='xs vfx dm aied'
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


<xsl:template match='/aied:EDUCATION_PERSON_LIST'>
<rdf:RDF>
<xsl:for-each select='aied:PERSON'>

<xsl:variable name='ctr'  select='@index'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>

<xsl:variable name='knownUri' 
select='vfx:knownUri(aied:fname, aied:mname, aied:lname, $extantPersons)'/>

<xsl:variable name='peruri' 
select="if($knownUri != '') then $knownUri else concat($g_instance,$uno)"/>

<xsl:if test='$knownUri != "" and aied:netid != ""'>

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
<xsl:if test='aied:netid != ""'>
<rdf:type 
rdf:resource='http://vivoweb.org/ontology/activity-insight#ActivityInsightPerson'/>
</xsl:if>
<rdfs:label>
<xsl:value-of select='vfx:trim(aied:fullname)'/>
</rdfs:label>

<core:middleName><xsl:value-of select='aied:mname'/></core:middleName>
<core:firstName><xsl:value-of select='aied:fname'/></core:firstName>
<foaf:firstName><xsl:value-of select='aied:fname'/></foaf:firstName>
<core:lastName><xsl:value-of select='aied:lname'/></core:lastName>
<foaf:lastName><xsl:value-of select='aied:lname'/></foaf:lastName>

<xsl:if test='aied:netid != ""'>

<xsl:variable name='nidxml' select="concat($rawXmlPath,'/',aied:netid , '.xml')"/>

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


<xsl:call-template name='process-education'>
<xsl:with-param name='list' select='aied:EDUCATION_LIST'/>
<xsl:with-param name='objref' select="$peruri"/>
</xsl:call-template>
</xsl:for-each>

<xsl:result-document href='{$extPerOut}'>
<xsl:element name='ExtantPersons' namespace=''>
<xsl:for-each select='aied:EDUCATION'>

<xsl:variable name='ctr'  select='@index'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<xsl:variable name='knownUri' 
select='vfx:knownUri(aied:fname, aied:mname, aied:lname, $extantPersons)'/>

<xsl:variable name='peruri' 
select="if($knownUri != '') then $knownUri else concat($g_instance,$uno)"/>

<!-- must prevent duplicates -->
<xsl:if test="$knownUri = ''">
<xsl:element name='person' namespace=''>
<xsl:element name='uri'  namespace=''>
<xsl:value-of select='$peruri'/>
</xsl:element>
<xsl:element name='fname' namespace=''>
<xsl:value-of select='aied:fname'/>
</xsl:element>
<xsl:element name='mname' namespace=''>
<xsl:value-of select='aied:mname'/>
</xsl:element>
<xsl:element name='lname' namespace=''>
<xsl:value-of select='aied:lname'/>
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
<xsl:template name='process-education'>
<xsl:param name='list'/>
<xsl:param name='objref'/>

<xsl:for-each select='$list/aied:EDUCATION'>
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


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
