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


<xsl:template match='/aigrant:GRANT_COMMENT_LIST'>
<rdf:RDF>
<xsl:for-each select='aigrant:GRANT_COMMENT'>
<!-- grant effort processing -->
<xsl:if test='@skip = "N"'>
<xsl:variable name='grid' select='aigrant:GRANTS_LIST/aigrant:GRANT_INFO/@grid'/>
<xsl:variable name='netid' 
select='aigrant:GRANTS_LIST/aigrant:GRANT_INFO/@ref-netid'/>
<rdf:Description 
rdf:about="{concat($g_instance,'AI-GCOM-',$grid)}" >
<rdf:type rdf:resource='http://vivoweb.org/activity-insight#GrantComment'/>
<rdfs:label>
<xsl:value-of select="aigrant:COMMENT"/>
</rdfs:label>
<xsl:if test='$netid != ""'>
<xsl:variable name='nidxml' select="concat($rawXmlPath,'/',$netid, '.xml')"/>
<!-- xsl:comment><xsl:value-of select='$nidxml'/></xsl:comment-->
<xsl:variable name='gr' select='document($nidxml)//dm:CONGRANT[@id = $grid]' />
<acti:grantCommentMonth>
<xsl:value-of select='$gr/dm:DTM_DATE'/></acti:grantCommentMonth>
<acti:grantCommentDay>
<xsl:value-of select='$gr/dm:DTD_DATE'/></acti:grantCommentDay>
<acti:grantCommentYear>
<xsl:value-of select='$gr/dm:DTY_DATE'/></acti:grantCommentYear>
<acti:grantCommentStartDate>
<xsl:value-of select='$gr/dm:DATE_START'/></acti:grantCommentStartDate>
<acti:grantCommentEndDate>
<xsl:value-of select='$gr/dm:DATE_START'/></acti:grantCommentEndDate>
</xsl:if>
</rdf:Description>
<!-- Declare property mapping acti:GrantComment to  core:Grant-->
<!-- 6-->
<rdf:Description 
rdf:about="{concat($g_instance,'AI-GCOM-',$grid)}" >
<acti:commentsOn
	rdf:resource="{concat('AI-',$grid)}"/>
</rdf:Description>
<!-- =================================================== -->
<!-- Declare property from  core:Grant to acti:CommentOnGrant  -->
<!-- 5 -->
<rdf:Description rdf:about="{concat('AI-',$grid)}">
<acti:hasComment
rdf:resource="{concat($g_instance,'AI-GCOM-',$grid)}"/>
</rdf:Description>
</xsl:if>
</xsl:for-each>

</rdf:RDF>
<xsl:value-of select='$NL'/>
</xsl:template>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
