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
	exclude-result-prefixes='xs vfx dm'
	>

<xsl:param name='unoMapFile'  required='yes'/>
<xsl:param name='rawXmlPath' required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>

<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>

<xsl:template match='/aisvcprof:SERVICE_PROFESSIONAL_LIST'>
<rdf:RDF>
<xsl:for-each select='aisvcprof:SERVICE_PROFESSIONAL'>
<xsl:if test='aisvcprof:netid != ""'>
<xsl:variable name='objid' select='@id'/>
<xsl:variable name='nidxml' 
select="concat($rawXmlPath,'/',aisvcprof:netid, '.xml')"/>

<xsl:variable name='ref' select='document($nidxml)//dm:SERVICE_PROFESSIONAL[@id = $objid]' />
<xsl:if test='$ref/dm:PUBLIC_VIEW="Yes"'>
<rdf:Description rdf:about="{concat($g_instance,'AI-',@id)}" >
<rdf:type 
rdf:resource='http://vivoweb.org/ontology/core#ServiceProviderRole'/>
<rdf:type 
rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>

<rdfs:label>

<xsl:value-of select="$ref/dm:ROLE"/>

</rdfs:label>

<core:description><xsl:value-of select='$ref/dm:DESC'/></core:description>
<acti:accomplishments><xsl:value-of select='$ref/dm:ACCOMPLISH'/></acti:accomplishments>
<acti:audience><xsl:value-of select='$ref/dm:AUDIENCE'/></acti:audience>

<xsl:if test='$ref/dm:START_START'>
<core:startDate rdf:datatype='http://www.w3.org/2001/XMLSchema#date'><xsl:value-of select='$ref/dm:START_START'/></core:startDate>
</xsl:if>

<xsl:if test='$ref/dm:END_END'>
<core:endDate rdf:datatype='http://www.w3.org/2001/XMLSchema#date'><xsl:value-of select='$ref/dm:END_END'/></core:endDate>
</xsl:if>

</rdf:Description>
</xsl:if>

</xsl:if>
</xsl:for-each>
</rdf:RDF>
<xsl:value-of select='$NL'/>
</xsl:template>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
