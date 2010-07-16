<?xml version="1.0"?>
<!-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">    

<!-- bdc34:  -->
<!--  For converting an Entity to be displayed using the cals css -->

	<xsl:template match="/">
			<xsl:apply-templates />
	</xsl:template>

   	<xsl:template match="/entity">
   	<div class="entity">
      <p>
        <h4><xsl:value-of select="name"/> | <xsl:value-of select="moniker"/> 
        <xsl:if test="url"> | <i><xsl:copy-of select="url"/></i> </xsl:if></h4>
        <xsl:if test="campus_address"><br/>address: <xsl:value-of select="campus_address"/></xsl:if>
        <xsl:if test="campus_phone"><br/>phone: <xsl:value-of select="campus_phone"/></xsl:if>
        <xsl:if test="email_address"><br/>email: <xsl:value-of select="email_address"/></xsl:if>
      </p>
      <xsl:if test="imageThumb">
        <p class="thumbnail"><img src="images/people/{imageThumb}" width="150"/></p>    
      </xsl:if>
      <xsl:apply-templates select="PropertyInstances"/>
      <div class="entityBody">
        <h5><xsl:value-of select="blurb"/></h5>
        <xsl:copy-of select="description"/>
      </div>          
      </div>
  </xsl:template>

  <xsl:template match="PropertyInstances">
      <!--p class="entityRelations"><xsl:value-of select="entity"/>:</p-->
      <ul class="entityListElements">
        <xsl:apply-templates select="entity"/>
      </ul>    
  </xsl:template>

  <xsl:template match="entity" >
    <li>
      <!--a class="entityLink" href="entityxml?id={@id}&amp;dir=eng"><xsl:value-of select="name"/></a-->
      <xsl:if test="name"><xsl:copy-of select="name"/></xsl:if>
      <xsl:if test="moniker"> | <xsl:value-of select="moniker"/></xsl:if> 
      <xsl:if test="url"> | <i><xsl:copy-of select="url"/></i></xsl:if>
      </li>
  </xsl:template>
</xsl:stylesheet>
