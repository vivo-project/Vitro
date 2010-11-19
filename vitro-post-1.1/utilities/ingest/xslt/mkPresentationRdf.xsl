<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
	xmlns:event="http://purl.org/NET/c4dm/event.owl#"
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
<xsl:value-of select='$NL'/>
<xsl:comment>
<xsl:value-of select='count(aipres:PRESENT)'/>
</xsl:comment>
<xsl:value-of select='$NL'/>

<xsl:for-each select='aipres:PRESENT'>
<xsl:variable name='pid' select='aipres:PRESENT_ID'/>

<xsl:variable name='hasAuthor'  select='$pid/@hasGoodAuthor'/>
<xsl:variable name='hasTitle'  select='$pid/@hasTitle'/>
<xsl:variable name='hasOrg'  select='$pid/@hasOrg'/>
<xsl:variable name='hasConf'  select='$pid/@hasConf'/>
<xsl:variable name='public'  select='$pid/@public'/>
<xsl:variable name='nid' select='aipres:netid'/>

<xsl:if test='$hasTitle = "Yes" and $hasAuthor = "Yes" and
              $nid != "" and 
              $public = "Yes"'>
<xsl:variable name='nidxml' 
select="concat($rawXmlPath,'/',$nid, '.xml')"/>
<xsl:variable name='ref' 
select='document($nidxml)//dm:PRESENT[@id = $pid]' />

<rdf:Description rdf:about="{concat($g_instance,'AI-',$pid)}" >

<xsl:variable name='pt' select='vfx:presentationType($ref/dm:PRESTYPE)'/>

<rdf:type rdf:resource='{if($pt != "Other" and $pt != "") then
	vfx:presentationClass($pt) else
	"http://vivoweb.org/ontology/core#Presentation"}'/>

<rdf:type rdf:resource=
	'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>

<rdfs:label>
<xsl:value-of select="$ref/dm:TITLE"/>
</rdfs:label>

<xsl:choose>
  <xsl:when test='$pt != "Other"'>
	<core:description>
	<xsl:value-of select='normalize-space($ref/dm:PRESTYPE_OTHER)'/>
	</core:description>
  </xsl:when>
  <xsl:otherwise>
	<acti:presentationType>
	<xsl:value-of select='vfx:presentationType($pt)'/>
	</acti:presentationType>
  </xsl:otherwise>
</xsl:choose>

<core:month>
<xsl:value-of select='$ref/dm:DTM_DATE'/>
</core:month>

<core:day>
<xsl:value-of select='$ref/dm:DTD_DATE'/>
</core:day>

<core:year>
<xsl:value-of select='$ref/dm:DTY_DATE'/>
</core:year>


</rdf:Description>
</xsl:if>

</xsl:for-each>

</rdf:RDF>
<xsl:value-of select='$NL'/>
</xsl:template>

<xsl:function name='vfx:presentationType'>
<xsl:param name='ptype'/>

<xsl:choose>
  <xsl:when test='starts-with($ptype, "Conference")'>
	<xsl:value-of select='"Conference Presentation"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Field Day")'>
	<xsl:value-of select='"Field Day Presentation"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Guest")'>
	<xsl:value-of select='"Guest Presentation"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Invited")'>
	<xsl:value-of select='"Invited presentation"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Keynote")'>
	<xsl:value-of select='"Keynote Address"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Panel")'>
	<xsl:value-of select='"Panel"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Pres")'>
	<xsl:value-of select='"Presentation"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Seminar")'>
	<xsl:value-of select='"Seminar Presentation"'/>
  </xsl:when>  
  <xsl:when test='starts-with($ptype, "Short ")'>
	<xsl:value-of select='"Short Course Presentation"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Symposium")'>
	<xsl:value-of select='"Symposium Presentation"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Tour Pres")'>
	<xsl:value-of select='"Tour Presentation"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Workshop")'>
	<xsl:value-of select='"Workshop Presentation"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Other")'>
	<xsl:value-of select='""'/>
  </xsl:when>

  <xsl:otherwise>
	<xsl:value-of select='""'/>
  </xsl:otherwise>
</xsl:choose>


</xsl:function>

<xsl:function name='vfx:presentationClass'>
<xsl:param name='ptype'/>

<xsl:choose>
  <xsl:when test='starts-with($ptype, "Conference")'>
	<xsl:value-of select='"http://vivoweb.org/ontology/core#Presentation"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Field Day")'>
	<xsl:value-of select='"http://vivoweb.org/ontology/core#Presentation"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Guest")'>
	<xsl:value-of select='"http://vivoweb.org/ontology/core#InvitedTalk"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Invited")'>
	<xsl:value-of select='"http://vivoweb.org/ontology/core#InvitedTalk"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Keynote")'>
	<xsl:value-of select='"http://vivoweb.org/ontology/core#InvitedTalk"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Panel")'>
	<xsl:value-of select='"http://vivoweb.org/ontology/core#Presentation"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Pres")'>
	<xsl:value-of select='"http://vivoweb.org/ontology/core#Presentation"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Seminar")'>
	<xsl:value-of select='"http://vivoweb.org/ontology/core#Presentation"'/>
  </xsl:when>  
  <xsl:when test='starts-with($ptype, "Short ")'>
	<xsl:value-of select='"http://vivoweb.org/ontology/core#Presentation"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Symposium")'>
	<xsl:value-of select='"http://vivoweb.org/ontology/core#Presentation"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Tour Pres")'>
	<xsl:value-of select='"http://vivoweb.org/ontology/core#Presentation"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Workshop")'>
	<xsl:value-of select='"http://vivoweb.org/ontology/core#Presentation"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "")'>
	<xsl:value-of select='""'/>
  </xsl:when>

  <xsl:otherwise>
	<xsl:value-of select='""'/>
  </xsl:otherwise>
</xsl:choose>


</xsl:function>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
