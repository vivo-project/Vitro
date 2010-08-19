<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aimc="http://vivoweb.org/activity-insight"
	xmlns:acti="http://vivoweb.org/activity-insight#"
        xmlns="http://vivoweb.org/activity-insight"
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

<xsl:template match='/aimc:MEDCONT_LIST'>
<rdf:RDF>
<xsl:for-each select='aimc:MEDCONT'>
<xsl:if test='aimc:netid != ""'>
<xsl:variable name='mcid' select='@id'/>
<xsl:variable name='nidxml' 
select="concat($rawXmlPath,'/',aimc:netid, '.xml')"/>

<xsl:variable name='mc' select='document($nidxml)//dm:MEDCONT[@id = $mcid]' />
<xsl:if test='$mc/dm:PUBLIC_VIEW="Yes"'>
<rdf:Description rdf:about="{concat($g_instance,@id)}" >
<rdf:type 
rdf:resource='http://vivoweb.org/activity-insight#MediaContribution'/>
<rdf:type 
rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>

<rdfs:label>
<xsl:value-of select="$mc/dm:TYPE"/>
</rdfs:label>

<acti:contributionArea>
<xsl:value-of select='$mc/dm:CONAREA'/></acti:contributionArea>
<acti:mediaName>
<xsl:value-of select='$mc/dm:NAME'/></acti:mediaName>

<core:website>
<xsl:value-of select='$mc/dm:WEBSITE'/></core:website>

<core:description><xsl:value-of select='$mc/dm:DESC'/></core:description>

<core:month>
<xsl:value-of select='$mc/dm:DTM_DATE'/>
</core:month>

<core:day>
<xsl:value-of select='$mc/dm:DTD_DATE'/>
</core:day>

<core:year>
<xsl:value-of select='$mc/dm:DTY_DATE'/>
</core:year>


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
