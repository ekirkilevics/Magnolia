<%
/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */
%>
<%@ taglib uri="cms-taglib" prefix="cms" %>
<%@ taglib uri="cms-util-taglib" prefix="cmsu" %>
<%@ taglib uri="JSTL" prefix="c" %>


<%-- ################################################## --%>
<%-- content title --%>
<%-- ################################################## --%>
<h1>
<cms:ifNotEmpty nodeDataName="contentTitle" actpage="true">
	<cms:out nodeDataName="contentTitle"/>
</cms:ifNotEmpty>
<cms:ifEmpty nodeDataName="contentTitle" actpage="true">
	<cms:out nodeDataName="title"/>
</cms:ifEmpty>
</h1>


<cms:contentNodeIterator contentNodeCollectionName="mainColumnParagraphs">
	<div style="clear:both;">
		<cms:adminOnly>
			<cms:editBar/>
		</cms:adminOnly>
		<%-- ################################################## --%>
		<%-- line --%>
		<%-- ################################################## --%>
		<c:set var="lineAbove"><cms:out nodeDataName="lineAbove"/></c:set>
		<c:if test="${lineAbove=='true'}">
			<div class="line"><br></div>
		</c:if>
		<cms:includeTemplate/>
	</div>

	<%-- ################################################## --%>
	<%-- spacer --%>
	<%-- ################################################## --%>
	<div style="clear:both;">
		<c:set var="spacer"><cms:out nodeDataName="spacer"/></c:set>
		<c:if test="${spacer=='1'}">
			<br>
		</c:if>
		<c:if test="${spacer=='2'}">
			<br><br>
		</c:if>
	</div>
</cms:contentNodeIterator>


