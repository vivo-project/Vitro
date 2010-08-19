<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aigrant="http://vivoweb.org/activity-insight"
	xmlns:acti="http://vivoweb.org/activity-insight#"
        xmlns="http://vivoweb.org/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"	
	xmlns:vfx='http://vivoweb.org/ext/functions'
	exclude-result-prefixes='xs vfx'
	>

<xsl:param name='unoMapFile'  required='yes'/>
<xsl:param name='rawXmlPath' required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>

<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>


<xsl:template match='/aigrant:GRANT_COST_LIST'>
<rdf:RDF>
<xsl:for-each select='aigrant:GRANT_COST'>
<!-- grant effort processing -->

<xsl:variable name='grid' select='aigrant:GRANTS_LIST/aigrant:GRANT_INFO/@grid'/>
<xsl:variable name='netid' select='aigrant:GRANTS_LIST/aigrant:GRANT_INFO/@ref-netid'/>
<rdf:Description 
rdf:about="{concat($g_instance,'AI-GC',$grid)}" >
<rdf:type rdf:resource='http://vivoweb.org/activity-insight#GrantCostForYear'/>
<rdfs:label>
<xsl:value-of select="@year"/>
</rdfs:label>
<xsl:if test='$netid != ""'>
<xsl:variable name='nidxml' select="concat($rawXmlPath,'/',$netid, '.xml')"/>
<!-- xsl:comment><xsl:value-of select='$nidxml'/></xsl:comment-->
<xsl:variable name='gr' select='document($nidxml)//dm:CONGRANT[@id = $grid]' />

<acti:directCost><xsl:value-of select='$gr/dm:DIRECT_COST'/></acti:directCost>
<acti:indirectCost><xsl:value-of select='$gr/dm:INDIRECT_COST'/></acti:indirectCost>
<acti:comment><xsl:value-of select='$gr/dm:COMMENT'/></acti:comment>
<acti:calendarStart><xsl:value-of select='$gr/dm:CALENDAR_START'/></acti:calendarStart>
<acti:calendarEnd><xsl:value-of select='$gr/dm:CALENDAR_END'/></acti:calendarEnd>
</xsl:if>
</rdf:Description>
<!-- Declare property mapping acti:GrantCostForYear to  core:Grant-->
<!-- 4 -->
<rdf:Description 
rdf:about="{concat($g_instance,'AI-GC',$grid)}" >
<acti:yearlyCostFor
	rdf:resource="{concat($g_instance,'AI-',$grid)}"/>
</rdf:Description>
<!-- =================================================== -->
<!-- Declare property from  core:Grant to acti:GrantCostForYear  -->
<!-- 3 -->
<rdf:Description rdf:about="{concat($g_instance,'AI-',$grid)}">
<acti:hasYearlyCost
rdf:resource="{concat($g_instance,'AI-GC',$grid)}"/>
</rdf:Description>
</xsl:for-each>

</rdf:RDF>
<xsl:value-of select='$NL'/>
</xsl:template>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
