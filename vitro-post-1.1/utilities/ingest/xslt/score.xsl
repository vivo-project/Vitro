<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:vfx='http://vivoweb.org/ext/functions'
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
exclude-result-prefixes='vfx xs'
>

<xsl:param name='fn1'  required='yes'/>
<xsl:param name='mn1'  required='yes'/>
<xsl:param name='ln1'  required='yes'/>
<xsl:param name='fn2'  required='yes'/>
<xsl:param name='mn2'  required='yes'/>
<xsl:param name='ln2'  required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>
<xsl:variable name='NL'>
<xsl:text>
</xsl:text>
</xsl:variable>

<xsl:template match='/'>
<Ans>
<xsl:value-of select='vfx:isoScore($fn1,$mn1,$ln1,$fn2,$mn2,$ln2)'/>
</Ans>
<xsl:value-of select='$NL'/>
</xsl:template>

<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
