<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aipres="http://vivoweb.org/ontology/activity-insight"
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

<xsl:template match='/aipres:PRESENT_LIST'>
<rdf:RDF>
<xsl:for-each select='aipres:PRESENT'>
<xsl:if test='aipres:netid != ""'>
<xsl:variable name='objid' select='./aipres:PRESENT_ID'/>
<xsl:variable name='nidxml' 
select="concat($rawXmlPath,'/',aipres:netid, '.xml')"/>

<xsl:variable name='ref' 
select='document($nidxml)//dm:PRESENT[@id = $objid]' />
<xsl:if test='$ref/dm:PUBLIC_VIEW!=""'>
<rdf:Description rdf:about="{concat($g_instance,$objid)}" >
<rdf:type 
rdf:resource='core:Presentation'/>
<rdf:type 
rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>

<rdfs:label>
<!-- e.g.
<xsl:value-of select="$ref/dm:TITLE"/>
-->
</rdfs:label>

<!-- 

  put other data proerties here 

-->

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
