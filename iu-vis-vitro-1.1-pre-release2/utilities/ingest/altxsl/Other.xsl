<?xml version="1.0" encoding="iso-8859-1"?>
<!-- Example of a XSLT that filters out nodes -->
<!-- for use with Activity Insight data -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dm="http://www.digitalmeasures.com/schema/data"
    xmlns:dmd="http://www.digitalmeasures.com/schema/data-metadata" version="2.0">
    <xsl:output indent="yes"/>

    <xsl:template match="/">
        <dm:Data>
            <xsl:apply-templates select="dm:Data/dm:Record"/>
        </dm:Data>
    </xsl:template>

    <xsl:template match="dm:Data/dm:Record">
        <xsl:copy>
            <xsl:copy-of select="@userId | @username"/>
            <xsl:apply-templates select="*" mode="topLevel"/>
        </xsl:copy>
    </xsl:template>

  
    
    
    

    
    <xsl:template mode="topLevel" match="dm:INTELLCONT">
        
	  <xsl:choose>
	  <xsl:when test='string(dm:PUBLIC_VIEW) = "Yes"'>
	    <xsl:variable name='place'>
	      <xsl:value-of select='position()'/>
	    </xsl:variable>
	    <xsl:if test="string(dm:CONTYPE) = 'Other' and  not(exists(dm:CONTYPE_OTHER))">
	      <dm:INTELLCONT  xmlns="http://www.digitalmeasures.com/schema/data">
	       <NO_OTHER place='{$place}'/>
	       </dm:INTELLCONT>
	    </xsl:if>
	    
	  
	  </xsl:when>
	  <xsl:otherwise>
	    <NOVIEW/>
	  </xsl:otherwise>
	  </xsl:choose>
        
    </xsl:template>
     
 

    <!-- replace default template so that we don't get all of the text() floating in the output -->
    <xsl:template match="*"/>
    <xsl:template mode="topLevel" match="*"/>
    <xsl:template mode="publicView" match="*"/>
</xsl:stylesheet>
	
	
