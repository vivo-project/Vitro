<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:dm="http://www.digitalmeasures.com/schema/data"
xmlns:dmd="http://www.digitalmeasures.com/schema/data-metadata"
xmlns:ai="http://www.digitalmeasures.com/schema/data"
xmlns:xs='http://www.w3.org/2001/XMLSchema'
xmlns:vfx='http://vivoweb.org/ext/functions'	
exclude-result-prefixes='vfx xs'
 version="2.0">   


<xsl:import href='matchvars.xsl'/>

<xsl:output method="xml" indent="yes"/> 

<xsl:variable name='MyNL'>
<xsl:text>
</xsl:text>
</xsl:variable>

<xsl:function name='vfx:isomorphic' as='xs:boolean'>
<xsl:param name='s1'/>
<xsl:param name='s2'/>
<xsl:variable name='cs1' select='vfx:collapse($s1)'/>
<xsl:variable name='cs2' select='vfx:collapse($s2)'/>
<xsl:value-of select='$cs1 = $cs2'/>
</xsl:function>

<!-- ============================================= -->
<!-- this extension function  
	1. removes whitespace and special chrs from the argument string
	2. shifts alphabetic characters to upper case
	3. returns the adjusted string for comparison
-->
<xsl:function name='vfx:collapseOld' as='xs:string'>
<xsl:param name='s1'/>
<xsl:variable name='res' select='replace($s1, "\s", "")'/>
<xsl:variable name='res1' select="replace($res,'''','')"/>
<xsl:value-of select='upper-case(replace($res1,"[\-.,;""]",""))'/>

</xsl:function>
<!-- ================================== -->

<xsl:function name='vfx:collapse' as='xs:string'>
<xsl:param name='s'/>
<xsl:variable name='res0' select='replace(upper-case($s),"[\-.,;""]","")'/>
<xsl:variable name='res1' select="replace($res0,'''','')"/>
<xsl:value-of select='normalize-space($res1)'/>
</xsl:function>

<!-- ================================== -->
<xsl:function name='vfx:collectByName' as='xs:string'>
<xsl:param name='ln'/>
<xsl:param name='fn'/>
<xsl:param name='mn'/>
<xsl:value-of select='vfx:collapse(concat(normalize-space($ln),"|",
                                          normalize-space($fn),"|",
                                          normalize-space($mn)))'/>
</xsl:function>
<!-- ================================== -->
<xsl:function name='vfx:collectByCe' as='xs:string'>
<xsl:param name='ce'/>
<xsl:param name='sc'/>
<xsl:value-of select='vfx:collapse(concat(normalize-space($ce),"|",
                                          normalize-space($sc)))'/>
</xsl:function>

<!-- ============================================= -->
<!-- this extension function 
removes leading and trailing whitespace from the argument string
-->
<xsl:function name='vfx:trim' as='xs:string'>
<xsl:param name='s1'/>
<xsl:choose>
<xsl:when test='$s1 != ""'>
<xsl:value-of select='normalize-space($s1)'/>
</xsl:when>
<xsl:otherwise>
<xsl:text>Unspecified</xsl:text>
</xsl:otherwise>
</xsl:choose>
</xsl:function>
<!-- ================================== -->
<xsl:function name='vfx:simple-trim' as='xs:string'>
<xsl:param name='s1'/>
<xsl:choose>
<xsl:when test='$s1 != ""'>
<xsl:value-of select='normalize-space($s1)'/>
</xsl:when>
<xsl:otherwise>
<xsl:text></xsl:text>
</xsl:otherwise>
</xsl:choose>
</xsl:function>
<!-- ================================== -->
<xsl:template name='hasMatch'>
<xsl:param name='n'/>
<xsl:param name='nlist'/>
<xsl:param name='res' select='false()'/>
<xsl:choose>
<xsl:when test='$nlist and not($res)'>
<xsl:variable name='comp' select='vfx:collapse($n) = vfx:collapse($nlist[1])'/>
<!-- xsl:variable name='comp' select='$n = $nlist[1]'/ -->
<xsl:call-template name='hasMatch'>
<xsl:with-param name='n' select='$n'/>
<xsl:with-param name='nlist' select='$nlist[position()>1]'/>
<xsl:with-param name='res' select='$res or $comp'/>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='$res'/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:function name='vfx:hasMatch' as='xs:boolean'>
<xsl:param name='n'/>
<xsl:param name='nlist'/>
<xsl:call-template name='hasMatch'>
<xsl:with-param name='n' select='$n'/>
<xsl:with-param name='nlist' select='$nlist'/>
</xsl:call-template>
</xsl:function>
<!-- ================================== -->
<xsl:template name='hasMatchingName'>
<xsl:param name='fn'/>
<xsl:param name='mn'/>
<xsl:param name='ln'/>
<xsl:param name='nlist'/>
<xsl:param name='res' select='false()'/>
<xsl:choose>
<xsl:when test='$nlist and not($res)'>
<xsl:variable name='fnmnln' select='concat($fn,"|",$mn ,"|",$ln)'/>
<xsl:variable name='listfnmnln' select='concat($nlist[1]/dm:FNAME,"|",$nlist[1]/dm:MNAME, "|",$nlist[1]/dm:LNAME)'/>
<xsl:variable name='comp' select='vfx:collapse($fnmnln) = vfx:collapse($listfnmnln)'/>

