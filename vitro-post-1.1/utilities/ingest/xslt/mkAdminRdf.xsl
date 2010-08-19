<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aiadm="http://vivoweb.org/activity-insight"
	xmlns:acti="http://vivoweb.org/activity-insight#"
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

<xsl:template match='/aiadm:ADMIN_LIST'>
<rdf:RDF>
<xsl:for-each select='aiadm:ADMIN'>
<xsl:if test='aiadm:netid != ""'>
<xsl:variable name='objid' select='@id'/>
<xsl:variable name='nidxml' 
select="concat($rawXmlPath,'/',aiadm:netid, '.xml')"/>

<xsl:variable name='ref' select='document($nidxml)//dm:ADMIN[@id = $objid]' />
<xsl:if test='$ref/dm:PUBLIC_VIEW="Yes" and $ref/dm:AC_YEAR = "2008-2009"'>
<rdf:Description rdf:about="{concat($g_instance,@id)}" >
<rdf:type 
rdf:resource='http://vivoweb.org/activity-insight#Admin'/>
<rdf:type 
rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>

<rdfs:label>
<xsl:value-of select="$ref/dm:AC_YEAR"/>
</rdfs:label>

<!-- put other data proerties here -->
<xsl:for-each select="$ref/dm:PRIORITY_AREA">
<acti:priorityArea><xsl:value-of select='.'/></acti:priorityArea>
</xsl:for-each>
<xsl:for-each select="$ref/dm:DISCIPLINE">
<acti:discipline><xsl:value-of select='.'/></acti:discipline>
</xsl:for-each>
<acti:rank><xsl:value-of select="$ref/dm:RANK"/></acti:rank>

<acti:department><xsl:value-of select="$ref/dm:DEP"/></acti:department>
<acti:college><xsl:value-of select="$ref/dm:COLLEGE"/></acti:college>
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
