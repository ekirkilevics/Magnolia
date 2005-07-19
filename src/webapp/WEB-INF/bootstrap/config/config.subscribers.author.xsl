<?xml version="1.0"?>
<!-- 
	Manipulates the config.subscribers.xml bootstrap file
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:sv="http://www.jcp.org/jcr/sv/1.0" version="1.0">
	<!--  copy everything -->
	<xsl:template match="@*|node()">
	  <xsl:copy>
	    <xsl:apply-templates select="@*|node()"/>
	  </xsl:copy>
	</xsl:template>
	
	<!-- /subscribers/SubscriberConfig/0001/active = true -->
	<xsl:template match="sv:node[@sv:name='subscribers']/sv:node[@sv:name='SubscriberConfig']/sv:node[@sv:name='0001']/sv:property[@sv:name='active']/sv:value">
		<sv:value>true</sv:value>
	</xsl:template>

</xsl:stylesheet>
