<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:aiic="http://vivoweb.org/activity-insight"
	xmlns:acti="http://vivoweb.org/activity-insight#"
        xmlns="http://vivoweb.org/activity-insight#"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"
	xmlns:vfx='http://vivoweb.org/ext/functions'	
	exclude-result-prefixes='vfx xs'
	>

<xsl:param name='abypFile'  required='yes'/>
<xsl:param name='unoMapFile'  required='yes'/>
<xsl:param name='aiicXmlPath' required='yes'/>
<xsl:param name='aiicPrefix' required='yes'/>
<xsl:param name='extPerIn' required='yes'/>
<xsl:param name='extPerOut' required='yes'/>
<xsl:output method='xml' indent='yes'/>

<xsl:strip-space elements="*"/>


<xsl:include href='commonvars.xsl' />

<xsl:variable name='alist' 
  select="document($abypFile)//aiic:INTELLCONT_INFO"/>

<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>

<xsl:variable name='extantPersons'
	select="document($extPerIn)/ExtantPersons"/>

<!-- ================================== -->
<xsl:template match='/aiic:INTELLCONT_AUTHOR_LIST'>
<rdf:RDF>

<xsl:call-template name='mkIntellconts'/>

<xsl:for-each select='aiic:INTELLCONT_ITEMS_BY_AUTHOR'>

<!-- create a foaf:person for this author  OR use one from VIVO-Cornell -->


<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<!-- xsl:variable name='foafuri' select="concat($g_instance,$uno)"/ -->

<!-- =================================================== -->
<!-- Declare a foaf:Person (use extant person if foaf exists) -->
<xsl:variable name='knownUri' select='vfx:knownUri(aiic:FirstName, aiic:MiddleName, aiic:LastName, $extantPersons)'/>

<xsl:variable name='foafuri' select="if($knownUri != '') then $knownUri else concat($g_instance,$uno)"/>

<xsl:if test='$knownUri = ""'>

<rdf:Description rdf:about="{$foafuri}">
<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource='http://xmlns.com/foaf/0.1/Person'/>

<rdfs:label>
<xsl:call-template name='trim'>
<xsl:with-param name='str' select='aiic:AUTHOR_NAME'/>
</xsl:call-template>
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
<xsl:with-param name='abya' select='aiic:INTELLCONT_LIST'/>
<xsl:with-param name='foafref' select="$foafuri"/>
</xsl:call-template>

</xsl:for-each>
<!-- =================================================== 
 at this point we re-run part of the last for loop to get a new list of persons 
 and their uri's to save in the extant Persons Out xml file
-->
<xsl:result-document href='{$extPerOut}'>
<xsl:element name='ExtantPersons'>
<xsl:for-each select='aiic:INTELLCONT_ITEMS_BY_AUTHOR'>
<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<xsl:variable name='knownUri' select='vfx:knownUri(aiic:FirstName, aiic:MiddleName, aiic:LastName, $extantPersons)'/>

<xsl:variable name='foafuri' select="if($knownUri != '') then $knownUri else concat($g_instance,$uno)"/>
<!-- must prevent duplicates -->
<xsl:if test="$knownUri = ''">
<xsl:element name='person' inherit-namespaces='no'>
<xsl:element name='uri' inherit-namespaces='no'>
<xsl:value-of select='$foafuri'/></xsl:element>
<xsl:element name='fname' inherit-namespaces='no'>
<xsl:value-of select='aiic:FirstName'/></xsl:element>
<xsl:element name='mname' inherit-namespaces='no'>
<xsl:value-of select='aiic:MiddleName'/></xsl:element>
<xsl:element name='lname' inherit-namespaces='no'>
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

<xsl:for-each select='$abya/aiic:INTELLCONT_INFO'>
<xsl:variable name='aiid' select='.'/>
<xsl:variable name='rank' select='@authorRank'/>

<!-- =================================================== -->
<!-- Declare property mapping intellcont instance to core:Authorship -->

<rdf:Description rdf:about="{concat($g_instance,$aiid)}" >
<core:informationResourceInAuthorship 
	rdf:resource="{concat($g_instance,$aiid,'-',$rank)}"/>
</rdf:Description>

<!-- =================================================== -->
<!-- Declare core:Authorship Individual Triples-->

<rdf:Description rdf:about="{concat($g_instance,$aiid,'-',$rank)}">

<rdfs:label>
<xsl:call-template name='trim'>
<xsl:with-param name='str' select='../../aiic:AUTHOR_NAME'/>
</xsl:call-template>
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
<xsl:template match='aiic:INTELLCONT_LIST'/>

<xsl:template match='aiic:ALT_SRC_INTELLCONT_INFO'/>

<xsl:template match='aiic:INTELLCONT_INFO'/>


<!-- =================================================== -->
<!-- =================================================== -->
<xsl:template name='mkIntellconts'>

<xsl:for-each select='$alist'>

<xsl:variable name='aiid' select='.'/>
<xsl:variable name='rawaiid' select='substring($aiid,$pfxlen)'/>
<xsl:variable name='rid' select='./@ref_netid'/>

<xsl:variable name='path' select="concat($aiicXmlPath,'/',$aiicPrefix, $rid, '.xml')"/>
<xsl:variable name='icpath' select="document($path)//dm:INTELLCONT[@id=$rawaiid]"/>

