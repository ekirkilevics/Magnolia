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



<a href="<%=request.getContextPath()%><cms:out nodeDataName="file"/>" target="_blank">
<cms:out nodeDataName="file" fileProperty="nameWithoutExtension"/> &ndash;
<cms:out nodeDataName="file" fileProperty="extensionUpperCase"/>-File,
<cms:out nodeDataName="file" fileProperty="size"/></a><br/>
<br/>

<div class="devShowBox">
	<span class="code">&lt;cms:out nodeDataName="file" fileProperty="..."/&gt;</span><br/>
	<br/>
	<b>where fileProperty is one of the following:</b><br/>
	<br/>
	<span class="code">path</span> (default): path including name<br/>
	<cms:out nodeDataName="file"/><br/>
	<br/>
	<span class="code">pathWithoutName:</span><br/>
	<cms:out nodeDataName="file" fileProperty="pathWithoutName"/><br/>
	<br/>
	<span class="code">name:</span> name and extension<br/>
	<cms:out nodeDataName="file" fileProperty="name"/><br/>
	<br/>
	<span class="code">nameWithoutExtension:</span><br/>
	<cms:out nodeDataName="file" fileProperty="nameWithoutExtension"/><br/>
	<br/>
	<span class="code">extension:</span> extension as stored<br/>
	<cms:out nodeDataName="file" fileProperty="extension"/><br/>
	<br/>
	<span class="code">extensionUpperCase:</span> extension in upper case<br/>
	<cms:out nodeDataName="file" fileProperty="extensionUpperCase"/><br/>
	<br/>
	<span class="code">extensionLowerCase:</span> extension in lower case<br/>
	<cms:out nodeDataName="file" fileProperty="extensionLowerCase"/><br/>
	<br/>
	<span class="code">sizeBytes:</span> size in bytes<br/>
	<cms:out nodeDataName="file" fileProperty="sizeBytes"/><br/>
	<br/>
	<span class="code">sizeKB:</span> size in KB<br/>
	<cms:out nodeDataName="file" fileProperty="sizeKB"/><br/>
	<br/>
	<span class="code">sizeMB:</span> size in MB<br/>
	<cms:out nodeDataName="file" fileProperty="sizeMB"/><br/>
	<br/>
	<span class="code">size:</span> size and unit in bytes, KB or MB (depending on size)<br/>
	<cms:out nodeDataName="file" fileProperty="size"/><br/>
	<br/>
	<span class="code">handle:</span><br/>
	<cms:out nodeDataName="file" fileProperty="handle"/><br/>
	<br/>
	<span class="code">contentType:</span><br/>
	<cms:out nodeDataName="file" fileProperty="contentType"/><br/>
	<br/>
</div>