<xsl:call-template name='hasMatchingName'>
<xsl:with-param name='fn' select='$fn'/>
<xsl:with-param name='mn' select='$mn'/>
<xsl:with-param name='ln' select='$ln'/>
<xsl:with-param name='nlist' select='$nlist[position()>1]'/>
<xsl:with-param name='res' select='$res or $comp'/>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='$res'/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:function name='vfx:hasMatchingName' as='xs:boolean'>
<xsl:param name='fn'/>
<xsl:param name='mn'/>
<xsl:param name='ln'/>
<xsl:param name='nlist'/>
<xsl:call-template name='hasMatchingName'>
<xsl:with-param name='fn' select='$fn'/>
<xsl:with-param name='mn' select='$mn'/>
<xsl:with-param name='ln' select='$ln'/>
<xsl:with-param name='nlist' select='$nlist'/>
</xsl:call-template>
</xsl:function>
<!-- ================================== -->
<xsl:function name='vfx:expandClass' as='xs:string'>
<xsl:param name='s'/>
<xsl:variable name='before' select='substring-before($s,":")'/>
<xsl:variable name='after' select='substring-after($s,":")'/>
<xsl:choose>
<xsl:when test='$before = "bibo"'>
<xsl:value-of select='concat("http://purl.org/ontology/bibo/",$after)'/>
</xsl:when>
<xsl:when test='$before = "core"'>
<xsl:value-of select='concat("http://vivoweb.org/ontology/core#",$after)'/>
</xsl:when>
<xsl:when test='$before = "ai"'>
<xsl:value-of select='concat("http://vivoweb.org/ontology/activity-insight#",$after)'/>
</xsl:when>
<xsl:when test='true()'>
<xsl:value-of select='concat("http://vivoweb.org/ontology/activity-insight#",$after)'/>
</xsl:when>
</xsl:choose>

</xsl:function>
<!-- ================================== -->

<xsl:function name='vfx:knownUriByNetidOrName'>
<xsl:param name='fn'/>
<xsl:param name='mn'/>
<xsl:param name='ln'/>
<xsl:param name='nid'/>
<xsl:param name='ep'/>
<xsl:choose>
<xsl:when test='$ep'>
<xsl:variable name='nfn' select='normalize-space($fn)'/>
<xsl:variable name='nmn' select='normalize-space($mn)'/>
<xsl:variable name='nln' select='normalize-space($ln)'/>
<xsl:variable name='unid' select='upper-case($nid)'/>



  <xsl:choose>
  <xsl:when test='$nid'>
	<xsl:variable name='plist' 
		select='$ep/person[netid != "" and
			           upper-case(netid) = $unid]/uri'/>
	<xsl:choose>
	<xsl:when test='$plist[1]'>
	<xsl:value-of select='$plist[1]'/>
	</xsl:when>
	<xsl:otherwise>
	<xsl:variable name='results' 
		select='$ep/person[upper-case(normalize-space(lname)) = 
				   upper-case($nln) and 
        		           vfx:isoName(fname,mname,lname,$fn,$mn,$ln)]/uri'/>

	<xsl:choose>
	<xsl:when test='$results[1]'>
	<xsl:value-of select='$results[1]'/>
	</xsl:when>
	<xsl:otherwise>
	<xsl:value-of select='""'/>
	</xsl:otherwise>
	</xsl:choose>
	</xsl:otherwise>
	</xsl:choose>
  </xsl:when>
  <xsl:otherwise>
	<xsl:variable name='results' 
		select='$ep/person[upper-case(normalize-space(lname)) = 
				   upper-case($nln) and 
        		           vfx:isoName(fname,mname,lname,$fn,$mn,$ln)]/uri'/>

	<xsl:choose>
	<xsl:when test='$results[1]'>
	<xsl:value-of select='$results[1]'/>
	</xsl:when>
	<xsl:otherwise>
	<xsl:value-of select='""'/>
	</xsl:otherwise>
	</xsl:choose>
  </xsl:otherwise>
  </xsl:choose>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='""'/>
</xsl:otherwise>
</xsl:choose>

</xsl:function>




<xsl:function name='vfx:knownUriByNetidOrNameKeyed'>
<xsl:param name='fn'/>
<xsl:param name='mn'/>
<xsl:param name='ln'/>
<xsl:param name='nid'/>
<xsl:param name='ep'/>
<xsl:choose>
<xsl:when test='$ep'>
<xsl:variable name='nfn' select='normalize-space($fn)'/>
<xsl:variable name='nmn' select='normalize-space($mn)'/>
<xsl:variable name='nln' select='normalize-space($ln)'/>
<xsl:variable name='unid' select='upper-case($nid)'/>



  <xsl:choose>
  <xsl:when test='$nid'>
	<xsl:variable name='plist' 
		select='$ep[netid != "" and $unid != "" and
			           upper-case(netid) = $unid]/uri'/>
	<xsl:choose>
	<xsl:when test='$plist[1] != ""'>
	<xsl:value-of select='$plist[1]'/>
	</xsl:when>
	<xsl:otherwise>
<!-- xsl:comment> boo </xsl:comment -->
	<xsl:variable name='results' 
		select='$ep[upper-case(normalize-space(lname)) = 
		            upper-case($nln) and 
        		    vfx:isoName(fname,mname,lname,$fn,$mn,$ln)]/uri'/>
<!-- xsl:comment> foo </xsl:comment -->
	<xsl:choose>
	<xsl:when test='$results[1]'>
	<xsl:value-of select='$results[1]'/>
	</xsl:when>
	<xsl:otherwise>
	<xsl:value-of select='""'/>
	</xsl:otherwise>
	</xsl:choose>
	</xsl:otherwise>
	</xsl:choose>
  </xsl:when>
  <xsl:otherwise>
<!-- xsl:comment> bar </xsl:comment -->
	<xsl:variable name='results' 
		select='$ep[upper-case(normalize-space(lname)) = 
		            upper-case($nln) and 
        		    vfx:isoName(fname,mname,lname,$fn,$mn,$ln)]/uri'/>

	<xsl:choose>
	<xsl:when test='$results[1] != ""'>
	<xsl:value-of select='$results[1]'/>
	</xsl:when>
	<xsl:otherwise>
	<xsl:value-of select='""'/>
	</xsl:otherwise>
	</xsl:choose>
  </xsl:otherwise>
  </xsl:choose>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='""'/>
</xsl:otherwise>
</xsl:choose>

</xsl:function>

<!-- ================================== -->
<xsl:function name='vfx:knownPersonByNetidOrNameKeyed'>
<xsl:param name='fn'/>
<xsl:param name='mn'/>
<xsl:param name='ln'/>
<xsl:param name='nid'/>
<xsl:param name='ep'/>
<xsl:choose>
<xsl:when test='$ep'>
<xsl:variable name='nfn' select='normalize-space($fn)'/>
<xsl:variable name='nmn' select='normalize-space($mn)'/>
<xsl:variable name='nln' select='normalize-space($ln)'/>
<xsl:variable name='unid' select='upper-case($nid)'/>

  <xsl:choose>
  <xsl:when test='$nid'>
	<xsl:variable name='plist' 
		select='$ep[netid != "" and $unid != "" and
			           upper-case(netid) = $unid]'/>
	<xsl:choose>
	<xsl:when test='$plist[1] != ""'>
	<xsl:copy-of select='$plist[1]'/>
	</xsl:when>
	<xsl:otherwise>
