<?xml version="1.0"?>
<!-- 
	Manipulates the config.server.xml bootstrap file
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:sv="http://www.jcp.org/jcr/sv/1.0" version="1.0">
	<!--  copy everything -->
	<xsl:template match="@*|node()">
	  <xsl:copy>
	    <xsl:apply-templates select="@*|node()"/>
	  </xsl:copy>
	</xsl:template>
	
	<!-- /server/admin -->
	<xsl:template match="sv:node[@sv:name='server']/sv:property[@sv:name='admin']/sv:value">
		<sv:value>false</sv:value>
	</xsl:template>

	<!-- /server/secureURIList/0001/URI -->
	<xsl:template match="sv:node[@sv:name='server']/sv:node[@sv:name='secureURIList']/sv:node[@sv:name='0001']/sv:property[@sv:name='URI']/sv:value">
		<sv:value>/.magnolia*</sv:value>
	</xsl:template>

	<!-- /server/ResourceNotAvailableURIMapping -->
	<xsl:template match="sv:node[@sv:name='server']/sv:property[@sv:name='ResourceNotAvailableURIMapping']/sv:value">
		<sv:value>/features.html</sv:value>
	</xsl:template>

</xsl:stylesheet>
