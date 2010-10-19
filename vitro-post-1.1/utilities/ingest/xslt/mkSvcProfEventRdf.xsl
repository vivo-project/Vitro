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

<xsl:template match='/aisvcprof:SERVICE_PROFESSIONAL_EVENT_LIST'>
<rdf:RDF>
<xsl:for-each select='aisvcprof:SERVICE_PROFESSIONAL_EVENT'>
<xsl:variable name='ctr'  select='@index'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<xsl:variable name='nidxml' 
select="concat($rawXmlPath,'/', @ref_netid, '.xml')"/>
<xsl:if test='@public="Yes"'>
<rdf:Description rdf:about="{concat($g_instance,$uno)}" >
<rdf:type 
rdf:resource='http://vivoweb.org/ontology/core#Event'/>
<rdf:type 
rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>

<rdfs:label>
<xsl:value-of select="aisvcprof:SERVICE_PROFESSIONAL_EVENT_NAME"/>
</rdfs:label>
<core:addressCity><xsl:value-of select='aisvcprof:city'/></core:addressCity>
<core:addressState><xsl:value-of select='aisvcprof:state'/></core:addressState>
<core:addressCountry><xsl:value-of select='aisvcprof:country'/></core:addressCountry>
</rdf:Description>

<xsl:for-each select='./aisvcprof:SERVICE_PROFESSIONAL_LIST/aisvcprof:SERVICE_PROFESSIONAL_INFO'>
<xsl:variable name='objid' select='@id'/>
<xsl:variable name='ref' select='document($nidxml)//dm:SERVICE_PROFESSIONAL[@id = $objid]' />

<rdf:Description rdf:about="{concat($g_instance,$uno)}" >

<core:relatedRole
rdf:resource="{concat($g_instance,'AI-',$objid)}"/>

</rdf:Description>

<rdf:Description rdf:about="{concat($g_instance,'AI-',$objid)}" >
<core:rollIn rdf:resource="{concat($g_instance,$uno)}"/>
</rdf:Description>


</xsl:for-each>
<xsl:value-of select='$NL'/>

<xsl:value-of select='$NL'/>
</xsl:if>
</xsl:for-each>
</rdf:RDF>
<xsl:value-of select='$NL'/>
</xsl:template>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