<!-- =============================== -->
	<xsl:variable name='results' 
		select='$ep[upper-case(normalize-space(lname)) = 
			upper-case($nln) and 
        		vfx:isoName(fname,mname,lname,$fn,$mn,$ln)]'/>

	<xsl:choose>
	<xsl:when test='count($results)>0'>

	<xsl:variable name='longest' 
  		select='vfx:maxNameLength($results)' as='xs:integer'/>
	<xsl:variable name='res' as='node()*' 
		select='$results[string-length(concat(normalize-space(./lname),"|",
                                           normalize-space(./fname),"|",
                                           normalize-space(./mname)))
                      = $longest]'/>


	<xsl:copy-of select='$res[1]'/> 

	</xsl:when>
	<xsl:otherwise>
	<xsl:value-of select='""'/>
	</xsl:otherwise>
	</xsl:choose>
<!-- =============================== <xsl:value-of select='""'/> -->

	</xsl:otherwise>
	</xsl:choose>
  </xsl:when>
  <xsl:otherwise>

<!-- =============================== -->
	<xsl:variable name='results' 
		select='$ep[upper-case(normalize-space(lname)) = 
			upper-case($nln) and 
        		vfx:isoName(fname,mname,lname,$fn,$mn,$ln)]'/>

	<xsl:choose>
	<xsl:when test='count($results)>0'>

	<xsl:variable name='longest' 
  		select='vfx:maxNameLength($results)' as='xs:integer'/>
	<xsl:variable name='res' as='node()*' 
	select='$results[string-length(concat(normalize-space(./lname),"|",
                                           normalize-space(./fname),"|",
                                           normalize-space(./mname)))
                      = $longest]'/>


	<xsl:copy-of select='$res[1]'/> 

	</xsl:when>
	<xsl:otherwise>
	<xsl:value-of select='""'/>
	</xsl:otherwise>
	</xsl:choose>
<!-- =============================== -->

  </xsl:otherwise>
  </xsl:choose>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='""'/>
</xsl:otherwise>
</xsl:choose>

</xsl:function>
<!--  ================================= -->
<xsl:function name='vfx:bestChoices' as='node()*' >
<xsl:param name='nlist'/>

<xsl:variable name='longest' 
  select='vfx:maxNameLength($nlist)' as='xs:integer'/>


<xsl:sequence
	select='$nlist[
            string-length(concat(normalize-space(./lname),"|",
                                 normalize-space(./fname),"|",
                                 normalize-space(./mname))) = $longest]' />

</xsl:function>

<!-- ================================== -->
<xsl:function name='vfx:knownPersonByNetidOrName'>
<xsl:param name='fn'/>
<xsl:param name='mn'/>
<xsl:param name='ln'/>
<xsl:param name='nid'/>
<xsl:param name='ep'/>
<xsl:choose>
<xsl:when test='$ep'>
<xsl:variable name='nfn' select='normalize-space($fn)'/>
<xsl:variable name='nmn' select='normalize-space($mn)'/>
<xsl:variable name='nln' select='normalize-space($ln)'/>
<xsl:variable name='unid' select='upper-case($nid)'/>

  <xsl:choose>
  <xsl:when test='$nid'>
	<xsl:variable name='plist' 
		select='$ep/person[netid != "" and $unid != "" and
			           upper-case(netid) = $unid]'/>
	<xsl:choose>
	<xsl:when test='$plist[1] != ""'>
	<xsl:copy-of select='$plist[1]'/>
	</xsl:when>
	<xsl:otherwise>
<!-- =============================== -->
	<xsl:variable name='results' 
		select='$ep/person[upper-case(normalize-space(lname)) = 
			upper-case($nln) and 
        		vfx:isoName(fname,mname,lname,$fn,$mn,$ln)]'/>

	<xsl:choose>
	<xsl:when test='count($results)>0'>

	<xsl:variable name='longest' 
  		select='vfx:maxNameLength($results)' as='xs:integer'/>
	<xsl:variable name='res' as='node()*' 
		select='$results[string-length(concat(normalize-space(./lname),"|",
                                           normalize-space(./fname),"|",
                                           normalize-space(./mname)))
                      = $longest]'/>


	<xsl:copy-of select='$res[1]'/> 

	</xsl:when>
	<xsl:otherwise>
	<xsl:value-of select='""'/>
	</xsl:otherwise>
	</xsl:choose>
<!-- =============================== <xsl:value-of select='""'/> -->

	</xsl:otherwise>
	</xsl:choose>
  </xsl:when>
  <xsl:otherwise>

<!-- =============================== -->
	<xsl:variable name='results' 
		select='$ep/person[upper-case(normalize-space(lname)) = 
			upper-case($nln) and 
        		vfx:isoName(fname,mname,lname,$fn,$mn,$ln)]'/>

	<xsl:choose>
	<xsl:when test='count($results)>0'>

	<xsl:variable name='longest' 
  		select='vfx:maxNameLength($results)' as='xs:integer'/>
	<xsl:variable name='res' as='node()*' 
		select='$results[string-length(concat(normalize-space(./lname),"|",
                                           normalize-space(./fname),"|",
                                           normalize-space(./mname)))
                      = $longest]'/>


	<xsl:copy-of select='$res[1]'/> 

	</xsl:when>
	<xsl:otherwise>
	<xsl:value-of select='""'/>
	</xsl:otherwise>
	</xsl:choose>
<!-- =============================== -->

  </xsl:otherwise>
  </xsl:choose>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='""'/>
</xsl:otherwise>
</xsl:choose>

</xsl:function>
<!-- ================================== -->
<xsl:function name='vfx:maxNameLength' as='xs:integer'>
<xsl:param name='nlist'/>

