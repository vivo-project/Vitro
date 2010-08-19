<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:acti="http://vivoweb.org/activity-insight#"	
	xmlns:aiic="http://vivoweb.org/activity-insight"
        xmlns="http://vivoweb.org/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"
	xmlns:vfx='http://vivoweb.org/ext/functions'
	exclude-result-prefixes='xs vfx'
	>

<xsl:param name='abyjFile'  required='yes'/>
<xsl:param name='unoMapFile'  required='yes'/>
<xsl:param name='aiicXmlPath' required='yes'/>
<xsl:param name='aiicPrefix' required='yes'/>
<xsl:param name='extPerIn' required='yes'/>
<xsl:param name='extPerOut' required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>

<xsl:variable name='alist' 
  select="document($abyjFile)//aiic:ARTICLE_INFO"/>

<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>

<xsl:variable name='extantPersons'
	select="document($extPerIn)/ExtantPersons"/>
<!-- ================================== -->
<xsl:template match='/aiic:AUTHOR_LIST'>
<rdf:RDF>

<xsl:call-template name='mkArticles'/>

<xsl:for-each select='aiic:ARTICLES_BY_AUTHOR'>

<!-- create a foaf:person for this author  OR use one from VIVO-Cornell -->


<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>


<!-- =================================================== -->
<!-- Declare a foaf:Person (use extant person if foaf exists) -->
<xsl:variable name='knownUri' select='vfx:knownUri(aiic:FirstName, aiic:MiddleName, aiic:LastName, $extantPersons)'/>

<xsl:variable name='foafuri' select="if($knownUri != '') then $knownUri else concat($g_instance,$uno)"/>

<xsl:if test='$knownUri = ""'>
<rdf:Description rdf:about="{$foafuri}">
<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource='http://xmlns.com/foaf/0.1/Person'/>

<rdfs:label>
<xsl:value-of select='vfx:trim(aiic:AUTHOR_NAME)'/>
<!-- xsl:call-template name='trim'>
<xsl:with-param name='str' select='aiic:AUTHOR_NAME'/>
</xsl:call-template -->
</rdfs:label>

<core:middleName><xsl:value-of select='aiic:MiddleName'/></core:middleName>
<core:firstName><xsl:value-of select='aiic:FirstName'/></core:firstName>
<foaf:firstName><xsl:value-of select='aiic:FirstName'/></foaf:firstName>
<core:lastName><xsl:value-of select='aiic:LastName'/></core:lastName>
<foaf:lastName><xsl:value-of select='aiic:LastName'/></foaf:lastName>

<xsl:if test='aiic:NetId != ""'>
<!-- xsl:variable name='nidxml' select="concat('../AIIC_XMLs/AIIC_',aiic:NetId , '.xml')"/ -->
<xsl:variable name='nidxml' select="concat($aiicXmlPath,'/',$aiicPrefix,aiic:NetId , '.xml')"/>
<xsl:variable name='pci' select="document($nidxml)//dm:PCI"/>
<core:workEmail><xsl:value-of select='$pci/dm:EMAIL'/></core:workEmail>
<bibo:prefixName><xsl:value-of select='$pci/dm:PREFIX'/> </bibo:prefixName>
<core:workFax>
<xsl:value-of select='$pci/dm:FAX1'/>-<xsl:value-of select='$pci/dm:FAX2'/>-<xsl:value-of select='$pci/dm:FAX3'/>
</core:workFax>
<core:workPhone>
<xsl:value-of select='$pci/dm:OPHONE1'/>-<xsl:value-of select='$pci/dm:OPHONE2'/>-<xsl:value-of select='$pci/dm:OPHONE3'/>
</core:workPhone>
</xsl:if>

</rdf:Description>
</xsl:if>

<!-- =================================================== -->
<!-- now process the articles attributed to this author -->

<xsl:call-template name='process-author'>
<xsl:with-param name='abya' select='aiic:ARTICLE_LIST'/>
<xsl:with-param name='foafref' select="$foafuri"/>
</xsl:call-template>

</xsl:for-each>

<!-- =================================================== 
 at this point we re-run part of the last for loop to get a new list of persons 
 and their uri's to save in the extant Persons Out xml file
-->
<xsl:result-document href='{$extPerOut}'>
<!-- xsl:element name='aiic:ExtantPersons' namespace='http://vivoweb.org/activity-insight' -->
<xsl:element name='ExtantPersons' namespace=''>
<xsl:for-each select='aiic:ARTICLES_BY_AUTHOR'>

<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<xsl:variable name='knownUri' select='vfx:knownUri(aiic:FirstName, aiic:MiddleName, aiic:LastName, $extantPersons)'/>

<xsl:variable name='foafuri' select="if($knownUri != '') then $knownUri else concat($g_instance,$uno)"/>
<!-- must prevent duplicates -->
<xsl:if test="$knownUri = ''">
<xsl:element name='person' namespace=''>
<xsl:element name='uri' namespace=''>
<xsl:value-of select='$foafuri'/></xsl:element>
<xsl:element name='fname' namespace=''>
<xsl:value-of select='aiic:FirstName'/></xsl:element>
<xsl:element name='mname' namespace=''>
<xsl:value-of select='aiic:MiddleName'/></xsl:element>
<xsl:element name='lname' namespace=''>
<xsl:value-of select='aiic:LastName'/></xsl:element>
</xsl:element>
</xsl:if>
</xsl:for-each>
</xsl:element>
</xsl:result-document>

