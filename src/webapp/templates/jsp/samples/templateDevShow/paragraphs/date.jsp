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
%><%@ taglib uri="cms-taglib" prefix="cms" %>

<cms:out nodeDataName="date" datePattern="EEEE, MMMM d, yyyy"/><br>
<br>
<div class="devShowBox">
<span class="code">&lt;cms:out nodeDataName="date" datePattern="..." dateLanguage="..."/&gt;</span><br>
<br>
<b>datePattern (samples):</b><br>
<br>
<span class="code">yyyy-MM-dd</span> (default): <cms:out nodeDataName="date"/><br>
<br>
<span class="code">yyyy-MM-dd'T'HH:mm:ss:</span> <cms:out nodeDataName="date" datePattern="yyyy-MM-dd'T'HH:mm:ss"/><br>
<br>
<span class="code">d. M. yy</span>: <cms:out nodeDataName="date" datePattern="d. M. yy"/><br>
<br>
<span class="code">EEE d. MMM. yy - HH:mm</span>: <cms:out nodeDataName="date" datePattern="EEE d. MMM yyyy - HH:mm"/><br>
<br>
<span class="code">EEEE, d. MMMM. yyyy</span>: <cms:out nodeDataName="date" datePattern="EEEE, d. MMMM yyyy"/><br>
<br><br>
<b>dateLanguage (samples):</b><br>
<br>
<span class="code">en</span>: <cms:out nodeDataName="date" datePattern="EEEE, d. MMMM yyyy" dateLanguage="en"/><br>
<br>
<span class="code">de</span>: <cms:out nodeDataName="date" datePattern="EEEE, d. MMMM yyyy" dateLanguage="de"/><br>
<br>
<span class="code">fi</span>: <cms:out nodeDataName="date" datePattern="EEEE, d. MMMM yyyy" dateLanguage="fi"/><br>
</div>