<xsl:variable name='lengths' as='xs:integer*' 
	select='if($nlist) 
		then 
		for $p in $nlist return 
                	string-length(concat(normalize-space($p/lname),"|",
                                     normalize-space($p/fname),"|",
                                     normalize-space($p/mname))) 
		else
		0 '/>


<xsl:value-of select='max($lengths)'/>
</xsl:function>


<!--  ================================= -->

<xsl:function name='vfx:knownUri'>
<xsl:param name='fn'/>
<xsl:param name='mn'/>
<xsl:param name='ln'/>
<xsl:param name='ep'/>
<xsl:choose>
<xsl:when test='$ep'>
<xsl:variable name='nfn' select='normalize-space($fn)'/>
<xsl:variable name='nmn' select='normalize-space($mn)'/>
<xsl:variable name='nln' select='normalize-space($ln)'/>


<xsl:variable name='results' 
select='$ep/person[normalize-space(lname) = $nln and 
        vfx:isoName(fname,mname,lname,$fn,$mn,$ln)]/uri'/>


	<xsl:choose>
	<xsl:when test='$results[1]'>
	<xsl:value-of select='$results[1]'/>
	</xsl:when>
	<xsl:otherwise>
	<xsl:value-of select='""'/>
	</xsl:otherwise>
	</xsl:choose>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='""'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>
<!-- ================================== -->
<xsl:function name='vfx:knownPerson' as='node()*' >
<xsl:param name='fn'/>
<xsl:param name='mn'/>
<xsl:param name='ln'/>
<xsl:param name='ep'/>
<xsl:choose>
<xsl:when test='$ep'>
<xsl:variable name='nfn' select='normalize-space($fn)'/>
<xsl:variable name='nmn' select='normalize-space($mn)'/>
<xsl:variable name='nln' select='normalize-space($ln)'/>
<!--
<xsl:variable name='results' 
select='$ep/person[normalize-space(lname) = $nln and vfx:isoName(fname,mname,lname,$fn,$mn,$ln)]/uri'/>
-->


<xsl:sequence select='$ep/person[vfx:isoName(fname,mname,lname,$fn,$mn,$ln)]/uri'/>


</xsl:when>
<xsl:otherwise>
<xsl:value-of select='""'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>
<!-- ================================== -->
<xsl:function name='vfx:seekKnownUri'>
<xsl:param name='fn'/>
<xsl:param name='mn'/>
<xsl:param name='ln'/>
<xsl:param name='nid'/>
<xsl:param name='ep'/>
<xsl:choose>

<xsl:when test='$ep'>
	<xsl:choose>
	  <xsl:when test='$nid'>
		<xsl:variable name='results' select='$ep/person[cornell=$nid]/uri'/>
		<xsl:choose>
			<xsl:when test='$results[1]'>
				<xsl:value-of select='$results[1]'/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select='""'/>
			</xsl:otherwise>
		</xsl:choose>
	  </xsl:when>
	  <xsl:otherwise>
		<xsl:variable name='results' 
			select='$ep/person[fname=$fn and mname = $mn and lname = $ln]/uri'/>
		<xsl:choose>
			<xsl:when test='$results[1]'>
				<xsl:value-of select='$results[1]'/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select='""'/>
			</xsl:otherwise>
		</xsl:choose>
	  </xsl:otherwise>
	</xsl:choose>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='""'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>

<!-- ================================== -->
<xsl:function name='vfx:knownJournalUri'>
<xsl:param name='n'/>
<xsl:param name='ep'/>
<xsl:choose>
<xsl:when test='$ep'>
<xsl:variable name='results' select='$ep/journal[name=$n]/uri'/>
	<xsl:choose>
	<xsl:when test='$results[1]'>
	<xsl:value-of select='$results[1]'/>
	</xsl:when>
	<xsl:otherwise>
	<xsl:value-of select='""'/>
	</xsl:otherwise>
	</xsl:choose>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='""'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>



<!-- ================================== -->
<xsl:function name='vfx:knownCEOrgUri'>
<xsl:param name='n'/>
<xsl:param name='sc'/>
<xsl:param name='ep'/>
<xsl:choose>
<xsl:when test='$ep'>
<xsl:variable name='results' 
	select='$ep/person[name=$n and stateCountry = $sc]'/>
<xsl:value-of select='$results/uri'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='""'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>
<!-- ================================== -->
<xsl:function name='vfx:knownOrgUri'>
<xsl:param name='n'/>
<xsl:param name='ep'/>
<xsl:choose>
<xsl:when test='$ep'>
<xsl:variable name='results' select='$ep/org[name=$n][1]'/>
<xsl:value-of select='$results/uri'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='""'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>
<!-- ================================== -->
<xsl:function name='vfx:knownGeoUri'>
<xsl:param name='n'/>
<xsl:param name='ep'/>
<xsl:choose>
<xsl:when test='$ep'>
<xsl:variable name='results' select='$ep/geo[title=$n][1]'/>
<xsl:value-of select='$results/uri'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='""'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>
<!-- ================================== -->
<xsl:function name='vfx:knownEmphUri'>
<xsl:param name='n'/>
<xsl:param name='ep'/>
<xsl:choose>
<xsl:when test='$ep'>
<xsl:variable name='results' select='$ep/emph[name=$n][1]'/>
<xsl:value-of select='$results/uri'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='""'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>
<!-- ================================== -->
<xsl:function name='vfx:knownUaUri'>
<xsl:param name='n'/>
<xsl:param name='ep'/>
<xsl:choose>
<xsl:when test='$ep'>
<xsl:variable name='results' select='$ep/area[name=$n][1]'/>
<xsl:value-of select='$results/uri'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='""'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>
<!-- ================================== -->
<xsl:function name='vfx:knownCaUri'>
<xsl:param name='n'/>
<xsl:param name='ep'/>
<xsl:choose>
<xsl:when test='$ep'>
<xsl:variable name='results' select='$ep/conarea[title=$n][1]'/>
<xsl:value-of select='$results/uri'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='""'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>

