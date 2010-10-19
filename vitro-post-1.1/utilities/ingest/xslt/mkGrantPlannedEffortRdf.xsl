<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aigrant="http://vivoweb.org/ontology/activity-insight"
	xmlns:acti="http://vivoweb.org/ontology/activity-insight#"
        xmlns="http://vivoweb.org/ontology/activity-insight"
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


<xsl:template match='/aigrant:GRANT_EFFORT_LIST'>
<rdf:RDF>
<xsl:for-each select='aigrant:GRANT_EFFORT'>
<!-- grant effort processing -->
<xsl:variable name='type' select='if(@type != "") then @type else "NA"'/>
<xsl:variable name='grid' select='aigrant:GRANTS_LIST/aigrant:GRANT_INFO/@grid'/>
<rdf:Description 
rdf:about="{concat($g_instance,'AI-GE',$grid,'-',$type)}" >
<rdf:type rdf:resource='http://vivoweb.org/ontology/activity-insight#PlannedEffort'/>
<rdfs:label>
<xsl:value-of select="$type"/>
</rdfs:label>

</rdf:Description>
<!-- Declare property mapping acti:PlannedEffort to  core:Grant-->
<!-- 8 -->
<rdf:Description 
rdf:about="{concat($g_instance,'AI-GE',$grid,'-',$type)}" >
<acti:plannedEffortFor
	rdf:resource="{concat($g_instance,'AI-',$grid)}"/>
</rdf:Description>
<!-- =================================================== -->
<!-- Declare property from  core:Grant to acti:PlannedEffort  -->
<!-- 7 -->
<rdf:Description rdf:about="{concat($g_instance,'AI-',$grid)}">
<acti:hasPlannedEffort
rdf:resource="{concat($g_instance,'AI-GE',$grid,'-',$type)}"/>
</rdf:Description>
</xsl:for-each>

</rdf:RDF>
<xsl:value-of select='$NL'/>
</xsl:template>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
