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
%><%@ page import="info.magnolia.cms.core.Content,
				   info.magnolia.cms.util.Resource,
				   info.magnolia.cms.gui.misc.Sources,
				   info.magnolia.cms.gui.inline.BarMain,
				   info.magnolia.cms.gui.misc.FileProperties,
				   info.magnolia.cms.gui.control.Button"%>
<%@ taglib uri="cms-taglib" prefix="cms" %>
<%@ taglib uri="cms-util-taglib" prefix="cmsu" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>


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


<cms:contentNodeIterator contentNodeCollectionName="mainColumnParagraphsDev">
	<div style="clear:both;">
		<cms:adminOnly>
			<cms:editBar/>
		</cms:adminOnly>
		<cms:includeTemplate/>
		<br><br>
	</div>
</cms:contentNodeIterator>

<cms:adminOnly>
	<div style="clear:both;">
		<cms:newBar contentNodeCollectionName="mainColumnParagraphsDev" paragraph="samplesDevShowRichEdit,samplesDevShowDate,samplesDevShowFile,samplesDevShowAllControls,samplesDevShowInclude"/>
	</div>
</cms:adminOnly>