<!-- ================================== -->
<xsl:function name='vfx:sameNetid' as='xs:boolean'>
<xsl:param name='n1'/>
<xsl:param name='n2'/>
<xsl:variable name='cs1' select='vfx:collapse($n1)'/>
<xsl:variable name='cs2' select='vfx:collapse($n2)'/>
<xsl:value-of select='$cs1 = $cs2'/>
</xsl:function>

<!-- ================================== -->
<xsl:function name='vfx:realNetid'>
<xsl:param name='nid'/>
<xsl:param name='path'/>
<xsl:variable name='nidxmlU' 
select="concat($path,'/',upper-case($nid), '.xml')"/>
<xsl:variable name='nidxmlL' 
select="concat($path,'/',lower-case($nid), '.xml')"/>
<xsl:choose>
<xsl:when test='$nid = ""'>
<xsl:value-of select='""'/>
</xsl:when>
<xsl:when test='doc-available($nidxmlL)'>
<xsl:value-of select='lower-case($nid)'/>
</xsl:when>
<xsl:when test='doc-available($nidxmlU)'>
<xsl:value-of select='upper-case($nid)'/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='"_void_"'/>
</xsl:otherwise>
</xsl:choose>
</xsl:function>

<!-- ================================== -->
<xsl:function name='vfx:nameWeight'>
<xsl:param name='fn'/>
<xsl:param name='mn'/>
<xsl:param name='ln'/>
<xsl:choose>
  <!-- [  ] * * -->
  <xsl:when test='string-length($ln) = 0'>
	<xsl:value-of select='0'/>
  </xsl:when>
  <xsl:otherwise>
	<!-- Last * * -->
	<xsl:choose>
  	<xsl:when test='vfx:stringWeight($fn) = 0'>
		<!-- Last [ ] * -->
		<xsl:choose>
  		<xsl:when test='vfx:stringWeight($mn) = 0'>
		<!-- Last [ ] [ ] -->
		<xsl:value-of select='1.0'/>
  		</xsl:when>
  		<xsl:when test='vfx:stringWeight($mn) = 1'>
		<!-- Last [ ] M -->
		<xsl:value-of select='1.25'/>
  		</xsl:when>
  		<xsl:otherwise>
		<!-- Last [ ] Middle -->
		<xsl:value-of select='1.5'/>
  		</xsl:otherwise>
		</xsl:choose>
  	</xsl:when>
  	<xsl:when test='vfx:stringWeight($fn) = 1'>
		<!-- Last F * -->
		<xsl:choose>
  		<xsl:when test='vfx:stringWeight($mn) = 0'>
		<!-- Last F [ ] -->
		<xsl:value-of select='1.75'/>
  		</xsl:when>
  		<xsl:when test='vfx:stringWeight($mn) = 1'>
		<!-- Last F M -->
		<xsl:value-of select='2.25'/>
  		</xsl:when>
  		<xsl:otherwise>
		<!-- Last F Middle -->
		<xsl:value-of select='2.5'/>
		</xsl:otherwise>
		</xsl:choose>
	</xsl:when>
		
	<xsl:otherwise>
		<!-- Last First * -->
		<xsl:choose>	
  		<xsl:when test='vfx:stringWeight($mn) = 0'>
		<!-- Last First [ ] -->
		<xsl:value-of select='2.00'/>
  		</xsl:when>
  		<xsl:when test='vfx:stringWeight($mn) = 1'>
		<!-- Last First M -->
		<xsl:value-of select='2.75'/>
  		</xsl:when>
  		<xsl:otherwise>
		<!-- Last First Middle -->
		<xsl:value-of select='3.00'/>
  		</xsl:otherwise>
		</xsl:choose>
  	</xsl:otherwise>
	</xsl:choose>
</xsl:otherwise>
</xsl:choose>
</xsl:function>
<!-- ================================== -->
<xsl:function name='vfx:stringWeight'>
<xsl:param name='s'/>

<xsl:choose>
  <xsl:when test='string-length($s) = 0'>
	<xsl:value-of select='0'/>
  </xsl:when>
  <xsl:otherwise>
	<xsl:choose>
  	<xsl:when test='string-length(substring($s,2)) = 0'>
	   	<xsl:value-of select='1'/>
  	</xsl:when>
  	<xsl:otherwise>
		<xsl:value-of select='2'/>
  	</xsl:otherwise>
	</xsl:choose>
  </xsl:otherwise>
</xsl:choose>
</xsl:function>
<!-- ================================== -->
<xsl:function name='vfx:notEvent' as='xs:boolean'>
<xsl:param name='n'/>

<xsl:value-of select='not(vfx:isEvent($n))'/>
</xsl:function>
<xsl:function name='vfx:isEvent' as='xs:boolean'>
<xsl:param name='n'/>
<xsl:variable name='nn' select='upper-case(normalize-space($n))'/>
<xsl:variable name='soviet' 
select='contains($nn,"COMMITTEE") or contains($nn,"COUNCIL")'/>
<xsl:choose>
  <xsl:when test='contains($nn,"CONFERENCE") and not($soviet)'>
	<xsl:value-of select='true()'/>
  </xsl:when>
  <xsl:when test='contains($nn,"WORKSHOP") and not($soviet)'>
	<xsl:value-of select='true()'/>	
  </xsl:when>
  <xsl:when test='contains($nn,"SYMPOSIUM") and not($soviet)'>
	<xsl:value-of select='true()'/>
  </xsl:when>
  <xsl:when test='contains($nn,"MEETING") and not($soviet)'>
	<xsl:value-of select='true()'/>
  </xsl:when>
  <xsl:otherwise>
	<xsl:value-of select='false()'/>
  </xsl:otherwise>
</xsl:choose>
</xsl:function>


<!-- ============================================= -->
<!-- this extension function  
	1. normalizes whitespace and removes special chrs from the argument string
	2. shifts alphabetic characters to upper case
	3. returns the adjusted string for comparison
-->
<xsl:function name='vfx:clean' as='xs:string'>
<xsl:param name='s1'/>
<xsl:variable name='res' select='replace($s1,"[\-.,;""]"," ")'/>
<xsl:variable name='res1' select="replace($res,'''',' ')"/>
<xsl:value-of select='upper-case(normalize-space($res1))'/>

</xsl:function>

<!-- ================================== -->

