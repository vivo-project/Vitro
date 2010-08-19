<?xml version="1.0" encoding="iso-8859-1"?>
<!-- a XSLT that filters out nodes -->
<!-- not used with Activity Insight INTELLCONT data -->


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dm="http://www.digitalmeasures.com/schema/data"
    xmlns:dmd="http://www.digitalmeasures.com/schema/data-metadata" version="2.0">
    <xsl:output indent="yes"/>

 <!-- ================================================= -->

    <xsl:template match="/">
        <dm:Data>
            <xsl:apply-templates select="dm:Data/dm:Record"/>
        </dm:Data>
    </xsl:template>

<!-- ================================================= -->

 
    <xsl:template match="dm:Data/dm:Record">
        <xsl:copy>
            <xsl:copy-of select="@userId | @username"/>
            <xsl:apply-templates select="*" mode="topLevel"/>
        </xsl:copy>
    </xsl:template>

<!-- ================================================= -->

 
    <xsl:template mode="topLevel" match="dm:PCI">
        <xsl:element name="{name()}" namespace="{namespace-uri()}">
            <xsl:copy-of select="@*|node()"/>
        </xsl:element>
    </xsl:template>


<!-- ================================================= -->

 
    <xsl:template mode="topLevel" match="dmd:IndexEntry[@indexKey eq 'COLLEGE']">
        <xsl:element name="{name()}" namespace="{namespace-uri()}">
            <xsl:copy-of select="@text"/>
        </xsl:element>
    </xsl:template>
 
<!-- ================================================= -->

    
    <xsl:template mode="topLevel" match="dmd:IndexEntry[@indexKey eq 'DEPARTMENT']">
        <xsl:element name="{name()}" namespace="{namespace-uri()}">
            <xsl:copy-of select="@text"/>
        </xsl:element>
    </xsl:template>

<!-- ================================================= -->

     
    <xsl:template mode="topLevel" match="dmd:IndexEntry[@indexKey eq 'RANK']">
        <xsl:element name="{name()}" namespace="{namespace-uri()}">
            <xsl:copy-of select="@text"/>
        </xsl:element>
    </xsl:template>

<!-- ================================================= -->

     
    <xsl:template mode="topLevel" match="dm:INTELLCONT_JOURNAL">

        <dm:INTELLCONT_JOURNAL xmlns="http://www.digitalmeasures.com/schema/data">
	   <xsl:attribute name='id'>
	    <xsl:value-of select='./@id'/>
	  </xsl:attribute>
          <xsl:attribute name='dmd:lastModified'>
	    <xsl:value-of select='./@dmd:lastModified'/>
	  </xsl:attribute>
	  <xsl:attribute name='dmd:primaryKey'>
	    <xsl:value-of select='./@dmd:primaryKey'/>
	  </xsl:attribute>


	    <!-- handle journal name specially -->
	    <!-- just copy other tags of interest -->

            <xsl:copy-of select="dm:PUBLIC_VIEW"/>
            <xsl:copy-of select="dm:DTM_PUB | dm:DTD_PUB | dm:DTY_PUB | dm:PUB_START | dm:PUB_END "/>
            <xsl:copy-of select="dm:CONAREA"/>

	    <!-- handle journal name specially -->
	    <xsl:apply-templates  mode="topLevel" select='dm:JOURNAL_NAME'/>

            <xsl:copy-of select="dm:STATUS | dm:REFEREED "/>
            <xsl:copy-of select="dm:TITLE | dm:VOLUME | dm:ISSUE | dm:PAGENUM"/>
            <xsl:copy-of select="dm:COMMENTS | dm:USER_REFERENCE_CREATOR "/>
            <xsl:copy-of select="dm:INTELLCONT_JOURNAL_AUTH"/>

        </dm:INTELLCONT_JOURNAL>

    </xsl:template>

<!-- ================================================= -->

    <xsl:template  mode="topLevel" match='dm:JOURNAL_NAME'>
	
	<xsl:choose>
		<xsl:when test='. = "Other" or . = ""'>
			<xsl:element name='dm:JOURNAL_NAME'>
			<xsl:value-of select="../dm:JOURNAL_NAME_OTHER"/>
			</xsl:element>
		</xsl:when>
		<xsl:otherwise>
			<xsl:copy-of select="."/>
		</xsl:otherwise>
	</xsl:choose>

    </xsl:template>	

<!-- ================================================= -->

 
    <xsl:template mode="topLevel" match="dm:INTELLCONT">

        <dm:INTELLCONT  xmlns="http://www.digitalmeasures.com/schema/data"> 
	  <xsl:attribute name='id'>
	    <xsl:value-of select='./@id'/>
	  </xsl:attribute>
          <xsl:attribute name='dmd:lastModified'>
	    <xsl:value-of select='./@dmd:lastModified'/>
	  </xsl:attribute>


            <xsl:copy-of select="dm:PUBLIC_VIEW"/>
            <xsl:copy-of select="dm:DTM_PUB | dm:DTD_PUB | dm:DTY_PUB | dm:PUB_START | dm:PUB_END "/>
            <xsl:copy-of select="dm:CONAREA"/>

	    <!-- handle contype tag specially -->
	    <xsl:apply-templates mode="topLevel"  select='dm:CONTYPE'/>

	    <xsl:copy-of select="dm:STATUS | dm:REFEREED | dm:PUBLICAVAIL"/>
            <xsl:copy-of select="dm:TITLE | dm:PUBLISHER | dm:PUBCTYST | dm:VOLUME | dm:ISSUE | dm:BOOK_TITLE | dm:PAGENUM | dm:EDITORS"/>
            <xsl:copy-of select="dm:COMMENTS | dm:USER_REFERENCE_CREATOR "/>
            <!-- xsl:copy-of select="dm:INTELLCONT_JOURNAL"/ -->
	    <xsl:copy-of select="dm:INTELLCONT_AUTH"/>

        </dm:INTELLCONT>

    </xsl:template>

<!-- ================================================= -->

    <xsl:template  mode="topLevel" match='dm:CONTYPE'>
	
	<xsl:choose>
		<xsl:when test='. = "Other" or . = ""'>
			<xsl:element name='dm:CONTYPE'>
			<xsl:value-of select="../dm:CONTYPE_OTHER"/>
			</xsl:element>
		</xsl:when>
		<xsl:otherwise>
			<xsl:copy-of select="."/>
		</xsl:otherwise>
	</xsl:choose>

    </xsl:template>	

<!-- ================================================= -->

      
   <xsl:template mode="topLevel" match="dm:MEDCONT">
        <dm:MEDCONT xmlns="http://www.digitalmeasures.com/schema/data">
	  <xsl:copy-of select="@*|node()"/>
	</dm:MEDCONT>
   </xsl:template>

<!-- ================================================= -->

 
    <!-- replace default template so that we don't get all of the text() floating in the output -->
    <xsl:template match="*"/>
    <xsl:template mode="topLevel" match="*"/>
    <xsl:template mode="publicView" match="*"/>
</xsl:stylesheet>
	
	
