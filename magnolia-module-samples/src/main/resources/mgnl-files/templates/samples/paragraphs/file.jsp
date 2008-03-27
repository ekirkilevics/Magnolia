<?xml version="1.0" encoding="UTF-8" ?>
<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="cms-taglib"
  xmlns:cmsu="cms-util-taglib" xmlns:c="http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page contentType="text/html; charset=UTF-8" session="false" />
  <cms:out nodeDataName="file" var="file" />
  <a href="${pageContext.request.contextPath}${file}" target="_blank">
    <cms:out nodeDataName="file" fileProperty="nameWithoutExtension" />
    &amp;ndash;
    <cms:out nodeDataName="file" fileProperty="extensionUpperCase" />
    -File,
    <cms:out nodeDataName="file" fileProperty="size" />
  </a>
  <br />
  <br />
  <div class="devShowBox">
    <span class="code">&amp;lt;cms:out nodeDataName="file" fileProperty="..."/&amp;gt;</span>
    <br />
    <br />
    <b>where fileProperty is one of the following:</b>
    <br />
    <br />
    <span class="code">path</span>
    (default): path including name
    <br />
    <cms:out nodeDataName="file" />
    <br />
    <br />
    <span class="code">pathWithoutName:</span>
    <br />
    <cms:out nodeDataName="file" fileProperty="pathWithoutName" />
    <br />
    <br />
    <span class="code">name:</span>
    name and extension
    <br />
    <cms:out nodeDataName="file" fileProperty="name" />
    <br />
    <br />
    <span class="code">nameWithoutExtension:</span>
    <br />
    <cms:out nodeDataName="file" fileProperty="nameWithoutExtension" />
    <br />
    <br />
    <span class="code">extension:</span>
    extension as stored
    <br />
    <cms:out nodeDataName="file" fileProperty="extension" />
    <br />
    <br />
    <span class="code">extensionUpperCase:</span>
    extension in upper case
    <br />
    <cms:out nodeDataName="file" fileProperty="extensionUpperCase" />
    <br />
    <br />
    <span class="code">extensionLowerCase:</span>
    extension in lower case
    <br />
    <cms:out nodeDataName="file" fileProperty="extensionLowerCase" />
    <br />
    <br />
    <span class="code">sizeBytes:</span>
    size in bytes
    <br />
    <cms:out nodeDataName="file" fileProperty="sizeBytes" />
    <br />
    <br />
    <span class="code">sizeKB:</span>
    size in KB
    <br />
    <cms:out nodeDataName="file" fileProperty="sizeKB" />
    <br />
    <br />
    <span class="code">sizeMB:</span>
    size in MB
    <br />
    <cms:out nodeDataName="file" fileProperty="sizeMB" />
    <br />
    <br />
    <span class="code">size:</span>
    size and unit in bytes, KB or MB (depending on size)
    <br />
    <cms:out nodeDataName="file" fileProperty="size" />
    <br />
    <br />
    <span class="code">handle:</span>
    <br />
    <cms:out nodeDataName="file" fileProperty="handle" />
    <br />
    <br />
    <span class="code">contentType:</span>
    <br />
    <cms:out nodeDataName="file" fileProperty="contentType" />
    <br />
    <br />
  </div>
</jsp:root>