<xsl:function name='vfx:weigh' as='xs:float'>
<xsl:param name='str1'/>
<xsl:param name='str2'/>

<xsl:variable name='weight'>
<xsl:variable name='str1t' select='vfx:clean($str1)'/>
<xsl:variable name='len1' select='string-length($str1t)'/>
<xsl:variable name='str2t' select='vfx:clean($str2)'/>
<xsl:variable name='len2' select='string-length($str2t)'/>

<xsl:choose>
  <xsl:when test='($len1>=1 and $len2>=1)'>
	<!-- James Micheal Smith vs John Michael Smith or
	     Jim Smith vs John Smith or  L Smith vs John Smith
	     J Smith vs John Smith
	 -->
	<xsl:choose>
	  <xsl:when test='($len1>1 and $len2>1)'>
		<xsl:choose>
  		<xsl:when test='$str1t = $str2t'>
			<xsl:value-of select='$wFullFullMatch'/>
  		</xsl:when>
  		<xsl:otherwise>
			<xsl:value-of select='$wFullFullMisMatch'/>
  		</xsl:otherwise>
		</xsl:choose>
	  </xsl:when>
	  <xsl:otherwise>  <!-- ($len1=1 or $len2=1)' -->
		<xsl:choose>
  		<xsl:when test='substring($str2t,1,1) = substring($str1t,1,1)'>
			<xsl:value-of select='$wPartialNonEmptyMatch'/>
  		</xsl:when>
  		<xsl:otherwise>
			<xsl:value-of select='$wPartialNonEmptyMisMatch'/>
  		</xsl:otherwise>
		</xsl:choose>
	  </xsl:otherwise>
          </xsl:choose>
   </xsl:when>
   <xsl:when test='$len1>0 and $len2=0'>
	<!-- 1 str empty -->
	<xsl:value-of select='$wEmptyNonEmptyMisMatch'/>
   </xsl:when>
   <xsl:when test='$len1=0 and $len2>0'>
	<!-- 1 str empty -->
	<xsl:value-of select='$wEmptyNonEmptyMisMatch'/>
   </xsl:when>
   <xsl:otherwise>
	<!-- both strs empty -->
	<xsl:value-of select='$wEmptyEmptyMatch'/>
   </xsl:otherwise>
</xsl:choose>
</xsl:variable>
<xsl:value-of select='$weight'/>
</xsl:function>

<xsl:function name='vfx:goodName' as='xs:boolean'>
<xsl:param name='fn'/>
<xsl:param name='mn'/>
<xsl:param name='ln'/>
<xsl:value-of select='if($fn = "" or $ln = "") then false() else true()'/>
</xsl:function>

<!-- ================================== -->
<xsl:function name='vfx:isoName' as='xs:boolean'>
<xsl:param name='fn1'/>
<xsl:param name='mn1'/>
<xsl:param name='ln1'/>
<xsl:param name='fn2'/>
<xsl:param name='mn2'/>
<xsl:param name='ln2'/>
<xsl:value-of select='vfx:isoNameMatch($fn1,$mn1,$ln1,$fn2,$mn2,$ln2)'/>
</xsl:function>


<!-- ================================== -->
<xsl:function name='vfx:isoNameStrict' as='xs:boolean'>
<xsl:param name='fn1'/>
<xsl:param name='mn1'/>
<xsl:param name='ln1'/>
<xsl:param name='fn2'/>
<xsl:param name='mn2'/>
<xsl:param name='ln2'/>
<xsl:variable name='ln2t' select='vfx:clean($ln2)'/>
<xsl:variable name='ln1t' select='vfx:clean($ln1)'/>
<xsl:variable name='fn2t' select='vfx:clean($fn2)'/>
<xsl:variable name='fn1t' select='vfx:clean($fn1)'/>
<xsl:variable name='mn2t' select='vfx:clean($mn2)'/>
<xsl:variable name='mn1t' select='vfx:clean($mn1)'/>
<xsl:value-of select='($ln1t = $ln2t) and ($fn1t = $fn2t) and ($mn1t = $mn2t)'/>
</xsl:function>



<xsl:function name='vfx:isoNameJava' as='xs:boolean'>
<xsl:param name='fn1'/>
<xsl:param name='mn1'/>
<xsl:param name='ln1'/>
<xsl:param name='fn2'/>
<xsl:param name='mn2'/>
<xsl:param name='ln2'/>
<xsl:value-of select='true()'/>
<!--
<xsl:value-of select='IsoMatch:iso($fn1,$mn1,$ln1,$fn2,$mn2,$ln2)' xmlns:IsoMatch='java:edu.cornell.saxonext.IsoMatch'/>
-->
</xsl:function>

<xsl:function name='vfx:isoNameMatch' as='xs:boolean'>
<xsl:param name='fn1'/>
<xsl:param name='mn1'/>
<xsl:param name='ln1'/>
<xsl:param name='fn2'/>
<xsl:param name='mn2'/>
<xsl:param name='ln2'/>
<xsl:variable name='ln2t' select='vfx:clean($ln2)'/>
<xsl:variable name='ln1t' select='vfx:clean($ln1)'/>

<xsl:choose>

<xsl:when test='$ln1t != $ln2t'>
	<!-- John M. Smith vs John M. Smyth -->
	<xsl:value-of select='false()'/>
</xsl:when>
<xsl:when test='concat(vfx:clean($fn1),"|",vfx:clean($mn1)) = 
		concat(vfx:clean($fn2),"|",vfx:clean($mn2))'>
	<xsl:value-of select='true()'/>
</xsl:when>

<xsl:otherwise>

<xsl:variable name='fweight' as='xs:float'
	select='vfx:weigh($fn1,$fn2)' />

<xsl:variable name='mweight'  as='xs:float'
	select='vfx:weigh($mn1, $mn2)' />

<xsl:variable name='conf' as='xs:float'
select='($fweight*$wFirstName + $mweight*$wMiddleName) div $wFM'/>
<!--xsl:comment><xsl:value-of select='$conf'/></xsl:comment-->
<xsl:choose>
  	<xsl:when test='$conf > $cutoff' >
		<xsl:value-of select='true()'/>
  	</xsl:when>
  	<xsl:otherwise>
		<xsl:value-of select='false()'/>
  	</xsl:otherwise>
