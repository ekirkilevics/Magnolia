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
	
	<!-- /modules/adminInterface/VirtualURIMapping/default/toURI -->
	<xsl:template match="sv:node[@sv:name='modules']/sv:node[@sv:name='adminInterface']/sv:node[@sv:name='VirtualURIMapping']/sv:node[@sv:name='default']/sv:property[@sv:name='toURI']/sv:value">
		<sv:value>/features.html</sv:value>
	</xsl:template>

	<!-- /modules/adminInterface/VirtualURIMapping/default/fromURI -->
	<xsl:template match="sv:node[@sv:name='modules']/sv:node[@sv:name='adminInterface']/sv:node[@sv:name='VirtualURIMapping']/sv:node[@sv:name='default']/sv:property[@sv:name='fromURI']/sv:value">
		<sv:value>/</sv:value>
	</xsl:template>

</xsl:stylesheet>