</rdf:RDF>
</xsl:template>

<!-- =================================================== -->
<!-- =================================================== -->
<xsl:template name='process-author'>
<xsl:param name='abya'/>
<xsl:param name='foafref'/>

<xsl:for-each select='$abya/aiic:ARTICLE_INFO'>
<xsl:variable name='aiid' select='.'/>
<xsl:variable name='rank' select='@authorRank'/>

<!-- =================================================== -->
<!-- Declare property mapping bibo:AcademicArticle to core:Authorship -->

<rdf:Description rdf:about="{concat($g_instance,$aiid)}" >
<core:informationResourceInAuthorship 
	rdf:resource="{concat($g_instance,$aiid,'-',$rank)}"/>
</rdf:Description>

<!-- =================================================== -->
<!-- Declare core:Authorship Individual Triples-->

<rdf:Description rdf:about="{concat($g_instance,$aiid,'-',$rank)}">

<rdfs:label>
<xsl:value-of select='vfx:trim(../../aiic:AUTHOR_NAME)'/>
<!-- xsl:call-template name='trim'>
<xsl:with-param name='str' select='../../aiic:AUTHOR_NAME'/>
</xsl:call-template -->
</rdfs:label>

<rdf:type rdf:resource='http://vivoweb.org/ontology/core#Authorship'/>

<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>

<core:linkedAuthor rdf:resource='{$foafref}'/>

<core:authorRank rdf:datatype='http://www.w3.org/2001/XMLSchema#int'>
<xsl:value-of select='$rank'/>
</core:authorRank>

<core:linkedInformationResource 
	rdf:resource="{concat($g_instance,$aiid)}"/>

</rdf:Description>

<!-- =================================================== -->
<!-- Deal with public/private issue here -->
<xsl:if test='@public != "No"'>
<rdf:Description rdf:about="{$foafref}">
<core:authorInAuthorship rdf:resource="{concat($g_instance,$aiid,'-',$rank)}"/>
</rdf:Description>
</xsl:if>


</xsl:for-each>

</xsl:template>

<!-- ================================== -->
<xsl:template match='aiic:ARTICLE_LIST'/>

<xsl:template match='aiic:ALT_SRC_ARTICLE_INFO'/>

<xsl:template match='aiic:ARTICLE_INFO'/>


<!-- =================================================== -->
<!-- =================================================== -->
<xsl:template name='mkArticles'>

<xsl:for-each select='$alist'>

<xsl:variable name='aiid' select='.'/>
<xsl:variable name='rawaiid' select='substring($aiid,$pfxlen)'/>
<xsl:variable name='rid' select='./@ref_netid'/>
<!-- xsl:variable name='path' select="concat('../AIIC_XMLs/AIIC_', $rid, '.xml')"/ -->
<xsl:variable name='path' select="concat($aiicXmlPath,'/',$aiicPrefix, $rid, '.xml')"/>
<xsl:variable name='ijpath' select="document($path)//dm:INTELLCONT_JOURNAL[@id=$rawaiid]"/>

<xsl:call-template name='mkAcademicArticle'>
<xsl:with-param name='ijp' select="$ijpath"/>
<xsl:with-param name='aiid' select='$aiid'/>
<xsl:with-param name='rid' select='$rid'/>
</xsl:call-template>

</xsl:for-each>
</xsl:template>

<!-- ================================== -->
<xsl:template name='mkAcademicArticle'>
<xsl:param name='ijp'/>
<xsl:param name='aiid'/>
<xsl:param name='rid'/>

<rdf:Description rdf:about="{concat($g_instance,$aiid)}" >
<rdf:type rdf:resource='http://purl.org/ontology/bibo/AcademicArticle'/>
<rdfs:label>
<xsl:value-of select="$ijp/dm:TITLE"/>
</rdfs:label>
<xsl:call-template name='pages'>
<xsl:with-param name='pgnoinfo' select="$ijp/dm:PAGENUM"/>
</xsl:call-template>
<core:year><xsl:value-of select="$ijp/dm:DTY_PUB"/></core:year>
<bibo:volume><xsl:value-of select="$ijp/dm:VOLUME"/></bibo:volume>
</rdf:Description>

</xsl:template>

<!-- ================================== -->
<xsl:template name='pages'>
<xsl:param name='pgnoinfo'/>
<xsl:choose>
<xsl:when test='$pgnoinfo != ""'>
<xsl:analyze-string select='$pgnoinfo' regex='^\s*(\d+)\s*(-|,)?\s*(\d*)\s*$'>
<xsl:matching-substring>
<bibo:pageStart rdf:datatype='http://www.w3.org/2001/XMLSchema#int'>
<xsl:value-of select="regex-group(1)"/>
</bibo:pageStart>
<xsl:if test="regex-group(3)">
<bibo:pageEnd rdf:datatype='http://www.w3.org/2001/XMLSchema#int'>
<xsl:value-of select="regex-group(3)"/>
</bibo:pageEnd>
</xsl:if>
</xsl:matching-substring>
</xsl:analyze-string>
</xsl:when>
<xsl:otherwise>
<xsl:text></xsl:text>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