</xsl:choose>
	
</xsl:otherwise>
</xsl:choose>

</xsl:function>

<!-- ================================== -->
<xsl:template name='hasIsoMatch'>
<xsl:param name='n'/>
<xsl:param name='nlist'/>
<xsl:param name='res' select='false()'/>
<xsl:choose>
<xsl:when test='$nlist and not($res)'>
<xsl:variable name='comp' select='vfx:isoName($n/fname,
						$n/mname,
						$n/lname,
						$nlist[1]/fname,
						$nlist[1]/mname,
						$nlist[1]/lname)'/>
<!-- xsl:variable name='comp' select='$n = $nlist[1]'/ -->
<xsl:call-template name='hasIsoMatch'>
<xsl:with-param name='n' select='$n'/>
<xsl:with-param name='nlist' select='$nlist[position()>1]'/>
<xsl:with-param name='res' select='$res or $comp'/>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='$res'/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>
<!-- ================================== -->
<xsl:function name='vfx:hasIsoMatch' as='xs:boolean'>
<xsl:param name='n'/>
<xsl:param name='nlist'/>
<xsl:call-template name='hasIsoMatch'>
<xsl:with-param name='n' select='$n'/>
<xsl:with-param name='nlist' select='$nlist'/>
</xsl:call-template>
</xsl:function>
<!-- ================================== -->
<xsl:function name='vfx:isoScore' as='xs:float'>
<xsl:param name='fn1'/>
<xsl:param name='mn1'/>
<xsl:param name='ln1'/>
<xsl:param name='fn2'/>
<xsl:param name='mn2'/>
<xsl:param name='ln2'/>
<xsl:variable name='ln2t' select='vfx:clean($ln2)'/>
<xsl:variable name='ln1t' select='vfx:clean($ln1)'/>

<xsl:choose>

<xsl:when test='$ln1t != $ln2t'>
	<!-- John M. Smith vs John M. Smyth -->
	<xsl:value-of select='$wFullFullMisMatch'/>
</xsl:when>

<xsl:when test='concat(vfx:clean($fn1),"|",vfx:clean($mn1)) = 
		concat(vfx:clean($fn2),"|",vfx:clean($mn2))'>
	<xsl:value-of select='$wFullFullMatch'/>
</xsl:when>

<xsl:otherwise>

<xsl:variable name='lweight' select='$wL' as='xs:float'/>

<xsl:variable name='fweight' as='xs:float'
	select='vfx:weigh($fn1, $fn2)' />

<xsl:variable name='mweight'  as='xs:float'
		select='vfx:weigh($mn1, $mn2)' />

<xsl:value-of
	select='($fweight*$wFirstName + $mweight*$wMiddleName) div $wFM'/>


</xsl:otherwise>
</xsl:choose>

</xsl:function>
<!-- ================================== -->

<xsl:function name='vfx:isoScoreTest' as='xs:float'>
<xsl:param name='fn1'/>
<xsl:param name='mn1'/>
<xsl:param name='ln1'/>
<xsl:param name='fn2'/>
<xsl:param name='mn2'/>
<xsl:param name='ln2'/>
<xsl:variable name='ln2t' select='vfx:clean($ln2)'/>
<xsl:variable name='ln1t' select='vfx:clean($ln1)'/>

<xsl:choose>

<xsl:when test='$ln1t != $ln2t'>
	<!-- John M. Smith vs John M. Smyth -->
	<xsl:value-of select='$wFullFullMisMatch'/>
</xsl:when>


<xsl:otherwise>

<xsl:variable name='lweight' select='$wL' as='xs:float'/>
<xsl:variable name='fweight' as='xs:float'
	select='vfx:weigh($fn1, $fn2)' />
<xsl:variable name='mweight'  as='xs:float'
		select='vfx:weigh($mn1, $mn2)' />

<xsl:value-of 
	select='($fweight*$wFirstName + $mweight*$wMiddleName) div $wFM'/>

</xsl:otherwise>
</xsl:choose>

</xsl:function>

<!-- ================================== -->
<xsl:template name='newPeople'>
<xsl:param name='knowns'/>

<xsl:element name='ExtantPersons' inherit-namespaces='no'>
<xsl:value-of select='$MyNL'/>
<xsl:for-each 
select='$knowns/person[not(vfx:hasIsoMatch(.,preceding-sibling::person))]'>
<xsl:variable name='this' select='.'/>

<xsl:variable name='fn' select='normalize-space(fname)'/>
<xsl:variable name='mn' select='normalize-space(mname)'/>
<xsl:variable name='ln' select='normalize-space(lname)'/>

<xsl:variable name='isos' 
select='$knowns/person[vfx:isoName(fname,mname,lname,$fn,$mn,$ln)]'/>

<xsl:variable name='res'  select='vfx:bestChoices($isos)'/>

<xsl:copy-of select='$res[1]'/><xsl:value-of select='$MyNL'/>

</xsl:for-each>
</xsl:element>
</xsl:template>

<xsl:template name='NewOrgs'>
<xsl:param name='knowns'/>
<xsl:element name='ExtantOrgs' namespace=''>
<xsl:comment><xsl:value-of select='count($knowns/org)'/> </xsl:comment>
<xsl:value-of select='$MyNL'/>
<xsl:for-each 
select='$knowns/org[not(name = preceding-sibling::org/name)]'>
<!--xsl:comment>
<xsl:value-of select='./name' separator=' | '/> 
</xsl:comment-->
<xsl:copy-of select='.'/>
<xsl:value-of select='$MyNL'/>
</xsl:for-each>
</xsl:element>
</xsl:template>

<xsl:template name='NewCEOrgs'>
<xsl:param name='knowns'/>
<xsl:element name='ExtantCEOrgs' namespace=''>
<xsl:comment><xsl:value-of select='count($knowns/org)'/> </xsl:comment>
<xsl:value-of select='$MyNL'/>
<xsl:for-each 
select='$knowns/org[not(name = preceding-sibling::org/name) and 
		   not(stateCountry = preceding-sibling::org/stateCountry)]'>