<xsl:call-template name='mkIntellcontItem'>
<xsl:with-param name='icp' select="$icpath"/>
<xsl:with-param name='aiid' select='$aiid'/>
<xsl:with-param name='rid' select='$rid'/>
</xsl:call-template>

</xsl:for-each>
</xsl:template>

<!-- ================================== -->
<xsl:template name='mkIntellcontItem'>
<xsl:param name='icp'/>
<xsl:param name='aiid'/>
<xsl:param name='rid'/>
<xsl:variable name='ilk' select='vfx:classify($icp)'/>
<rdf:Description rdf:about="{concat($g_instance,$aiid)}" >
<rdf:type rdf:resource='{$ilk}'/>
<rdfs:label>
<xsl:value-of select="$icp/dm:TITLE"/>
</rdfs:label>
<xsl:call-template name='pages'>
<xsl:with-param name='pgnoinfo' select="$icp/dm:PAGENUM"/>
</xsl:call-template>
<core:year><xsl:value-of select="$icp/dm:DTY_PUB"/></core:year>
<bibo:volume><xsl:value-of select="$icp/dm:VOLUME"/></bibo:volume>
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
<xsl:template name='trim'>
<xsl:param name='str'/>
<xsl:choose>
<xsl:when test='$str != ""'>
<xsl:analyze-string select='$str' regex='^\s*(.+?)\s*$'>
<xsl:matching-substring>
<xsl:value-of select="regex-group(1)"/>
</xsl:matching-substring>
</xsl:analyze-string>
</xsl:when>
<xsl:otherwise>
<xsl:text></xsl:text>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<!-- ============================================================= -->

<xsl:function name='vfx:classify' as='xs:string'>
<xsl:param name='n'/>

<xsl:choose>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "ABSTRACT"'>
<xsl:value-of select='"http://purl.org/ontology/bibo/DocumentPart"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOKCHAPTER"'>
<xsl:value-of select='"http://purl.org/ontology/BookChapter"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOKREVIEW"'>
<xsl:value-of select='"http://vivoweb.org/ontology/core#Review"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOKSECTION"'>
<xsl:value-of select='"http://purl.org/ontology/BookSection"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOKSCHOLARLY"'>
<xsl:value-of select='"http://purl.org/ontology/bibo/Book"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "BOOKTEXTBOOK"'>
<xsl:value-of select='"http://purl.org/ontology/bibo/Book"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "CITEDRESEARCH"'>
<xsl:value-of select='"http://vivoweb.org/ontology/core#InformationResource"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "CONFERENCEPROCEEDING"'>
<xsl:value-of select='"http://vivoweb.org/ontology/core#ConferencePaper"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "INSTRUCTORSMANUAL"'>
<xsl:value-of select='"http://purl.org/ontology/bibo/Manual"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "INTERNET"'>
<xsl:value-of select='"http://purl.org/ontology/bibo/WebSite"'/>
</xsl:when>
<xsl:when test='starts-with(vfx:collapse( $n/dm:CONTYPE ), "MAGAZINE")'>
<xsl:value-of select='"http://purl.org/ontology/bibo/Article"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "MONOGRAPH"'>
<xsl:value-of select='"http://purl.org/ontology/bibo/Book"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "NEWSLETTER"'>
<xsl:value-of select='"http://purl.org/ontology/bibo/Article"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "NEWSPAPER"'>
<xsl:value-of select='"http://purl.org/ontology/bibo/Article"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "OTHER"'>
<xsl:value-of select='"core:InformationResource"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "POLICYREPORT"'>
<xsl:value-of select='"http://purl.org/ontology/bibo/Report"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "POSTER"'>
<xsl:value-of select='"http://vivoweb.org/ontology/core#ConferencePoster"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "RADIO"'>
<xsl:value-of select='"http://purl.org/ontology/bibo/AudioDocument"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "RESEARCHREPORT"'>
<xsl:value-of select='"http://purl.org/ontology/bibo/Report"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "SOFTWARE"'>
<xsl:value-of select='"http://vivoweb.org/ontology/core#Software"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TV"'>
<xsl:value-of select='"http://purl.org/ontology/bibo/AudioVisualDocument"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TECHNICALREPORT"'>
<xsl:value-of select='"http://purl.org/ontology/bibo/Report"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TRADEPUBLICATION"'>
<xsl:value-of select='"http://purl.org/ontology/bibo/Article"'/>
</xsl:when>
<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TRAININGMANUAL"'>
<xsl:value-of select='"http://purl.org/ontology/bibo/Manual"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "TRANSLATION"'>
<xsl:value-of select='"http://vivoweb.org/ontology/core#Translation"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "WRITTENCASE"'>
<xsl:value-of select='"http://vivoweb.org/ontology/core#CaseStudy"'/>
</xsl:when>

<xsl:when test='vfx:collapse( $n/dm:CONTYPE )  = "eCORNELLCOURSE"'>
<xsl:value-of select='"http://purl.org/ontology/bibo/AudioVisualDocument"'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='"http://vivoweb.org/activity-insight#Intellcont"'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>


<xsl:include href='vivofuncs.xsl'/>

<xsl:template name='saveNewPeople'>
<xsl:param name='file'/>
<xsl:param name='newpeople'/>
<xsl:result-document href='{$file}'>
<xsl:element name='ExtantPersons'>
<xsl:for-each select='$newpeople'>
<xsl:copy-of select='.'/><xsl:value-of select='$NL'/>
</xsl:for-each>
</xsl:element>
</xsl:result-document>
</xsl:template>
</xsl:stylesheet>
