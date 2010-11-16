<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:xs='http://www.w3.org/2001/XMLSchema'
>

<xsl:variable name='cutoff' select='.50'/>

<xsl:variable name='wFirstName' select='2.0' as='xs:float'/>
<xsl:variable name='wMiddleName' select='1.0' as='xs:float'/>
<xsl:variable name='wFM' select='$wFirstName + $wMiddleName' as='xs:float'/>

<xsl:variable name='wL' select='5.0'/>

<xsl:variable name='wFullFullMatch' select='1.0'/>
<xsl:variable name='wFullFullMisMatch' select='-1.0'/>
<xsl:variable name='wEmptyEmptyMatch' select='0.0'/>
<xsl:variable name='wEmptyNonEmptyMisMatch' select='-.55'/>
<xsl:variable name='wPartialNonEmptyMisMatch' select='-.75'/>
<xsl:variable name='wPartialNonEmptyMatch' select='.50'/>



</xsl:stylesheet>