<xsl:copy-of select='.'/>
<xsl:value-of select='$MyNL'/>
</xsl:for-each>
</xsl:element>
</xsl:template>

<xsl:template name='NewUsdaAreas'>
<xsl:param name='knowns'/>
<xsl:element name='ExtantUsdaAreas' namespace=''>
<xsl:comment><xsl:value-of select='count($knowns/area)'/> </xsl:comment>
<xsl:value-of select='$MyNL'/>
<xsl:for-each
select='$knowns/area[not(name = preceding-sibling::area/name)]'>
<!--xsl:comment>
<xsl:value-of select='./area' separator=' | '/>
</xsl:comment-->
<xsl:copy-of select='.'/>
<xsl:value-of select='$MyNL'/>
</xsl:for-each>
</xsl:element>
</xsl:template>

<xsl:template name='NewJournals'>
<xsl:param name='knowns'/>
<xsl:element name='ExtantJournals' namespace=''>
<xsl:comment><xsl:value-of select='count($knowns/journal)'/> </xsl:comment>
<xsl:value-of select='$MyNL'/>
<xsl:for-each 
select='$knowns/journal[not(name = preceding-sibling::journal/name)]'>
<xsl:comment><xsl:value-of select='./name' separator=' | '/> </xsl:comment>
<xsl:copy-of select='.'/>
<xsl:value-of select='$MyNL'/>
</xsl:for-each>
</xsl:element>
</xsl:template>

<xsl:template name='NewGeos'>
<xsl:param name='knowns'/>
<xsl:element name='ExtantGeoLocs' namespace=''>
<xsl:comment><xsl:value-of select='count($knowns/geo)'/> </xsl:comment>
<xsl:value-of select='$MyNL'/>
<xsl:for-each 
select='$knowns/geo[not(title = preceding-sibling::geo/title)]'>
<!--xsl:comment>
<xsl:value-of select='./title' separator=' | '/> 
</xsl:comment-->
<xsl:copy-of select='.'/>
<xsl:value-of select='$MyNL'/>
</xsl:for-each>
</xsl:element>
</xsl:template>

<xsl:template name='NewConAreas'>
<xsl:param name='knowns'/>
<xsl:element name='ExtantConAreas' namespace=''>
<xsl:comment><xsl:value-of select='count($knowns/conarea)'/> </xsl:comment>
<xsl:value-of select='$MyNL'/>
<xsl:for-each 
select='$knowns/conarea[not(title = preceding-sibling::conarea/title)]'>
<xsl:comment>
<xsl:value-of select='./title' separator=' | '/> 
</xsl:comment>
<xsl:copy-of select='.'/>
<xsl:value-of select='$MyNL'/>
</xsl:for-each>
</xsl:element>
</xsl:template>

<xsl:template name='NewEmphs'>
<xsl:param name='knowns'/>
<xsl:element name='ExtantEmphs' namespace=''>
<xsl:comment><xsl:value-of select='count($knowns/emph)'/> </xsl:comment>
<xsl:value-of select='$MyNL'/>
<xsl:for-each 
select='$knowns/emph[not(name = preceding-sibling::emph/name)]'>
<!--xsl:comment>
<xsl:value-of select='./name' separator=' | '/> 
</xsl:comment-->
<xsl:copy-of select='.'/>
<xsl:value-of select='$MyNL'/>
</xsl:for-each>
</xsl:element>
</xsl:template>

<!-- ================================== -->
<xsl:template name='saveNewPeople'>
<xsl:param name='file'/>
<xsl:param name='newpeople'/>
<xsl:result-document href='{$file}'>
<xsl:for-each select='$newpeople'>
<xsl:copy-of select='.'/><xsl:value-of select='$MyNL'/>
</xsl:for-each>
</xsl:result-document>
</xsl:template>

<xsl:template name='saveNewItems'>
<xsl:param name='file'/>
<xsl:param name='newitems'/>
<xsl:result-document href='{$file}'>
<xsl:for-each select='$newitems'>
<xsl:copy-of select='.'/><xsl:value-of select='$MyNL'/>
</xsl:for-each>
</xsl:result-document>
</xsl:template>


<xsl:template name='NewPeopleOut'>
<xsl:param name='file'/>
<xsl:param name='newpeople'/>
<xsl:result-document href='{$file}'>

<xsl:element name='ExtantPersons' inherit-namespaces='no'>
<xsl:value-of select='$MyNL'/>
<!-- xsl:comment>
<xsl:value-of select='count($newpeople//person)'/>
</xsl:comment-->

<xsl:for-each select='$newpeople//person'>

<!-- xsl:comment><xsl:value-of select='.'/></xsl:comment -->

<xsl:element name='person' inherit-namespaces='no'>

<xsl:element name='uri' inherit-namespaces='no'>
<xsl:value-of select='substring-after(uri,"NEW-")'/></xsl:element>
<xsl:element name='fname' inherit-namespaces='no'>
<xsl:value-of select='fname'/></xsl:element>
<xsl:element name='mname' inherit-namespaces='no'>
<xsl:value-of select='mname'/></xsl:element>
<xsl:element name='lname' inherit-namespaces='no'>
<xsl:value-of select='lname'/></xsl:element>
<xsl:element name='netid' inherit-namespaces='no'>
<xsl:value-of select='netid'/></xsl:element>
</xsl:element>

</xsl:for-each>

</xsl:element>
<xsl:value-of select='$MyNL'/>
</xsl:result-document>
</xsl:template>

<xsl:function name='vfx:hasOneGoodName' as='xs:boolean'>
<xsl:param name='list'/>
<xsl:variable name='res' select='$list[vfx:goodName(dm:FNAME,
						    dm:MNAME,
						    dm:LNAME)][1]'/>
<xsl:value-of select='if($res != "") then true() else false()'/>

</xsl:function>

<xsl:function name='vfx:IS-hasOneGoodName' as='xs:boolean'>
<xsl:param name='list'/>
<xsl:variable name='res' select='$list[vfx:goodName(FNAME,
						    MNAME,
						    LNAME)][1]'/>
<xsl:value-of select='if($res != "") then true() else false()'/>

</xsl:function>
</xsl:stylesheet>
