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
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>


<cms:ifNotEmpty nodeDataName="image">
	<img src="<cms:out nodeDataName="image"/>" class="contentImage_<cms:out nodeDataName="imageFloat"/>" alt="<cms:out nodeDataName="imageAlt"/>">
</cms:ifNotEmpty>

<cms:ifNotEmpty nodeDataName="title">
	<h2><cms:out nodeDataName="title"/></h2>
</cms:ifNotEmpty>
<cms:out nodeDataName="text"/>


