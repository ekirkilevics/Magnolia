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



<cms:contentNodeIterator contentNodeCollectionName="rightColumnParagraphs">
	<div style="clear:both;">
		<cms:adminOnly>
			<cms:editBar/>
		</cms:adminOnly>
		<cms:includeTemplate/>
	</div>
</cms:contentNodeIterator>

<cms:adminOnly>
	<div style="clear:both;">
		<cms:newBar contentNodeCollectionName="rightColumnParagraphs" paragraph="samplesRightColumn"/>
	</div>
</cms:adminOnly>
