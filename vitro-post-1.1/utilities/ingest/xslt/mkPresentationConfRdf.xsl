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

<xsl:template match='/aipres:PRESENT_CONF_LIST'>
<rdf:RDF>
<xsl:value-of select='$NL'/>
<xsl:comment>
<xsl:value-of select='count(aipres:PRESENT_CONF)'/>
</xsl:comment>
<xsl:value-of select='$NL'/>

<xsl:for-each select='aipres:PRESENT_CONF'>
<xsl:if test='./aipres:PRESENT_CONF_NAME != "Unspecified"'>

<xsl:for-each select='aipres:PRESENT_LIST/aipres:PRESENT_INFO'>
<xsl:comment>
<xsl:value-of select='count(../aipres:PRESENT_INFO)'/>
</xsl:comment>
<xsl:value-of select='$NL'/>
<xsl:variable name='pid' select='.'/>

<xsl:variable name='hasAuthor'  select='$pid/@hasGoodAuthor'/>
<xsl:variable name='hasTitle'  select='$pid/@hasTitle'/>
<xsl:variable name='hasOrg'  select='$pid/@hasOrg'/>
<xsl:variable name='public'  select='$pid/@public'/>
<xsl:variable name='nid' select='$pid/@ref_netid'/>
<xsl:comment>
<xsl:value-of select='concat($hasTitle,"|",$hasAuthor,"|",$nid,"|",$public)'/>
</xsl:comment>
<xsl:value-of select='$NL'/>
<xsl:if test='$hasAuthor = "Yes" and
              $nid != "" and $public = "Yes"'>

<xsl:variable name='nidxml' 
select="concat($rawXmlPath,'/',$nid, '.xml')"/>
<xsl:variable name='ref' 
select='document($nidxml)//dm:PRESENT[@id = $pid]' />

<xsl:variable name='pt' select='$ref/dm:PRESTYPE'/>

<xsl:if test='$pt != "Other"'>

<rdf:Description rdf:about="{concat($g_instance,'AI-CONF-',$pid)}" >


<rdf:type rdf:resource='vfx:conferenceClass($pt)'/>

<rdf:type rdf:resource=
	'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>

<acti:locationAsListed>
<xsl:value-of select="$ref/dm:LOCATION"/>
</acti:locationAsListed>

<rdfs:label>
<xsl:value-of select="$ref/dm:NAME"/>
</rdfs:label>


</rdf:Description>


<xsl:if test='$hasTitle = "Yes"'>
<rdf:Description rdf:about="{concat($g_instance,'AI-',$pid)}" >
<core:eventWithin rdf:resource="{concat($g_instance,'AI-CONF-',$pid)}" />
</rdf:Description>
</xsl:if>

</xsl:if> <!-- $pt != "Other" -->

</xsl:if> <!-- $hasConf = "Yes" and $hasAuthor and ... -->

</xsl:for-each> <!-- aipres:PRESENT_LIST/aipres:PRESENT_INFO -->

</xsl:if> <!-- ./aipres:PRESENT_CONF_NAME != "Unspecified" -->

</xsl:for-each> <!-- aipres:PRESENT_CONF -->
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

<xsl:function name='vfx:conferenceClass'>
<xsl:param name='ptype'/>

<xsl:choose>
  <xsl:when test='starts-with($ptype, "Conference")'>
	<xsl:value-of select='"http://purl.org/ontology/bibo/Conference"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Field Day")'>
	<xsl:value-of select='"http://purl.org/NET/c4dm/event.owl#Event"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Guest")'>
	<xsl:value-of select='"http://purl.org/ontology/bibo/Conference"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Invited")'>
	<xsl:value-of select='"http://purl.org/ontology/bibo/Conference"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Keynote")'>
	<xsl:value-of select='"http://purl.org/ontology/bibo/Conference"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Panel")'>
	<xsl:value-of select='"http://purl.org/NET/c4dm/event.owl#Event"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Pres")'>
	<xsl:value-of select='"http://purl.org/NET/c4dm/event.owl#Event"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Seminar")'>
	<xsl:value-of select='"http://purl.org/NET/c4dm/event.owl#Event"'/>
  </xsl:when>  
  <xsl:when test='starts-with($ptype, "Short ")'>
	<xsl:value-of select='"http://purl.org/NET/c4dm/event.owl#Event"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Symposium")'>
	<xsl:value-of select='"http://purl.org/ontology/bibo/Conference"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Tour Pres")'>
	<xsl:value-of select='"http://purl.org/NET/c4dm/event.owl#Event"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "Workshop")'>
	<xsl:value-of select='"http://purl.org/ontology/bibo/Workshop"'/>
  </xsl:when>
  <xsl:when test='starts-with($ptype, "")'>
	<xsl:value-of select='"http://purl.org/NET/c4dm/event.owl#Event"'/>
  </xsl:when>

  <xsl:otherwise>
	<xsl:value-of select='"http://purl.org/NET/c4dm/event.owl#Event"'/>
  </xsl:otherwise>
</xsl:choose>

</xsl:function>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
